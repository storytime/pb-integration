package com.github.storytime.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SignatureGeneratorServiceTest {

    private static final String EXPECTED_VALID_SIGN = "Signature must be not null";
    private static final String START_DATE = "01.01.2018";
    private static final String END_DATE = "01.02.2018";
    private static final String CARD = "1234123412341234";
    private static final String PASS = "12345Qwerty12345ABCDE12345ABCDE12";
    private static final String VALID_SIGNATURE = "fe611b9f3b88425556a32b905699d2be24271c1c";

    @Autowired
    private SignatureGeneratorService signatureGeneratorService;

    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() throws NoSuchAlgorithmException {
        final String signature = signatureGeneratorService.generateSignature(START_DATE, END_DATE, CARD, PASS);
        assertThat(signature).as(EXPECTED_VALID_SIGN).isNotNull();
        assertThat(signature).as(EXPECTED_VALID_SIGN).isEqualTo(VALID_SIGNATURE);
    }

}
