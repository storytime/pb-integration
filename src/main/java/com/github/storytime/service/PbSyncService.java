package com.github.storytime.service;

import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.ZenDiffRequest;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);

    private final MerchantService merchantService;
    private final PbStatementsService pbStatementsService;
    private final UserService userService;
    private final ZenDiffService zenDiffService;
    private final PbToZenMapper pbToZenMapper;

    @Autowired
    public PbSyncService(final MerchantService merchantService,
                         final PbStatementsService pbStatementsService,
                         final UserService userService,
                         final ZenDiffService zenDiffService,
                         final PbToZenMapper pbToZenMapper) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.zenDiffService = zenDiffService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
    }

    @Async
    public void sync(final Function<MerchantService, List<MerchantInfo>> selectFunction) {
        userService.findAll().forEach(user -> {
            final List<MerchantInfo> merchants = selectFunction.apply(merchantService);

            if (merchants.isEmpty()) {
                LOGGER.warn("There are no merchants to sync");
                return;
            }

            final List<List<Statement>> newPbDataList = merchants
                    .stream()
                    .map(merchantInfo -> pbStatementsService.getPbTransactions(user, merchantInfo))
                    .collect(toList())
                    .stream()
                    .map(CompletableFuture::join) // get doUpdateZenInfoRequest result
                    .collect(toList());

            final long amountOfNewData = newPbDataList.stream().mapToLong(List::size).sum(); // any new
            if (amountOfNewData > 0) {
                LOGGER.info("User: {} has  {} transactions sync period", user.getId(), amountOfNewData);
                doUpdateZenInfoRequest(user, newPbDataList, merchants);
            } else {
                LOGGER.warn("User: {} has NO new transactions from bank", user.getId());
            }
        });
    }

    private void doUpdateZenInfoRequest(final User user,
                                        final List<List<Statement>> newPbData,
                                        final List<MerchantInfo> merchants) {

        supplyAsync(() -> zenDiffService.getZenDiffByUser(user))
                .thenAccept(ozr -> ozr.ifPresent(zenDiff -> {
                            final ZenDiffRequest request = pbToZenMapper.buildZenReqFromPbData(newPbData, zenDiff, user);
                            zenDiffService.pushToZen(user, request).ifPresent(zr -> {
                                merchantService.saveAll(merchants);
                                userService.updateUserLastZenSyncTime(user.setZenLastSyncTimestamp(zenDiff.getServerTimestamp()));
                            });
                        })
                );
    }

}
