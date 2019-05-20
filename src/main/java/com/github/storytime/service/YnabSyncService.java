package com.github.storytime.service;


import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.ynab.YnabZenComplianceObject;
import com.github.storytime.model.ynab.YnabZenHolder;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.request.YnabTransactions;
import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TagItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.ynab.transaction.YnabTransactionColour.*;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class YnabSyncService {

    private static final Logger LOGGER = getLogger(YnabSyncService.class);
    private final ZenDiffService zenDiffService;
    private final UserService userService;
    private final YnabService ynabService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;
    private final Executor cfThreadPool;
    private final DateService dateService;

    @Autowired
    public YnabSyncService(final ZenDiffService zenDiffService,
                           final ZenDiffLambdaHolder zenDiffLambdaHolder,
                           final YnabService ynabService,
                           final DateService dateService,
                           final Executor cfThreadPool,
                           final UserService userService) {
        this.zenDiffService = zenDiffService;
        this.userService = userService;
        this.ynabService = ynabService;
        this.cfThreadPool = cfThreadPool;
        this.dateService = dateService;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
    }

    public void syncTransactions(final long userId) {
        try {
            LOGGER.debug("Calling ynab sync for user: [{}]", userId);
            userService.findUserById(userId)
                    .map(this::validateUserSettings)
                    .map(appUser -> doSync(appUser));


        } catch (Exception e) {
            LOGGER.error("Cannot push Diff to ZEN request ", e.getCause());
        }
    }

    public Optional<Object> doSync(AppUser appUser) {
        final var clientSyncTime = now().getEpochSecond();
        supplyAsync(() -> zenDiffService.getZenDiffByUser(zenDiffLambdaHolder.getYnabFunction(appUser, clientSyncTime)), cfThreadPool)
                .thenApply(maybeZenResponse -> maybeZenResponse
                        .flatMap(zenResponse -> of(zenResponse.getTransaction())
                                .flatMap(transactionItems -> transactionItems.isEmpty() ? empty() : ynabService.getBudget(appUser)
                                        .flatMap(ynabBudgetResponse -> ynabBudgetResponse
                                                .getYnabBudgetData()
                                                .getBudgets()
                                                .stream()
                                                .filter(budgetsItem -> budgetsItem.getName().equals(appUser.getYnabSyncBudget()))
                                                .findFirst())
                                        .flatMap(budgetsItem -> ynabService.getCategories(appUser, budgetsItem.getId())
                                                .flatMap(ynabCategoryResponse -> ynabService.getAccounts(appUser, budgetsItem.getId())
                                                        .flatMap(ynabAccountResponse -> mapData(budgetsItem.getId(), appUser, zenResponse, ynabCategoryResponse, ynabAccountResponse)))

                                        )

                                ))
                );
        return empty();
    }

    public AppUser validateUserSettings(@NotNull final AppUser user) {
        final String ynabToken = user.getYnabAuthToken();
        if (isEmpty(ynabToken)) {
            LOGGER.error("Ynab auth token of for user [{}] is empty. Stopping ynab sync", user.id);
            return null;
        }

        final String yanbBudgetName = user.getYnabSyncBudget();
        if (isEmpty(yanbBudgetName)) {
            LOGGER.error("Ynab budget name of for user [{}] is empty. Stopping ynab sync", user.id);
            return null;
        }

        final Long yanbStartSyncDate = user.getYnabLastSyncTimestamp();
        if (yanbStartSyncDate == null || yanbStartSyncDate == 0L) {
            LOGGER.error("Ynab start date is empty for user [{}]. Stopping ynab sync", user.id);
            return null;
        }

        if (!user.getYnabSyncEnabled()) {
            LOGGER.debug("Ynab sync for user [{}] is no enabled.", user.id);
            return null;
        }

        return user;
    }

    public Optional<String> mapData(final String id,
                                    final AppUser appUser,
                                    final ZenResponse zenResponse,
                                    final YnabCategoryResponse ynabCategoryResponse,
                                    final YnabAccountResponse ynabAccountResponse) {

        final List<YnabAccounts> ynabAccounts = ofNullable(ynabAccountResponse.getYnabAccountData().getAccounts()).orElse(emptyList());
        final List<AccountItem> zenAccounts = ofNullable(zenResponse.getAccount()).orElse(emptyList());
        final List<TagItem> zenTags = ofNullable(zenResponse.getTag()).orElse(emptyList());

        final YnabZenHolder commonAccounts = mapCommonAccounts(zenAccounts, ynabAccounts);
        final YnabZenHolder commonTags = mapCommonTags(zenTags, ynabCategoryResponse);
        final List<TransactionItem> transaction = filterZenTransactionToSync(appUser, zenResponse, commonAccounts);

        final List<YnabTransactions> ynabTransactions = transaction
                .stream()
                .map(zTr -> commonAccounts
                        .findByZenId(zTr.getIncomeAccount())
                        .map(ynabZenComplianceObject -> createYnabTransactions(commonTags, zTr, ynabZenComplianceObject))
                        .get())
                .collect(toUnmodifiableList());


        final YnabTransactionsRequest ynabTransactionsRequest = new YnabTransactionsRequest();
        ynabTransactionsRequest.setTransactions(ynabTransactions);
        return ynabService.pushToYnab(appUser, id, ynabTransactionsRequest);
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
        ynabTransactions.setCategoryId(tagId);
        ynabTransactions.setPayeeName(zTr.getPayee());

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

    public List<TransactionItem> filterZenTransactionToSync(final AppUser appUser,
                                                            final ZenResponse zenResponse,
                                                            final YnabZenHolder commonAccounts) {
        return zenResponse.getTransaction()
                .stream()
                .filter(zt -> zt.getCreated() > appUser.getYnabLastSyncTimestamp()) //only new transactions
                .filter(zt -> commonAccounts.isExistsByZenId(zt.getIncomeAccount())) //only for common accounts
                .filter(zt -> commonAccounts.isExistsByZenId(zt.getOutcomeAccount())) //only for common accounts
                .filter(not(TransactionItem::isDeleted))
                .filter(zt -> dateService.zenStringToZonedSeconds(zt.getDate(), appUser.getTimeZone()) >= appUser.getYnabLastSyncTimestamp()) //sometimes tr can have wrong date
                .sorted(comparing(TransactionItem::getCreated))
                .collect(toUnmodifiableList());
    }

    public YnabZenHolder mapCommonTags(final List<TagItem> responseZenTags,
                                       final YnabCategoryResponse ynabCategoryResponse) {

        final YnabZenHolder commonTags = new YnabZenHolder();

        //filter zen tags to avoid #projects
        final List<TagItem> zenTags = responseZenTags
                .stream()
                .filter(not(t -> t.getTitle().startsWith(PROJECT_TAG)))
                .collect(toUnmodifiableList());

        //collect ynab tags
        final List<YnabCategories> ynabTags =
                ofNullable(ynabCategoryResponse.getYnabCategoryData())
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
