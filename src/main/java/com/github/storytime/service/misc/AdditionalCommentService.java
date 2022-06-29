package com.github.storytime.service.misc;

import com.github.storytime.function.CurrencyCommentFunction;
import com.github.storytime.model.AdditionalComment;
import com.github.storytime.model.CurrencyType;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.misc.CurrencyService;
import com.github.storytime.service.misc.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.Float.valueOf;
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

        this.currencyCommentFunction = (rate, s, b, a) -> {
            final BigDecimal sum = currencyService.convertDivide(valueOf(substringBefore(s.getCardamount(), SPACE)), rate.getBuyRate());
            return b + sum + a;
        };
    }

    public List<Statement> addAdditionalAwsComments(final List<Statement> statementList,
                                                    final PbMerchant merchantInfo,
                                                    final String timeZone) {
        statementList.forEach(statement -> mapAwsCommentForAStatement(merchantInfo, timeZone, statement));
        return statementList;
    }

    private void mapAwsCommentForAStatement(PbMerchant merchantInfo, String timeZone, Statement s) {
        final var additionalCommentList = merchantInfo.getAwsAdditionalComment()
                .stream()
                .map(ac -> switch (ac) {
                    case AdditionalComment.PB_CURRENT_BUSINESS_DAY -> mapPbCurrentBusinessDayComment(s, timeZone);
                    case AdditionalComment.NBU_PREV_MOUTH_LAST_BUSINESS_DAY -> EMPTY;
                    default -> EMPTY;
                }).toList();
        s.setCustomComment(String.join(SPACE, additionalCommentList) + SPACE);
    }

    private String mapPbCurrentBusinessDayComment(final Statement s, final String timeZone) {
        final ZonedDateTime startDate = dateService.getPbStatementZonedDateTime(timeZone, s.getTrandate());
        return currencyService.pbUsdCashDayRates(startDate, CurrencyType.USD)
                .map(rate -> currencyCommentFunction.generate(rate, s, BANK_RATE, USD_COMMENT))
                .orElse(EMPTY);
    }

    public String exchangeInfoComment(final Double opAmount, final String opCurrency, final Double cardAmount) {
        return opAmount + SPACE + opCurrency + RATE + currencyService.convertDivide(cardAmount, opAmount) + SPACE;
    }
}
