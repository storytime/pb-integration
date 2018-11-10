package com.github.storytime.service;

import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.ExpiredPbStatement;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction;

    @Autowired
    public PbSyncService(final MerchantService merchantService,
                         final PbStatementsService pbStatementsService,
                         final UserService userService,
                         final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction,
                         final ZenDiffService zenDiffService,
                         final PbToZenMapper pbToZenMapper) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.zenDiffService = zenDiffService;
        this.alreadyMappedPbZenTransaction = alreadyMappedPbZenTransaction;
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

            final List<Statement> allNewPbData = newPbDataList
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            final List<ExpiredPbStatement> maybePushed = new ArrayList<>();
            allNewPbData.forEach(t -> {
                final ExpiredPbStatement expiredPbStatement = new ExpiredPbStatement(t);
                if (!alreadyMappedPbZenTransaction.contains(expiredPbStatement)) {
                    maybePushed.add(expiredPbStatement);
                }
            });

            // TODO: fix in java 11 can change value inside lambda
            final Map<String, Boolean> updateNeeded = new HashMap<>();
            updateNeeded.put(IS_UPDATE_NEEDED, TRUE);

            if (maybePushed.isEmpty()) {
                LOGGER.info("No new transaction for user: {} Nothing to push in current sync thread", user.getId());
            } else {
                LOGGER.info("User: {} has {} transactions sync period", user.getId(), maybePushed.size());
                doUpdateZenInfoRequest(user, newPbDataList, updateNeeded);
            }

            //      commit that sync WAS OK
            if (updateNeeded.get(IS_UPDATE_NEEDED)) {
                merchantService.saveAll(merchants);
                alreadyMappedPbZenTransaction.addAll(maybePushed);
            }

        });
    }

    private Map<String, Boolean> doUpdateZenInfoRequest(final AppUser appUser,
                                                        final List<List<Statement>> newPbData,
                                                        final Map<String, Boolean> updateNeeded) {

        supplyAsync(() -> zenDiffService.getZenDiffByUser(appUser))
                .thenAccept(ozr -> {
                            if (ozr.isPresent()) {
                                final ZenResponse zenDiff = ozr.get();
                                //TODo: improve in java 11
                                final Optional<ZenDiffRequest> maybeDiffRequest = pbToZenMapper.buildZenReqFromPbData(newPbData, zenDiff, appUser);
                                if (maybeDiffRequest.isPresent()) {
                                    if (zenDiffService.pushToZen(appUser, maybeDiffRequest.get()).isPresent()) {
                                        userService.updateUserLastZenSyncTime(appUser.setZenLastSyncTimestamp(zenDiff.getServerTimestamp()));
                                    } else {
                                        updateNeeded.put(IS_UPDATE_NEEDED, FALSE);
                                    }
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
