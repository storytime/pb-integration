package com.github.storytime.service.info;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.mapper.YnabCommonMapper;
import com.github.storytime.mapper.ZenCommonMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.common.ZenYnabAccountReconcileProxyObject;
import com.github.storytime.model.ynab.common.ZenYnabTagReconcileProxyObject;
import com.github.storytime.model.ynab.transaction.from.TransactionsItem;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.DateService;
import com.github.storytime.service.ReconcileTableService;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.exchange.PbAccountsService;
import com.github.storytime.service.exchange.YnabExchangeService;
import com.github.storytime.service.exchange.ZenDiffService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.service.ReconcileTableService.X;
import static java.lang.String.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
public class ReconcileService {


    private static final Logger LOGGER = LogManager.getLogger(ReconcileService.class);

    private final ZenDiffService zenDiffService;
    private final UserService userService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;
    private final Executor cfThreadPool;
    private final YnabExchangeService ynabExchangeService;
    private final MerchantService merchantService;
    private final PbAccountsService pbAccountsService;
    private final ZenCommonMapper zenCommonMapper;
    private final YnabCommonMapper ynabCommonMapper;
    private final DateService dateService;
    private final ReconcileTableService reconcileTableService;

    @Autowired
    public ReconcileService(final ZenDiffService zenDiffService,
                            final UserService userService,
                            final Executor cfThreadPool,
                            final MerchantService merchantService,
                            final ZenCommonMapper zenCommonMapper,
                            final YnabExchangeService ynabExchangeService,
                            final PbAccountsService pbAccountsService,
                            final DateService dateService,
                            final YnabCommonMapper ynabCommonMapper,
                            final ReconcileTableService reconcileTableService,
                            final ZenDiffLambdaHolder zenDiffLambdaHolder) {
        this.zenDiffService = zenDiffService;
        this.userService = userService;
        this.cfThreadPool = cfThreadPool;
        this.merchantService = merchantService;
        this.ynabExchangeService = ynabExchangeService;
        this.zenCommonMapper = zenCommonMapper;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
        this.ynabCommonMapper = ynabCommonMapper;
        this.pbAccountsService = pbAccountsService;
        this.reconcileTableService = reconcileTableService;
        this.dateService = dateService;
    }


    public String getRecompileTable(final long userId, final String budgetName, int year, int mouth) {
        var table = new StringBuilder(EMPTY);
        try {
            LOGGER.debug("Building reconcile table, collecting info, for user: [{}]", userId);
            userService.findUserById(userId).ifPresent(appUser -> runAsync(() -> {
                final long startDate = dateService.getStartOfMouthInSeconds(year, mouth, appUser);
                final long endDate = dateService.getEndOfMouthInSeconds(year, mouth, appUser);

                var pbAccs = getPbAccounts(appUser);
                var ynabBudget = getBudget(appUser, budgetName);
                var ynabAccs = getYnabAccounts(appUser, ynabBudget);
                var ynabTransactions = getYnabTransactions(appUser, ynabBudget);
                var ynabCategories = getYnabCategories(appUser, ynabBudget);
                var maybeZr = getZenDiff(appUser, startDate);
                var zenAccs = zenCommonMapper.getZenAccounts(maybeZr);

                var allInfoForTagTable = mapInfoForTagsTable(appUser, ynabTransactions, ynabCategories, maybeZr, startDate, endDate);
                var allInfoForAccountTable = mapInfoForAccountTable(zenAccs, ynabAccs, pbAccs);

                LOGGER.debug("Combine accounts info collecting info, for user: [{}]", userId);
                reconcileTableService.buildAccountHeader(table);
                allInfoForAccountTable.forEach(o -> reconcileTableService.buildAccountRow(table, o.getAccount(), o.getPbAmount(), o.getZenAmount(), o.getYnabAmount(), o.getPbZenDiff(), o.getZenYnabDiff(), o.getStatus()));
                reconcileTableService.buildAccountLastLine(table);

                reconcileTableService.addEmptyLine(table);
                reconcileTableService.addEmptyLine(table);

                LOGGER.debug("Combine all category info, for user: [{}]", userId);
                reconcileTableService.buildTagHeader(table);
                allInfoForTagTable.forEach(t -> reconcileTableService.buildTagSummaryRow(table, t.getCategory(), t.getZenAmount(), t.getYnabAmount(), t.getDiff()));
                reconcileTableService.buildTagLastLine(table);

            }, cfThreadPool).join());
        } catch (Exception e) {
            LOGGER.error("Cannot build reconcile table ", e.getCause());
            return table.toString();
        }
        LOGGER.debug("Finish building reconcile table, for user: [{}]", userId);
        return table.toString();
    }

