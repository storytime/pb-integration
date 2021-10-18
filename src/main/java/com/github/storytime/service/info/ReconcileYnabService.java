package com.github.storytime.service.info;

import com.github.storytime.mapper.ReconcileCommonMapper;
import com.github.storytime.mapper.YnabCommonMapper;
import com.github.storytime.mapper.response.YnabResponseMapper;
import com.github.storytime.mapper.zen.ZenCommonMapper;
import com.github.storytime.model.api.PbZenReconcileResponse;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.common.ZenYnabTagReconcileProxyObject;
import com.github.storytime.model.ynab.transaction.from.TransactionsItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.repository.YnabSyncServiceRepository;
import com.github.storytime.service.PbAccountService;
import com.github.storytime.service.ReconcileTableService;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.YnabAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
import com.github.storytime.service.utils.DateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.error.AsyncErrorHandlerUtil.*;
import static java.time.YearMonth.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Deprecated
@Component
public class ReconcileYnabService {

    private static final Logger LOGGER = LogManager.getLogger(ReconcileYnabService.class);

    private final UserService userService;
    private final YnabAsyncService ynabAsyncService;
    private final MerchantService merchantService;
    private final PbAccountService pbAccountService;
    private final ZenCommonMapper zenCommonMapper;
    private final YnabResponseMapper ynabResponseMapper;
    private final YnabCommonMapper ynabCommonMapper;
    private final DateService dateService;
    private final ReconcileTableService reconcileTableService;
    private final YnabSyncServiceRepository ynabSyncServiceRepository;
    private final ZenAsyncService zenAsyncService;
    private final ReconcileCommonMapper reconcileCommonMapper;

    @Autowired
    public ReconcileYnabService(
            final UserService userService,
            final MerchantService merchantService,
            final ZenCommonMapper zenCommonMapper,
            final PbAccountService pbAccountService,
            final DateService dateService,
            final ReconcileCommonMapper reconcileCommonMapper,
            final ZenAsyncService zenAsyncService,
            final YnabAsyncService ynabAsyncService,
            final YnabResponseMapper ynabResponseMapper,
            final YnabCommonMapper ynabCommonMapper,
            final ReconcileTableService reconcileTableService,
            final YnabSyncServiceRepository ynabSyncServiceRepository) {
        this.zenAsyncService = zenAsyncService;
        this.userService = userService;
        this.merchantService = merchantService;
        this.zenCommonMapper = zenCommonMapper;
        this.ynabCommonMapper = ynabCommonMapper;
        this.pbAccountService = pbAccountService;
        this.ynabAsyncService = ynabAsyncService;
        this.ynabResponseMapper = ynabResponseMapper;
        this.reconcileTableService = reconcileTableService;
        this.ynabSyncServiceRepository = ynabSyncServiceRepository;
        this.dateService = dateService;
        this.reconcileCommonMapper = reconcileCommonMapper;
    }

