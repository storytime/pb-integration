package com.github.storytime.mapper;

import com.github.storytime.error.exception.ZenUserNotFoundException;
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
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class ZenCommonMapper {

    public TransactionItem flatToParentCategory(final List<TagItem> zenTags, final TransactionItem zt) {
        final String innerTagId = ofNullable(zt.getTag()).orElse(emptyList())
                .stream()
                .filter(not(s -> s.startsWith(PROJECT_TAG)))
                .findFirst()
                .orElse(EMPTY);

        final String parentTag = zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(innerTagId))
                .findFirst()
                .map(tagItem -> ofNullable(tagItem.getParent()).orElse(innerTagId))
                .orElse(innerTagId);

        return zt.setTag(List.of(parentTag)); //TODO: use copy
    }

    public String getTagNameByTagId(final List<TagItem> zenTags, final String id) {
        return zenTags
                .stream()
                .filter(tagItem -> tagItem.getId().equalsIgnoreCase(id))
                .map(TagItem::getTitle)
                .findFirst()
                .orElse(EMPTY);
    }

    public List<TagItem> getTags(final Optional<ZenResponse> maybeZr) {
        return maybeZr.flatMap(zr -> ofNullable(zr.getTag())).orElse(emptyList());
    }

    public List<AccountItem> getZenAccounts(final Optional<ZenResponse> maybeZr) {
        return maybeZr.flatMap(zr -> ofNullable(zr.getAccount())).orElse(emptyList());
    }

    public List<TransactionItem> getZenTransactions(final Optional<ZenResponse> maybeZr) {
        return maybeZr.flatMap(zr -> ofNullable(zr.getTransaction())).orElse(emptyList());
    }

    public TreeMap<String, BigDecimal> getZenTagsSummaryByCategory(long startDate,
                                                                   long endDate,
                                                                   final Optional<ZenResponse> maybeZr) {


        final List<TransactionItem> transactionItems = this.getZenTransactions(maybeZr);
        final List<TagItem> zenTags = this.getTags(maybeZr)
                .stream()
                .filter(not(t -> t.getTitle().startsWith(PROJECT_TAG)))
                .collect(toUnmodifiableList());

        final var zenTr = transactionItems
                .stream()
                .filter(not(TransactionItem::isDeleted))
                .filter(zTr -> zTr.getCreated() >= startDate && zTr.getCreated() < endDate)
                .collect(toUnmodifiableList())
                .stream()
                .map(zt -> this.flatToParentCategory(zenTags, zt))
                .sorted(comparing(TransactionItem::getCreated))
                .collect(toUnmodifiableList());

        //TODO: add amount of transactions
        final Map<String, DoubleSummaryStatistics> groupByTags = zenTr
                .stream()
                .collect(groupingBy(transactionItem -> transactionItem.getTag().stream().findFirst().orElse(EMPTY),
                        Collectors.summarizingDouble(TransactionItem::getOutcome)));

        final TreeMap<String, BigDecimal> zenSummary = new TreeMap<>();
        groupByTags.forEach((zenTagId, summary) -> zenSummary.put(this.getTagNameByTagId(zenTags, zenTagId), BigDecimal.valueOf(summary.getSum())));

        return zenSummary;
    }

    public int getUserId(final ZenResponse zenDiff) {
        return zenDiff
                .getUser()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ZenUserNotFoundException("Zen User not found")).getId();
    }

}
