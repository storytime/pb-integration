package com.github.storytime.mapper.response;

import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.InstrumentItem;
import com.github.storytime.model.zen.MerchantItem;
import com.github.storytime.model.zen.ZenResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.*;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class ZenResponseMapper {

    public int findUserId(final ZenResponse zenDiff) {
        return zenDiff
                .getUser()
                .stream()
                .findFirst()
                .orElseThrow().getId();
    }

    public String findAccountIdByPbCard(final ZenResponse zenDiff, final Long card) {
        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return ofNullable(zenDiff.getAccount()).orElse(emptyList())
                .stream()
                .filter(a -> ofNullable(a.getSyncID()).orElse(emptyList()).contains(carLastDigits))
                .findFirst()
                .map(AccountItem::getId)
                .orElse(EMPTY);
    }

    public String findMerchantByNicePayee(final ZenResponse zenDiff, final String nicePayee) {
        return ofNullable(zenDiff.getMerchant()).orElse(emptyList())
                .stream()
                .filter(a -> ofNullable(a.getTitle()).orElse(EMPTY).trim().equals(nicePayee))
                .findFirst()
                .map(MerchantItem::getId)
                .orElse(null);
    }

    public Integer findCurrencyIdByShortLetter(final ZenResponse zenDiff, final String shortLetter) {
        return zenDiff.getInstrument()
                .stream()
                .filter(i -> i.getShortTitle().equalsIgnoreCase(shortLetter))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }

    public Optional<AccountItem> findCashAccountByCurrencyId(final ZenResponse zenDiff, final Integer curId) {
        return ofNullable(zenDiff.getAccount()).orElse(emptyList())
                .stream()
                .filter(a -> a.getType().equalsIgnoreCase(CASH) && a.getInstrument() == curId)
                .findFirst();
    }

    public Optional<String> findAccountIdByTwoCardDigits(final ZenResponse zenDiff,
                                                         final String lastTwoDigits,
                                                         final Long card) {
        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return ofNullable(zenDiff.getAccount()).orElse(emptyList())
                .stream()
                .filter(not(a -> ofNullable(a.getSyncID())
                        .orElse(emptyList())
                        .contains(carLastDigits)))
                .filter(a -> ofNullable(a.getSyncID())
                        .orElse(emptyList())
                        .stream()
                        .anyMatch(s -> right(valueOf(s), CARD_TWO_DIGITS).equalsIgnoreCase(lastTwoDigits)))
                .findFirst()
                .map(AccountItem::getId);
    }

    public Integer getZenCurrencyFromPbTransaction(final ZenResponse zenDiff, final String transactionString) {
        final String shortName = substringAfter(transactionString, SPACE);
        return ofNullable(zenDiff.getInstrument()).orElse(emptyList())
                .stream()
                .filter(zenCurr -> zenCurr.getShortTitle().equalsIgnoreCase(shortName))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }

    public String getZenCurrencySymbol(final ZenResponse zenDiff, final int id) {
        return ofNullable(zenDiff.getInstrument()).orElse(emptyList())
                .stream()
                .filter(zenCurr -> zenCurr.getId() == id)
                .findFirst()
                .map(InstrumentItem::getSymbol)
                .orElseThrow();
    }

    public List<AccountItem> getSavingsAccounts(final ZenResponse zenDiff) {
        return ofNullable(zenDiff.getAccount()).orElse(emptyList())
                .stream()
                .filter(AccountItem::getSavings)
                .filter(not(AccountItem::isArchive))
                .filter(ai -> ai.getBalance() > EMPTY_BAL).toList();
    }
}
