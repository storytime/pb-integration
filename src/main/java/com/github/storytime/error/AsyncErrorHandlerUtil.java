package com.github.storytime.error;

import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.function.BiFunction;

import static com.github.storytime.STUtils.getTime;
import static java.util.Collections.emptyList;

public interface AsyncErrorHandlerUtil {

    Logger LOGGER = LogManager.getLogger(AsyncErrorHandlerUtil.class);

    static BiFunction<List<Statement>, Throwable, List<Statement>> getPbServiceAsyncHandler() {
        return (s, t) -> {
            if (t != null) {
                LOGGER.error("Async error, cannot continue with PB transactions ", t);
                return emptyList();
            }
            return s;
        };
    }

    static BiFunction<Void, Throwable, Void> getZenDiffUpdateHandler() {
        return (s, t) -> {
            if (t != null) {
                LOGGER.error("Async error, cannot continue zen diff update ", t);
                return null;
            }
            return s;
        };
    }

    static void logSavingCf(long userId,
                            final StopWatch st,
                            final Logger logger,
                            final Throwable e) {
        if (e == null)
            logger.debug("Calling get savings for user: [{}], time: [{}] - finish endpoint ===", userId, getTime(st));
        else
            logger.error("Cannot collect saving for user: [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTime(st), e.getCause(), e);
    }

    static void logVersionCf(final StopWatch st, final Logger logger, final Throwable e) {
        if (e == null)
            logger.debug("Version time: [{}] - finish endpoint ===", getTime(st));
        else
            logger.error("Version time: [{}], error: [{}] - error endpoint ===", getTime(st), e.getMessage(), e);
    }

    static void logReconcileByBudgetCf(final long userId,
                                       final String budget,
                                       final StopWatch st,
                                       final Logger logger,
                                       final Throwable e) {
        if (e == null)
            logger.debug("Built reconciled YNAB user: [{}], budget: [{}], time [{}] - finish endpoint ===", userId, budget, getTime(st));
        else
            logger.error("Cannot  build reconciled YNAB user: [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTime(st), e.getCause(), e);
    }

    static void logReconcileTableDefaultAll(final long userId, final StopWatch st, final Logger logger, final Throwable e) {
        if (e == null)
            logger.debug("Built YNAB reconcile all user: [{}], time: [{}] - finish endpoint ===", userId, getTime(st));
        else
            logger.error("Cannot build YNAB reconcile all user: [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTime(st), e.getCause(), e);
    }

    static void logReconcilePbJson(final long userId, final StopWatch st, final Logger logger, final Throwable e) {
        if (e == null)
            logger.debug("Building pb/zen: [{}], time: [{}] - finish endpoint ===", userId, getTime(st));
        else
            logger.error("Cannot build pb/zen json for user: [{}], time: [{}], error [{}] - error endpoint ===", userId, getTime(st), e.getCause(), e);
    }

}
