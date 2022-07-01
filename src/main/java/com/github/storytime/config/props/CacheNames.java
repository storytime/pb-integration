package com.github.storytime.config.props;

public class CacheNames {

    public static final String USERS_CACHE = "users";
    public static final String USERS_PERMANENT_CACHE = "users-permanent";
    public static final String USER_PERMANENT_CACHE = "user-permanent";
    public static final String ZM_SAVING_CACHE = "zm-savings";
    public static final String CURRENCY_CACHE = "currency";
    public static final String TR_TAGS_DIFF = "tr-tags-diff";
    public static final String VERSION_CACHE = "version";
    public static final String CUSTOM_PAYEE_CACHE = "custom-payee";
    public static final String OUT_DATA_BY_MONTH = "get-out-data-by-month";
    public static final String IN_DATA_BY_MONTH = "get-in-data-by-month";
    public static final String OUT_DATA_BY_YEAR = "get-out-data-by-year";
    public static final String IN_DATA_BY_YEAR = "get-in-data-by-year";
    public static final String OUT_DATA_BY_QUARTER = "get-out-data-by-quarter";
    public static final String IN_DATA_BY_QUARTER = "get-in-data-by-quarter";

    private CacheNames() {
        throw new IllegalStateException("Utility class");
    }
}
