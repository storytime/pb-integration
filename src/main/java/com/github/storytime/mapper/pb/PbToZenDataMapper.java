package com.github.storytime.mapper.pb;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenDataMapper {

    private static final Logger LOGGER = getLogger(PbToZenDataMapper.class);
    private final PbToZenAccountMapper pbToZenAccountMapper;
    private final PbToZenTransactionMapper pbToZenTransactionMapper;

    @Autowired
    public PbToZenDataMapper(final PbToZenAccountMapper pbToZenAccountMapper,
                             final PbToZenTransactionMapper pbToZenTransactionMapper) {

        this.pbToZenAccountMapper = pbToZenAccountMapper;
        this.pbToZenTransactionMapper = pbToZenTransactionMapper;
    }

    public Optional<ZenDiffRequest> buildZenReqFromPbData(final List<List<Statement>> newPbTransaction,
                                                          final ZenResponse zenDiff,
                                                          final AppUser appUser) {

        try {
            LOGGER.debug("Starting build data for zen, for user: [{}] ", appUser.getId());
            final boolean isAccountsPushNeeded = newPbTransaction
                    .stream()
                    .map(t -> pbToZenAccountMapper.mapPbAccountToZen(t, zenDiff))
                    .collect(toSet())
                    .stream()
                    .anyMatch(r -> r == TRUE);

            LOGGER.debug("No new accounts, from bank, for user: [{}]", appUser.getId());
            final List<TransactionItem> allMappedTransactionsForZen = newPbTransaction
                    .stream()
                    .map(t -> pbToZenTransactionMapper.mapPbTransactionToZen(t, zenDiff, appUser))
                    .flatMap(Collection::stream)
                    .sorted(comparingLong(TransactionItem::getCreated).reversed()).toList();

            // based on createIdForZenForOwnTransfer we can have 2 IDs for 2 transfer between own account
            final var groupedById = allMappedTransactionsForZen.stream().collect(groupingBy(TransactionItem::getId));

            final Stream<TransactionItem> transactionItemsByUniqId = groupedById.values()
                    .stream().filter(x -> x.size() == 1).toList()
                    .stream().flatMap(List::stream);

            final Stream<TransactionItem> transactionOfTransfer = groupedById.values().stream().filter(trList -> trList.size() > 1).toList().stream()
                    .flatMap(trList -> trList.stream().filter(TransactionItem::isAmountNotZero));

            final List<TransactionItem> allNew = Stream.concat(transactionItemsByUniqId, transactionOfTransfer).toList();
            LOGGER.debug("Transactions from bank, are ready for user: [{}], total count: [{}]", appUser.getId(), allNew.size());

            return of(new ZenDiffRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setLastServerTimestamp(zenDiff.getServerTimestamp())
                    .setAccount(isAccountsPushNeeded ? zenDiff.getAccount() : emptyList())
                    .setTransaction(allNew));

        } catch (Exception e) {
            LOGGER.error("Error during mapping: [{}]", e.getMessage(), e);
            return empty();
        }
    }
}
