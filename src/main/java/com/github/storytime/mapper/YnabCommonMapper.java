package com.github.storytime.mapper;

import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.ynab.YnabTransactionProxyObject;
import com.github.storytime.model.ynab.category.YnabCategories;
import com.github.storytime.model.ynab.transaction.from.TransactionsItem;
import com.github.storytime.service.utils.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static com.github.storytime.config.props.Constants.CURRENCY_SCALE;
import static com.github.storytime.config.props.Constants.EMPTY;
import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.util.stream.Collectors.*;

@Component
public class YnabCommonMapper {

    private final DateService dateService;

    @Autowired
    public YnabCommonMapper(final DateService dateService) {
        this.dateService = dateService;
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

    public BigDecimal parseYnabBal(final String balStr) {
        return BigDecimal.valueOf(Float.parseFloat(balStr) / 1000).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    private Map<String, DoubleSummaryStatistics> getYnabExtendedSummaryByCategory(final AppUser appUser,
                                                                                  final List<TransactionsItem> ynabTransactions,
                                                                                  final List<YnabCategories> ynabCategories,
                                                                                  final long startDate,
                                                                                  final long endDate) {
        final List<YnabTransactionProxyObject> ynabTransProxy = ynabTransactions
                .stream()
                .filter(transactionsItem -> filterTransactions(appUser, startDate, endDate, transactionsItem))
                .map(transactionsItem -> mapTransactionsToSimpleRepresentation(ynabCategories, transactionsItem)).toList();

        return ynabTransProxy
                .stream()
                .collect(groupingBy(YnabTransactionProxyObject::getCategoryId, summarizingDouble(YnabTransactionProxyObject::getAmount)));
    }

    private YnabTransactionProxyObject mapTransactionsToSimpleRepresentation(final List<YnabCategories> ynabCategories,
                                                                             final TransactionsItem transactionsItem) {
        final BigDecimal bigDecimal = parseYnabBal(valueOf(transactionsItem.getAmount()));
        final String tagNameByTagId = this.getTagNameByTagId(ynabCategories, transactionsItem.getCategoryId());
        return new YnabTransactionProxyObject(tagNameByTagId, abs(bigDecimal.doubleValue()));
    }

    private boolean filterTransactions(final AppUser appUser,
                                       final long startDate,
                                       final long endDate,
                                       final TransactionsItem transactionsItem) {
        final Long yTrDate = dateService.zenStringToZonedSeconds(transactionsItem.getDate(), appUser.getTimeZone());
        return yTrDate >= startDate && yTrDate < endDate;
    }


    public TreeMap<String, BigDecimal> getYnabSummaryByCategory(final AppUser appUser,
                                                                final List<TransactionsItem> ynabTransactions,
                                                                final List<YnabCategories> ynabCategories,
                                                                final long startDate,
                                                                final long endDate) {
        final Map<String, DoubleSummaryStatistics> ynabGroupBy =
                this.getYnabExtendedSummaryByCategory(appUser, ynabTransactions, ynabCategories, startDate, endDate);

        final TreeMap<String, BigDecimal> ynabSummary = new TreeMap<>();
        ynabGroupBy.forEach((ynabTagName, summary) -> ynabSummary.put(ynabTagName, BigDecimal.valueOf(summary.getSum())));
        return ynabSummary;
    }

    public Set<String> selectTags(final List<YnabCategories> ynabTags,
                                  final Predicate<YnabCategories> ynabCategoriesPredicate) {
        return ynabTags
                .stream()
                .filter(ynabCategoriesPredicate)
                .map(YnabCategories::getName)
                .collect(toUnmodifiableSet());
    }
}
