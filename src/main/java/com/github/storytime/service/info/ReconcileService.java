package com.github.storytime.service.info;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.budget.YnabBudgets;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.repository.YnabSyncServiceRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static com.github.storytime.config.props.Constants.*;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ReconcileService {

    public static final int BALANCE_AFTER_DIGITS = 3;
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String VERTICAL_BAR = "|";
    public static final String END_HEADER_LINE = "+\n|";
    public static final String END_LINE = "|\n";
    public static final int ACCOUNT_NAME = 30;
    public static final int ZEN_BALANCE = 15;
    public static final int YNAB_BALANCE = 15;
    public static final int PB_BALANCE = 15;
    public static final int ZEN_YNAB_DIFF = 15;
    public static final int ZEN_PB_DIFF = 15;
    public static final int ACCOUNT_STATUS = 12;
    public static final int VERTICAL_BAR_SIZE = 1;
    public static final int END_LINE_SIZE = 2;
    public static final int END_HEADER_SIZE = 3;

    //Total table size
    public static final int TABLE_SIZE = ACCOUNT_NAME + ZEN_BALANCE + YNAB_BALANCE + PB_BALANCE + ZEN_PB_DIFF + ZEN_YNAB_DIFF + ACCOUNT_STATUS + END_HEADER_SIZE + END_LINE_SIZE + VERTICAL_BAR_SIZE + 1;
    public static final String ACCOUNT = "ACCOUNT";
    public static final String ZEN = "ZEN";
    public static final String YNAB = "YNAB";
    public static final String PB = "PB";
    public static final String PB_ZEN = "PB-ZEN";
    public static final String ZEN_YNAB = "ZEN-YNAB";
    public static final String STATUS = "STATUS";
    public static final String X = "X";
    private static final Logger LOGGER = LogManager.getLogger(ReconcileService.class);
    private final ZenDiffService zenDiffService;
    private final UserService userService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;
    private final YnabSyncServiceRepository ynabSyncServiceRepository;
    private final Executor cfThreadPool;
    private final YnabExchangeService ynabExchangeService;
    private final MerchantService merchantService;
    private final PbAccountsService pbAccountsService;

    @Autowired
    public ReconcileService(final ZenDiffService zenDiffService,
                            final UserService userService,
                            final Executor cfThreadPool,
                            final MerchantService merchantService,
                            final YnabSyncServiceRepository ynabSyncServiceRepository,
                            final YnabExchangeService ynabExchangeService,
                            final PbAccountsService pbAccountsService,
                            final ZenDiffLambdaHolder zenDiffLambdaHolder) {
        this.zenDiffService = zenDiffService;
        this.userService = userService;
        this.cfThreadPool = cfThreadPool;
        this.merchantService = merchantService;
        this.ynabExchangeService = ynabExchangeService;
        this.ynabSyncServiceRepository = ynabSyncServiceRepository;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
        this.pbAccountsService = pbAccountsService;
    }


    public String getRecompileTable(final long userId, final String budgetName) {
        var table = new StringBuilder(EMPTY);
        try {
            LOGGER.debug("Building reconcile table, collecting info, for user: [{}]", userId);
            userService.findUserById(userId).ifPresent(appUser -> runAsync(() -> {
                var pbAccs = getPbAccounts(appUser);
                var ynabAccs = getYnabAccounts(appUser, findUserBudget(userId, budgetName));
                var zenAccs = getZenAccounts(appUser);

                LOGGER.debug("Combine accounts info collecting info, for user: [{}]", userId);
                buildRow(table, ACCOUNT, ZEN, YNAB, PB, PB_ZEN, ZEN_YNAB, STATUS);
                zenAccs.forEach(zenAcc -> combineAccountsInfo(table, ynabAccs, pbAccs, zenAcc));
                buildCell(table, rightPad(PLUS, TABLE_SIZE, MINUS), PLUS, VERTICAL_BAR_SIZE);

            }, cfThreadPool).join());
        } catch (Exception e) {
            LOGGER.error("Cannot build reconcile table ", e.getCause());
            return table.toString();
        }
        LOGGER.debug("Finish building reconcile table, for user: [{}]", userId);
        return table.toString();
    }

    public String findUserBudget(final long userId, final String budgetName) {
        return ynabSyncServiceRepository
                .findByUserId(userId)
                .orElse(emptyList())
                .stream()
                .collect(toUnmodifiableList())
                .stream()
                .filter(ynabSyncConfig -> ynabSyncConfig.getBudgetName().equalsIgnoreCase(budgetName))
                .findFirst()
                .map(YnabSyncConfig::getBudgetName)
                .orElse(EMPTY);
    }

    public List<AccountItem> getZenAccounts(final AppUser appUser) {
        LOGGER.debug("Fetching ZEN accounts, for user: [{}]", appUser.getId());
        return supplyAsync(() -> zenDiffService.getZenDiffByUser(zenDiffLambdaHolder.getAccount(appUser)), cfThreadPool)
                .join()
                .flatMap(zr -> ofNullable(zr.getAccount()))
                .orElse(emptyList());
    }

    public List<YnabAccounts> getYnabAccounts(final AppUser appUser, final String budgetToReconcile) {
        return supplyAsync(() -> mapYnabBudgetData(appUser, budgetToReconcile), cfThreadPool)
                .join()
                .map(budgets -> mapYnabAccounts(appUser, budgets))
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
        LOGGER.debug("Fetching Ynab data, for user: [{}]", appUser.getId());
        return ynabExchangeService.getBudget(appUser)
                .flatMap(ynabBudgetResponse -> ynabBudgetResponse
                        .getYnabBudgetData()
                        .getBudgets()
                        .stream()
                        .filter(budget -> budgetToReconcile.equalsIgnoreCase(budget.getName()))
                        .collect(toUnmodifiableList())
                        .stream()
                        .findFirst());
    }

    public List<PbAccountBalance> getPbAccounts(final AppUser appUser) {
        var merchantInfos = ofNullable(merchantService.getAllEnabledMerchants()).orElse(emptyList());
        return pbAccountsService.getPbAsyncAccounts(appUser, merchantInfos);
    }

    public void combineAccountsInfo(final StringBuilder table,
                                    final List<YnabAccounts> ynabAccs,
                                    final List<PbAccountBalance> pbAccs,
                                    final AccountItem zenAcc) {
        var zenAccTitle = zenAcc.getTitle();
        ynabAccs.stream()
                .filter(yA -> yA.getName().equalsIgnoreCase(zenAccTitle))
                .collect(toUnmodifiableList())
                .stream()
                .findFirst()
                .ifPresent(yAcc -> buildTable(table, pbAccs, zenAcc, yAcc));

    }

    public void buildTable(final StringBuilder table,
                           final List<PbAccountBalance> pbAccs,
                           final AccountItem zenAcc,
                           final YnabAccounts yAcc) {
        var ynabBal = parseYnabBal(yAcc);
        var zenAccTitle = zenAcc.getTitle();
        var zenBal = BigDecimal.valueOf(zenAcc.getBalance());
        var zenYnabDiff = zenBal.subtract(ynabBal).setScale(CURRENCY_SCALE, HALF_DOWN);
        var status = zenYnabDiff.longValue() == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;

        final Consumer<PbAccountBalance> ifExistsFk = pbAccountBalance -> {
            final BigDecimal balance = pbAccountBalance.getBalance();
            var zenPbDiff = balance.subtract(zenBal).setScale(CURRENCY_SCALE, HALF_DOWN);
            var fullStatus = (zenYnabDiff.longValue() + zenPbDiff.longValue()) == ZERO_DIIF ? RECONCILE_OK : RECONCILE_NOT_OK;

            buildRow(table, zenAccTitle, zenBal.toString(), ynabBal.toString(), balance.toString(), zenPbDiff.toString(), zenYnabDiff.toString(), fullStatus);
        };

        final Runnable ifNotExistsFk =
                () -> buildRow(table, zenAccTitle, zenBal.toString(), ynabBal.toString(), X, X, zenYnabDiff.toString(), status);

        pbAccs.stream()
                .filter(pbAccountBalance -> pbAccountBalance.getAccount().equalsIgnoreCase(zenAccTitle))
                .findFirst()
                .ifPresentOrElse(ifExistsFk, ifNotExistsFk);
    }

    public BigDecimal parseYnabBal(YnabAccounts yAcc) {
        var balStr = String.valueOf(yAcc.getBalance());
        var endIndex = balStr.length() - BALANCE_AFTER_DIGITS;
        var beforeDot = String.valueOf(yAcc.getBalance()).substring(START_POS, endIndex);
        var afterDot = String.valueOf(yAcc.getBalance()).substring(endIndex, balStr.length());
        return BigDecimal.valueOf(Float.valueOf(beforeDot + DOT + afterDot)).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    public void buildRow(final StringBuilder table,
                         final String s1,
                         final String s2,
                         final String s3,
                         final String s4,
                         final String s5,
                         final String s6,
                         final String s7) {
        buildCell(table, rightPad(PLUS, TABLE_SIZE, MINUS), END_HEADER_LINE, END_HEADER_SIZE);
        buildCell(table, center(s1, ACCOUNT_NAME, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s2, ZEN_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s3, YNAB_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s4, PB_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s5, ZEN_PB_DIFF, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s6, ZEN_YNAB_DIFF, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildCell(table, center(s7, ACCOUNT_STATUS, SPACE), END_LINE, END_LINE_SIZE);
    }

    public void buildCell(final StringBuilder table,
                          final String s,
                          final String endHeaderLine,
                          final int endHeaderSize) {
        table.append(s);
        table.append(rightPad(endHeaderLine, endHeaderSize));
    }
}
