package com.github.storytime.builder;

import com.github.storytime.BaseTestConfig;
import com.github.storytime.config.props.TextProperties;
import com.github.storytime.model.jaxb.statement.request.Request;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.storytime.config.props.Constants.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class StatementRequestBuilderTest extends BaseTestConfig {

    private static final String EXPECTED_NOT_NULL_MERCH = "Merchant be not null or empty";
    private static final String EXPECTED_NOT_NULL_CARD = "Card be not null or empty";
    private static final String EXPECTED_NOT_NULL_PASSWORD = "Password be not null or empty";
    private static final String EXPECTED_NOT_NULL_DATE = "Date must be not null or empty";
    private static final String EXPECTED_NOT_NULL_REQUEST = "Request must be not null";

    private static final String EXPECTED_MERCH_FORMAT = "Merchant positive number";
    private static final String EXPECTED_CARD_FORMAT = "Card must has valid length";
    private static final String EXPECTED_PASSWORD_FORMAT = "Password must has valid length";
    private static final String EXPECTED_DATE_FORMAT = "Date must has dd.mm.yyyy format";

    private static final int VALID_MERCHANT_ID = 12345;
    private static final String VALID_PASSWORD = "12345678123456781234567812345678";
    private static final String VALID_DATE = "01.01.2018";
    private static final String VALID_CARD = "1234567812345678";

    private static final String SHORT_PASSWORD = "12345";
    private static final String NOT_VALID_DATE = "12.22-199";
    private static final String LONG_PASSWORD = "12345678123456781234567812345678123456781234567812345678";
    private static final String SHORT_CARD = "12345";
    private static final String LONG_CARD = "1234512345123451234512345123451234512345";

    @Autowired
    private StatementRequestBuilder statementRequestBuilder;

    @Autowired
    private TextProperties messages;

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMerchant() {
        try {
            statementRequestBuilder.buildStatementRequest(null, EMPTY, EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getMerchIdNull()).as(EXPECTED_NOT_NULL_MERCH).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMerchant() {
        try {
            statementRequestBuilder.buildStatementRequest(-10, EMPTY, EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getMerchIdFormat()).as(EXPECTED_MERCH_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPassword() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, "", EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getPasswordNull()).as(EXPECTED_NOT_NULL_PASSWORD).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShortPassword() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, SHORT_PASSWORD, EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getPasswordLength()).as(EXPECTED_PASSWORD_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLongPassword() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, LONG_PASSWORD, EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getPasswordLength()).as(EXPECTED_PASSWORD_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyStartDate() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, EMPTY, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getStartDateNull()).as(EXPECTED_NOT_NULL_DATE).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotValidStartDate() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, NOT_VALID_DATE, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getStartDateFormat()).as(EXPECTED_DATE_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyEndDate() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, EMPTY, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getEndDateNull()).as(EXPECTED_NOT_NULL_DATE).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotValidEndDate() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, NOT_VALID_DATE, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getStartDateFormat()).as(EXPECTED_DATE_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyCard() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, VALID_DATE, EMPTY);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getCardNull()).as(EXPECTED_NOT_NULL_CARD).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShortCard() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, VALID_DATE, SHORT_CARD);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getCardFormat()).as(EXPECTED_CARD_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogCard() {
        try {
            statementRequestBuilder.buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, VALID_DATE, LONG_CARD);
        } catch (IllegalArgumentException re) {
            assertThat(messages.getCardFormat()).as(EXPECTED_CARD_FORMAT).isEqualTo(re.getMessage());
            throw re;
        }
    }

    @Test
    public void testWitValidData() {
        final Request request = statementRequestBuilder
                .buildStatementRequest(VALID_MERCHANT_ID, VALID_PASSWORD, VALID_DATE, VALID_DATE, VALID_CARD);
        assertThat(request).as(EXPECTED_NOT_NULL_REQUEST).isNotNull();
    }
}