    private List<ZenYnabTagReconcileProxyObject> mapInfoForTagsTable(AppUser appUser, List<TransactionsItem> ynabTransactions,
                                                                     List<YnabCategories> ynabCategories, Optional<ZenResponse> maybeZr, long startDate, long endDate) {
        final TreeMap<String, BigDecimal> zenSummary =
                zenCommonMapper.getZenTagsSummaryByCategory(startDate, endDate, maybeZr);

        final TreeMap<String, BigDecimal> ynabSummary =
                ynabCommonMapper.getYnabSummaryByCategory(appUser, ynabTransactions, ynabCategories, startDate, endDate);

        List<ZenYnabTagReconcileProxyObject> allInfoForTagTable = new ArrayList<>();
        ynabSummary.forEach((ynabTag, ynabAmount) -> {
            final BigDecimal zenAmount = zenSummary.get(ynabTag);
            if (zenAmount != null) {
                allInfoForTagTable.add(new ZenYnabTagReconcileProxyObject(ynabTag, zenAmount, ynabAmount));
            }
        });
        return allInfoForTagTable
                .stream()
                .filter(not(x -> x.getCategory().isEmpty()))
                .sorted()
                .collect(toUnmodifiableList());
    }

    private Optional<ZenResponse> getZenDiff(AppUser appUser, long startDate) {
        return supplyAsync(() -> {
            LOGGER.debug("Fetching ZEN accounts, for user: [{}]", appUser.getId());
            return zenDiffService.getZenDiffByUser(zenDiffLambdaHolder.getAccount(appUser, startDate));
        }, cfThreadPool).join();
    }

    public List<YnabAccounts> getYnabAccounts(final AppUser appUser, final Optional<YnabBudgets> budgetToReconcile) {
        return budgetToReconcile
                .map(budgets -> mapYnabAccounts(appUser, budgets))
                .orElse(emptyList());
    }

    public Optional<YnabBudgets> getBudget(AppUser appUser, String budgetToReconcile) {
        return supplyAsync(() -> mapYnabBudgetData(appUser, budgetToReconcile), cfThreadPool)
                .join();
    }

    public List<TransactionsItem> getYnabTransactions(final AppUser appUser, final Optional<YnabBudgets> ynabBudget) {
        return ynabBudget
                .map(budgets -> supplyAsync(() -> mapYnabTransactionsData(appUser, budgets.getId()), cfThreadPool).join())
                .orElse(emptyList());
    }

    public List<YnabCategories> getYnabCategories(final AppUser appUser, final Optional<YnabBudgets> ynabBudget) {
        return ynabBudget
                .map(budgets -> supplyAsync(() -> ynabExchangeService.getCategories(appUser, budgets.getId()), cfThreadPool).join())
                .map(ynabCommonMapper::mapYnabCategoriesFromResponse)
                .orElse(emptyList());
    }

