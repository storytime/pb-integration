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
                .map(pbStatement -> parseTransactionItem(zenDiff, u, pbStatement))
                .filter(Objects::nonNull)
                .collect(toUnmodifiableList());
    }

    private String createIdForZen(final long userId, final Double amount, final byte[] trDateBytes) {
        final var userIdBytes = Long.toString(userId).getBytes();
        final var trAmountByes = String.valueOf(amount).getBytes();
        final var capacity = userIdBytes.length + trDateBytes.length + trAmountByes.length;
        final var idBytes = ByteBuffer.allocate(capacity)
                .put(userIdBytes)
                .put(trDateBytes)
                .put(trAmountByes)
                .array();
        return UUID.nameUUIDFromBytes(idBytes).toString();
    }

    public TransactionItem parseTransactionItem(final ZenResponse zenDiff, final AppUser u, final Statement s) {
        final var newZenTr = new TransactionItem();
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
        final var idTr = createIdForZen(u.getId(), Math.abs(opAmount), trDate.getBytes());
        final var userId = zenCommonMapper.getUserId(zenDiff);
        final var nicePayee = customPayeeService.getNicePayee(transactionDesc);
        final var merchantId = zenDiffHttpService.findMerchantByNicePayee(zenDiff, nicePayee);

        newZenTr.setIncomeBankID(cardAmount > EMPTY_AMOUNT ? appCode : EMPTY);
        newZenTr.setOutcomeBankID(cardAmount < EMPTY_AMOUNT ? appCode : EMPTY);
        newZenTr.setId(idTr);
        newZenTr.setChanged(NOT_CHANGED);
        newZenTr.setCreated(createdTime);
        newZenTr.setUser(userId);
        newZenTr.setDeleted(false);
        newZenTr.setPayee(nicePayee);
        newZenTr.setOriginalPayee(transactionDesc);
        newZenTr.setComment(s.getCustomComment());
        newZenTr.setDate(trDate);
        newZenTr.setIncomeAccount(accountId);
        newZenTr.setIncome(cardAmount > EMPTY_AMOUNT ? cardAmount : EMPTY_AMOUNT);
        newZenTr.setOutcomeAccount(accountId);
        newZenTr.setOutcome(cardAmount < EMPTY_AMOUNT ? -cardAmount : EMPTY_AMOUNT);
        newZenTr.setIncomeInstrument(currency);
        newZenTr.setOutcomeInstrument(currency);
        newZenTr.setViewed(false);
        newZenTr.setMerchant(merchantId);

        // transaction in different currency
        final var isAnotherCurrency = opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency);
        final var currencyIdByShortLetter = zenDiffHttpService.findCurrencyIdByShortLetter(zenDiff, opCurrency);
        if (isAnotherCurrency) {
            mapDifferentCurrency(newZenTr, opAmount, currencyIdByShortLetter);
            final var newComment = additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + newZenTr.getComment();
            newZenTr.setComment(newComment);
        }

        // cash withdrawal
        if (regExpService.isCashWithdrawal(transactionDesc)) {
            final var maybeCashCurrency = zenDiffHttpService.isCashAccountInCurrencyExists(zenDiff, currencyIdByShortLetter);
            maybeCashCurrency.ifPresent(updateIncomeIfCashWithdrawal(newZenTr, opAmount));
            LOGGER.info("Cash withdrawal transaction");
            return newZenTr;
        }

        // parse transfer
        if (regExpService.isInternalTransfer(transactionDesc)) {
            newZenTr.setPayee(EMPTY);
            newZenTr.setOriginalPayee(EMPTY);

            if (regExpService.isInternalFrom(transactionDesc)) {
                final String id = transferInfoService.generateIdForFromTransfer(u, s, opAmount, transactionDesc);
                if (transferInfoService.isAlreadyHandled(id)) {
                    LOGGER.info("FROM transfer id:[{}] that is already handled", id);
                    return null;
                }

                final String cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
                final Optional<String> fromAcc = zenDiffHttpService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, s.getCard());
                if (fromAcc.isPresent()) {
                    newZenTr.setOutcomeAccount(fromAcc.get());
                    newZenTr.setOutcome(opAmount);
                    newZenTr.setIncomeBankID(null);
                    transferInfoService.save(id);
                    LOGGER.info("FROM transfer storage id:[{}], account id:[{}]", id, fromAcc.get());

                } else {
                    LOGGER.info("FROM transfer storage id:[{}], without account", id);
                    newZenTr.setComment("Перевод <-- " + regExpService.getCardDigits(transactionDesc));
                    transferInfoService.save(id);
                    return newZenTr;
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
                    newZenTr.setIncome(opAmount);
                    newZenTr.setOutcome(opAmount);
                    newZenTr.setOutcomeBankID(null);
                    newZenTr.setIncomeAccount(toAcc.get());
                    transferInfoService.save(id);
                    LOGGER.info("TO transfer storage id:[{}], account id:[{}]", id, toAcc.get());
                } else {
                    LOGGER.info("TO transfer storage id:[{}], without account", id);
                    newZenTr.setComment("Перевод --> " + regExpService.getCardDigits(transactionDesc));
                    transferInfoService.save(id);
                    return newZenTr;
                }
            }
        }

        return newZenTr;
    }

    private void mapDifferentCurrency(final TransactionItem t,
                                      final Double opAmount,
                                      final Integer currencyIdByShortLetter) {
        if (opAmount > EMPTY_AMOUNT) {
            t.setOpIncome(abs(opAmount));
            t.setOpIncomeInstrument(currencyIdByShortLetter);
        } else {
            t.setOutcome(abs(opAmount));
            t.setOpOutcomeInstrument(currencyIdByShortLetter);
        }
    }

    private Consumer<AccountItem> updateIncomeIfCashWithdrawal(final TransactionItem t, final Double opAmount) {
        return a -> {
            t.setIncome(opAmount);
            t.setIncomeAccount(a.getId());
        };
    }
}