package com.github.storytime.service.info;

import com.github.storytime.config.props.Constants;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.service.CurrencyService;
import com.github.storytime.service.ZenDiffService;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.CurrencyType.EUR;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class SavingsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(Constants.ONE_HUNDRED);
    private static final Logger LOGGER = getLogger(SavingsService.class);
    private static final int USD_ID = 1;
    private static final int EUR_ID = 3;
    private final CurrencyService currencyService;
    private final UserService userService;
    private final ZenResponseMapper zenResponseMapper;
    private final ZenDiffService zenDiffService;

    @Autowired
    public SavingsService(final CurrencyService currencyService,
                          final UserService userService,
                          final ZenDiffService zenDiffService,
                          final ZenResponseMapper zenResponseMapper) {
        this.currencyService = currencyService;
        this.userService = userService;
        this.zenResponseMapper = zenResponseMapper;
        this.zenDiffService = zenDiffService;
    }

    public String getAllSavingsInfo(final long userId) {
        try {
            LOGGER.debug("Calling get savings info for user: [{}]", userId);
            return userService.findUserById(userId)
                    .map(appUser -> {
                        //todo maybe async?
                        final List<SavingsInfo> savingsInfoList = getUserSavings(appUser);
                        final BigDecimal totalAmountInUah = savingsInfoList.stream()
                                .map(SavingsInfo::getInUah)
                                .reduce(ZERO, BigDecimal::add)
                                .setScale(ZERO_SCALE, HALF_DOWN);

                        savingsInfoList.forEach(sa -> sa.setPercent(sa.getInUah()
                                .multiply(ONE_HUNDRED)
                                .divide(totalAmountInUah, CURRENCY_SCALE, HALF_UP)
                                .setScale(CURRENCY_SCALE, HALF_DOWN))
                        );

                        LOGGER.debug("Finish get savings info for user: [{}]", userId);
                        return getNiceText(savingsInfoList, totalAmountInUah);
                    })
                    .orElse(EMPTY);
        } catch (Exception e) {
            //todo return server error
            LOGGER.error("Cannot collect saving info for user [{}] request:[{}]", userId, e.getCause());
            return EMPTY;
        }
    }

    public String getNiceText(final List<SavingsInfo> savingsInfoList, final BigDecimal totalAmountInUah) {
        final var response = new StringBuilder();
        savingsInfoList
                .stream()
                .sorted(Comparator.comparing(SavingsInfo::getPercent))
                .collect(Collectors.toUnmodifiableList())
                .forEach(si -> response
                        .append(rightPad(si.getTitle() + DOTS + SPACE, SAVINGS_STRING_SIZE))
                        .append(rightPad(si.getBalance() + SPACE + si.getCurrencySymbol() + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                        .append(rightPad(si.getInUah() + SPACE + UAH + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                        .append(rightPad(si.getPercent() + SPACE + PERCENT, SAVINGS_PERCENT_SIZE))
                        .append(LF));
        response.append(TOTAL).append(totalAmountInUah).append(SPACE).append(UAH);
        return response.toString();
    }

    private List<SavingsInfo> getUserSavings(final AppUser appUser) {
        final var zenDiff = zenDiffService.zenDiffByUserForSavings(appUser)
                .orElseThrow(() -> new RuntimeException("Cannot get zen diff to map"));
        return zenResponseMapper.getSavingsAccounts(zenDiff)
                .stream()
                .collect(toUnmodifiableList())
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
        final var inUah = instrument == USD_ID ? currencyService.pbUsdCashDayRates(startDate, USD).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) :
                instrument == EUR_ID ? currencyService.pbUsdCashDayRates(startDate, EUR).map(cr -> bal.multiply(cr.getSellRate())).orElse(bal) : bal;

        return new SavingsInfo()
                .setBalance(bal.setScale(ZERO_SCALE, HALF_DOWN))
                .setCurrencySymbol(zenCurrencySymbol)
                .setTitle(accountItem.getTitle().trim())
                .setInUah(inUah.setScale(ZERO_SCALE, HALF_DOWN));
    }
}
