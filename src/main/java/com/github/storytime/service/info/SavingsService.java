package com.github.storytime.service.info;

import com.github.storytime.config.props.Constants;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.api.SavingsInfoAsJson;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.service.CurrencyService;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
public class SavingsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(Constants.ONE_HUNDRED);
    private static final Logger LOGGER = getLogger(SavingsService.class);
    private static final int USD_ID = 1;
    private static final int EUR_ID = 3;
    private final CurrencyService currencyService;
    private final UserService userService;
    private final ZenResponseMapper zenResponseMapper;
    private final ZenAsyncService zenAsyncService;

    @Autowired
    public SavingsService(final CurrencyService currencyService,
                          final UserService userService,
                          final ZenAsyncService zenAsyncService,
                          final ZenResponseMapper zenResponseMapper) {
        this.currencyService = currencyService;
        this.userService = userService;
        this.zenResponseMapper = zenResponseMapper;
        this.zenAsyncService = zenAsyncService;
    }

    public String getAllSavingsAsTable(final long userId) {
        try {
            LOGGER.debug("Calling get savings info as table for user: [{}]", userId);
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

                        LOGGER.debug("Finish get savings info as table for user: [{}]", userId);
                        final var niceSavingsText = getNiceSavings(savingsInfoList);
                        final var niceTotalInUah = formatAmount(totalAmountInUah);
                        return niceSavingsText.append(TOTAL).append(niceTotalInUah).append(SPACE).append(UAH).toString();
                    })
                    .orElse(EMPTY);
        } catch (Exception e) {
            //todo return server error
            LOGGER.error("Cannot collect saving info as table for user: [{}] request:[{}]", userId, e.getCause());
            return EMPTY;
        }
    }

    public ResponseEntity<SavingsInfoAsJson> getAllSavingsJson(final long userId) {
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
                        final var niceTotalInUah = formatAmount(totalAmountInUah);

                        final var resp = new SavingsInfoAsJson()
                                .setSavings(savingsInfoList)
                                .setTotal(niceTotalInUah);

                        return new ResponseEntity<>(resp, OK);
                    })
                    .orElse(new ResponseEntity<>(NO_CONTENT));
        } catch (Exception e) {
            //todo return server error
            LOGGER.error("Cannot collect saving info for user: [{}] request:[{}]", userId, e.getCause());
            return new ResponseEntity<>(NO_CONTENT);
        }
    }

    public String formatAmount(final BigDecimal amount) {
        final String[] totalAsArray = amount.toPlainString().split(SPLITTER_EMPTY);
        final StreamEx<String> values = StreamEx.ofReversed(totalAsArray);
        final IntStreamEx indexes = IntStreamEx.range(START_INCLUSIVE, totalAsArray.length);

        final String formattedTotal = values.zipWith(indexes)
                .map(z -> z.getValue() % FORMATTER_SPLITTER == Constants.ZERO ? SPACE + z.getKey() : z.getKey())
                .collect(Collectors.joining());

        return StringUtils.reverse(formattedTotal.trim());
    }

    public StringBuilder getNiceSavings(final List<SavingsInfo> savingsInfoList) {
        final String mapped = savingsInfoList
                .stream()
                .sorted(Comparator.comparing(SavingsInfo::getPercent))
                .map(this::mapToNiceSavingsString)
                .collect(Collectors.joining());
        return new StringBuilder().append(mapped);
    }

    private StringBuilder mapToNiceSavingsString(final SavingsInfo si) {
        return new StringBuilder()
                .append(rightPad(si.getTitle() + DOTS + SPACE, SAVINGS_STRING_SIZE))
                .append(rightPad(formatAmount(si.getBalance()) + SPACE + si.getCurrencySymbol() + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                .append(rightPad(formatAmount(si.getInUah()) + SPACE + UAH + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                .append(rightPad(si.getPercent() + SPACE + PERCENT, SAVINGS_PERCENT_SIZE))
                .append(LF);
    }

    private List<SavingsInfo> getUserSavings(final AppUser appUser) {
        final var zenDiff = zenAsyncService.zenDiffByUserForSavings(appUser)
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
                .setInUah(inUah.setScale(ZERO_SCALE, HALF_DOWN))
                .setInUahStr(formatAmount(inUah.setScale(ZERO_SCALE, HALF_DOWN)))
                .setBalanceStr(formatAmount(bal.setScale(ZERO_SCALE, HALF_DOWN)));
    }
}
