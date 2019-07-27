package com.github.storytime.service.sync;


import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.api.YnabBudgetSyncStatus;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.db.inner.YnabTagsSyncProperties;
import com.github.storytime.model.ynab.YnabToZenSyncHolder;
import com.github.storytime.model.ynab.YnabZenComplianceObject;
import com.github.storytime.model.ynab.YnabZenHolder;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.request.YnabTransactions;
import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TagItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.repository.YnabSyncServiceRepository;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.exchange.YnabExchangeService;
import com.github.storytime.service.exchange.ZenDiffService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.YnabTagsSyncProperties.MATCH_INNER_TAGS;
import static com.github.storytime.model.db.inner.YnabTagsSyncProperties.MATCH_PARENT_TAGS;
import static com.github.storytime.model.ynab.transaction.YnabTransactionColour.*;
import static java.time.Instant.now;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
public class YnabSyncService {

    private static final Logger LOGGER = getLogger(YnabSyncService.class);
    private final ZenDiffService zenDiffService;
    private final UserService userService;
    private final YnabExchangeService ynabExchangeService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;
    private final Executor cfThreadPool;
    private final YnabSyncServiceRepository ynabSyncServiceRepository;

    @Autowired
    public YnabSyncService(final ZenDiffService zenDiffService,
                           final ZenDiffLambdaHolder zenDiffLambdaHolder,
                           final YnabExchangeService ynabExchangeService,
                           final Executor cfThreadPool,
                           final YnabSyncServiceRepository ynabSyncServiceRepository,
                           final UserService userService) {
        this.zenDiffService = zenDiffService;
        this.userService = userService;
        this.ynabExchangeService = ynabExchangeService;
        this.cfThreadPool = cfThreadPool;
        this.ynabSyncServiceRepository = ynabSyncServiceRepository;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
    }

    public ResponseEntity syncTransactions(final long userId) {
        try {
            LOGGER.debug("Calling ynab sync for user:[{}]", userId);

            final AppUser appUser = userService.findUserById(userId).get();
            final String ynabAuthToken = appUser.getYnabAuthToken();

            if (ynabAuthToken == null) {
                LOGGER.warn("Ynab sync is stopped for user:[{}], token not installed", userId);
                return new ResponseEntity(INTERNAL_SERVER_ERROR);
            }

            if (!appUser.getYnabSyncEnabled()) {
                LOGGER.warn("Ynab sync is stopped for user:[{}], not ynab sync enabled", userId);
                return new ResponseEntity(INTERNAL_SERVER_ERROR);
            }

            final var clientSyncTime = now().getEpochSecond();

            final List<YnabSyncConfig> ynabSyncConfigs = ynabSyncServiceRepository
                    .findByUserId(userId)
                    .orElse(emptyList())
                    .stream()
                    .map(this::correctYnabSyncConfig)
                    .collect(toUnmodifiableList());


            final List<CompletableFuture<YnabToZenSyncHolder>> collect = ynabSyncConfigs
                    .stream()
                    .map(ynabSyncConfig -> getZenDiffForBudget(appUser, clientSyncTime, ynabSyncConfig))
                    .collect(toUnmodifiableList());

            final List<YnabBudgetSyncStatus> pushToYnabResponse = allOf(collect.toArray(new CompletableFuture[collect.size()]))
                    .thenApply(aVoid -> collect.stream().map(CompletableFuture::join).collect(toUnmodifiableList()))
                    .thenApply(ynabToZenSyncHoldersList -> pushNewZenTransactionToYnab(appUser, ynabToZenSyncHoldersList))
                    .join();

            pushToYnabResponse
                    .stream()
                    .filter(not(ynabBudgetSyncStatus -> ynabBudgetSyncStatus.getStatus().isEmpty()))
                    .collect(toUnmodifiableList())
                    .forEach(ynabBudgetSyncStatus -> ynabSyncConfigs
                            .stream()
                            .filter(ynabSyncConfig -> ynabSyncConfig.getBudgetName().equalsIgnoreCase(ynabBudgetSyncStatus.getName()))
                            .findFirst()
                            .ifPresent(ynabSyncConfig -> ynabSyncServiceRepository.save(ynabSyncConfig.setLastSync(clientSyncTime))));

            if (pushToYnabResponse.isEmpty()) {
                return new ResponseEntity(NO_CONTENT);
            } else {
                return ResponseEntity.ok().body(pushToYnabResponse.stream().map(YnabBudgetSyncStatus::getName));
            }

        } catch (Exception e) {
            LOGGER.error("Cannot push Diff to ZEN request ", e.getCause());
            return new ResponseEntity(INTERNAL_SERVER_ERROR);
        }
    }


