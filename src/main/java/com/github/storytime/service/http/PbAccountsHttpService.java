package com.github.storytime.service.http;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.mapper.PbStatementMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.model.pb.jaxb.request.Request;
import io.micrometer.core.instrument.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CARD_LAST_DIGITS;
import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbAccountsHttpService {

    private static final Logger LOGGER = LogManager.getLogger(PbAccountsHttpService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;
    private final PbRequestBuilder pbRequestBuilder;
    private final PbStatementMapper pbStatementMapper;
    private final Timer pbRequestTimeTimer;
    private final Executor cfThreadPool;

    @Autowired
    public PbAccountsHttpService(final RestTemplate restTemplate,
                                 final CustomConfig customConfig,
                                 final PbStatementMapper pbStatementMapper,
                                 final Timer pbRequestTimeTimer,
                                 final Executor cfThreadPool,
                                 final PbRequestBuilder pbRequestBuilder) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
        this.pbStatementMapper = pbStatementMapper;
        this.pbRequestTimeTimer = pbRequestTimeTimer;
        this.cfThreadPool = cfThreadPool;
        this.pbRequestBuilder = pbRequestBuilder;
    }

    public List<PbAccountBalance> getPbAsyncAccounts(final AppUser appUser, final List<MerchantInfo> merchantInfos) {
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

        LOGGER.info("Fetching account balance u:[{}] desc:[{}] mId:[{}] mNumb:[{}]",
                u.getId(),
                ofNullable(m.getShortDesc()).orElse(EMPTY),
                m.getId(),
                right(m.getCardNumber(), CARD_LAST_DIGITS)
        );

        final Request requestToBank = pbRequestBuilder.buildAccountRequest(m);
        final Supplier<PbAccountBalance> pullPbAccountsSupplier = () -> pullPbAccounts(requestToBank)
                .map(b -> handleResponse(m, b))
                .get();

        return supplyAsync(pullPbAccountsSupplier, cfThreadPool);
    }


    private PbAccountBalance handleResponse(final MerchantInfo m, final ResponseEntity<String> body) {
        final BigDecimal bigDecimal = pbStatementMapper.mapAccountRequestBody(body);
        return new PbAccountBalance(m.getShortDesc(), bigDecimal);
    }

    //todo maybe move to common class?
    public Optional<ResponseEntity<String>> pullPbAccounts(final Request requestToBank) {
        try {
            final String pbAccountsUrl = customConfig.getPbAccountsUrl();
            LOGGER.debug("Going to call:[{}]", pbAccountsUrl);
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ResponseEntity<String>> response = of(restTemplate.postForEntity(pbAccountsUrl, requestToBank, String.class));
            st.stop();
            LOGGER.debug("Receive bank account response, execution time:[{}] sec", st.getTotalTimeSeconds());
            pbRequestTimeTimer.record(st.getTotalTimeMillis(), TimeUnit.MILLISECONDS);
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot do bank request:[{}]", e.getMessage());
            return empty();
        }
    }
}
