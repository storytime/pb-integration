package com.github.storytime.service;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.github.storytime.config.Constants.SPACE_SEPARATOR;
import static java.lang.Float.valueOf;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Component
public class AdditionalCommentService {

    private final CurrencyService currencyService;

    @Autowired
    public AdditionalCommentService(final CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public void handle(final List<Statement> onlyNewPbTransactions,
                       final MerchantInfo merchantInfo,
                       final String timeZone) {

        onlyNewPbTransactions.forEach(s -> merchantInfo.getAdditionalComment().forEach(ad -> {
            StringBuilder comment = new StringBuilder(100);
            switch (ad) {
                case NBU_PREV_MOUTH_LAST_BUSINESS_DAY:
                    currencyService
                            .nbuPrevMouthLastBusinessDayRate(s, timeZone)
                            .ifPresent(rate -> {
                                final BigDecimal sum = currencyService
                                        .convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE_SEPARATOR)), rate.getBuyRate());
                                comment.append("In: ").append(sum).append("$ ");
                            });
                    break;

                case PB_CURRENT_BUSINESS_DAY:
                    currencyService.pbCashDayRates(s, timeZone)
                            .ifPresent(rate -> {
                                final BigDecimal sum = currencyService
                                        .convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE_SEPARATOR)), rate.getBuyRate());
                                comment.append("BC: ").append(sum).append("$ ");
                            });
                    break;
                default:
                    break;
            }

            s.setCustomComment(ofNullable(s.getCustomComment()).orElse("") + comment.toString());
        }));
    }
}
