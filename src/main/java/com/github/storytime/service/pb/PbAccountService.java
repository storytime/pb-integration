package com.github.storytime.service.pb;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.mapper.response.PbAccountBalanceResponseMapper;
import com.github.storytime.mapper.response.PbResponseMapper;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.service.async.PbAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.DEFAULT_ACC_BALANCE;

@Service
public class PbAccountService {

    private final PbRequestBuilder pbRequestBuilder;
    private final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper;
    private final PbAsyncService pbAsyncService;
    private final PbResponseMapper pbResponseMapper;

    @Autowired
    public PbAccountService(final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper,
                            final PbAsyncService pbAsyncService,
                            final PbResponseMapper pbResponseMapper,
                            final PbRequestBuilder pbRequestBuilder) {
        this.pbAccountBalanceResponseMapper = pbAccountBalanceResponseMapper;
        this.pbAsyncService = pbAsyncService;
        this.pbRequestBuilder = pbRequestBuilder;
        this.pbResponseMapper = pbResponseMapper;
    }

    public CompletableFuture<List<PbAccountBalance>> getPbAsyncAccounts(final List<PbMerchant> merchantInfos) {
        final List<CompletableFuture<PbAccountBalance>> pbAccountCf = merchantInfos
                .stream()
                .map(this::getPbAsyncAccounts).toList();

        return CompletableFuture
                .allOf(pbAccountCf.toArray(new CompletableFuture[pbAccountCf.size()]))
                .thenApply(aVoid -> pbAccountCf.stream().map(CompletableFuture::join).toList());
        //Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere
    }

    public CompletableFuture<PbAccountBalance> getPbAsyncAccounts(final PbMerchant pbMerchant) {
        return pbAsyncService.pullPbAccounts(pbRequestBuilder.buildAccountRequest(pbMerchant))
                .thenApply(r -> r.map(pbResponseMapper::mapAccountRequestBody).orElse(DEFAULT_ACC_BALANCE))
                .thenApply(r -> pbAccountBalanceResponseMapper.buildSimpleObject(r, pbMerchant));
    }
}
