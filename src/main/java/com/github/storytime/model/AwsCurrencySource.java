package com.github.storytime.model;


/**
 * Values is saved in db by name, so names changed not allowed
 */
public final class AwsCurrencySource {

    public static final String USD = "NBU";
    public static final String PB_CASH = "PB_CASH";

    private AwsCurrencySource() {
        throw new IllegalStateException("Utility class");
    }
}