    public CompletableFuture<String> reconcileTableByBudget(final long userId, final String budget) {
        final var st = createSt();
        try {
            LOGGER.debug("Building reconciled YNAB user: [{}], budget: [{}] - stated", userId, budget);
            return getUserAsync(userId)
                    .thenApply(user -> reconcileTableByDate(user, budget, getYear(user), getMonth(user)))
                    .whenComplete((r, e) -> logReconcileByBudgetCf(userId, budget, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot reconciled YNAB user: [{}], time: [{}], error: [{}] - error", userId, getTime(st), e.getCause(), e);
            return completedFuture(EMPTY);
        }
    }

    public CompletableFuture<String> reconcileTableByBudgetForDate(final long userId, final String budget, int year, int mouth) {
        final var st = createSt();
        try {
            LOGGER.debug("Building reconciled YNAB for date for user: [{}], budget: [{}]  - stated", userId, budget);
            return getUserAsync(userId)
                    .thenApply(user -> reconcileTableByDate(user, budget, year, mouth))
                    .whenComplete((r, e) -> logReconcileByBudgetCf(userId, budget, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot reconcile YNAB for date for user: [{}], budget: [{}],  time: [{}], error: [{}] - error", userId, budget, getTime(st), e.getCause(), e);
            return completedFuture(EMPTY);
        }
    }

    public CompletableFuture<String> reconcileTableDefaultAll(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Building YNAB reconcile all user: [{}] - stated", userId);
            return getUserAsync(userId)
                    .thenApply(appUser -> ynabSyncServiceRepository
                            .findAllByEnabledIsTrueAndUserId(appUser.getId())
                            .orElse(emptyList())
                            .stream()
                            .map(YnabSyncConfig::getBudgetName).toList()
                            .stream()
                            .map(budgetName -> reconcileTableByDate(appUser, budgetName, getYear(appUser), getMonth(appUser)))
                            .collect(joining()))
                    .whenComplete((r, e) -> logReconcileTableDefaultAll(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot build YNAB reconcile all user: [{}], time: [{}], error: [{}] - error", userId, getTime(st), e.getCause(), e);
            return completedFuture(EMPTY);
        }
    }

    public CompletableFuture<ResponseEntity<PbZenReconcileResponse>> reconcilePbJson(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Building pb/zen reconcile json, for user: [{}] - stared", userId);
            return getUserAsync(userId)
                    .thenCompose(appUser -> {
                        final var merchantInfos = merchantService.getAllEnabledMerchants();
                        final var startDate = dateService.getUserStarDateInMillis(appUser);
                        final var pbAccsFuture = pbAccountService.getPbAsyncAccounts(merchantInfos);
                        final var zenAccsFuture = zenAsyncService.zenDiffByUserForPbAccReconcile(appUser, startDate)
                                .thenApply(Optional::get)
                                .thenApply(zenCommonMapper::getZenAccounts);
                        return zenAccsFuture.thenCombine(pbAccsFuture, reconcileCommonMapper::mapInfoForAccountJson);
                    })
                    .thenApply(r -> new ResponseEntity<>(new PbZenReconcileResponse(r), OK))
                    .whenComplete((r, e) -> logReconcilePbJson(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot build pb/zen json for user: [{}], time: [{}], error [{}] - error", userId, getTime(st), e.getCause(), e);
            return completedFuture(new ResponseEntity<>(NO_CONTENT));
        }
    }

    public String reconcileTableByDate(final AppUser appUser, final String budgetName, int year, int mouth) {
        var table = new StringBuilder(EMPTY);
        final long userId = appUser.getId();

        final long startDate = dateService.getStartOfMouthInSeconds(year, mouth, appUser);
        final long endDate = dateService.getEndOfMouthInSeconds(year, mouth, appUser);

        var ynabBudget = mapYnabBudgetData(appUser, budgetName);
        var ynabAccs = getYnabAccounts(appUser, ynabBudget);
        var merchantInfos = ofNullable(merchantService.getAllEnabledMerchants())
                .orElse(emptyList())
                .stream()
                .filter(m -> ynabAccs.stream().anyMatch(ynabAccount -> ynabAccount.getName().equals(ofNullable(m.getShortDesc()).orElse(EMPTY)))).toList();
        var pbAccs = pbAccountService.getPbAsyncAccounts(merchantInfos);
        var ynabTransactions = getYnabTransactions(appUser, ynabBudget);
        var ynabCategories = getYnabCategories(appUser, ynabBudget);
        var maybeZr = zenAsyncService.zenDiffByUserTagsAndTransaction(appUser, startDate).join().orElseThrow();
        var zenAccs = zenCommonMapper.getZenAccounts(maybeZr);

        var allInfoForTagTable = mapInfoForTagsTable(appUser, ynabTransactions, ynabCategories, maybeZr, startDate, endDate);
        var allInfoForAccountTable = reconcileCommonMapper.mapInfoForAccountTable(zenAccs, ynabAccs, pbAccs.join());

        LOGGER.debug("Combine accounts info collecting info, for user: [{}]", userId);
        reconcileTableService.buildAccountHeader(table);
        allInfoForAccountTable.forEach(o -> reconcileTableService.buildAccountRow(table, o.getAccount(), o.getPbAmount(), o.getZenAmount(), o.getYnabAmount(), o.getPbZenDiff(), o.getZenYnabDiff(), o.getStatus()));
        reconcileTableService.buildAccountLastLine(table);

        if (!allInfoForTagTable.isEmpty()) {
            reconcileTableService.addEmptyLine(table);
            reconcileTableService.addEmptyLine(table);
            LOGGER.debug("Combine all category info, for user: [{}]", userId);
            reconcileTableService.buildTagHeader(table);
            allInfoForTagTable.forEach(t -> reconcileTableService.buildTagSummaryRow(table, t.getCategory(), t.getZenAmountAsString(), t.getYnabAmountAsString(), t.getDiff()));
            reconcileTableService.buildTagLastLine(table);
        }

        LOGGER.debug("Finish building reconcile table, for user: [{}]", userId);
        reconcileTableService.addEmptyLine(table);
        reconcileTableService.addEmptyLine(table);
        reconcileTableService.addEmptyLine(table);
        return table.toString();
    }

    //TODO: MOVE to mappers
    private List<ZenYnabTagReconcileProxyObject> mapInfoForTagsTable(final AppUser appUser,
                                                                     final List<TransactionsItem> ynabTransactions,
                                                                     final List<YnabCategories> ynabCategories,
                                                                     final ZenResponse maybeZr,
                                                                     final long startDate,
                                                                     final long endDate) {
        final TreeMap<String, BigDecimal> zenSummary =
                zenCommonMapper.getZenTagsSummaryByCategory(startDate, endDate, maybeZr);
        final TreeMap<String, BigDecimal> ynabSummary =
                ynabCommonMapper.getYnabSummaryByCategory(appUser, ynabTransactions, ynabCategories, startDate, endDate);

        final List<ZenYnabTagReconcileProxyObject> allInfoForTagTable = new ArrayList<>();
        //TODO: One map
        ynabSummary.forEach((ynabTag, ynabAmount) -> {
            final BigDecimal zenAmount = zenSummary.get(ynabTag);
            if (zenAmount != null) {
                allInfoForTagTable.add(new ZenYnabTagReconcileProxyObject(ynabTag, zenAmount, ynabAmount));
            }
        });

        return allInfoForTagTable
                .stream()
                .filter(not(x -> x.getCategory().isEmpty()))
                .sorted(comparing(ZenYnabTagReconcileProxyObject::getZenAmount).reversed()).toList();
    }

    public List<YnabAccounts> getYnabAccounts(final AppUser appUser,
                                              final YnabBudgets budgetToReconcile) {
        return Optional.of(budgetToReconcile)
                .map(b -> mapYnabAccounts(appUser, b))
                .orElse(emptyList());
    }


    private List<TransactionsItem> getYnabTransactions(final AppUser appUser, final YnabBudgets ynabBudget) {
        return Optional.of(ynabBudget)
                .map(b -> mapYnabTransactionsData(appUser, b.getId()))
                .orElse(emptyList());
    }

    private List<YnabCategories> getYnabCategories(final AppUser appUser, final YnabBudgets ynabBudget) {
        return Optional.of(ynabBudget)
                .map(budgets -> ynabAsyncService.getYnabCategories(appUser, budgets.getId()).join())
                .map(ynabResponseMapper::mapYnabCategoriesFromResponse)
                .orElse(emptyList());
    }

    private List<YnabAccounts> mapYnabAccounts(final AppUser appUser, final YnabBudgets budgets) {
        return ynabAsyncService.getYnabAccounts(appUser, budgets.getId())
                .join()
                .flatMap(yc -> ofNullable(yc.getYnabAccountData().getAccounts()))
                .orElse(emptyList());
    }

    private YnabBudgets mapYnabBudgetData(final AppUser appUser, final String budgetToReconcile) {
        return ynabAsyncService.getYnabBudget(appUser)
                .join()
                .flatMap(ynabBudgetResponse -> ynabResponseMapper.mapBudgets(budgetToReconcile, ynabBudgetResponse))
                .orElseThrow();
    }

    private List<TransactionsItem> mapYnabTransactionsData(final AppUser appUser, final String budgetToReconcile) {
        return ynabAsyncService.getYnabTransactions(appUser, budgetToReconcile)
                .join()
                .map(ynabResponseMapper::mapTransactionsFromResponse)
                .orElse(emptyList());
    }

    private CompletableFuture<AppUser> getUserAsync(long userId) {
        return userService.findUserByIdAsync(userId).thenApply(Optional::get);
    }

    private int getYear(final AppUser appUser) {
        return now(ZoneId.of(appUser.getTimeZone())).getYear();
    }

    private int getMonth(final AppUser appUser) {
        return now(ZoneId.of(appUser.getTimeZone())).getMonthValue();
    }
}
