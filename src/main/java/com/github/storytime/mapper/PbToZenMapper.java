package com.github.storytime.mapper;

import com.github.storytime.model.ExpiredPbStatement;
import com.github.storytime.model.db.AppUser;
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
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenMapper {

    private static final Logger LOGGER = getLogger(PbToZenMapper.class);
    private final PbToZenAccountMapper pbToZenAccountMapper;
    private final PbToZenTransactionMapper pbToZenTransactionMapper;

    @Autowired
    public PbToZenMapper(final PbToZenAccountMapper pbToZenAccountMapper,
                         final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction,
                         final PbToZenTransactionMapper pbToZenTransactionMapper) {
        this.pbToZenAccountMapper = pbToZenAccountMapper;
        this.pbToZenTransactionMapper = pbToZenTransactionMapper;
    }

    public Optional<ZenDiffRequest> buildZenReqFromPbData(final List<List<Statement>> newPbTransaction,
                                                          final ZenResponse zenDiff,
                                                          final AppUser appUser) {

        newPbTransaction.forEach(t -> pbToZenAccountMapper.mapPbAccountToZen(t, zenDiff));

        final List<TransactionItem> allTransactionsToZen = newPbTransaction
                .stream()
                .map(t -> new ArrayList<>(pbToZenTransactionMapper.mapPbTransactionToZen(t, zenDiff, appUser)))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .sorted(comparingLong(TransactionItem::getCreated).reversed())
                .collect(toList());

        return of(new ZenDiffRequest()
                .setCurrentClientTimestamp(now().getEpochSecond())
                .setLastServerTimestamp(zenDiff.getServerTimestamp())
                .setAccount(zenDiff.getAccount())
                .setTransaction(allTransactionsToZen));
    }
}
