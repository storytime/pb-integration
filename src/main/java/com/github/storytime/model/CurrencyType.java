package com.github.storytime.model;


public final class CurrencyType {

    public static final String USD = "USD";
    public static final String EUR = "EUR";

    private CurrencyType() {
        throw new IllegalStateException("Utility class");
    }
}
