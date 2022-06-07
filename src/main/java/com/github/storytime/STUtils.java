package com.github.storytime;


import org.apache.commons.lang3.time.StopWatch;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class STUtils {

    private static final DecimalFormat df = new DecimalFormat("#####.#####");

    public static StopWatch createSt() {
        final var st = new StopWatch();
        st.start();
        return st;
    }

    public static String getTimeAndReset(final StopWatch st) {
        st.stop();
        String time = st.getTime() > 1000 ? df.format(st.getTime(SECONDS)).concat(" sec") : valueOf(st.getTime()).concat(" ms");
        st.reset();
        st.start();
        return time;
    }

    public static String getTime(final StopWatch st) {
        return  st.getTime() > 1000 ? df.format(st.getTime(SECONDS)).concat(" sec") : valueOf(st.getTime()).concat(" ms");
    }
}
