package com.github.storytime.mapper.response;

import com.github.storytime.mapper.zen.ZenCommonMapper;
import com.github.storytime.model.export.ExportTransaction;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.DigitsFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.storytime.config.props.Constants.*;
import static java.math.RoundingMode.UP;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByKey;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

@Component
public class ExportMapper {

    private final DigitsFormatter digitsFormatter;
    private final ZenCommonMapper zenCommonMapper;

    @Autowired
    public ExportMapper(final DigitsFormatter digitsFormatter,
                        final ZenCommonMapper zenCommonMapper) {
        this.digitsFormatter = digitsFormatter;
        this.zenCommonMapper = zenCommonMapper;
    }

    public static String getCategory(TransactionItem t) {
        return t.getTag().stream().findFirst().orElse(EMPTY);
    }

    public static String getYear(TransactionItem t) {
        return t.getDate().substring(YEAR_INDEX_BEGIN, YEAR_END_INDEX);
    }

    public static String getMonth(TransactionItem t) {
        return t.getDate().substring(MONTH_BEGIN_INDEX, MONTH_END_INDEX);
    }

    private String getTotal(final Map<String, DoubleSummaryStatistics> groupedByDate) {
        return digitsFormatter.formatAmount(BigDecimal.valueOf(getSum(groupedByDate)).setScale(ZERO_SCALE, UP));
    }

    private String getAvg(final Map<String, DoubleSummaryStatistics> groupedByDate, int dateRangeSize) {
        return digitsFormatter.formatAmount(BigDecimal.valueOf(getSum(groupedByDate) / dateRangeSize).setScale(ZERO_SCALE, UP));
    }

    private Double getSum(final Map<String, DoubleSummaryStatistics> groupedByDate) {
        return groupedByDate
                .values()
                .stream()
                .map(DoubleSummaryStatistics::getSum)
                .reduce(INITIAL_VALUE, Double::sum);
    }

    public List<Map<String, String>> mapExportData(final LinkedHashMap<String, List<ExportTransaction>> groupedByCat,
                                                   final List<ExportTransaction> transactions) {

        final var dateRange = transactions.stream().map(ExportTransaction::date).collect(toSet());
        final List<Map<String, String>> response = new ArrayList<>();

        groupedByCat.forEach((final String categoryId, final List<ExportTransaction> tagsInCategory) -> {
            final var groupedByDate = tagsInCategory
                    .stream()
                    .collect(groupingBy(ExportTransaction::date, summarizingDouble(ExportTransaction::amount)));

            final LinkedHashMap<String, BigDecimal> unSortedMap = new LinkedHashMap<>();
            groupedByDate.forEach((date, amountInfo) -> unSortedMap.put(date, BigDecimal.valueOf(amountInfo.getSum()).setScale(ZERO_SCALE, UP)));

            final LinkedHashMap<String, String> sortedMap = new LinkedHashMap<>();
            sortedMap.put(CATEGORY, categoryId);
            sortedMap.put(TOTAL_EXPORT, this.getTotal(groupedByDate));
            sortedMap.put(MEDIAN, this.getAvg(groupedByDate, dateRange.size()));

            dateRange.forEach(r -> unSortedMap.putIfAbsent(r, new BigDecimal(0)));
            unSortedMap.entrySet()
                    .stream()
                    .sorted(comparingByKey(reverseOrder()))
                    .forEachOrdered(r -> sortedMap.put(r.getKey(), digitsFormatter.formatAmount(r.getValue())));

            response.add(sortedMap);
        });

        return response;
    }

    public List<ExportTransaction> mapTransaction(final Function<TransactionItem, ExportTransaction> transactionMapper,
                                                  final Predicate<TransactionItem> transactionFilter,
                                                  final ZenResponse zenDiff) {
        final var tags = zenCommonMapper.getTags(zenDiff);

        final var notDeletedTr = zenCommonMapper
                .getZenTransactions(zenDiff)
                .stream()
                .filter(transactionFilter)
                .filter(not(TransactionItem::isDeleted))
                .toList();

        return zenCommonMapper
                .flatToParentCategoryTransactionList(tags, notDeletedTr)
                .stream()
                .map(transactionMapper)
                .toList();
    }
}
