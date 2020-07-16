package com.github.storytime.service.sync;

import com.github.storytime.mapper.YnabZenCommonMapper;
import com.github.storytime.mapper.response.YnabResponseMapper;
import com.github.storytime.model.api.YnabBudgetSyncStatus;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.ynab.YnabToZenSyncHolder;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TagItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.repository.YnabSyncServiceRepository;
import com.github.storytime.service.YnabService;
import com.github.storytime.service.ZenDiffService;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.EMPTY;
import static java.lang.Boolean.FALSE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.*;

@Service
public class YnabSyncService {

    private static final Logger LOGGER = getLogger(YnabSyncService.class);
    private final UserService userService;
    private final YnabService ynabService;
    private final ZenDiffService zenDiffService;
    private final YnabResponseMapper ynabResponseMapper;
    private final YnabZenCommonMapper ynabZenCommonMapper;
    private final YnabSyncServiceRepository ynabSyncServiceRepository;

    @Autowired
    public YnabSyncService(final ZenDiffService zenDiffService,
                           final YnabService ynabService,
                           final YnabZenCommonMapper ynabZenCommonMapper,
                           final YnabResponseMapper ynabResponseMapper,
                           final YnabSyncServiceRepository ynabSyncServiceRepository,
                           final UserService userService) {
        this.userService = userService;
        this.ynabZenCommonMapper = ynabZenCommonMapper;
        this.ynabSyncServiceRepository = ynabSyncServiceRepository;
        this.zenDiffService = zenDiffService;
        this.ynabService = ynabService;
        this.ynabResponseMapper = ynabResponseMapper;
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
                .map(ynabSyncConfig -> zenDiffService.zenDiffByUserForYnab(appUser, startFrom, ynabSyncConfig))
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

    public List<YnabBudgetSyncStatus> pushZenTransactionToYnab(final AppUser user, final List<YnabToZenSyncHolder> budgetsToSync) {

        final List<YnabToZenSyncHolder> newTransactionListEmpty = ynabZenCommonMapper.isNewTransactionListEmpty(budgetsToSync);
        if (newTransactionListEmpty.isEmpty()) {
            LOGGER.warn("No new zen transactions, since last push nothing to push to YNAB for user:[{}]", user.id);
            return emptyList();
        }

        final Set<String> userConfigBudgetNames = newTransactionListEmpty
                .stream()
                .map(ysc -> ysc.getYnabSyncConfig().getBudgetName().trim())
                .collect(toUnmodifiableSet());

        final List<CompletableFuture<YnabBudgetSyncStatus>> listOfPushRequests =
                ynabService.getYnabCategories(user)
                        .thenApply(ynabBudgets -> newTransactionListEmpty
                                .stream()
                                .map(ynabToZenSyncHolder -> ynabBudgets
                                        .map(ynabBudgetResponse -> ynabResponseMapper.getSameBudgets(userConfigBudgetNames, ynabBudgetResponse))
                                        .orElse(emptyList())
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


    public CompletableFuture<YnabBudgetSyncStatus> collectAllNeededYnabData(final AppUser user,
                                                                            final List<AccountItem> zenAccounts,
                                                                            final List<TagItem> zenTags,
                                                                            final List<TransactionItem> zenTransactions,
                                                                            final YnabBudgets budgetToSync,
                                                                            final YnabSyncConfig ynabSyncConfig) {
        final CompletableFuture<Optional<YnabCategoryResponse>> yCategoriesCf =
                ynabService.getYnabCategories(user, budgetToSync.getId());
        final CompletableFuture<Optional<YnabAccountResponse>> yAccountsCf =
                ynabService.getYnabAccounts(user, budgetToSync.getId());

        return yCategoriesCf
                .thenCombine(yAccountsCf, (yMaybeCat, yMaybeAcc) -> {
                    final List<YnabAccounts> ynabAccounts = ynabResponseMapper.mapYnabAccountsFromResponse(yMaybeAcc);
                    final List<YnabCategories> ynabCategories = ynabResponseMapper.mapYnabCategoriesFromResponse(yMaybeCat);
                    return ynabZenCommonMapper.mapDataAllDataForYnab(zenAccounts, zenTags, zenTransactions, ynabCategories, ynabAccounts, ynabSyncConfig, user);
                })
                .thenApply(ynabTransactionsRequest -> ynabTransactionsRequest
                        .flatMap(ynabTransactionsRequest1 -> ynabService.pushToYnab(user, budgetToSync.getId(), ynabTransactionsRequest1))
                )
                .thenApply(pushResponse -> new YnabBudgetSyncStatus(budgetToSync.getName(), pushResponse.orElse(EMPTY)));
    }

    public YnabSyncConfig correctYnabSyncConfig(final YnabSyncConfig config) {

        if (config.getLastSync() <= 0) {
            config.setLastSync(now().toEpochMilli());
        }

        return config;
    }
}
