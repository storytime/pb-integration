package com.github.storytime.service.utils;

import org.apache.commons.lang3.time.StopWatch;

import java.text.DecimalFormat;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;

public class STUtils {

    private static final DecimalFormat df = new DecimalFormat(LOGS_TIME_FORMAT);

    public static StopWatch createSt() {
        final var st = new StopWatch();
        st.start();
        return st;
    }

    public static String getTimeAndReset(final StopWatch st) {
        st.stop();
        var time = st.getTime() > MILLIS_IN_SEC ? df.format(st.getTime(SECONDS)).concat(SEC) : valueOf(st.getTime()).concat(MS);
        st.reset();
        st.start();
        return time;
    }

    public static String getTime(final StopWatch st) {
        return st.getTime() > MILLIS_IN_SEC ? df.format(st.getTime(SECONDS)).concat(SEC) : valueOf(st.getTime()).concat(MS);
    }
}
