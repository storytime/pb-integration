package com.github.storytime.mapper.zen;

import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TagItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.PROJECT_TAG;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;

@Component
public class ZenCommonMapper {

    public TransactionItem flatToParentCategoryId(final List<TagItem> zenTags, final TransactionItem zt) {
        final String innerTagId = findInnerTag(zt);
        final var parentTag = findParentTagId(zenTags, innerTagId);
        return zt.setTag(List.of(parentTag)); //TODO: use copy
    }

    public TransactionItem flatToParentCategoryName(final List<TagItem> zenTags, final TransactionItem zt) {
        final var innerTagId = findInnerTag(zt);
        final var parentTag = findParentTagId(zenTags, innerTagId);
        final var parentTagTitle = zenTags
                .stream()
                .filter(t -> t.getId().equalsIgnoreCase(parentTag))
                .findFirst()
                .map(tag -> ofNullable(tag.getTitle()).orElse(parentTag))
                .orElse(parentTag);

        return zt.setTag(List.of(parentTagTitle)); //TODO: use copy
    }

    private String findParentTagId(List<TagItem> zenTags, String innerTagId) {
        return zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(innerTagId))
                .findFirst()
                .map(tagItem -> ofNullable(tagItem.getParent()).orElse(innerTagId))
                .orElse(innerTagId);
    }

    private String findInnerTag(TransactionItem zt) {
        return ofNullable(zt.getTag()).orElse(emptyList())
                .stream()
                .filter(not(s -> s.startsWith(PROJECT_TAG)))
                .findFirst()
                .orElse(EMPTY);
    }

    public List<TransactionItem> flatToParentCategoryTransactionList(final List<TagItem> zenTags,
                                                                     final List<TransactionItem> trList){
        return trList.stream().map(tagItem -> flatToParentCategoryName(zenTags, tagItem)).toList();
    }

    public String getTagNameByTagId(final List<TagItem> zenTags, final String id) {
        return zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(id))
                .map(TagItem::getTitle)
                .findFirst()
                .orElse(EMPTY);
    }

    public List<TagItem> getTags(final ZenResponse maybeZr) {
        return Optional.of(maybeZr).flatMap(zr -> ofNullable(zr.getTag())).orElse(emptyList());
    }

    public List<AccountItem> getZenAccounts(final ZenResponse maybeZr) {
        return of(maybeZr).flatMap(zr -> ofNullable(zr.getAccount())).orElse(emptyList());
    }

    public List<TransactionItem> getZenTransactions(final ZenResponse maybeZr) {
        return Optional.of(maybeZr).flatMap(zr -> ofNullable(zr.getTransaction())).orElse(emptyList());
    }

    public TreeMap<String, BigDecimal> getZenTagsSummaryByCategory(long startDate,
                                                                   long endDate,
                                                                   final ZenResponse maybeZr) {

        final List<TransactionItem> transactionItems = this.getZenTransactions(maybeZr);
        final List<TagItem> zenTags = this.getTags(maybeZr)
                .stream()
                .filter(not(t -> t.getTitle().startsWith(PROJECT_TAG))).toList();

        final var zenTr = transactionItems
                .stream()
                .filter(not(TransactionItem::isDeleted))
                .filter(zTr -> zTr.getCreated() >= startDate && zTr.getCreated() < endDate).toList()
                .stream()
                .map(zt -> this.flatToParentCategoryId(zenTags, zt))
                .sorted(comparing(TransactionItem::getCreated)).toList();

        //TODO: add amount of transactions
        final Map<String, DoubleSummaryStatistics> groupByTags = zenTr
                .stream()
                .collect(groupingBy(transactionItem -> transactionItem.getTag().stream().findFirst().orElse(EMPTY),
                        Collectors.summarizingDouble(TransactionItem::getOutcome)));

        final TreeMap<String, BigDecimal> zenSummary = new TreeMap<>();
        groupByTags.forEach((zenTagId, summary) -> zenSummary.put(this.getTagNameByTagId(zenTags, zenTagId), BigDecimal.valueOf(summary.getSum())));

        return zenSummary;
    }
}
