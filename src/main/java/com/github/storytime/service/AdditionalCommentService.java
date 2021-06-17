package com.github.storytime.service;

import com.github.storytime.function.CurrencyCommentFunction;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.inner.AdditionalComment;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.utils.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.lang.Float.valueOf;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Component
public class AdditionalCommentService {

    private final CurrencyService currencyService;
    private final DateService dateService;
    private final CurrencyCommentFunction currencyCommentFunction;

    @Autowired
    public AdditionalCommentService(final CurrencyService currencyService,
                                    final DateService dateService) {
        this.currencyService = currencyService;
        this.dateService = dateService;
        this.currencyCommentFunction = (c, rate, s, b, a) -> {
            final BigDecimal sum = currencyService
                    .convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE)), rate.getBuyRate());
            c.append(b).append(sum).append(a);
        };
    }

    public void handle(final Statement s, final MerchantInfo merchantInfo, final String timeZone) {
        merchantInfo
                .getAdditionalComment()
                .forEach(ac -> {
                    final StringBuilder comment = new StringBuilder(COMMENT_SIZE);
                    if (ac == AdditionalComment.PB_CURRENT_BUSINESS_DAY) {
                        final ZonedDateTime startDate = dateService.getPbStatementZonedDateTime(timeZone, s.getTrandate());
                        currencyService
                                .pbUsdCashDayRates(startDate, USD)
                                .ifPresent(rate -> currencyCommentFunction.generate(comment, rate, s, BANK_RATE, USD_COMMENT));
                    }

                    s.setCustomComment(ofNullable(s.getCustomComment()).orElse(EMPTY) + comment);
                });
    }

    public String exchangeInfoComment(final Double opAmount, final String opCurrency, final Double cardAmount) {
        return opAmount + SPACE + opCurrency + RATE + currencyService.convertDivide(cardAmount, opAmount) + SPACE;
    }
}
