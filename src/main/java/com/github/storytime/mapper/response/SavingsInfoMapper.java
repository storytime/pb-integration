package com.github.storytime.mapper.response;

import com.github.storytime.config.props.Constants;
import com.github.storytime.model.CurrencyType;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.CurrencyService;
import com.github.storytime.service.DigitsFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.github.storytime.config.props.Constants.*;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalTime.MIN;
import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Component
public class SavingsInfoMapper {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(Constants.ONE_HUNDRED);
    private final CurrencyService currencyService;
    private final ZenResponseMapper zenResponseMapper;
    private final DigitsFormatter digitsFormatter;

    @Autowired
    public SavingsInfoMapper(final CurrencyService currencyService,
                             final DigitsFormatter digitsFormatter,
                             final ZenResponseMapper zenResponseMapper) {
        this.currencyService = currencyService;
        this.digitsFormatter = digitsFormatter;
        this.zenResponseMapper = zenResponseMapper;
    }

    public BigDecimal getTotalInUah(final List<SavingsInfo> savingsInfoList) {
        return savingsInfoList.stream()
                .map(SavingsInfo::getInUah)
                .reduce(ZERO, BigDecimal::add)
                .setScale(ZERO_SCALE, HALF_DOWN);
    }

    public StringBuilder getNiceSavings(final List<SavingsInfo> savingsInfoList) {
        final String mapped = savingsInfoList
                .stream()
                .sorted(comparing(SavingsInfo::getPercent))
                .map(digitsFormatter::mapToNiceSavingsString)
                .collect(joining());
        return new StringBuilder().append(mapped);
    }

    public List<SavingsInfo> calculatePercents(final BigDecimal totalAmountInUah,
                                               final List<SavingsInfo> savingsInfoList) {
        return savingsInfoList.stream()
                .map(sa -> sa.setPercent(sa.getInUah()
                        .multiply(ONE_HUNDRED)
                        .divide(totalAmountInUah, PERCENTS_SCALE, HALF_UP)
                        .setScale(PERCENTS_SCALE, HALF_DOWN))).toList();
    }

    public List<SavingsInfo> getUserSavings(final List<AccountItem> savingsAccounts, final ZenResponse zenDiff) {
        return savingsAccounts
                .stream()
                .map(a -> buildSavingsInfo(a, zenResponseMapper.getZenCurrencySymbol(zenDiff, a.getInstrument()))).toList();
    }

    private SavingsInfo buildSavingsInfo(final AccountItem accountItem,
                                         final String zenCurrencySymbol) {
        final var instrument = accountItem.getInstrument();
        final var balance = accountItem.getBalance();
        final var startDate = now(systemDefault()).with(MIN);
        final var bal = valueOf(balance);
        final var inUah =
                instrument == USD_ID ? currencyService.pbUsdCashDayRates(startDate, CurrencyType.USD).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) :
                        instrument == EUR_ID ? currencyService.pbUsdCashDayRates(startDate, CurrencyType.EUR).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) :
                                bal;

        return SavingsInfo
                .builder()
                .balance(bal.setScale(ZERO_SCALE, HALF_DOWN))
                .currencySymbol(zenCurrencySymbol)
                .title(accountItem.getTitle().trim())
                .inUah(inUah.setScale(ZERO_SCALE, HALF_DOWN))
                .inUahStr(digitsFormatter.formatAmount(inUah.setScale(ZERO_SCALE, HALF_DOWN)))
                .balanceStr(digitsFormatter.formatAmount(bal.setScale(ZERO_SCALE, HALF_DOWN)))
                .build();
    }
}
