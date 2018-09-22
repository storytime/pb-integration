package com.github.storytime.service;

import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);
    private static final String IS_UPDATE_NEEDED = "isUpdateNeeded";

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
            // TODO: fix in java 11
            final Map<String, Boolean> updateNeeded = new HashMap<>();

            if (amountOfNewData > 0) {
                LOGGER.info("User: {} has  {} transactions sync period", user.getId(), amountOfNewData);
                doUpdateZenInfoRequest(user, newPbDataList, updateNeeded);
            } else {
                LOGGER.warn("User: {} has NO new transactions from bank", user.getId());
            }

            if (updateNeeded.get(IS_UPDATE_NEEDED)) {
                merchantService.saveAll(merchants);
            }

        });
    }

    private Map<String, Boolean> doUpdateZenInfoRequest(final User user,
                                                        final List<List<Statement>> newPbData,
                                                        final Map<String, Boolean> updateNeeded) {

        updateNeeded.put(IS_UPDATE_NEEDED, TRUE);
        supplyAsync(() -> zenDiffService.getZenDiffByUser(user))
                .thenAccept(ozr -> {
                            if (ozr.isPresent()) {
                                final ZenResponse zenDiff = ozr.get();
                                final ZenDiffRequest request = pbToZenMapper.buildZenReqFromPbData(newPbData, zenDiff, user);
                                if (zenDiffService.pushToZen(user, request).isPresent()) {
                                    userService.updateUserLastZenSyncTime(user.setZenLastSyncTimestamp(zenDiff.getServerTimestamp()));
                                } else {
                                    updateNeeded.put(IS_UPDATE_NEEDED, FALSE);
                                }
                            } else {
                                updateNeeded.put(IS_UPDATE_NEEDED, FALSE);
                            }
                        }
                );
        return updateNeeded;
    }

}
