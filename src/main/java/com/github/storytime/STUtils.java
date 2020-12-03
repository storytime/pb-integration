package com.github.storytime;

import org.springframework.util.StopWatch;

import static java.lang.String.valueOf;

public class STUtils {

    public static StopWatch createSt() {
        final var st = new StopWatch();
        st.start();
        return st;
    }

    public static String getTime(final StopWatch st) {
        st.stop();
        return st.getTotalTimeMillis() > 1000 ? valueOf(st.getTotalTimeSeconds()).concat(" sec") : valueOf(st.getTotalTimeMillis()).concat(" ms");
    }
}
