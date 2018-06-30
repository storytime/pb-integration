package com.github.storytime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lanwen.verbalregex.VerbalExpression;

import static ru.lanwen.verbalregex.VerbalExpression.regex;

@Configuration
public class VerbalExpressionConfig {

    private final CustomConfig customConfig;

    @Autowired
    public VerbalExpressionConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @Bean
    public VerbalExpression commentRegexp() {
        return regex()
                .find(customConfig.getPbCommentSeparator())
                .capture()
                .anything()
                .endCapture().build();
    }

    @Bean
    public VerbalExpression cashWithdrawal() {
        return regex()
                .startOfLine().withAnyCase()
                .then(customConfig.getPbCashWithdrawal())
                .anything()
                .endOfLine()
                .build();
    }

    @Bean
    public VerbalExpression internalTransfer() {
        return VerbalExpression.regex()
                .startOfLine().anything()
                .oneOf(customConfig.getPbInternalTransferTo(), customConfig.getPbInternalTransferFrom())
                .space()
                .anything()
                .endOfLine()
                .build();
    }

    @Bean
    public VerbalExpression internalTo() {
        return VerbalExpression.regex()
                .startOfLine().anything()
                .then(customConfig.getPbInternalTransferTo())
                .space()
                .anything()
                .endOfLine()
                .build();
    }

    @Bean
    public VerbalExpression internalFrom() {
        return VerbalExpression.regex()
                .startOfLine().anything()
                .then(customConfig.getPbInternalTransferFrom())
                .space()
                .anything()
                .endOfLine()
                .build();
    }

    @Bean
    public VerbalExpression internalTransferCard() {
        return VerbalExpression
                .regex()
                .capture().digit().count(2).endCapture()
                .then(customConfig.getPbInternalTransferSeparator())
                .capture().digit().count(2).endCapture()
                .build();
    }

    @Bean
    public VerbalExpression internalTransferComment() {
        return VerbalExpression
                .regex()
                .capture().digit().count(2)
                .then(customConfig.getPbInternalTransferSeparator())
                .capture().digit().count(2)
                .build();
    }
}
