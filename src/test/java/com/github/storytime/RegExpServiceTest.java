package com.github.storytime;


import com.github.storytime.service.RegExpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Generate nice payee")
public class RegExpServiceTest extends GenericContextMock {

    @Autowired
    private RegExpService regExpService;

    private static Stream<Arguments> getData() {
        return Stream.of(
                Arguments.of(null, EMPTY),
                Arguments.of(EMPTY, EMPTY),
                Arguments.of("AZS OKKO", "AZS OKKO"),
                Arguments.of(" AZS OKKO ", "AZS OKKO"),
                Arguments.of("AZS &quot;OKKO&quot;", "AZS \"OKKO\""),
                Arguments.of("AZS &apos;OKKO&apos;", "AZS  OKKO"),
                Arguments.of("AZS &gt;OKKO&gt;", "AZS  OKKO"),
                Arguments.of("AZS &lt;OKKO&lt;", "AZS  OKKO"),
                Arguments.of("AZS <[^>]*OKKO<[^>]*", "AZS  OKKO")
        );
    }


    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @MethodSource("getData")
    public void testNormalizeDescription(final String in, final String expected) {
        final String actual = regExpService.normalizeDescription(in);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }

}