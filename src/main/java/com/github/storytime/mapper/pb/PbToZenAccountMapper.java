package com.github.storytime.mapper.pb;

import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.right;

@Component
public class PbToZenAccountMapper {

    private static final Logger LOGGER = LogManager.getLogger(PbToZenAccountMapper.class);
    private final ZenResponseMapper zenResponseMapper;

    @Autowired
    public PbToZenAccountMapper(final ZenResponseMapper zenResponseMapper) {
        this.zenResponseMapper = zenResponseMapper;
    }

    public Boolean mapPbAccountToZen(final List<Statement> pbStatementList, final ZenResponse zenDiff) {
        final List<AccountItem> zenAccounts = zenDiff.getAccount();
        final List<String> pbCards = getCardsFromBank(pbStatementList); // Get accounts from bank response

        if (pbCards.isEmpty())
            return FALSE;

        // check if current account exists in Zen
        return isPbAccountExistsInZen(zenAccounts, pbCards)
                .flatMap(accountItem -> of(updateExistingAccount(pbCards, accountItem)))
                .or(() -> of(zenAccounts.add(createNewZenAccount(pbStatementList, zenDiff, pbCards))))
                .orElse(FALSE);
    }

    private AccountItem createNewZenAccount(final List<Statement> statementList,
                                            final ZenResponse zenDiff,
                                            final List<String> cardsFromBank) {

        final Integer accountCurrency = statementList
                .stream()
                .findFirst()
                .map(s -> zenResponseMapper.getZenCurrencyFromPbTransaction(zenDiff, s.getRest()))
                .orElse(DEFAULT_CURRENCY_ZEN);

        final AccountItem newZenAccount = new AccountItem()
                .setId(randomUUID().toString())
                .setUser(zenResponseMapper.findUserId(zenDiff))
                .setInstrument(accountCurrency)
                .setType(ZEN_ACCOUNT_TYPE)
                .setRole(null)
                .setJsonMemberPrivate(false)
                .setSavings(false)
                .setTitle(ACCOUNT_TITLE_PREFIX + join(cardsFromBank, TITLE_CARD_SEPARATOR))
                .setInBalance(false)
                .setCreditLimit(0)
                .setStartBalance(0)
                .setCompany(PB_ZEN_ID)
                .setArchive(false)
                .setEnableCorrection(false)
                .setStartDate(null)
                .setCapitalization(null)
                .setPercent(null)
                .setChanged(now().getEpochSecond())
                .setSyncID(cardsFromBank)
                .setEnableSMS(false)
                .setEndDateOffset(null)
                .setEndDateOffsetInterval(null)
                .setPayoffStep(null)
                .setPayoffInterval(null);

        LOGGER.info("Create new account:[{}] with cards:[{}]", newZenAccount.getId(), cardsFromBank);
        return newZenAccount;
    }

    private Boolean updateExistingAccount(List<String> cardsFromBank, AccountItem existingAccount) {
        final var zenCards = ofNullable(existingAccount.getSyncID()).orElse(emptyList());

        // if any new cards
        if (zenCards.containsAll(cardsFromBank)) {
            LOGGER.debug("No new cards for account:[{}]", existingAccount.getId());
            return FALSE;
        } else {
            final List<String> uniqueCards = concat(zenCards.stream(), cardsFromBank.stream())
                    .distinct()
                    .collect(toUnmodifiableList());
            existingAccount.getSyncID().clear();
            existingAccount.setSyncID(uniqueCards);
            existingAccount.setChanged(now().toEpochMilli());
            LOGGER.info("Update existing account:[{}] with cards:[{}]", existingAccount.getId(), uniqueCards);
            return TRUE;
        }
    }

    private Optional<AccountItem> isPbAccountExistsInZen(final List<AccountItem> zenAccounts,
                                                         final List<String> pbCards) {
        return zenAccounts
                .stream()
                .filter(not(za -> isAccountEmpty(za, pbCards)))
                .findFirst();
    }

    private boolean isAccountEmpty(final AccountItem zenAccounts,
                                   final List<String> pbCards) {
        return ofNullable(zenAccounts.getSyncID())
                .orElse(emptyList())
                .stream()
                .filter(pbCards::contains)
                .collect(toUnmodifiableList())
                .isEmpty();
    }

    private List<String> getCardsFromBank(final List<Statement> pbStatementList) {
        return pbStatementList
                .stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(Statement::getCard))), ArrayList::new))
                .stream()
                .map(s -> right(String.valueOf(s.getCard()), CARD_LAST_DIGITS))
                .collect(toUnmodifiableList());
    }
}
