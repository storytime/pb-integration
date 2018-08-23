package com.github.storytime.mapper;

import com.github.storytime.model.ExpiredTransactionItem;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.time.Instant.now;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenMapper {

    private static final Logger LOGGER = getLogger(PbToZenMapper.class);
    private final PbToZenAccountMapper pbToZenAccountMapper;
    private final PbToZenTransactionMapper pbToZenTransactionMapper;
    private final Set<ExpiredTransactionItem> alreadyMappedPbZenTransaction;

    @Autowired
    public PbToZenMapper(final PbToZenAccountMapper pbToZenAccountMapper,
                         final Set<ExpiredTransactionItem> alreadyMappedPbZenTransaction,
                         final PbToZenTransactionMapper pbToZenTransactionMapper) {
        this.pbToZenAccountMapper = pbToZenAccountMapper;
        this.alreadyMappedPbZenTransaction = alreadyMappedPbZenTransaction;
        this.pbToZenTransactionMapper = pbToZenTransactionMapper;
    }

    public ZenDiffRequest buildZenReqFromPbData(final List<List<Statement>> newPbTransaction,
                                                final ZenResponse zenDiff,
                                                final User user) {

        newPbTransaction.forEach(t -> pbToZenAccountMapper.mapPbAccountToZen(t, zenDiff));

        final List<TransactionItem> allTransactionsToZen = newPbTransaction
                .stream()
                .map(t -> new ArrayList<>(pbToZenTransactionMapper.mapPbTransactionToZen(t, zenDiff, user)))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .sorted(comparingLong(TransactionItem::getCreated).reversed())
                .collect(toList());

        final List<TransactionItem> notPushedTransactionsToZen = new ArrayList<>(allTransactionsToZen.size());
        allTransactionsToZen.forEach(t -> {
            final ExpiredTransactionItem expiredTransactionItem = new ExpiredTransactionItem(t);
            if (!alreadyMappedPbZenTransaction.contains(expiredTransactionItem)) {
                alreadyMappedPbZenTransaction.add(expiredTransactionItem);
                notPushedTransactionsToZen.add(t);
            }
        });

        if (notPushedTransactionsToZen.isEmpty()) {
            LOGGER.warn("All Transaction for user: {} were already pushed", user.getId());
        } else {
            notPushedTransactionsToZen.forEach(transactionItem -> LOGGER.debug("New transaction: {}", transactionItem));
        }

        return new ZenDiffRequest()
                .setCurrentClientTimestamp(now().getEpochSecond())
                .setLastServerTimestamp(zenDiff.getServerTimestamp())
                .setAccount(zenDiff.getAccount())
                .setTransaction(notPushedTransactionsToZen);
    }
}
