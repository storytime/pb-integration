package com.github.storytime.service.sync;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.mapper.YnabCommonMapper;
import com.github.storytime.mapper.ZenCommonMapper;
import com.github.storytime.model.api.YnabBudgetSyncStatus;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.db.inner.YnabTagsSyncProperties;
import com.github.storytime.model.ynab.YnabToZenSyncHolder;
import com.github.storytime.model.ynab.YnabZenHolder;
import com.github.storytime.model.ynab.YnabZenSyncObject;
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
import com.github.storytime.service.DateService;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.http.YnabExchangeHttpService;
import com.github.storytime.service.http.ZenDiffHttpService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.YnabTagsSyncProperties.MATCH_INNER_TAGS;
import static com.github.storytime.model.db.inner.YnabTagsSyncProperties.MATCH_PARENT_TAGS;
import static com.github.storytime.model.ynab.transaction.YnabTransactionColour.*;
import static java.lang.Boolean.FALSE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.*;

@Service
public class YnabSyncService {

    private static final Logger LOGGER = getLogger(YnabSyncService.class);
    private final ZenDiffHttpService zenDiffHttpService;
    private final UserService userService;
    private final YnabExchangeHttpService ynabExchangeHttpService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;
    private final Executor cfThreadPool;
    private final DateService dateService;
    private final ZenCommonMapper zenCommonMapper;
    private final YnabCommonMapper ynabCommonMapper;
    private final YnabSyncServiceRepository ynabSyncServiceRepository;
    //TODO: replace
    private final ConcurrentHashMap<String, Boolean> ynabTransfer = new ConcurrentHashMap<>();

    @Autowired
    public YnabSyncService(final ZenDiffHttpService zenDiffHttpService,
                           final ZenDiffLambdaHolder zenDiffLambdaHolder,
                           final YnabExchangeHttpService ynabExchangeHttpService,
                           final DateService dateService,
                           final Executor cfThreadPool,
                           final ZenCommonMapper zenCommonMapper,
                           final YnabCommonMapper ynabCommonMapper,
                           final YnabSyncServiceRepository ynabSyncServiceRepository,
                           final UserService userService) {
        this.zenDiffHttpService = zenDiffHttpService;
        this.userService = userService;
        this.ynabExchangeHttpService = ynabExchangeHttpService;
        this.cfThreadPool = cfThreadPool;
        this.dateService = dateService;
        this.zenCommonMapper = zenCommonMapper;
        this.ynabCommonMapper = ynabCommonMapper;
        this.ynabSyncServiceRepository = ynabSyncServiceRepository;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
    }


