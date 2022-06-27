package com.github.storytime.service.info;

import com.github.storytime.mapper.response.SavingsInfoMapper;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.api.SavingsInfoResponse;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.service.DigitsFormatter;
import com.github.storytime.service.async.UserAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.TOTAL;
import static com.github.storytime.config.props.Constants.UAH;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logSavingCf;
import static com.github.storytime.service.utils.STUtils.createSt;
import static com.github.storytime.service.utils.STUtils.getTimeAndReset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
public class SavingsService {


    private static final Logger LOGGER = getLogger(SavingsService.class);

    private final UserAsyncService userAsyncService;
    private final ZenAsyncService zenAsyncService;
    private final SavingsInfoMapper savingsInfoMapper;
    private final ZenResponseMapper zenResponseMapper;
    private final DigitsFormatter digitsFormatter;

    @Autowired
    public SavingsService(final UserAsyncService userAsyncService,
                          final SavingsInfoMapper savingsInfoMapper,
                          final ZenResponseMapper zenResponseMapper,
                          final DigitsFormatter digitsFormatter,
                          final ZenAsyncService zenAsyncService) {
        this.userAsyncService = userAsyncService;
        this.zenAsyncService = zenAsyncService;
        this.zenResponseMapper = zenResponseMapper;
        this.savingsInfoMapper = savingsInfoMapper;
        this.digitsFormatter = digitsFormatter;
    }

    public CompletableFuture<String> getAllSavingsAsTable(final String userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get savings info as table for user: [{}] - start", userId);
            return userAsyncService.getById(userId)
                    .thenApply(Optional::get)
                    .thenCompose(this::getUserSavings)
                    .thenApply(savings -> savingsInfoMapper.calculatePercents(savingsInfoMapper.getTotalInUah(savings), savings))
                    .thenApply(savingsInfo -> savingsInfoMapper.getNiceSavings(savingsInfo)
                            .append(TOTAL)
                            .append(digitsFormatter.formatAmount(savingsInfoMapper.getTotalInUah(savingsInfo)))
                            .append(SPACE)
                            .append(UAH)
                            .toString())
                    .whenComplete((r, e) -> logSavingCf(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect saving info as table for user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(EMPTY);
        }
    }

    public CompletableFuture<ResponseEntity<SavingsInfoResponse>> getAllSavingsJson(final String userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get savings info as JSON for user: [{}] - start", userId);
            return userAsyncService.getById(userId)
                    .thenApply(Optional::get)
                    .thenCompose(this::getUserSavings)
                    .thenApply(savings -> savingsInfoMapper.calculatePercents(savingsInfoMapper.getTotalInUah(savings), savings))
                    .thenApply(updatedSavings -> new SavingsInfoResponse()
                            .setSavings(updatedSavings)
                            .setTotal(digitsFormatter.formatAmount(savingsInfoMapper.getTotalInUah(updatedSavings))))
                    .thenApply((SavingsInfoResponse r) -> new ResponseEntity<>(r, OK))
                    .whenComplete((t, e) -> logSavingCf(userId, st, LOGGER, e));
        } catch (final Exception e) {
            LOGGER.error("Cannot get savings info as JSON for user: [{}], time: [{}], error: [{}] - error, endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(new ResponseEntity<>(NO_CONTENT));
        }
    }

    private CompletableFuture<List<SavingsInfo>> getUserSavings(final AppUser appUser) {
        return zenAsyncService
                .zenDiffByUserForSavings(appUser)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> savingsInfoMapper.getUserSavings(zenResponseMapper.getSavingsAccounts(zenDiff), zenDiff));
    }
}