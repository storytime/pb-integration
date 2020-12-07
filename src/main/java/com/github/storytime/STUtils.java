package com.github.storytime;

import org.springframework.util.StopWatch;

import java.text.DecimalFormat;

import static java.lang.String.valueOf;

public class STUtils {

    private static final DecimalFormat df = new DecimalFormat("###.###");

    public static StopWatch createSt() {
        final var st = new StopWatch();
        st.start();
        return st;
    }

    public static String getTime(final StopWatch st) {
        st.stop();
        return st.getTotalTimeMillis() > 1000 ? df.format(st.getTotalTimeSeconds()).concat(" sec") : valueOf(st.getTotalTimeMillis()).concat(" ms");
    }
}
