package com.github.storytime.service;

import com.github.storytime.function.CurrencyCommentFunction;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.Float.valueOf;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Component
public class AdditionalCommentService {

    private final CurrencyService currencyService;
    private final CurrencyCommentFunction currencyCommentFunction;

    @Autowired
    public AdditionalCommentService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
        this.currencyCommentFunction = (c, rate, s, b, a) -> {
            final BigDecimal sum = currencyService
                    .convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE_SEPARATOR)), rate.getBuyRate());
            c.append(b).append(sum).append(a);
        };
    }

    public void handle(final List<Statement> onlyNewPbTransactions, final MerchantInfo merchantInfo, final String timeZone) {

        onlyNewPbTransactions.forEach(s -> merchantInfo.getAdditionalComment().forEach(ac -> {
            final StringBuilder comment = new StringBuilder(COMMENT_SIZE);
            switch (ac) {
                case NBU_PREV_MOUTH_LAST_BUSINESS_DAY:
                    currencyService
                            .nbuPrevMouthLastBusinessDayRate(s, timeZone)
                            .ifPresent(rate -> currencyCommentFunction.generate(comment, rate, s, NBU_LAST_DAY, USD_COMMENT));
                    break;

                case PB_CURRENT_BUSINESS_DAY:
                    currencyService
                            .pbCashDayRates(s, timeZone)
                            .ifPresent(rate -> currencyCommentFunction.generate(comment, rate, s, BANK_RATE, USD_COMMENT));
                    break;
                default:
                    break;
            }

            s.setCustomComment(ofNullable(s.getCustomComment()).orElse(EMPTY) + comment.toString());
        }));
    }
}
