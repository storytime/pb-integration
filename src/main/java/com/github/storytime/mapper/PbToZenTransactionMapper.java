package com.github.storytime.mapper;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.AdditionalCommentService;
import com.github.storytime.service.CustomPayeeService;
import com.github.storytime.service.DateService;
import com.github.storytime.service.RegExpService;
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
    private final CustomPayeeService customPayeeService;
    private final ZenCommonMapper zenCommonMapper;
    private final AdditionalCommentService additionalCommentService;

    @Autowired
    public PbToZenTransactionMapper(final DateService dateService,
                                    final RegExpService regExpService,
                                    final CustomPayeeService customPayeeService,
                                    final ZenDiffHttpService zenDiffHttpService,
                                    final ZenCommonMapper zenCommonMapper,
                                    final AdditionalCommentService additionalCommentService) {
        this.dateService = dateService;
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

    public TransactionItem parseTransactionItem(final ZenResponse zenDiff, final AppUser u, final Statement pbTr) {
        final var newZenTr = new TransactionItem();
        final var transactionDesc = regExpService.normalizeDescription(pbTr.getDescription());
        final var opAmount = valueOf(substringBefore(pbTr.getAmount(), SPACE));
        final var opCurrency = substringAfter(pbTr.getAmount(), SPACE);
        final var cardAmount = Double.parseDouble(substringBefore(pbTr.getCardamount(), SPACE));
        final var cardCurrency = substringAfter(pbTr.getCardamount(), SPACE);
        final var pbCard = pbTr.getCard();
        final var accountId = zenDiffHttpService.findAccountIdByPbCard(zenDiff, pbCard);
        final var currency = zenDiffHttpService.findCurrencyIdByShortLetter(zenDiff, cardCurrency);
        final var trDate = dateService.toZenFormat(pbTr.getTrandate(), pbTr.getTrantime(), u.getTimeZone());
        final var appCode = Optional.ofNullable(pbTr.getAppcode()).orElse(EMPTY);
        final var createdTime = dateService.xmlDateTimeToZoned(pbTr.getTrandate(), pbTr.getTrantime(), u.getTimeZone()).toInstant().getEpochSecond();
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
        newZenTr.setComment(pbTr.getCustomComment());
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

        // parse transfer between own cards
        if (regExpService.isInternalTransfer(transactionDesc)) {
            newZenTr.setPayee(EMPTY);
            newZenTr.setOriginalPayee(EMPTY);

            final var cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
            final var maybeAcc = zenDiffHttpService.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, pbCard);
            final var isAccountExists = maybeAcc.isPresent();

            if (regExpService.isInternalFrom(transactionDesc) && isAccountExists) {
                newZenTr.setOutcome(opAmount);
                newZenTr.setIncomeBankID(null);
                newZenTr.setOutcomeAccount(maybeAcc.get());
            }

            if (regExpService.isInternalTo(transactionDesc) && isAccountExists) {
                newZenTr.setIncome(opAmount);
                newZenTr.setOutcome(opAmount);
                newZenTr.setOutcomeBankID(null);
                newZenTr.setIncomeAccount(maybeAcc.get());
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