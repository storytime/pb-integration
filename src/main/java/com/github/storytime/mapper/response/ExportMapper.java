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
import static java.math.BigDecimal.valueOf;
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
        return digitsFormatter.formatAmount(valueOf(getSum(groupedByDate)).setScale(ZERO_SCALE, UP));
    }

    private String getAvg(final Map<String, DoubleSummaryStatistics> groupedByDate, int dateRangeSize) {
        return digitsFormatter.formatAmount(valueOf(getSum(groupedByDate) / dateRangeSize).setScale(ZERO_SCALE, UP));
    }

    private Double getSum(final Map<String, DoubleSummaryStatistics> groupedByDate) {
        return groupedByDate
                .values()
                .stream()
                .map(DoubleSummaryStatistics::getSum)
                .reduce(INITIAL_VALUE, Double::sum);
    }

    public List<Map<String, String>> mapExportData(final Map<String, List<ExportTransaction>> groupedByCat) {

        final Set<String> dateRange = groupedByCat
                .entrySet().stream().flatMap(e -> e.getValue().stream()).toList()
                .stream().map(ExportTransaction::date).collect(toSet());

        return groupedByCat
                .entrySet()
                .stream()
                .map(entry -> {
                    final Map<String, BigDecimal> defaultResultMap = dateRange.stream().collect(toMap(k -> k, v -> new BigDecimal(ZERO)));
                    final Map<String, DoubleSummaryStatistics> groupedByDate = entry.getValue()
                            .stream()
                            .collect(groupingBy(ExportTransaction::date, summarizingDouble(ExportTransaction::amount)));

                    final Map<String, String> resultHeaderMap = createHeader(defaultResultMap.size(), entry.getKey(), groupedByDate);
                    final LinkedHashMap<String, BigDecimal> unSortedMap = groupedByDate
                            .entrySet()
                            .stream()
                            .collect(toMap(Map.Entry::getKey, e -> valueOf(e.getValue().getSum()).setScale(ZERO_SCALE, UP), (o1, o2) -> o1, LinkedHashMap::new));

                    defaultResultMap.putAll(unSortedMap);

                    final LinkedHashMap<String, String> sortedMapWithValues = defaultResultMap.entrySet()
                            .stream()
                            .sorted(comparingByKey(reverseOrder()))
                            .collect(toMap(Map.Entry::getKey, e -> digitsFormatter.formatAmount(e.getValue()), (o1, o2) -> o1, LinkedHashMap::new));

                    resultHeaderMap.putAll(sortedMapWithValues);
                    return resultHeaderMap;
                }).toList();

    }

    private LinkedHashMap<String, String> createHeader(final int maxResultSize,
                                                       final String categoryId,
                                                       final Map<String, DoubleSummaryStatistics> groupedByDate) {
        final LinkedHashMap<String, String> headersMap = new LinkedHashMap<>();
        headersMap.put(CATEGORY, categoryId);
        headersMap.put(TOTAL_EXPORT, this.getTotal(groupedByDate));
        headersMap.put(MEDIAN, this.getAvg(groupedByDate, maxResultSize));
        headersMap.put(AVG_VALUED, this.getAvg(groupedByDate, groupedByDate.size()));
        return headersMap;
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
