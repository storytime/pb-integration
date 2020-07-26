package com.github.storytime.service;

import com.github.storytime.BaseTestConfig;
import com.github.storytime.service.utils.RegExpService;
import junitparams.Parameters;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;

public class RegExpServiceTest extends BaseTestConfig {

    @Autowired
    private RegExpService regExpService;

    @Test
    @Parameters(method = "dataForParseComment")
    public void testNormalizeDescription(final String in, final String expected) {
        final String actual = regExpService.normalizeDescription(in);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
        assertThat(actual).doesNotEndWith(SPACE);
        assertThat(actual).doesNotStartWith(SPACE);
    }

    @Test
    @Parameters(method = "dataForIsCashWithdrawal")
    public void testIsCashWithdrawal(final String in, final boolean expected) {
        assertThat(regExpService.isCashWithdrawal(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForIsInternalTransfer")
    public void testIsInternalTransfer(final String in, final boolean expected) {
        assertThat(regExpService.isInternalTransfer(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForIsInternalFrom")
    public void testIsInternalFrom(final String in, final boolean expected) {
        assertThat(regExpService.isInternalFrom(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForIsInternalTo")
    public void testIsInternalTo(final String in, final boolean expected) {
        assertThat(regExpService.isInternalTo(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForGetCardFirstDigits")
    public void testGetCardFirstDigits(final String in, final String expected) {
        assertThat(regExpService.getCardFirstDigits(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForGetCardLastDigits")
    public void testGetCardLastDigits(final String in, final String expected) {
        assertThat(regExpService.getCardLastDigits(in)).isEqualTo(expected);
    }

    @Test
    @Parameters(method = "dataForGetCardDigits")
    public void testGetCardDigits(final String in, final String expected) {
        assertThat(regExpService.getCardDigits(in)).isEqualTo(expected);
    }

    private Object[] dataForGetCardDigits() {
        return new Object[]{
                new Object[]{EMPTY, EMPTY},
                new Object[]{getRandomTextString() + "22**", EMPTY},
                new Object[]{getRandomTextString() + "22**11" + getRandomTextString(), "22**11"},
                new Object[]{"22**11BBB" + getRandomTextString(), "22**11"},
                new Object[]{getRandomTextString() + "2211BBB33**44", "33**44"},
                new Object[]{getRandomTextString() + "22*11BBB33**44" + getRandomTextString(), "33**44"},
        };
    }

    private Object[] dataForGetCardLastDigits() {
        return new Object[]{
                new Object[]{EMPTY, EMPTY},
                new Object[]{getRandomTextString() + "22**", EMPTY},
                new Object[]{getRandomTextString() + "22**11" + getRandomTextString(), "11"},
                new Object[]{"22**11BBB" + getRandomTextString(), "11"},
                new Object[]{getRandomTextString() + "2211BBB33**44", "44"},
                new Object[]{getRandomTextString() + "22*11BBB33**44" + getRandomTextString(), "44"},
        };
    }

    private Object[] dataForGetCardFirstDigits() {
        return new Object[]{
                new Object[]{EMPTY, EMPTY},
                new Object[]{getRandomTextString() + "22**", EMPTY},
                new Object[]{getRandomTextString() + "22**11" + getRandomTextString(), "22"},
                new Object[]{"22**11BBB" + getRandomTextString(), "22"},
                new Object[]{getRandomTextString() + "2211BBB33**44", "33"},
                new Object[]{getRandomTextString() + "22*11BBB33**44" + getRandomTextString(), "33"},
        };
    }

    private Object[] dataForIsInternalTo() {
        return new Object[]{
                new Object[]{null, false},
                new Object[]{EMPTY, false},
                new Object[]{getRandomTextString(), false},
                new Object[]{getRandomTextString() + "Перевод на свою карту" + getRandomTextString(), true},
                new Object[]{getRandomTextString() + SPACE + "Перевод на свою карту" + SPACE + getRandomTextString(), true},
                new Object[]{"Перевод на свою карту", true},
                new Object[]{"Перевод на свою картуПеревод со своей карты", true},
                new Object[]{"Перевод на свою карту" + SPACE + "Перевод со своей карты", true},
        };
    }

    private Object[] dataForIsInternalFrom() {
        return new Object[]{
                new Object[]{null, false},
                new Object[]{EMPTY, false},
                new Object[]{getRandomTextString(), false},
                new Object[]{getRandomTextString() + "Перевод со своей карты" + getRandomTextString(), true},
                new Object[]{getRandomTextString() + SPACE + "Перевод со своей карты" + SPACE + getRandomTextString(), true},
                new Object[]{"Перевод со своей карты", true},
                new Object[]{"Перевод со своей картыПеревод на свою картуП", true},
                new Object[]{"Перевод со своей карты" + SPACE + "Перевод на свою карту", true},
        };
    }

    private Object[] dataForIsInternalTransfer() {
        return new Object[]{
                new Object[]{null, false},
                new Object[]{EMPTY, false},
                new Object[]{getRandomTextString(), false},
                new Object[]{getRandomTextString() + "Перевод на свою карту" + getRandomTextString(), true},
                new Object[]{getRandomTextString() + SPACE + "Перевод на свою карту" + SPACE + getRandomTextString(), true},
                new Object[]{"Перевод на свою карту", true},
                new Object[]{"Перевод на свою картуПеревод со своей карты", true},
                new Object[]{"Перевод на свою карту" + SPACE + "Перевод со своей карты", true},
                new Object[]{getRandomTextString() + "Перевод со своей карты" + getRandomTextString(), true},
                new Object[]{getRandomTextString() + SPACE + "Перевод со своей карты" + SPACE + getRandomTextString(), true},
                new Object[]{"Перевод со своей карты", true},
                new Object[]{"Перевод со своей картыПеревод на свою картуП", true},
                new Object[]{"Перевод со своей карты" + SPACE + "Перевод на свою карту", true},
        };
    }

    private Object[] dataForIsCashWithdrawal() {
        return new Object[]{
                new Object[]{null, false},
                new Object[]{EMPTY, false},
                new Object[]{getRandomTextString(), false},
                new Object[]{"Снятие наличных", true},
                new Object[]{getRandomTextString() + "Снятие наличных" + getRandomTextString(), true}
        };
    }

    private Object[] dataForParseComment() {
        return new Object[]{
                new Object[]{null, EMPTY},
                new Object[]{EMPTY, EMPTY},
                new Object[]{"AZS OKKO", "AZS OKKO"},
                new Object[]{" AZS OKKO ", "AZS OKKO"},
                new Object[]{"AZS &quot;OKKO&quot;", "AZS \"OKKO\""},
                new Object[]{"AZS &apos;OKKO&apos;", "AZS  OKKO"},
                new Object[]{"AZS &gt;OKKO&gt;", "AZS  OKKO"},
                new Object[]{"AZS &lt;OKKO&lt;", "AZS  OKKO"},
                new Object[]{"AZS <[^>]*OKKO<[^>]*", "AZS  OKKO"},
        };
    }
}
