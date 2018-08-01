package com.github.storytime.mapper;

import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.github.storytime.config.Constants.*;
import static java.time.Instant.now;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.right;

@Component
public class PbToZenAccountMapper {

    private static final Logger LOGGER = LogManager.getLogger(PbToZenAccountMapper.class);
    private final ZenInstrumentsMapper zenInstrumentsMapper;

    @Autowired
    public PbToZenAccountMapper(ZenInstrumentsMapper zenInstrumentsMapper) {
        this.zenInstrumentsMapper = zenInstrumentsMapper;
    }

    public void mapPbAccountToZen(final List<Statement> statementList, final ZenResponse zenDiff) {
        final List<AccountItem> zenAccounts = zenDiff.getAccount();
        // Get accounts from bank response
        final List<String> cardsFromBank = getCardsFromBank(statementList);

        if (cardsFromBank.isEmpty()) {
            return;
        }

        // check if current account exists in Zen
        final Optional<AccountItem> existingAccount = isPbAccountExistsInZen(zenAccounts, cardsFromBank);

        if (existingAccount.isPresent()) {
            updateExistingAccount(cardsFromBank, existingAccount.get());
        } else {
            zenAccounts.add(createNewZenAccount(statementList, zenDiff, cardsFromBank));
        }
    }


    private AccountItem createNewZenAccount(final List<Statement> statementList,
                                            final ZenResponse zenDiff,
                                            final List<String> cardsFromBank) {
        final int zenUserId = zenDiff
                .getUser()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error not zen user")).getId();

        final Integer accountCurrency = statementList
                .stream()
                .findFirst()
                .map(s -> zenInstrumentsMapper.getZenCurrencyFromPbTransaction(zenDiff, s.getRest()))
                .orElse(DEFAULT_CURRENCY_ZEN);

        final AccountItem newZenAccount = new AccountItem()
                .setId(randomUUID().toString())
                .setUser(zenUserId)
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

        LOGGER.info("Create new account: {} with cards: {}", newZenAccount.getId(), cardsFromBank);
        return newZenAccount;
    }

    private void updateExistingAccount(List<String> cardsFromBank, AccountItem existingAccount) {
        final List<String> zenCards = ofNullable(existingAccount.getSyncID()).orElseGet(Collections::emptyList);

        // if any new cards
        if (zenCards.containsAll(cardsFromBank)) {
            LOGGER.debug("No new cards for account: {}", existingAccount.getId());
        } else {
            final List<String> uniqueCards = concat(zenCards.stream(), cardsFromBank.stream())
                    .distinct()
                    .collect(toList());
            existingAccount.getSyncID().clear();
            existingAccount.setSyncID(uniqueCards);
            existingAccount.setChanged(now().toEpochMilli());
            LOGGER.info("Update existing account {} with cards {}", existingAccount.getId(), uniqueCards);
        }
    }

    private Optional<AccountItem> isPbAccountExistsInZen(List<AccountItem> zenAccounts, List<String> cardsFromBank) {
        return zenAccounts.stream()
                .filter(za -> ofNullable(za.getSyncID())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .filter(cardsFromBank::contains).collect(toList()).size() > 0)
                .findFirst();
    }

    private List<String> getCardsFromBank(List<Statement> statementList) {
        return statementList
                .stream()
                .collect(collectingAndThen(toCollection(() ->
                        new TreeSet<>(comparing(Statement::getCard))), ArrayList::new))
                .stream()
                .map(s -> right(String.valueOf(s.getCard()), CARD_LAST_DIGITS))
                .collect(toList());
    }


}
