package com.github.storytime.mapper;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.*;
import com.github.storytime.service.http.ZenDiffHttpService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.*;
import static java.lang.Double.valueOf;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenTransactionMapper {

    private static final Logger LOGGER = getLogger(PbToZenTransactionMapper.class);

    private final DateService dateService;
    private final ZenDiffHttpService zenDiffHttpService;
    private final RegExpService regExpService;
    private final PbInternalTransferInfoService transferInfoService;
    private final CustomPayeeService customPayeeService;
    private final ZenCommonMapper zenCommonMapper;
    private final AdditionalCommentService additionalCommentService;

    @Autowired
    public PbToZenTransactionMapper(final PbInternalTransferInfoService pbInternalTransferInfoService,
                                    final DateService dateService,
                                    final RegExpService regExpService,
                                    final CustomPayeeService customPayeeService,
                                    final ZenDiffHttpService zenDiffHttpService,
                                    final ZenCommonMapper zenCommonMapper,
                                    final AdditionalCommentService additionalCommentService) {
        this.dateService = dateService;
        this.transferInfoService = pbInternalTransferInfoService;
        this.regExpService = regExpService;
        this.zenDiffHttpService = zenDiffHttpService;
        this.customPayeeService = customPayeeService;
        this.zenCommonMapper = zenCommonMapper;
        this.additionalCommentService = additionalCommentService;
    }


    public List<TransactionItem> mapPbTransactionToZen(final List<Statement> statementList,
                                                       final ZenResponse zenDiff,
                                                       final AppUser u) {

        return statementList
                .stream()
                .map(s -> parseTransactionItem(zenDiff, u, s))
                .filter(Objects::nonNull)
                .collect(toUnmodifiableList());
    }

    private String createIdForZen(final long userId, final String amount, final String trDate) {
        final var userIdBytes = Long.toString(userId).getBytes();
        final var trDateBytes = trDate.getBytes();
        final var trAmountByes = amount.getBytes();
        final var idBytes = ByteBuffer.allocate(userIdBytes.length + trDateBytes.length + trAmountByes.length)
                .put(userIdBytes)
                .put(trDateBytes)
                .put(trAmountByes)
                .array();
        return UUID.nameUUIDFromBytes(idBytes).toString();
    }

    public TransactionItem parseTransactionItem(final ZenResponse zenDiff, final AppUser u, final Statement s) {
        final var t = new TransactionItem();
        final var transactionDesc = regExpService.normalizeDescription(s.getDescription());
        final var opAmount = valueOf(substringBefore(s.getAmount(), SPACE));
        final var opCurrency = substringAfter(s.getAmount(), SPACE);
        final var cardAmount = Double.parseDouble(substringBefore(s.getCardamount(), SPACE));
        final var cardCurrency = substringAfter(s.getCardamount(), SPACE);
        final var accountId = zenDiffHttpService.findAccountIdByPbCard(zenDiff, s.getCard());
        final var currency = zenDiffHttpService.findCurrencyIdByShortLetter(zenDiff, cardCurrency);
        final var trDate = dateService.toZenFormat(s.getTrandate(), s.getTrantime(), u.getTimeZone());
        final var appCode = Optional.ofNullable(s.getAppcode()).orElse(EMPTY);
        final var createdTime = dateService.xmlDateTimeToZoned(s.getTrandate(), s.getTrantime(), u.getTimeZone()).toInstant().getEpochSecond();
        final var idTr = createIdForZen(u.getId(), s.getAmount(), trDate);
        final var userId = zenCommonMapper.getUserId(zenDiff);
        final var nicePayee = customPayeeService.getNicePayee(transactionDesc);

        t.setIncomeBankID(cardAmount > EMPTY_AMOUNT ? appCode : EMPTY);
        t.setOutcomeBankID(cardAmount < EMPTY_AMOUNT ? appCode : EMPTY);
        t.setId(idTr);
        t.setChanged(NOT_CHANGED);
        t.setCreated(createdTime);
        t.setUser(userId);
        t.setDeleted(false);
        t.setPayee(nicePayee);
        t.setOriginalPayee(transactionDesc);
        t.setComment(s.getCustomComment());
        t.setDate(trDate);
        t.setIncomeAccount(accountId);
        t.setIncome(cardAmount > EMPTY_AMOUNT ? cardAmount : EMPTY_AMOUNT);
        t.setOutcomeAccount(accountId);
        t.setOutcome(cardAmount < EMPTY_AMOUNT ? -cardAmount : EMPTY_AMOUNT);
        t.setIncomeInstrument(currency);
        t.setOutcomeInstrument(currency);
        t.setViewed(false);

        // transaction in different currency
        //handleTransactionInDifferentCurrency(zenDiff, t, opAmount, opCurrency, cardAmount, cardCurrency);

        final var isAnotherCurrency = opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency);
        final var currencyIdByShortLetter = zenDiffHttpService.findCurrencyIdByShortLetter(zenDiff, opCurrency);
        final var isIncome = isAnotherCurrency && opAmount > EMPTY_AMOUNT;
        final var isOutcome = isAnotherCurrency && opAmount < EMPTY_AMOUNT;
        final var newComment = additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + t.getComment();

        t.setOpIncome(isIncome ? abs(opAmount) : EMPTY_AMOUNT);
        t.setOpIncomeInstrument(currencyIdByShortLetter);
        t.setOutcome(isOutcome ? abs(opAmount): EMPTY_AMOUNT);
        t.setOpOutcomeInstrument(currencyIdByShortLetter);
        t.setComment(newComment);

        // cash withdrawal
        if (regExpService.isCashWithdrawal(transactionDesc)) {
            zenDiffHttpService.isCashAccountInCurrencyExists(zenDiff, opCurrency)
                    .ifPresent(updateIncomeIfCashWithdrawal(t, opAmount));
            //todo improve comment and separate bank tax
            LOGGER.info("Cash withdrawal transaction");
            return t;
        }

        // parse transfer
        if (regExpService.isInternalTransfer(transactionDesc)) {
            t.setPayee(EMPTY);
            t.setOriginalPayee(EMPTY);

            if (regExpService.isInternalFrom(transactionDesc)) {
                final String id = transferInfoService.generateIdForFromTransfer(u, s, opAmount, transactionDesc);
                if (transferInfoService.isAlreadyHandled(id)) {
                    LOGGER.info("FROM transfer id:[{}] that is already handled", id);
                    return null;
                }

                final String cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
                final Optional<String> fromAcc = zenDiffHttpService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
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
                final Optional<String> toAcc = zenDiffHttpService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
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
    }

//    private void handleTransactionInDifferentCurrency(final ZenResponse zenDiff,
//                                                      final TransactionItem t,
//                                                      final Double opAmount,
//                                                      final String opCurrency,
//                                                      final Double cardAmount,
//                                                      final String cardCurrency) {
//
//        final var isAnotherCurrency = opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency);
//        final var currencyIdByShortLetter = zenDiffHttpService.findCurrencyIdByShortLetter(zenDiff, opCurrency);
//        final var isIncome = isAnotherCurrency && opAmount > EMPTY_AMOUNT;
//        final var isOutcome = isAnotherCurrency && opAmount < EMPTY_AMOUNT;
//        final var newComment = additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + t.getComment();
//
//        t.setOpIncome(isIncome ? abs(opAmount) : EMPTY_AMOUNT);
//        t.setOpIncomeInstrument(currencyIdByShortLetter);
//        t.setOutcome(isOutcome ? abs(opAmount): EMPTY_AMOUNT);
//        t.setOpOutcomeInstrument(currencyIdByShortLetter);
//        t.setComment(newComment);
//    }

    private Consumer<AccountItem> updateIncomeIfCashWithdrawal(final TransactionItem t, final Double opAmount) {
        return a -> {
            t.setIncome(opAmount);
            t.setIncomeAccount(a.getId());
        };
    }

//    private void setAppCode(final Statement s, final TransactionItem t, final Double cardAmount) {
//        final String appCode = s.getAppcode();
//        if (appCode != null) {
//            if (cardAmount > EMPTY_AMOUNT) {
//                t.setIncomeBankID(appCode);
//            } else {
//                t.setOutcomeBankID(appCode);
//            }
//        }
//    }

//    private void setAppCode(final Statement s, final TransactionItem t, final Double cardAmount) {
//        var appCode = Optional.ofNullable(s.getAppcode()).orElse(EMPTY);
//        t.setIncomeBankID(cardAmount > EMPTY_AMOUNT ? appCode : EMPTY);
//        t.setOutcomeBankID(cardAmount < EMPTY_AMOUNT ? appCode : EMPTY);
//    }

}