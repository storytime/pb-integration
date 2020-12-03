package com.github.storytime.service.info;

import com.github.storytime.mapper.SavingsInfoMapper;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.api.SavingsInfoResponse;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.service.SavingsInfoFormatter;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.config.props.Constants.TOTAL;
import static com.github.storytime.config.props.Constants.UAH;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
public class SavingsService {


    private static final Logger LOGGER = getLogger(SavingsService.class);

    private final UserService userService;
    private final ZenAsyncService zenAsyncService;
    private final SavingsInfoMapper savingsInfoMapper;
    private final ZenResponseMapper zenResponseMapper;
    private final SavingsInfoFormatter savingsInfoFormatter;

    @Autowired
    public SavingsService(final UserService userService,
                          final SavingsInfoMapper savingsInfoMapper,
                          final ZenResponseMapper zenResponseMapper,
                          final SavingsInfoFormatter savingsInfoFormatter,
                          final ZenAsyncService zenAsyncService) {
        this.userService = userService;
        this.zenAsyncService = zenAsyncService;
        this.zenResponseMapper = zenResponseMapper;
        this.savingsInfoMapper = savingsInfoMapper;
        this.savingsInfoFormatter = savingsInfoFormatter;
    }

    public CompletableFuture<String> getAllSavingsAsTable(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get savings info as table for user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(this::getUserSavings)
                    .thenApply(savings -> savingsInfoMapper.calculatePercents(savingsInfoMapper.getTotalInUah(savings), savings))
                    .thenApply(savingsInfo -> savingsInfoMapper.getNiceSavings(savingsInfo)
                            .append(TOTAL)
                            .append(savingsInfoFormatter.formatAmount(savingsInfoMapper.getTotalInUah(savingsInfo)))
                            .append(SPACE)
                            .append(UAH)
                            .toString())
                    .whenComplete((r, e) -> logExecution(userId, getTime(st), e));
        } catch (Exception e) {
            LOGGER.error("Cannot collect saving info as table for user: [{}], time [{}], request: [{}]", userId, getTime(st), e.getCause());
            return completedFuture(EMPTY);
        }
    }

    public CompletableFuture<ResponseEntity<SavingsInfoResponse>> getAllSavingsJson(final long userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get savings info as JSON for user: [{}] - start", userId);
            return userService.findUserByIdAsync(userId)
                    .thenApply(Optional::get)
                    .thenCompose(this::getUserSavings)
                    .thenApply(savings -> savingsInfoMapper.calculatePercents(savingsInfoMapper.getTotalInUah(savings), savings))
                    .thenApply(updatedSavings -> new SavingsInfoResponse()
                            .setSavings(updatedSavings)
                            .setTotal(savingsInfoFormatter.formatAmount(savingsInfoMapper.getTotalInUah(updatedSavings))))
                    .thenApply(resp -> new ResponseEntity<>(resp, OK))
                    .whenComplete((resp, ex) -> logExecution(userId, getTime(st), ex));
        } catch (Throwable e) {
            LOGGER.error("Cannot get savings info as JSON for user: [{}], time: [{}], error: [{}] - error", userId, getTime(st), e.getCause(), e);
            return completedFuture(new ResponseEntity<>(NO_CONTENT));
        }
    }

    private CompletableFuture<List<SavingsInfo>> getUserSavings(final AppUser appUser) {
        return zenAsyncService
                .zenDiffByUserForSavings(appUser)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> savingsInfoMapper.getUserSavings(zenResponseMapper.getSavingsAccounts(zenDiff), zenDiff))
                .exceptionally(ex -> emptyList());
    }

    private void logExecution(long userId, final String time, final Throwable e) {
        if (e == null)
            LOGGER.debug("Calling get savings info for user: [{}], time: [{}] - finish", userId, time);
        else
            LOGGER.error("Cannot collect saving info for user: [{}], time: [{}], error: [{}] - error", userId, time, e.getCause(), e);
    }
}