package com.github.storytime.config.props;

public class Constants {

    public static final int PB_WAIT = 0;
    public static final String START_DATE = "sd";
    public static final String END_DATE = "ed";
    public static final String CARD = "card";
    public static final String EMPTY = "";
    public static final String CMT = "cmt";
    public static final int TEST = 0;
    public static final int XML_VERSION = 1;
    public static final int PASSWORD_LENGTH = 32;
    public static final String CARD_REG_EXP = "^\\d{16}$";
    public static final String DATE_REG_EXP = "\\d{2}.\\d{2}.\\d{4}";
    public static final String MD5 = "MD5";
    public static final String SHA_1 = "SHA-1";
    public static final String N_A = "N/A";
    public static final String API_PREFIX = "/app/api";
    public static final int CARD_LAST_DIGITS = 4;
    public static final int CARD_TWO_DIGITS = 2;
    public static final String ACCOUNT_TITLE_PREFIX = "Счет ";
    public static final String TITLE_CARD_SEPARATOR = "-";
    public static final Integer DEFAULT_CURRENCY_ZEN = 4;
    public static final String SPACE_SEPARATOR = " ";
    public static final String VERSION_PROPERTIES = "version.properties";
    public static final String END_LINE_SEPARATOR = "\n";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final int PB_ZEN_ID = 12574;
    public static final String ZEN_ACCOUNT_TYPE = "checking";
    public static final String RATE = " Rate: ";
    public static final String CASH = "cash";
    public static final int CURRENCY_SCALE = 2;
    public static final String UAH_STR = "UAH";
    public static final String USD_STR = "USD";
    public static final int COMMENT_SIZE = 50;
    public static final String NBU_LAST_DAY = "In: ";
    public static final String USD_COMMENT = "$ ";
    public static final String BANK_RATE = "BR: ";
    public static final int EMPTY_AMOUNT = 0;
    public static final int NOT_CHANGED = 0;

    private Constants() {
    }
}
