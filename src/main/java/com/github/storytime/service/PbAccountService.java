package com.github.storytime.service;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.mapper.response.PbAccountBalanceResponseMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.pb.jaxb.request.Request;
import com.github.storytime.service.http.PbAccountsHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CARD_LAST_DIGITS;
import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbAccountService {

    private static final Logger LOGGER = LogManager.getLogger(PbAccountService.class);

    private final PbRequestBuilder pbRequestBuilder;
    private final PbAccountsHttpService pbAccountsHttpService;
    private final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper;
    private final Executor cfThreadPool;

    @Autowired
    public PbAccountService(final PbAccountsHttpService pbAccountsHttpService,
                            final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper,
                            final Executor cfThreadPool,
                            final PbRequestBuilder pbRequestBuilder) {
        this.pbAccountsHttpService = pbAccountsHttpService;
        this.pbAccountBalanceResponseMapper = pbAccountBalanceResponseMapper;
        this.cfThreadPool = cfThreadPool;
        this.pbRequestBuilder = pbRequestBuilder;
    }

    public List<PbAccountBalance> getPbAsyncAccounts(final AppUser appUser,
                                                     final List<MerchantInfo> merchantInfos) {

        LOGGER.debug("Fetching PB accounts, for user: [{}]", appUser.getId());

        final List<CompletableFuture<PbAccountBalance>> pbAccountCf = merchantInfos
                .stream()
                .map(merchantInfo -> getPbAsyncAccounts(appUser, merchantInfo))
                .collect(toUnmodifiableList());

        return CompletableFuture
                .allOf(pbAccountCf.toArray(new CompletableFuture[pbAccountCf.size()]))
                .thenApply(aVoid -> pbAccountCf.stream().map(CompletableFuture::join).collect(toUnmodifiableList()))
                .join();

    }

    public CompletableFuture<PbAccountBalance> getPbAsyncAccounts(final AppUser u, final MerchantInfo m) {

        LOGGER.info("Fetching PB balance u:[{}] desc:[{}] mId:[{}] mNumb:[{}]", u.getId(), ofNullable(m.getShortDesc()).orElse(EMPTY), m.getId(), right(m.getCardNumber(), CARD_LAST_DIGITS));

        final Request requestToBank = pbRequestBuilder.buildAccountRequest(m);
        final Supplier<PbAccountBalance> pullPbAccountsSupplier = () -> pbAccountsHttpService.pullPbAccounts(requestToBank)
                .map(b -> pbAccountBalanceResponseMapper.mapResponse(m, b))
                .get();

        return supplyAsync(pullPbAccountsSupplier, cfThreadPool);
    }
}
