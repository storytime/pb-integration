package com.github.storytime.service.info;

import com.github.storytime.mapper.ReconcileCommonMapper;
import com.github.storytime.mapper.YnabCommonMapper;
import com.github.storytime.mapper.response.YnabResponseMapper;
import com.github.storytime.mapper.zen.ZenCommonMapper;
import com.github.storytime.model.api.PbZenReconcileJson;
import com.github.storytime.model.db.AppUser;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

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

    public String reconcileTableDefault(final long userId, final String budgetName) {
        try {
            var appUser = userService.findUserById(userId).orElseThrow();
            int year = YearMonth.now(ZoneId.of(appUser.getTimeZone())).getYear();
            int month = YearMonth.now(ZoneId.of(appUser.getTimeZone())).getMonthValue();
            return this.reconcileTableByDate(userId, budgetName, year, month);
        } catch (Exception e) {
            LOGGER.error("Cannot build reconcile table for user [{}], error [{}] for default dates", userId, e.getCause());
            return EMPTY;
        }
    }

    public String reconcileTableAll(final long userId) {
        try {
            var appUser = userService.findUserById(userId).orElseThrow();
            int year = YearMonth.now(ZoneId.of(appUser.getTimeZone())).getYear();
            int month = YearMonth.now(ZoneId.of(appUser.getTimeZone())).getMonthValue();

            return ynabSyncServiceRepository
                    .findAllByEnabledIsTrueAndUserId(appUser.getId())
                    .orElse(emptyList())
                    .stream()
                    .map(YnabSyncConfig::getBudgetName)
                    .collect(toUnmodifiableList())
                    .stream()
                    .map(budgetName -> reconcileTableByDate(userId, budgetName, year, month))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            LOGGER.error("Cannot build reconcile table for user [{}], error [{}] for default dates", userId, e.getCause());
            return EMPTY;
        }
    }

    public ResponseEntity<PbZenReconcileJson> reconcilePbJson(final long userId) {
        try {
            LOGGER.debug("Building pb/zen json, collecting info, for user: [{}]", userId);
            return userService.findUserById(userId).map(appUser -> {
                var merchantInfos = ofNullable(merchantService.getAllEnabledMerchants()).orElse(emptyList());
                var pbAccs = pbAccountService.getPbAsyncAccounts(appUser, merchantInfos);
                var maybeZr = zenAsyncService.zenDiffByUserForReconcile(appUser, 0).join().orElseThrow();
                var zenAccs = zenCommonMapper.getZenAccounts(maybeZr);

                LOGGER.debug("Combine pb/zen info collecting info, for user: [{}]", userId);
                var pbZenReconcile = reconcileCommonMapper.mapInfoForAccountJson(zenAccs, pbAccs.join());
                LOGGER.debug("All is ready pb/zen, for user: [{}]", userId);
                return new ResponseEntity<>(new PbZenReconcileJson(pbZenReconcile), OK);
            }).orElse(new ResponseEntity<>(NO_CONTENT));
        } catch (Exception e) {
            LOGGER.error("Cannot build pb/zen json for user [{}], error [{}]", userId, e.getCause());
            return new ResponseEntity<>(NO_CONTENT);
        }
    }

    public String reconcileTableByDate(final long userId, final String budgetName, int year, int mouth) {
        var table = new StringBuilder(EMPTY);
        try {
            LOGGER.debug("Building reconcile table, collecting info, for user: [{}]", userId);
            userService.findUserById(userId).ifPresent(appUser -> {
                final long startDate = dateService.getStartOfMouthInSeconds(year, mouth, appUser);
                final long endDate = dateService.getEndOfMouthInSeconds(year, mouth, appUser);

                var ynabBudget = mapYnabBudgetData(appUser, budgetName);
                var ynabAccs = getYnabAccounts(appUser, ynabBudget);
                var merchantInfos = ofNullable(merchantService.getAllEnabledMerchants())
                        .orElse(emptyList())
                        .stream()
                        .filter(m -> ynabAccs.stream().anyMatch(ynabAccount -> ynabAccount.getName().equals(ofNullable(m.getShortDesc()).orElse(EMPTY))))
                        .collect(toUnmodifiableList());
                var pbAccs = pbAccountService.getPbAsyncAccounts(appUser, merchantInfos);
                var ynabTransactions = getYnabTransactions(appUser, ynabBudget);
                var ynabCategories = getYnabCategories(appUser, ynabBudget);
                var maybeZr = zenAsyncService.zenDiffByUserForReconcile(appUser, startDate).join().orElseThrow();
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

            });
        } catch (Exception e) {
            LOGGER.error("Cannot build reconcile table for user [{}], error [{}]", userId, e.getCause());
            return table.toString();
        }
        LOGGER.debug("Finish building reconcile table, for user: [{}]", userId);
        reconcileTableService.addEmptyLine(table);
        reconcileTableService.addEmptyLine(table);
        reconcileTableService.addEmptyLine(table);
        return table.toString();
    }

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
                .sorted(comparing(ZenYnabTagReconcileProxyObject::getZenAmount).reversed())
                .collect(toUnmodifiableList());
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
}