    public ResponseEntity<String> startSync(final long userId, final long startFrom) {
        try {
            final AppUser appUser = userService.findUserById(userId).orElseThrow();
            final String ynabAuthToken = appUser.getYnabAuthToken();

            if (ynabAuthToken == null) {
                LOGGER.warn("YNAB sync is stopped for user:[{}], token not installed", userId);
                return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
            }

            if (appUser.getYnabSyncEnabled().equals(FALSE)) {
                LOGGER.warn("YNAB sync is stopped for user:[{}], not YNAB sync enabled", userId);
                return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
            }

            var pushToYnabResponse = pushToYnab(appUser, startFrom);

            if (pushToYnabResponse.isEmpty()) {
                return new ResponseEntity<>(NO_CONTENT);
            } else {
                return new ResponseEntity<>(OK);
            }

        } catch (Exception e) {
            LOGGER.error("Cannot push to YNAB", e.getCause());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    private List<YnabBudgetSyncStatus> pushToYnab(final AppUser appUser, final long startFrom) {

        final List<YnabSyncConfig> ynabSyncConfigs = ynabSyncServiceRepository
                .findAllByEnabledIsTrueAndUserId(appUser.getId())
                .orElse(emptyList())
                .stream()
                .map(this::correctYnabSyncConfig)
                .collect(toUnmodifiableList());

        //get zen data
        final List<CompletableFuture<YnabToZenSyncHolder>> collect = ynabSyncConfigs
                .stream()
                .map(ynabSyncConfig -> getZenDiffForBudget(appUser, startFrom, ynabSyncConfig))
                .collect(toUnmodifiableList());

        final List<YnabBudgetSyncStatus> pushToYnabResponse = allOf(collect.toArray(new CompletableFuture[collect.size()]))
                .thenApply(aVoid -> collect.stream().map(CompletableFuture::join).collect(toUnmodifiableList()))
                .thenApply(ynabToZenSyncHoldersList -> pushZenTransactionToYnab(appUser, ynabToZenSyncHoldersList))
                .join();

        //update repo
        pushToYnabResponse
                .stream()
                .filter(not(ynabBudgetSyncStatus -> ynabBudgetSyncStatus.getStatus().isEmpty()))
                .collect(toUnmodifiableList())
                .forEach(ynabBudgetSyncStatus -> ynabSyncConfigs
                        .stream()
                        .filter(ynabSyncConfig -> ynabSyncConfig.getBudgetName().equalsIgnoreCase(ynabBudgetSyncStatus.getName()))
                        .findFirst()
                        .ifPresent(ynabSyncConfig -> ynabSyncServiceRepository.save(ynabSyncConfig.setLastSync(startFrom))));

        return pushToYnabResponse;
    }

    public List<YnabBudgetSyncStatus> pushZenTransactionToYnab(final AppUser user,
                                                               final List<YnabToZenSyncHolder> budgetsToSync) {

        final List<YnabToZenSyncHolder> newTransactionListEmpty = isNewTransactionListEmpty(budgetsToSync);
        if (newTransactionListEmpty.isEmpty()) {
            LOGGER.warn("No new zen transactions, since last push nothing to push to YNAB for user:[{}]", user.id);
            return emptyList();
        }

        final Set<String> userConfigBudgetNames = newTransactionListEmpty
                .stream()
                .map(ysc -> ysc.getYnabSyncConfig().getBudgetName().trim())
                .collect(toUnmodifiableSet());

        final List<CompletableFuture<YnabBudgetSyncStatus>> listOfPushRequests = getYnabBudgetsFromYnabInUse(user, userConfigBudgetNames)
                .thenApply(ynabBudgets -> newTransactionListEmpty
                        .stream()
                        .map(ynabToZenSyncHolder -> ynabBudgets
                                .stream()
                                .filter(b -> ynabToZenSyncHolder.getYnabSyncConfig().getBudgetName().equalsIgnoreCase(b.getName()))
                                .map(budget -> {  //always will find one element because names in YNAB are unique
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

    public List<YnabToZenSyncHolder> isNewTransactionListEmpty(final List<YnabToZenSyncHolder> budgetsToSync) {
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
                                                                      final YnabSyncConfig config) {
        return supplyAsync(() -> {
            LOGGER.debug("Calling ZEN diff for YNAB budget config: [{}], last sync [{}], tags method [{}]", config.getBudgetName(), config.getLastSync(), config.getTagsSyncProperties());
            final Optional<ZenResponse> zenDiffByUser = zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getYnabFunction(user, clientSyncTime, config));
            return new YnabToZenSyncHolder(zenDiffByUser, config);
        }, cfThreadPool);
    }

    public CompletableFuture<List<YnabBudgets>> getYnabBudgetsFromYnabInUse(AppUser user, Set<String> budgetNames) {
        return supplyAsync(() -> ynabExchangeHttpService.getBudget(user)
                        .map(ynabBudgetResponse -> ynabBudgetResponse
                                .getYnabBudgetData()
                                .getBudgets()
                                .stream()
                                .filter(budget -> budgetNames.contains(budget.getName()))
                                .collect(toUnmodifiableList()))
                        .orElse(emptyList()),
                cfThreadPool);
    }

    public CompletableFuture<YnabBudgetSyncStatus> collectAllNeededYnabData(final AppUser user,
                                                                            final List<AccountItem> zenAccounts,
                                                                            final List<TagItem> zenTags,
                                                                            final List<TransactionItem> zenTransactions,
                                                                            final YnabBudgets budgetToSync,
                                                                            final YnabSyncConfig ynabSyncConfig) {
        final CompletableFuture<Optional<YnabCategoryResponse>> yCategoriesCf =
                supplyAsync(() -> ynabExchangeHttpService.getCategories(user, budgetToSync.getId()), cfThreadPool);
        final CompletableFuture<Optional<YnabAccountResponse>> yAccountsCf =
                supplyAsync(() -> ynabExchangeHttpService.getAccounts(user, budgetToSync.getId()), cfThreadPool);

        return yCategoriesCf
                .thenCombine(yAccountsCf, (yMaybeCat, yMaybeAcc) -> {
                    final List<YnabAccounts> ynabAccounts = mapYnabAccountsFromResponse(yMaybeAcc);
                    final List<YnabCategories> ynabCategories = ynabCommonMapper.mapYnabCategoriesFromResponse(yMaybeCat);
                    return mapDataAllDataForYnab(zenAccounts, zenTags, zenTransactions, ynabCategories, ynabAccounts, ynabSyncConfig, user);
                })
                .thenApply(ynabTransactionsRequest -> ynabTransactionsRequest
                        .flatMap(ynabTransactionsRequest1 -> ynabExchangeHttpService.pushToYnab(user, budgetToSync.getId(), ynabTransactionsRequest1))
                )
                .thenApply(pushResponse -> new YnabBudgetSyncStatus(budgetToSync.getName(), pushResponse.orElse(EMPTY)));
    }


    public List<YnabAccounts> mapYnabAccountsFromResponse(final Optional<YnabAccountResponse> yMaybeAcc) {
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

    public Optional<YnabTransactionsRequest> mapDataAllDataForYnab(final List<AccountItem> zenAccounts,
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

        final YnabZenHolder sameAccounts = mapSameAccounts(zenAccounts, ynabAccounts);
        final YnabZenHolder sameTags = mapSameTags(zenTags, ynabCategories);
        List<TransactionItem> zenTransaction = emptyList();

        if (ynabTagsSyncProperty.equals(MATCH_INNER_TAGS)) {
            zenTransaction = selectZenNotSyncedTransactions(zenTransactions, sameAccounts, ynabSyncConfig);
        } else if (ynabTagsSyncProperty.equals(MATCH_PARENT_TAGS)) {
            zenTransaction = selectZenNotSyncedTransactions(zenTransactions, sameAccounts, ynabSyncConfig)
                    .stream()
                    .map(zt -> zenCommonMapper.flatToParentCategory(zenTags, zt))
                    .collect(toUnmodifiableList());
        }

        final List<YnabTransactions> ynabTransactions = zenTransaction
                .stream()
                .map(zTr -> sameAccounts
                        .findByZenId(zTr.getIncomeAccount())
                        .or(() -> sameAccounts.findByZenId(zTr.getOutcomeAccount()))
                        .map(ynabZenSyncObject -> createYnabTransactions(sameTags, zTr, ynabZenSyncObject, user))
                        .get())
                .collect(toUnmodifiableList());

        if (sameAccounts.isEmpty()) {
            LOGGER.debug("Finish! No same accounts for budget: [{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.id);
            return empty();
        }

        if (ynabTransactions.isEmpty()) {
            LOGGER.debug("Finish! No not synced transactions for budget: [{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.id);
            return empty();
        }

        ynabTransactions.forEach(yTr -> LOGGER.debug("Going to push to YNAB: [{}], payee: [{}], date: [{}], catI: [{}]", yTr.getAmount(), yTr.getPayeeName(), yTr.getDate(), yTr.getCategoryId()));

        final YnabTransactionsRequest ynabTransactionsRequest = new YnabTransactionsRequest();
        ynabTransactionsRequest.setTransactions(ynabTransactions);
        return of(ynabTransactionsRequest);
    }

    public YnabTransactions createYnabTransactions(final YnabZenHolder sameTags,
                                                   final TransactionItem zenRawTr,
                                                   final YnabZenSyncObject sameAccount,
                                                   final AppUser user) {
        final YnabTransactions ynabTransactions = new YnabTransactions();
        var zTag = ofNullable(zenRawTr.getTag())
                .orElse(emptyList())
                .stream()
                .filter(not(String::isEmpty))
                .findFirst()
                .orElse(UNCATEGORIZED);

        final String ynabTagId = sameTags
                .findByZenId(zTag)
                .map(YnabZenSyncObject::getYnabId)
                .orElse(EMPTY);

        mapTransactionType(zenRawTr, ynabTransactions, zTag);

        ynabTransactions.setAccountId(sameAccount.getYnabId());
        ynabTransactions.setDate(dateService.secsToIsoFormat(zenRawTr.getCreated(), user));
        ynabTransactions.setMemo(zenRawTr.getComment());
        ynabTransactions.setCategoryId(ynabTagId);
        ynabTransactions.setPayeeName(zenRawTr.getPayee());
        ynabTransactions.setCleared(CLEARED);
        ynabTransactions.setApproved(true);
        ynabTransactions.setImportId(zenRawTr.getId());
        return ynabTransactions;
    }

    public void mapTransactionType(final TransactionItem zTr,
                                   final YnabTransactions ynabTransaction,
                                   final String zTag) {
        final Double outcome = zTr.getOutcome();
        final Double income = zTr.getIncome();

        if (outcome != EMPTY_AMOUNT && income == EMPTY_AMOUNT) {
            // outcome
            final double amount = outcome * YNAB_AMOUNT_CONST;
            ynabTransaction.setAmount(-(int) amount);
            ynabTransaction.setFlagColor(RED);
        }

        if (income != EMPTY_AMOUNT && outcome == EMPTY_AMOUNT) {
            // income
            final double amount = income * YNAB_AMOUNT_CONST;
            ynabTransaction.setAmount((int) amount);
            ynabTransaction.setFlagColor(GREEN);
        }

        if (outcome != EMPTY_AMOUNT && income != EMPTY_AMOUNT) {
            // transfer
            if (ynabTransfer.get(zTag) == null) {
                ynabTransaction.setAmount((int) (income * YNAB_AMOUNT_CONST));
                ynabTransfer.clear();
            } else {
                ynabTransaction.setAmount(-(int) (outcome * YNAB_AMOUNT_CONST));
            }

            ynabTransaction.setFlagColor(BLUE);
        }
    }

    public List<TransactionItem> selectZenNotSyncedTransactions(final List<TransactionItem> zenTransactions,
                                                                final YnabZenHolder sameAccounts,
                                                                final YnabSyncConfig ynabSyncConfig) {
        return zenTransactions
                .stream()
                .filter(not(TransactionItem::isDeleted))
                .filter(not(zt -> ofNullable(zt.getComment()).orElse(EMPTY).trim().startsWith(YNAB_IGNORE)))
                .filter(zt -> zt.getCreated() < EPOCH_MILLI_FIX)
                .filter(zt -> zt.getCreated() > ynabSyncConfig.getLastSync()) //only new
                .filter(zt -> sameAccounts.isExistsByZenId(zt.getIncomeAccount()) || sameAccounts.isExistsByZenId(zt.getOutcomeAccount()))
                .sorted(comparing(TransactionItem::getCreated))
                .collect(toUnmodifiableList());
    }


    public YnabZenHolder mapSameTags(final List<TagItem> responseZenTags,
                                     final List<YnabCategories> ynabTags) {

        final YnabZenHolder sameTags = new YnabZenHolder();

        //filter zen tags to avoid #projects
        final List<TagItem> zenTags = responseZenTags
                .stream()
                .filter(not(t -> t.getTitle().startsWith(PROJECT_TAG)))
                .collect(toUnmodifiableList());

        //same tags to use
        ynabTags.forEach(yTag -> zenTags
                .stream()
                .filter(zTag -> yTag
                        .getName()
                        .trim()
                        .equalsIgnoreCase(zTag.getTitle().trim()))
                .findFirst()
                .map(ztag -> sameTags.add(new YnabZenSyncObject(ztag.getId(), yTag.getId(), ztag.getTitle().trim())))
        );

        ynabTags.stream()
                .filter(yTag -> yTag.getName().equals(UNCATEGORIZED))
                .findFirst()
                .map(yTag -> sameTags.add(new YnabZenSyncObject(UNCATEGORIZED, yTag.getId(), yTag.getName().trim())));

        LOGGER.info("Same tags mapped: [{}], YNAB tags count [{}], zen tags count [{}]", sameTags.size(), ynabTags.size(), zenTags.size());

        //log not same tags
        Predicate<YnabCategories> ynabCategoriesPredicate = yTag -> sameTags.isExistsByName(yTag.getName().trim());
        LOGGER.debug("YNAB tags that were NOT mapped [{}]", selectTags(ynabTags, not(ynabCategoriesPredicate)));
        LOGGER.debug("YNAB tags that were mapped [{}]", selectTags(ynabTags, ynabCategoriesPredicate));

        return sameTags;
    }

    private Set<String> selectTags(final List<YnabCategories> ynabTags,
                                   final Predicate<YnabCategories> ynabCategoriesPredicate) {
        return ynabTags
                .stream()
                .filter(ynabCategoriesPredicate)
                .map(YnabCategories::getName)
                .collect(toUnmodifiableSet());
    }

    public YnabZenHolder mapSameAccounts(final List<AccountItem> zenAccounts,
                                         final List<YnabAccounts> ynabAccounts) {

        final YnabZenHolder sameAccounts = new YnabZenHolder();
        ynabAccounts.forEach(yAccount -> zenAccounts
                .stream()
                .filter(zAccount -> yAccount.getName().trim().equalsIgnoreCase(zAccount.getTitle().trim()))
                .findFirst()
                .map(zAccount -> sameAccounts.add(new YnabZenSyncObject(zAccount.getId(), yAccount.getId(), zAccount.getTitle().trim())))
        );
        LOGGER.info("Same accounts mapped: [{}], YNAB acc count [{}], zen acc count [{}]", sameAccounts.size(), ynabAccounts.size(), zenAccounts.size());
        return sameAccounts;
    }
}