    public List<YnabBudgetSyncStatus> pushNewZenTransactionToYnab(final AppUser user,
                                                                  final List<YnabToZenSyncHolder> budgetsToSync) {

        final List<YnabToZenSyncHolder> newTranasactionListEmpty = isNewTransactionListEmpty(budgetsToSync);
        if (newTranasactionListEmpty.isEmpty()) {
            LOGGER.warn("No new zen transactions, since last push nothing to push to YNAB for user:[{}]", user.id);
            return emptyList();
        }

        final Set<String> userConfigBudgetNames = newTranasactionListEmpty
                .stream()
                .map(ysc -> ysc.getYnabSyncConfig().getBudgetName().trim())
                .collect(toUnmodifiableSet());

        final List<CompletableFuture<YnabBudgetSyncStatus>> listOfPushRequests = getYnabBudgetsFromYnabInUse(user, userConfigBudgetNames)
                .thenApply(ynabBudgets -> newTranasactionListEmpty
                        .stream()
                        .map(ynabToZenSyncHolder -> ynabBudgets
                                .stream()
                                .filter(b -> ynabToZenSyncHolder.getYnabSyncConfig().getBudgetName().equalsIgnoreCase(b.getName()))
                                .map(budget -> {  //always will find one element because names in ynab are unique
                                    final YnabSyncConfig ynabSyncConfig = ynabToZenSyncHolder.getYnabSyncConfig();
                                    final ZenResponse zenResponse = ynabToZenSyncHolder.getZenResponse().get();
                                    final List<AccountItem> zenAccounts = ofNullable(zenResponse.getAccount()).orElse(emptyList());
                                    final List<TagItem> zenTags = ofNullable(zenResponse.getTag()).orElse(emptyList());
                                    final List<TransactionItem> zenTransactions = ofNullable(zenResponse.getTransaction()).orElse(emptyList());
                                    return collectAllNeededYnabData(user, zenAccounts, zenTags, zenTransactions, budget, ynabSyncConfig);
                                })
                                .collect(toUnmodifiableList()))
                        .flatMap(Collection::stream)
                        .collect(toUnmodifiableList())
                ).join();


        return CompletableFuture.allOf(listOfPushRequests.toArray(new CompletableFuture[listOfPushRequests.size()]))
                .thenApply(aVoid -> listOfPushRequests.stream().map(CompletableFuture::join).collect(toUnmodifiableList()))
                .join();

    }

    public List<YnabToZenSyncHolder> isNewTransactionListEmpty(List<YnabToZenSyncHolder> budgetsToSync) {
        return budgetsToSync
                .stream()
                .filter(not(ynabToZenSyncHolder -> ynabToZenSyncHolder
                        .getZenResponse()
                        .flatMap(zenResponse -> ofNullable(zenResponse.getTransaction()))
                        .orElse(emptyList())
                        .isEmpty()))
                .collect(toUnmodifiableList());
    }