    public List<YnabAccounts> mapYnabAccounts(final AppUser appUser, final YnabBudgets budgets) {
        LOGGER.debug("Fetching Ynab accounts, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabExchangeService.getAccounts(appUser, budgets.getId()), cfThreadPool)
                .join()
                .flatMap(yc -> ofNullable(yc.getYnabAccountData().getAccounts()))
                .orElse(emptyList());
    }

    public Optional<YnabBudgets> mapYnabBudgetData(final AppUser appUser, final String budgetToReconcile) {
        LOGGER.debug("Fetching Ynab budgets, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabExchangeService.getBudget(appUser), cfThreadPool)
                .join()
                .flatMap(ynabBudgetResponse -> ynabBudgetResponse
                        .getYnabBudgetData()
                        .getBudgets()
                        .stream()
                        .filter(budget -> budgetToReconcile.equalsIgnoreCase(budget.getName()))
                        .collect(toUnmodifiableList())
                        .stream()
                        .findFirst());
    }

    public List<TransactionsItem> mapYnabTransactionsData(final AppUser appUser, final String budgetToReconcile) {
        LOGGER.debug("Fetching Ynab transactions, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabExchangeService.getYnabTransactions(appUser, budgetToReconcile), cfThreadPool)
                .join()
                .map(ynabBudgetResponse -> ynabBudgetResponse
                        .getData()
                        .getTransactions()
                        .stream()
                        .filter(not(TransactionsItem::isDeleted))
                        .collect(toUnmodifiableList()))
                .orElse(emptyList());
    }

    public List<PbAccountBalance> getPbAccounts(final AppUser appUser) {
        var merchantInfos = ofNullable(merchantService.getAllEnabledMerchants()).orElse(emptyList());
        return pbAccountsService.getPbAsyncAccounts(appUser, merchantInfos);
    }

    public List<ZenYnabAccountReconcileProxyObject> mapInfoForAccountTable(final List<AccountItem> zenAccs,
                                                                           final List<YnabAccounts> ynabAccs,
                                                                           final List<PbAccountBalance> pbAccs) {
        return zenAccs
                .stream()
                .map(zenAcc -> ynabAccs.stream()
                        .filter(yA -> yA.getName().equalsIgnoreCase(zenAcc.getTitle()))
                        .collect(toUnmodifiableList())
                        .stream()
                        .map(yAcc -> mapSimpleRepresentation(pbAccs, zenAcc, zenAcc.getTitle(), yAcc))
                        .collect(toUnmodifiableList()))
                .collect(toUnmodifiableList())
                .stream()
                .flatMap(Collection::stream)
                .collect(toUnmodifiableList());
    }

    private ZenYnabAccountReconcileProxyObject mapSimpleRepresentation(List<PbAccountBalance> pbAccs, AccountItem zenAcc, String zenAccTitle, YnabAccounts yAcc) {
        var ynabBal = ynabCommonMapper.parseYnabBal(valueOf(yAcc.getBalance()));
        var zenBal = BigDecimal.valueOf(zenAcc.getBalance());
        var zenYnabDiff = zenBal.subtract(ynabBal).setScale(CURRENCY_SCALE, HALF_DOWN);
        var status = zenYnabDiff.longValue() == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;

        final Optional<PbAccountBalance> pbAcc = pbAccs.stream()
                .filter(pbAccountBalance -> pbAccountBalance.getAccount().equalsIgnoreCase(zenAccTitle))
                .findFirst();

        if (pbAcc.isPresent()) {
            final BigDecimal pbBal = pbAcc.get().getBalance();
            var zenPbDiff = pbBal.subtract(zenBal).setScale(CURRENCY_SCALE, HALF_DOWN);
            var fullStatus = (zenYnabDiff.longValue() + zenPbDiff.longValue()) == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;
            return new ZenYnabAccountReconcileProxyObject(zenAccTitle, zenBal.toString(), ynabBal.toString(), pbBal.toString(), zenPbDiff.toString(), zenYnabDiff.toString(), fullStatus);
        } else {
            return new ZenYnabAccountReconcileProxyObject(zenAccTitle, zenBal.toString(), ynabBal.toString(), X, X, zenYnabDiff.toString(), status);
        }
    }
}
