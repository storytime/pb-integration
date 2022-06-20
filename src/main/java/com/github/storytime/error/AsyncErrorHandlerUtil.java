package com.github.storytime.error;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;

import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.STUtils.getTimeAndReset;

public interface AsyncErrorHandlerUtil {

    static void logPbCf(final String userId,
                        final Logger logger,
                        final Throwable e) {
        try {
            if (e != null)
                logger.error("Async error, cannot continue with PB transactions for user: [{}], error: [{}]", userId, e.getCause());
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logSavingCf(String userId,
                            final StopWatch st,
                            final Logger logger,
                            final Throwable e) {
        try {
            if (e == null)
                logger.debug("Calling get savings for user: [{}], time: [{}] - finish endpoint ===", userId, getTimeAndReset(st));
            else
                logger.error("Cannot collect saving for user: [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logExport(String userId,
                          final StopWatch st,
                          final Logger logger,
                          final Throwable e) {
        try {
            if (e == null)
                logger.debug("Calling get export for user: [{}], time: [{}] - finish endpoint ===", userId, getTimeAndReset(st));
            else
                logger.error("Cannot not get export [{}], time: [{}], error: [{}] - error endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logCache(final StopWatch st,
                         final Logger logger,
                         final Throwable e) {
        try {
            if (e == null)
                logger.debug("Refresh zend cache time: [{}] - finish", getTimeAndReset(st));
            else
                logger.error("Refresh zend cache time:time: [{}], error: [{}]", getTimeAndReset(st), e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logVersionCf(final StopWatch st, final Logger logger, final Throwable e) {
        try {
            if (e == null)
                logger.debug("Version time: [{}] - finish endpoint ===", getTimeAndReset(st));
            else
                logger.error("Version time: [{}], error: [{}] - error endpoint ===", getTimeAndReset(st), e.getMessage(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logReconcilePbJson(final String userId, final StopWatch st, final Logger logger, final Throwable e) {
        try {
            if (e == null)
                logger.debug("Building pb/zen: [{}], time: [{}] - finish endpoint ===", userId, getTimeAndReset(st));
            else
                logger.error("Cannot build pb/zen json for user: [{}], time: [{}], error [{}] - error endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);

        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    //CF will be finished without any visible error if this will be crashed, not error, but state will be finished
    static void logSyncStatements(final String userId, final Logger logger, final Throwable e) {
        try {
            if (e != null)
                logger.error("CF statement user: [{}] init error [{}]", userId, e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logSyncPushByUserNotEmpty(final String userId, StopWatch st, final Logger logger, final Throwable e) {
        try {
            if (e == null)
                logger.info("Sync for user: [{}], time: [{}] - finished without issues", userId, getTime(st));
            else
                logger.error("Error! Sync for user: [{}], time: [{}], error [{}] - finished with error", userId, getTime(st), e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logSyncPushByUserEmpty(final String userId, StopWatch st, int size, final Logger logger, final Throwable e) {
        try {
            if (e == null)
                logger.debug("No new transaction for user: [{}], merch: [{}] time: [{}] - nothing to push - sync finished", userId, size, getTime(st));
            else
                logger.error("Error! Sync for user: [{}], time: [{}], error [{}] - finished with error", userId, getTime(st), e.getCause(), e);
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logSyncInitPerUser(final String userId, final StopWatch st, final Logger logger, final Throwable e) {
        try {
            if (e != null)
                logger.error("CF user: [{}] init error [{}], time: [{}]", userId, getTime(st), e.getCause());
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }

    static void logAllSync(final StopWatch st, final Logger logger, final Throwable e) {
        try {
            if (e != null)
                logger.error("CF all users error [{}], time: [{}], sync finished with error", e.getCause(), getTime(st));
        } catch (final Exception localEx) {
            logger.error("Error! [{}]", localEx.getCause(), e);
        }
    }
}
