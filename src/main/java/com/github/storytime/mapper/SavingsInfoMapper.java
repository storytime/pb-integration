package com.github.storytime.mapper;

import com.github.storytime.config.props.Constants;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.CurrencyService;
import com.github.storytime.service.SavingsInfoFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.storytime.config.props.Constants.PERCENTS_SCALE;
import static com.github.storytime.model.db.inner.CurrencyType.EUR;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toUnmodifiableList;

@Component
public class SavingsInfoMapper {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(Constants.ONE_HUNDRED);
    private final CurrencyService currencyService;
    private final ZenResponseMapper zenResponseMapper;
    private final SavingsInfoFormatter savingsInfoFormatter;

    @Autowired
    public SavingsInfoMapper(final CurrencyService currencyService,
                             final SavingsInfoFormatter savingsInfoFormatter,
                             final ZenResponseMapper zenResponseMapper) {
        this.currencyService = currencyService;
        this.savingsInfoFormatter = savingsInfoFormatter;
        this.zenResponseMapper = zenResponseMapper;
    }

    public BigDecimal getTotalInUah(final List<SavingsInfo> savingsInfoList) {
        return savingsInfoList.stream()
                .map(SavingsInfo::getInUah)
                .reduce(ZERO, BigDecimal::add)
                .setScale(Constants.ZERO_SCALE, HALF_DOWN);
    }

    public StringBuilder getNiceSavings(final List<SavingsInfo> savingsInfoList) {
        final String mapped = savingsInfoList
                .stream()
                .sorted(Comparator.comparing(SavingsInfo::getPercent))
                .map(savingsInfoFormatter::mapToNiceSavingsString)
                .collect(Collectors.joining());
        return new StringBuilder().append(mapped);
    }

    public List<SavingsInfo> calculatePercents(final BigDecimal totalAmountInUah,
                                               final List<SavingsInfo> savingsInfoList) {
        return savingsInfoList.stream()
                .map(sa -> sa.setPercent(sa.getInUah()
                        .multiply(ONE_HUNDRED)
                        .divide(totalAmountInUah, PERCENTS_SCALE, HALF_UP)
                        .setScale(PERCENTS_SCALE, HALF_DOWN)))
                .collect(toUnmodifiableList());
    }

    public List<SavingsInfo> getUserSavings(final List<AccountItem> savingsAccounts, final ZenResponse zenDiff) {
        return savingsAccounts
                .stream()
                .map(a -> buildSavingsInfo(a, zenResponseMapper.getZenCurrencySymbol(zenDiff, a.getInstrument())))
                .collect(toUnmodifiableList());
    }

    private SavingsInfo buildSavingsInfo(final AccountItem accountItem,
                                         final String zenCurrencySymbol) {
        final var instrument = accountItem.getInstrument();
        final var balance = accountItem.getBalance();
        final var startDate = ZonedDateTime.now(ZoneId.systemDefault()).with(LocalTime.MIN);
        final var bal = valueOf(balance);
        final var inUah = instrument == Constants.USD_ID ? currencyService.pbUsdCashDayRates(startDate, USD).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) :
                instrument == Constants.EUR_ID ? currencyService.pbUsdCashDayRates(startDate, EUR).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) : bal;

        return new SavingsInfo()
                .setBalance(bal.setScale(Constants.ZERO_SCALE, HALF_DOWN))
                .setCurrencySymbol(zenCurrencySymbol)
                .setTitle(accountItem.getTitle().trim())
                .setInUah(inUah.setScale(Constants.ZERO_SCALE, HALF_DOWN))
                .setInUahStr(savingsInfoFormatter.formatAmount(inUah.setScale(Constants.ZERO_SCALE, HALF_DOWN)))
                .setBalanceStr(savingsInfoFormatter.formatAmount(bal.setScale(Constants.ZERO_SCALE, HALF_DOWN)));
    }
}
