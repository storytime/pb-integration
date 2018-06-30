package com.github.storytime.mapper;

import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.time.Instant.now;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

@Component
public class PbToZenMapper {

    private final PbToZenAccountMapper pbToZenAccountMapper;
    private final PbToZenTransactionMapper pbToZenTransactionMapper;

    @Autowired
    public PbToZenMapper(final PbToZenAccountMapper pbToZenAccountMapper,
                         final PbToZenTransactionMapper pbToZenTransactionMapper) {
        this.pbToZenAccountMapper = pbToZenAccountMapper;
        this.pbToZenTransactionMapper = pbToZenTransactionMapper;
    }

    public ZenDiffRequest buildZenReqFromPbData(final List<List<Statement>> newPbTransaction,
                                                final ZenResponse zenDiff,
                                                final User user) {

        newPbTransaction.forEach(t -> pbToZenAccountMapper.mapPbAccountToZen(t, zenDiff));

        final List<TransactionItem> transactionsToZen = newPbTransaction
                .stream()
                .map(t -> new ArrayList<>(pbToZenTransactionMapper.mapPbTransactionToZen(t, zenDiff, user)))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .sorted(comparingLong(TransactionItem::getCreated).reversed())
                .collect(toList());

        return new ZenDiffRequest()
                .setCurrentClientTimestamp(now().getEpochSecond())
                .setLastServerTimestamp(zenDiff.getServerTimestamp())
                .setAccount(zenDiff.getAccount())
                .setTransaction(transactionsToZen);
    }


}
