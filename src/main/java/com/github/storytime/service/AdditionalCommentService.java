package com.github.storytime.service;

import com.github.storytime.function.CurrencyCommentFunction;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.utils.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.lang.Float.valueOf;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Component
public class AdditionalCommentService {

    private final CurrencyService currencyService;
    private final DateService dateService;
    private final CurrencyCommentFunction currencyCommentFunction2;

    @Autowired
    public AdditionalCommentService(final CurrencyService currencyService,
                                    final DateService dateService) {
        this.currencyService = currencyService;
        this.dateService = dateService;

        this.currencyCommentFunction2 = (rate, s, b, a) -> {
            final BigDecimal sum = currencyService.convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE)), rate.getBuyRate());
            return b + sum + a;
        };
    }

    public void addAdditionalComment(final Statement s, final MerchantInfo merchantInfo, final String timeZone) {
        final var additionalCommentList = merchantInfo.getAdditionalComment()
                .stream()
                .map(ac -> switch (ac) {
                    case PB_CURRENT_BUSINESS_DAY -> mapPbCurrentBusinessDayComment(s, timeZone);
                    case NBU_PREV_MOUTH_LAST_BUSINESS_DAY -> EMPTY;
                }).toList();
        s.setCustomComment(String.join(SPACE, additionalCommentList));
    }

    private String mapPbCurrentBusinessDayComment(final Statement s, final String timeZone) {
        final ZonedDateTime startDate = dateService.getPbStatementZonedDateTime(timeZone, s.getTrandate());
        return currencyService.pbUsdCashDayRates(startDate, USD)
                .map(rate -> currencyCommentFunction2.generate(rate, s, BANK_RATE, USD_COMMENT))
                .orElse(EMPTY);
    }

    public String exchangeInfoComment(final Double opAmount, final String opCurrency, final Double cardAmount) {
        return opAmount + SPACE + opCurrency + RATE + currencyService.convertDivide(cardAmount, opAmount) + SPACE;
    }
}
