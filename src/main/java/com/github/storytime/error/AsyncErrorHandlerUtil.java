package com.github.storytime.error;

import org.apache.logging.log4j.Logger;
import org.springframework.util.StopWatch;

import static com.github.storytime.STUtils.getTime;

public interface AsyncErrorHandlerUtil {

    static void logPbCf(long userId,
                        final Logger logger,
                        final Throwable e) {
        if (e != null)
            logger.error("Async error, cannot continue with PB transactions for user: [{}], error: [{}]", userId, e.getCause(), e);
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

    static void logExport(long userId,
                          final StopWatch st,
                          final Logger logger,
                          final Throwable e) {
        if (e == null)
            logger.debug("Calling get export for user: [{}], time: [{}] - finish endpoint ===", userId, getTime(st));
        else
            logger.error("Cannot not get export [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTime(st), e.getCause(), e);
    }

    static void logCache(final StopWatch st,
                         final Logger logger,
                         final Throwable e) {
        if (e == null)
            logger.debug("Refresh zend cache time: [{}] - finish", getTime(st));
        else
            logger.error("Refresh zend cache time:time: [{}], error: [{}]", getTime(st), e.getCause(), e);
    }

    static void logVersionCf(final StopWatch st, final Logger logger, final Throwable e) {
        if (e == null)
            logger.debug("Version time: [{}] - finish endpoint ===", getTime(st));
        else
            logger.error("Version time: [{}], error: [{}] - error endpoint ===", getTime(st), e.getMessage(), e);
    }

    @Deprecated
    static void logReconcileByBudgetCf(final long userId, final String budget, final StopWatch st, final Logger logger, final Throwable e) {
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

    static void logSync(final long userId, final StopWatch st, final Logger logger, final Throwable e) {
        if (e == null)
            logger.debug("Sync for user: [{}], time: [{}] - finished without issues", userId, getTime(st));
        else
            logger.error("Error! Sync for user: [{}], time: [{}], error [{}] - finished with error", userId, getTime(st), e.getCause(), e);
    }

}
