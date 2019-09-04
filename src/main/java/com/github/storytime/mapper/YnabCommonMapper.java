package com.github.storytime.mapper;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.ynab.YnabTransactionProxyObject;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.from.TransactionsItem;
import com.github.storytime.service.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.service.ReconcileTableService.BALANCE_AFTER_DIGITS;
import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

@Component
public class YnabCommonMapper {

    private final DateService dateService;

    @Autowired
    public YnabCommonMapper(final DateService dateService) {
        this.dateService = dateService;
    }

    public List<YnabCategories> mapYnabCategoriesFromResponse(Optional<YnabCategoryResponse> yMaybeCat) {
        //collect YNAB tags
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

    public String getTagNameByTagId(final List<YnabCategories> zenTags,
                                    final String id) {
        return zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(id))
                .map(YnabCategories::getName)
                .findFirst()
                .orElse(EMPTY);
    }

    public BigDecimal parseYnabBal(String balStr) {
        var endIndex = balStr.length() - BALANCE_AFTER_DIGITS;
        var beforeDot = balStr.substring(START_POS, endIndex);
        var afterDot = balStr.substring(endIndex);
        return BigDecimal.valueOf(Float.valueOf(beforeDot + DOT + afterDot)).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    private Map<String, DoubleSummaryStatistics> getYnabExtendedSummaryByCategory(AppUser appUser,
                                                                                  List<TransactionsItem> ynabTransactions,
                                                                                  List<YnabCategories> ynabCategories,
                                                                                  long startDate,
                                                                                  long endDate) {
        final List<YnabTransactionProxyObject> ynabTransProxy = ynabTransactions
                .stream()
                .filter(transactionsItem -> filterTransactions(appUser, startDate, endDate, transactionsItem))
                .map(transactionsItem -> mapTransactionsToSimpleRepresentation(ynabCategories, transactionsItem))
                .collect(toUnmodifiableList());

        return ynabTransProxy
                .stream()
                .collect(groupingBy(YnabTransactionProxyObject::getCategoryId, summarizingDouble(YnabTransactionProxyObject::getAmount)));
    }

    private YnabTransactionProxyObject mapTransactionsToSimpleRepresentation(List<YnabCategories> ynabCategories,
                                                                             TransactionsItem transactionsItem) {
        final BigDecimal bigDecimal = parseYnabBal(valueOf(transactionsItem.getAmount()));
        final String tagNameByTagId = this.getTagNameByTagId(ynabCategories, transactionsItem.getCategoryId());
        return new YnabTransactionProxyObject(tagNameByTagId, abs(bigDecimal.doubleValue()));
    }

    private boolean filterTransactions(AppUser appUser,
                                       long startDate,
                                       long endDate,
                                       TransactionsItem transactionsItem) {
        final Long yTrDate = dateService.zenStringToZonedSeconds(transactionsItem.getDate(), appUser.getTimeZone());
        return yTrDate >= startDate && yTrDate < endDate;
    }


    public TreeMap<String, Double> getYnabSummaryByCategory(AppUser appUser,
                                                            List<TransactionsItem> ynabTransactions,
                                                            List<YnabCategories> ynabCategories,
                                                            long startDate,
                                                            long endDate) {
        final Map<String, DoubleSummaryStatistics> ynabGroupBy =
                this.getYnabExtendedSummaryByCategory(appUser, ynabTransactions, ynabCategories, startDate, endDate);

        final TreeMap<String, Double> ynabSummary = new TreeMap<>();
        ynabGroupBy.forEach((ynabTagName, summary) -> ynabSummary.put(ynabTagName, summary.getSum()));
        return ynabSummary;
    }
}