    public CompletableFuture<YnabToZenSyncHolder> getZenDiffForBudget(final AppUser user,
                                                                      final long clientSyncTime,
                                                                      final YnabSyncConfig ynabSyncConfig) {
        return supplyAsync(() -> {
            final Optional<ZenResponse> zenDiffByUser = zenDiffService.getZenDiffByUser(zenDiffLambdaHolder.getYnabFunction(user, clientSyncTime, ynabSyncConfig));
            return new YnabToZenSyncHolder(zenDiffByUser, ynabSyncConfig);
        }, cfThreadPool);
    }

    public CompletableFuture<List<YnabBudgets>> getYnabBudgetsFromYnabInUse(AppUser user, Set<String> budgetNames) {
        return supplyAsync(() -> ynabExchangeService.getBudget(user)
                        .map(ynabBudgetResponse -> ynabBudgetResponse
                                .getYnabBudgetData()
                                .getBudgets()
                                .stream()
                                .filter(budget -> budgetNames.contains(budget.getName()))
                                .collect(toUnmodifiableList()))
                        .orElse(emptyList()),
                cfThreadPool);
    }

    public CompletableFuture<YnabBudgetSyncStatus> collectAllNeededYnabData(AppUser user,
                                                                            List<AccountItem> zenAccounts,
                                                                            List<TagItem> zenTags,
                                                                            List<TransactionItem> zenTransactions,
                                                                            YnabBudgets budgetToSync,
                                                                            YnabSyncConfig ynabSyncConfig) {
        final CompletableFuture<Optional<YnabCategoryResponse>> yCategoriesCf =
                supplyAsync(() -> ynabExchangeService.getCategories(user, budgetToSync.getId()), cfThreadPool);
        final CompletableFuture<Optional<YnabAccountResponse>> yAccountsCf =
                supplyAsync(() -> ynabExchangeService.getAccounts(user, budgetToSync.getId()), cfThreadPool);

        return yCategoriesCf
                .thenCombine(yAccountsCf, (yMaybeCat, yMaybeAcc) -> {
                    final List<YnabAccounts> ynabAccounts = mapYnabAccountsFromResponse(yMaybeAcc);
                    final List<YnabCategories> ynabCategories = mapYnabCategoriesFromResponse(yMaybeCat);
                    return mapData(zenAccounts, zenTags, zenTransactions, ynabCategories, ynabAccounts, ynabSyncConfig, user);
                })
                .thenApply(ynabTransactionsRequest -> ynabTransactionsRequest
                        .flatMap(ynabTransactionsRequest1 -> ynabExchangeService.pushToYnab(user, budgetToSync.getId(), ynabTransactionsRequest1))
                )
                .thenApply(pushResponse -> new YnabBudgetSyncStatus(budgetToSync.getName(), pushResponse.orElse(EMPTY)));
    }

    public List<YnabCategories> mapYnabCategoriesFromResponse(Optional<YnabCategoryResponse> yMaybeCat) {
        //collect ynab tags
        return yMaybeCat
                .map(yCat -> ofNullable(yCat.getYnabCategoryData())
                        .map(data -> ofNullable(data.getCategoryGroups())
                                .orElse(emptyList()))
                        .stream()
                        .flatMap(categoryGroupsItems ->
                                ofNullable(categoryGroupsItems)
                                        .orElse(emptyList())
                                        .stream()
                                        .flatMap(categoryGroupsItem -> ofNullable(categoryGroupsItem.getCategories())
                                                .orElse(emptyList())
                                                .stream()))
                        .collect(toUnmodifiableList()))
                .orElse(emptyList());
    }

    public List<YnabAccounts> mapYnabAccountsFromResponse(Optional<YnabAccountResponse> yMaybeAcc) {
        return yMaybeAcc
                .map(yAcc -> ofNullable(yAcc.getYnabAccountData().getAccounts()).orElse(emptyList()))
                .orElse(emptyList());
    }

    public YnabSyncConfig correctYnabSyncConfig(final YnabSyncConfig config) {

        if (config.getLastSync() <= 0) {
            config.setLastSync(now().toEpochMilli());
        }

        return config;
    }

