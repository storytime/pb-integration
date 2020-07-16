package com.github.storytime.service;

import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ReconcileTableService {


    public static final String TABLE_PLUS = "+";
    public static final String TABLE_MINUS = "-";
    public static final String TABLE_VERTICAL_BAR = "|";
    public static final String TABLE_END_HEADER_LINE = "+\n|";
    public static final int TAG_NAME = 30;
    public static final int SUM_ZEN_BALANCE = 15;
    public static final int SUM_YNAB_BALANCE = 15;
    public static final int TAGS_DIFF = 15;
    public static final int TABLE_VERTICAL_BAR_SIZE = 1;
    public static final int TABLE_END_LINE_SIZE = 2;
    public static final int TABLE_END_HEADER_SIZE = 3;
    public static final String TABLE_END_LINE = "|\n";

    //Total table size
    public static final int SIZE = TAG_NAME + SUM_ZEN_BALANCE + SUM_YNAB_BALANCE + TAGS_DIFF + TABLE_END_LINE_SIZE + TABLE_VERTICAL_BAR_SIZE + 1;
    public static final String TAG = "CATEGORY";
    public static final String ZEN = "ZEN";
    public static final String YNAB = "YNAB";
    public static final String ZEN_YNAB = "ZEN-YNAB";

    public static final int BALANCE_AFTER_DIGITS = 3;
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String VERTICAL_BAR = "|";
    public static final String END_HEADER_LINE = "+\n|";
    public static final String END_LINE = "|\n";
    public static final int ACCOUNT_NAME = 30;
    public static final int ZEN_BALANCE = 15;
    public static final int YNAB_BALANCE = 15;
    public static final int PB_BALANCE = 15;
    public static final int ZEN_YNAB_DIFF = 15;
    public static final int ZEN_PB_DIFF = 15;
    public static final int ACCOUNT_STATUS = 12;
    public static final int VERTICAL_BAR_SIZE = 1;
    public static final int END_LINE_SIZE = 2;
    public static final int END_HEADER_SIZE = 3;

    //Total table size
    public static final int TABLE_SIZE = ACCOUNT_NAME + ZEN_BALANCE + YNAB_BALANCE + PB_BALANCE + ZEN_PB_DIFF + ZEN_YNAB_DIFF + ACCOUNT_STATUS + END_HEADER_SIZE + END_LINE_SIZE + VERTICAL_BAR_SIZE + 1;
    public static final String ACCOUNT = "ACCOUNT";
    public static final String PB = "PB";
    public static final String PB_ZEN = "PB-ZEN";
    public static final String STATUS = "STATUS";
    public static final String X = "X";


    public void buildTagHeader(final StringBuilder table) {
        buildTagSummaryRow(table, TAG, ZEN, YNAB, ZEN_YNAB);
    }

    public void buildTagLastLine(final StringBuilder table) {
        buildTagsCell(table, rightPad(TABLE_PLUS, SIZE, TABLE_MINUS), TABLE_PLUS, TABLE_VERTICAL_BAR_SIZE);
    }

    public void addEmptyLine(final StringBuilder table) {
        table.append("\n");
    }

    public void buildTagSummaryRow(final StringBuilder table,
                                   final String s1,
                                   final String s2,
                                   final String s3,
                                   final String s4) {
        buildTagsCell(table, rightPad(TABLE_PLUS, SIZE, TABLE_MINUS), TABLE_END_HEADER_LINE, TABLE_END_HEADER_SIZE);
        buildTagsCell(table, center(s1, TAG_NAME, SPACE), TABLE_VERTICAL_BAR, TABLE_VERTICAL_BAR_SIZE);
        buildTagsCell(table, center(s2, SUM_ZEN_BALANCE, SPACE), TABLE_VERTICAL_BAR, TABLE_VERTICAL_BAR_SIZE);
        buildTagsCell(table, center(s3, SUM_YNAB_BALANCE, SPACE), TABLE_VERTICAL_BAR, TABLE_VERTICAL_BAR_SIZE);
        buildTagsCell(table, center(s4, TAGS_DIFF, SPACE), TABLE_END_LINE, TABLE_VERTICAL_BAR_SIZE);
    }

    public void buildTagsCell(final StringBuilder table,
                              final String s,
                              final String endHeaderLine,
                              final int endHeaderSize) {
        table.append(s);
        table.append(rightPad(endHeaderLine, endHeaderSize));
    }

    public void buildAccountRow(final StringBuilder table,
                                final String s1,
                                final String s2,
                                final String s3,
                                final String s4,
                                final String s5,
                                final String s6,
                                final String s7) {
        buildAccountCell(table, rightPad(PLUS, TABLE_SIZE, MINUS), END_HEADER_LINE, END_HEADER_SIZE);
        buildAccountCell(table, center(s1, ACCOUNT_NAME, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s2, ZEN_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s3, YNAB_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s4, PB_BALANCE, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s5, ZEN_PB_DIFF, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s6, ZEN_YNAB_DIFF, SPACE), VERTICAL_BAR, VERTICAL_BAR_SIZE);
        buildAccountCell(table, center(s7, ACCOUNT_STATUS, SPACE), END_LINE, END_LINE_SIZE);
    }

    public void buildAccountCell(final StringBuilder table,
                                 final String s,
                                 final String endHeaderLine,
                                 final int endHeaderSize) {
        table.append(s);
        table.append(rightPad(endHeaderLine, endHeaderSize));
    }

    public void buildAccountHeader(final StringBuilder table) {
        buildAccountRow(table, ACCOUNT, PB, ZEN, YNAB, PB_ZEN, ZEN_YNAB, STATUS);
    }

    public void buildAccountLastLine(final StringBuilder table) {
        buildAccountCell(table, rightPad(PLUS, TABLE_SIZE, MINUS), PLUS, VERTICAL_BAR_SIZE);
    }

}

