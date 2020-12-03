package com.github.storytime.mapper;

import com.github.storytime.mapper.zen.ZenCommonMapper;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.ynab.YnabToZenSyncHolder;
import com.github.storytime.model.ynab.YnabZenHolder;
import com.github.storytime.model.ynab.YnabZenSyncObject;
import com.github.storytime.model.ynab.account.YnabAccounts;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.transaction.request.YnabTransactions;
import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TagItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.service.utils.DateService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.ynab.transaction.YnabTransactionColour.*;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.*;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class YnabZenCommonMapper {

    private static final Logger LOGGER = getLogger(YnabZenCommonMapper.class);
    private final YnabCommonMapper ynabCommonMapper;
    private final ZenCommonMapper zenCommonMapper;
    private final DateService dateService;
    //TODO: replace
    private final ConcurrentHashMap<String, Boolean> ynabTransfer = new ConcurrentHashMap<>();

    @Autowired
    public YnabZenCommonMapper(final YnabCommonMapper ynabCommonMapper,
                               final ZenCommonMapper zenCommonMapper,
                               final DateService dateService) {
        this.ynabCommonMapper = ynabCommonMapper;
        this.zenCommonMapper = zenCommonMapper;
        this.dateService = dateService;
    }


    public YnabZenHolder mapYnabZenSameTags(final List<TagItem> responseZenTags, final List<YnabCategories> ynabTags) {

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
        LOGGER.debug("YNAB tags that were NOT mapped [{}]", ynabCommonMapper.selectTags(ynabTags, not(ynabCategoriesPredicate)));
        LOGGER.debug("YNAB tags that were mapped [{}]", ynabCommonMapper.selectTags(ynabTags, ynabCategoriesPredicate));

        return sameTags;
    }

    public YnabZenHolder mapYnabZenSameAccounts(final List<AccountItem> zenAccounts,
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


    public Optional<YnabTransactionsRequest> mapDataAllDataForYnab(final List<AccountItem> zenAccounts,
                                                                   final List<TagItem> zenTags,
                                                                   final List<TransactionItem> zenTransactions,
                                                                   final List<YnabCategories> ynabCategories,
                                                                   final List<YnabAccounts> ynabAccounts,
                                                                   final YnabSyncConfig ynabSyncConfig,
                                                                   final AppUser user) {

        final YnabZenHolder sameAccounts = this.mapYnabZenSameAccounts(zenAccounts, ynabAccounts);
        final YnabZenHolder sameTags = this.mapYnabZenSameTags(zenTags, ynabCategories);
        final List<TransactionItem> zenTransaction = this.selectZenNotSyncedTransactions(zenTransactions, sameAccounts, ynabSyncConfig)
                .stream()
                .map(zt -> zenCommonMapper.flatToParentCategory(zenTags, zt))
                .collect(toUnmodifiableList());

        final List<YnabTransactions> ynabTransactions = zenTransaction
                .stream()
                .map(zTr -> sameAccounts
                        .findByZenId(zTr.getIncomeAccount())
                        .or(() -> sameAccounts.findByZenId(zTr.getOutcomeAccount()))
                        .map(ynabZenSyncObject -> createYnabTransactions(sameTags, zTr, ynabZenSyncObject, user))
                        .get())
                .collect(toUnmodifiableList());

        if (sameAccounts.isEmpty()) {
            LOGGER.debug("Finish! No same accounts for budget: [{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.getId());
            return empty();
        }

        if (ynabTransactions.isEmpty()) {
            LOGGER.debug("Finish! No not synced transactions for budget: [{}] for user [{}]", ynabSyncConfig.getBudgetName(), user.getId());
            return empty();
        }

        return of(new YnabTransactionsRequest(ynabTransactions));
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
                .orElse(null);

        this.mapTransactionType(zenRawTr, ynabTransactions, zTag);
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
}