    public Optional<YnabTransactionsRequest> mapData(final List<AccountItem> zenAccounts,
                                                     final List<TagItem> zenTags,
                                                     final List<TransactionItem> zenTransactions,
                                                     final List<YnabCategories> ynabCategories,
                                                     final List<YnabAccounts> ynabAccounts,
                                                     final YnabSyncConfig ynabSyncConfig,
                                                     final AppUser user) {


        final YnabTagsSyncProperties ynabTagsSyncProperty = ofNullable(ynabSyncConfig.getTagsSyncProperties()).orElse(emptyList())
                .stream()
                .findFirst()
                .orElse(MATCH_PARENT_TAGS);

        final YnabZenHolder commonAccounts = mapCommonAccounts(zenAccounts, ynabAccounts);
        final YnabZenHolder commonTags = mapCommonTags(zenTags, ynabCategories);
        List<TransactionItem> transaction = emptyList();

        if (ynabTagsSyncProperty.equals(MATCH_INNER_TAGS)) {
            transaction = filterZenTransactionToSync(zenTransactions, commonAccounts, ynabSyncConfig);
        } else if (ynabTagsSyncProperty.equals(MATCH_PARENT_TAGS)) {
            transaction = filterZenTransactionToSync(zenTransactions, commonAccounts, ynabSyncConfig)
                    .stream()
                    .map(zt -> flatToParentCategory(zenTags, zt))
                    .collect(toUnmodifiableList());
        }

        final List<YnabTransactions> ynabTransactions = transaction
                .stream()
                .map(zTr -> commonAccounts
                        .findByZenId(zTr.getIncomeAccount())
                        .map(ynabZenComplianceObject -> createYnabTransactions(commonTags, zTr, ynabZenComplianceObject))
                        .get())
                .collect(toUnmodifiableList());

        if (commonAccounts.isEmpty()) {
            LOGGER.error("No common accounts for budget:[{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.id);
            return empty();
        }

        if (ynabTransactions.isEmpty()) {
            LOGGER.warn("No ansynced transactions :[{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.id);
            return empty();
        }

        final YnabTransactionsRequest ynabTransactionsRequest = new YnabTransactionsRequest();
        ynabTransactionsRequest.setTransactions(ynabTransactions);
        return of(ynabTransactionsRequest);
    }

    public YnabTransactions createYnabTransactions(final YnabZenHolder commonTags,
                                                   final TransactionItem zTr,
                                                   final YnabZenComplianceObject ynabZenComplianceObject) {
        final YnabTransactions ynabTransactions = new YnabTransactions();
        final String zenTagId = ofNullable(zTr.getTag())
                .flatMap(zTags -> zTags.stream().findFirst())
                .orElse(EMPTY);
        final String tagId = commonTags
                .findByZenId(zenTagId)
                .map(YnabZenComplianceObject::getYnabId)
                .orElse(null);
        final String date = zTr.getDate();

        mapTransactionType(zTr, ynabTransactions);

        ynabTransactions.setAccountId(ynabZenComplianceObject.getYnabId());
        ynabTransactions.setDate(date);
        ynabTransactions.setMemo(zTr.getComment());
        ynabTransactions.setCategoryId(tagId);
        ynabTransactions.setPayeeName(zTr.getPayee());
        ynabTransactions.setCleared("cleared");
        ynabTransactions.setApproved(false);
        ynabTransactions.setImportId(zTr.getId());

        return ynabTransactions;
    }

    public void mapTransactionType(final TransactionItem zTr,
                                   final YnabTransactions ynabTransactions) {
        final Double outcome = zTr.getOutcome();
        final Double income = zTr.getIncome();

        if (outcome != EMPTY_AMOUNT) {
            final double amount = outcome * YNAB_AMOUNT_CONST;
            ynabTransactions.setAmount(-(int) amount);
            ynabTransactions.setFlagColor(RED);
        }

        if (income != EMPTY_AMOUNT) {
            final double amount = income * YNAB_AMOUNT_CONST;
            ynabTransactions.setAmount((int) amount);
            ynabTransactions.setFlagColor(GREEN);
        }

        if (outcome != EMPTY_AMOUNT && income != EMPTY_AMOUNT) {
            ynabTransactions.setFlagColor(BLUE);
        }
    }

    public List<TransactionItem> filterZenTransactionToSync(final List<TransactionItem> zenTransactions,
                                                            final YnabZenHolder commonAccounts,
                                                            final YnabSyncConfig ynabSyncConfig) {
        return zenTransactions
                .stream()
                .filter(not(TransactionItem::isDeleted))
                .filter(zt -> {
                    try {
                        ofEpochSecond(zt.getCreated());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(zt -> zt.getCreated() > ynabSyncConfig.getLastSync()) //only new transactions
                .filter(zt -> commonAccounts.isExistsByZenId(zt.getIncomeAccount())) //only for common accounts
                .filter(zt -> commonAccounts.isExistsByZenId(zt.getOutcomeAccount())) //only for common accounts
                .sorted(comparing(TransactionItem::getCreated))
                .collect(toUnmodifiableList());
    }


    public TransactionItem flatToParentCategory(final List<TagItem> zenTags,
                                                final TransactionItem zt) {
        final String innerTagId = ofNullable(zt.getTag()).orElse(emptyList())
                .stream()
                .filter(not(s -> s.startsWith(PROJECT_TAG)))
                .findFirst()
                .orElse(EMPTY);
        final String parentTag = zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(innerTagId))
                .findFirst()
                .map(tagItem -> ofNullable(tagItem.getParent()).orElse(innerTagId))
                .orElse(innerTagId);

        return zt.setTag(List.of(parentTag));
    }

    public YnabZenHolder mapCommonTags(final List<TagItem> responseZenTags,
                                       final List<YnabCategories> ynabTags) {

        final YnabZenHolder commonTags = new YnabZenHolder();

        //filter zen tags to avoid #projects
        final List<TagItem> zenTags = responseZenTags
                .stream()
                .filter(not(t -> t.getTitle().startsWith(PROJECT_TAG)))
                .collect(toUnmodifiableList());

        //common tags to use
        ynabTags.forEach(yTag -> zenTags
                .stream()
                .filter(zTag -> yTag
                        .getName()
                        .trim()
                        .equalsIgnoreCase(zTag.getTitle().trim()))
                .findFirst()
                .map(ztag -> commonTags.add(new YnabZenComplianceObject(ztag.getId(), yTag.getId(), ztag.getTitle().trim())))
        );

        LOGGER.info("Common tags mapped: [{}], ynab tags count [{}], zen tags count [{}]", commonTags.size(), ynabTags.size(), zenTags.size());

        //log not common tags
        final Set<String> ynabNonCompliance = ynabTags
                .stream()
                .filter(not(yTag -> commonTags.isExistsByName(yTag.getName().trim())))
                .map(YnabCategories::getName)
                .collect(toUnmodifiableSet());

        LOGGER.debug("Ynab tags that were not mapped [{}]", ynabNonCompliance);

        return commonTags;
    }

    public YnabZenHolder mapCommonAccounts(final List<AccountItem> zenAccounts,
                                           final List<YnabAccounts> ynabAccounts) {

        final YnabZenHolder commonAccounts = new YnabZenHolder();
        ynabAccounts.forEach(yAccount -> zenAccounts
                .stream()
                .filter(zAccount -> yAccount.getName().trim().equalsIgnoreCase(zAccount.getTitle().trim()))
                .findFirst()
                .map(zAccount -> commonAccounts.add(new YnabZenComplianceObject(zAccount.getId(), yAccount.getId(), zAccount.getTitle().trim())))
        );
        LOGGER.info("Common accounts mapped: [{}], ynab acc count [{}], zen acc count [{}]", commonAccounts.size(), ynabAccounts.size(), zenAccounts.size());
        return commonAccounts;
    }
}
