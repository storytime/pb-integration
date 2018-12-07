package com.github.storytime.mapper;

import com.github.storytime.error.exception.ZenUserNotFoundException;
import com.github.storytime.model.db.AppUser;
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
import java.util.function.Consumer;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.Math.abs;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableList;
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
    private final CustomPayeeService customPayeeService;

    @Autowired
    public PbToZenTransactionMapper(final PbInternalTransferInfoService pbInternalTransferInfoService,
                                    final DateService dateService,
                                    final CurrencyService currencyService,
                                    final RegExpService regExpService,
                                    final CustomPayeeService customPayeeService,
                                    final ZenDiffService zenDiffService) {
        this.dateService = dateService;
        this.transferInfoService = pbInternalTransferInfoService;
        this.currencyService = currencyService;
        this.regExpService = regExpService;
        this.zenDiffService = zenDiffService;
        this.customPayeeService = customPayeeService;
    }


    public List<TransactionItem> mapPbTransactionToZen(final List<Statement> statementList,
                                                       final ZenResponse zenDiff,
                                                       final AppUser u) {

        return statementList
                .stream()
                .map((Statement s) -> {
                    final var t = new TransactionItem();
                    final var transactionDesc = regExpService.normalizeDescription(s.getDescription());
                    final var opAmount = Double.valueOf(substringBefore(s.getAmount(), SPACE_SEPARATOR));
                    final String opCurrency = substringAfter(s.getAmount(), SPACE_SEPARATOR);
                    final var cardAmount = Double.valueOf(substringBefore(s.getCardamount(), SPACE_SEPARATOR));
                    final String cardCurrency = substringAfter(s.getCardamount(), SPACE_SEPARATOR);
                    final String accountId = zenDiffService.findAccountIdByPbCard(zenDiff, s.getCard());
                    final Integer currency = zenDiffService.findCurrencyIdByShortLetter(zenDiff, cardCurrency);

                    setAppCode(s, t, cardAmount);

                    t.setId(randomUUID().toString());
                    t.setChanged(NOT_CHANGED);
                    t.setCreated(dateService.xmlDateTimeToZoned(s.getTrandate(), s.getTrantime(), u.getTimeZone()).toInstant().toEpochMilli());
                    t.setUser(zenDiff
                            .getUser()
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new ZenUserNotFoundException("Zen User not found")).getId());
                    t.setDeleted(false);
                    t.setPayee(customPayeeService.getNicePayee(transactionDesc));
                    t.setOriginalPayee(transactionDesc);
                    t.setComment(s.getCustomComment());
                    t.setDate(dateService.toZenFormat(s.getTrandate(), s.getTrantime(), u.getTimeZone()));
                    t.setIncomeAccount(accountId);
                    t.setIncome(cardAmount > EMPTY_AMOUNT ? cardAmount : EMPTY_AMOUNT);
                    t.setOutcomeAccount(accountId);
                    t.setOutcome(cardAmount < EMPTY_AMOUNT ? -cardAmount : EMPTY_AMOUNT);
                    t.setIncomeInstrument(currency);
                    t.setOutcomeInstrument(currency);

                    // transaction in different currency
                    if (opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency)) {
                        if (opAmount > EMPTY_AMOUNT) {
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
                        zenDiffService.isCashAccountInCurrencyExists(zenDiff, opCurrency)
                                .ifPresent(updateIncomeIfCashWithdrawal(t, opAmount));
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
                                LOGGER.info("FROM transfer id:[{}] that is already handled", id);
                                return null;
                            }

                            final String cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
                            final Optional<String> fromAcc = zenDiffService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
                            if (fromAcc.isPresent()) {
                                t.setOutcomeAccount(fromAcc.get());
                                t.setOutcome(opAmount);
                                t.setIncomeBankID(null);
                                transferInfoService.save(id);
                                LOGGER.info("FROM transfer storage id:[{}], account id:[{}]", id, fromAcc.get());

                            } else {
                                LOGGER.info("FROM transfer storage id:[{}], without account", id);
                                t.setComment("Перевод <-- " + regExpService.getCardDigits(transactionDesc));
                                transferInfoService.save(id);
                                return t;
                            }
                        }

                        if (regExpService.isInternalTo(transactionDesc)) {
                            final String id = transferInfoService.generateIdForToTransfer(u, s, opAmount, transactionDesc);
                            if (transferInfoService.isAlreadyHandled(id)) {
                                LOGGER.info("TO transfer id:[{}] that is already handled", id);
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
                                LOGGER.info("TO transfer storage id:[{}], account id:[{}]", id, toAcc.get());
                            } else {
                                LOGGER.info("TO transfer storage id:[{}], without account", id);
                                t.setComment("Перевод --> " + regExpService.getCardDigits(transactionDesc));
                                transferInfoService.save(id);
                                return t;
                            }
                        }
                    }

                    return t;
                })
                .collect(toUnmodifiableList());
    }

    public Consumer<AccountItem> updateIncomeIfCashWithdrawal(final TransactionItem t, final Double opAmount) {
        return a -> {
            t.setIncome(opAmount);
            t.setIncomeAccount(a.getId());
        };
    }

    private void setAppCode(final Statement s, final TransactionItem t, final Double cardAmount) {
        final String appCode = s.getAppcode();
        if (appCode != null) {
            if (cardAmount > EMPTY_AMOUNT) {
                t.setIncomeBankID(appCode);
            } else {
                t.setOutcomeBankID(appCode);
            }
        }
    }

}