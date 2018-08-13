package com.github.storytime.mapper;

import com.github.storytime.config.props.TextProperties;
import com.github.storytime.exception.ZenUserNotFoundException;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.storytime.config.props.Constants.RATE;
import static com.github.storytime.config.props.Constants.SPACE_SEPARATOR;
import static java.lang.Float.valueOf;
import static java.lang.Math.abs;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Component
public class PbToZenTransactionMapper {

    private static final Logger LOGGER = LogManager.getLogger(PbToZenTransactionMapper.class);

    private final DateService dateService;
    private final ZenDiffService zenDiffService;
    private final CurrencyService currencyService;
    private final RegExpService regExpService;
    private final PbInternalTransferInfoService transferInfoService;
    private final TextProperties textProperties;
    private final CustomPayeeService customPayeeService;

    @Autowired
    public PbToZenTransactionMapper(final PbInternalTransferInfoService pbInternalTransferInfoService,
                                    final DateService dateService,
                                    final CurrencyService currencyService,
                                    final RegExpService regExpService,
                                    final TextProperties textProperties,
                                    final CustomPayeeService customPayeeService,
                                    final ZenDiffService zenDiffService) {
        this.dateService = dateService;
        this.transferInfoService = pbInternalTransferInfoService;
        this.currencyService = currencyService;
        this.regExpService = regExpService;
        this.textProperties = textProperties;
        this.zenDiffService = zenDiffService;
        this.customPayeeService = customPayeeService;
    }


    public List<TransactionItem> mapPbTransactionToZen(final List<Statement> statementList,
                                                       final ZenResponse zenDiff,
                                                       final User u) {
        // TODO: cheat to sort transactions; java 9+ clock has nano time
        final AtomicInteger i = new AtomicInteger(0);
        return statementList
                .stream()
                .map((Statement s) -> {
                    final TransactionItem t = new TransactionItem();
                    final String transactionDesc = regExpService.normalizeDescription(s);
                    final Float opAmount = valueOf(substringBefore(s.getAmount(), SPACE_SEPARATOR));
                    final String opCurrency = substringAfter(s.getAmount(), SPACE_SEPARATOR);
                    final Float cardAmount = valueOf(substringBefore(s.getCardamount(), SPACE_SEPARATOR));
                    final String cardCurrency = substringAfter(s.getCardamount(), SPACE_SEPARATOR);
                    final String accountId = zenDiffService.findAccountIdByPbCard(zenDiff, s.getCard());
                    final Integer currency = zenDiffService.findCurrencyIdByShortLetter(zenDiff, cardCurrency);
                    //final String payee = regExpService.parseComment(transactionDesc);

                    setAppCode(s, t, cardAmount);

                    t.setId(randomUUID().toString());
                    t.setChanged(0);
                    t.setCreated(now().toEpochMilli() + i.incrementAndGet());
                    t.setUser(zenDiff.getUser().stream().findFirst()
                            .orElseThrow(() -> new ZenUserNotFoundException(textProperties.getZenUserNotFound())).getId());
                    t.setDeleted(false);
                    t.setPayee(customPayeeService.getNicePayee(transactionDesc));
                    t.setOriginalPayee(transactionDesc);
                    t.setComment(s.getCustomComment());
                    t.setDate(dateService.toZenFormat(s.getTrandate(), s.getTrantime(), u.getTimeZone()));
                    t.setIncomeAccount(accountId);
                    t.setIncome(cardAmount > 0 ? cardAmount : 0);
                    t.setOutcomeAccount(accountId);
                    t.setOutcome(cardAmount < 0 ? -cardAmount : 0);
                    t.setIncomeInstrument(currency);
                    t.setOutcomeInstrument(currency);

                    // transaction in different currency
                    if (opAmount != 0 && !opCurrency.equalsIgnoreCase(cardCurrency)) {
                        if (opAmount > 0) {
                            t.setOpIncome(abs(opAmount));
                            t.setOpIncomeInstrument(zenDiffService.findCurrencyIdByShortLetter(zenDiff, opCurrency));
                        } else {
                            t.setOutcome(abs(opAmount));
                            t.setOpOutcomeInstrument(zenDiffService.findCurrencyIdByShortLetter(zenDiff, opCurrency));
                        }
                        final String exchangeInfo = opAmount + SPACE_SEPARATOR + opCurrency + RATE
                                + currencyService.convertDivide(cardAmount, opAmount) + SPACE_SEPARATOR;
                        t.setComment(exchangeInfo + t.getComment());
                    }

                    // cash withdrawal
                    if (regExpService.isCashWithdrawal(transactionDesc)) {
                        final Optional<AccountItem> account = zenDiffService.isCashAccountInCurrencyExists(zenDiff, opCurrency);
                        account.ifPresent(a -> {
                            t.setIncome(opAmount);
                            t.setIncomeAccount(a.getId());
                        });
                        //todo improve comment and separate bank tax
                        LOGGER.info("Cash withdrawal transaction");
                        return t;
                    }

                    // parse transfer
                    if (regExpService.isInternalTransfer(transactionDesc)) {
                        t.setPayee("");
                        t.setOriginalPayee("");

                        if (regExpService.isInternalFrom(transactionDesc)) {
                            final String id = transferInfoService.generateIdForFromTransfer(u, s, opAmount, transactionDesc);
                            if (transferInfoService.isAlreadyHandled(id)) {
                                LOGGER.info("FROM transfer id: {} that is already handled", id);
                                return null;
                            }

                            final String cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
                            final Optional<String> fromAcc = zenDiffService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
                            if (fromAcc.isPresent()) {
                                t.setOutcomeAccount(fromAcc.get());
                                t.setOutcome(opAmount);
                                t.setIncomeBankID(null);
                                transferInfoService.save(id);
                                LOGGER.info("FROM transfer storage id: {}, account id: {}", id, fromAcc.get());

                            } else {
                                LOGGER.info("FROM transfer storage id: {}, without account", id);
                                t.setComment("Перевод <-- " + regExpService.getCardDigits(transactionDesc));
                                transferInfoService.save(id);
                                return t;
                            }
                        }

                        if (regExpService.isInternalTo(transactionDesc)) {
                            final String id = transferInfoService.generateIdForToTransfer(u, s, opAmount, transactionDesc);
                            if (transferInfoService.isAlreadyHandled(id)) {
                                LOGGER.info("TO transfer id: {} that is already handled", id);
                                return null;
                            }

                            final String cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
                            final Optional<String> toAcc = zenDiffService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
                            if (toAcc.isPresent()) {
                                t.setIncome(opAmount);
                                t.setOutcome(opAmount);
                                t.setOutcomeBankID(null);
                                t.setIncomeAccount(toAcc.get());
                                transferInfoService.save(id);
                                LOGGER.info("TO transfer storage id: {}, account id: {}", id, toAcc.get());
                            } else {
                                LOGGER.info("TO transfer storage id: {}, without account", id);
                                t.setComment("Перевод --> " + regExpService.getCardDigits(transactionDesc));
                                transferInfoService.save(id);
                                return t;
                            }
                        }
                    }

                    return t;
                })
                .collect(toList());
    }

    private void setAppCode(Statement s, TransactionItem t, Float cardAmount) {
        final Integer appCode = s.getAppcode();
        if (appCode != null) {
            if (cardAmount > 0) {
                t.setIncomeBankID(String.valueOf(appCode));
            } else {
                t.setOutcomeBankID(String.valueOf(appCode));
            }
        }
    }

}
