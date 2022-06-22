package com.github.storytime.mapper.pb;

import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.aws.AwsCustomPayee;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.AdditionalCommentService;
import com.github.storytime.service.utils.CustomPayeeService;
import com.github.storytime.service.utils.DateService;
import com.github.storytime.service.utils.RegExpService;
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
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class PbToZenTransactionMapper {

    private final DateService dateService;
    private final RegExpService regExpService;
    private final CustomPayeeService customPayeeService;
    private final AdditionalCommentService additionalCommentService;
    private final ZenResponseMapper zenResponseMapper;

    @Autowired
    public PbToZenTransactionMapper(final DateService dateService,
                                    final RegExpService regExpService,
                                    final CustomPayeeService customPayeeService,
                                    final AdditionalCommentService additionalCommentService,
                                    final ZenResponseMapper zenResponseMapper) {
        this.dateService = dateService;
        this.regExpService = regExpService;
        this.customPayeeService = customPayeeService;
        this.additionalCommentService = additionalCommentService;
        this.zenResponseMapper = zenResponseMapper;
    }

    public List<TransactionItem> mapPbTransactionToZen(final List<Statement> statementList,
                                                       final ZenResponse zenDiff,
                                                       final AwsUser u) {

        return statementList
                .stream()
                .map(pbStatement -> parseTransactionItem(zenDiff, u, pbStatement))
                .filter(Objects::nonNull).toList();
    }

    private String createIdForZen(final String userId,
                                  final Double amount,
                                  final byte[] trDateBytes,
                                  final Long card,
                                  final String appcode,
                                  final String terminal) {
        // final String transactionDesc) {
        final var userIdBytes = userId.getBytes();
        final var trAmountByes = String.valueOf(amount).getBytes();
        final var cardBytes = Long.toString(card).getBytes();
        final var appCodeBytes = appcode.getBytes();
        final var descBytes = terminal.getBytes();
        //  final var transactionDescBytes = transactionDesc.getBytes();
        final var capacity = userIdBytes.length + trDateBytes.length +
                trAmountByes.length + cardBytes.length + appCodeBytes.length + descBytes.length;// + transactionDescBytes.length;

        final var idBytes = ByteBuffer.allocate(capacity)
                .put(userIdBytes)
                .put(trDateBytes)
                .put(trAmountByes)
                .put(cardBytes)
                .put(appCodeBytes)
                .put(descBytes)
                //       .put(transactionDescBytes)
                .array();

        return UUID.nameUUIDFromBytes(idBytes).toString();
    }

    public TransactionItem parseTransactionItem(final ZenResponse zenDiff, final AwsUser u, final Statement pbTr) {
        final var newZenTr = new TransactionItem();
        final var transactionDesc = regExpService.normalizeDescription(pbTr.getDescription());
        final var opAmount = valueOf(substringBefore(pbTr.getAmount(), SPACE));
        final var opCurrency = substringAfter(pbTr.getAmount(), SPACE);
        final var cardAmount = Double.parseDouble(substringBefore(pbTr.getCardamount(), SPACE));
        final var cardCurrency = substringAfter(pbTr.getCardamount(), SPACE);
        final var pbCard = pbTr.getCard();
        final var accountId = zenResponseMapper.findAccountIdByPbCard(zenDiff, pbCard);
        final var currency = zenResponseMapper.findCurrencyIdByShortLetter(zenDiff, cardCurrency);
        final var trDate = dateService.toZenFormat(pbTr.getTrandate(), pbTr.getTrantime(), u.getTimeZone());
        final var appCode = Optional.ofNullable(pbTr.getAppcode()).orElse(EMPTY);
        final var createdTime = dateService.xmlDateTimeToZoned(pbTr.getTrandate(), pbTr.getTrantime(), u.getTimeZone()).toInstant().getEpochSecond();
        final var idTr = createIdForZen(u.getId(), opAmount, trDate.getBytes(), pbTr.getCard(), pbTr.getAppcode(), pbTr.getTerminal());
        final var userId = zenResponseMapper.findUserId(zenDiff);
        final var nicePayee = customPayeeService.getNicePayee(transactionDesc, u);
        final var merchantId = zenResponseMapper.findMerchantByNicePayee(zenDiff, nicePayee);

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

        //add payeer to users list TODO
        final Optional<AwsCustomPayee> first = u.getAwsCustomPayee()
                .stream()
                .filter(t -> t.getContainsValue().equals(transactionDesc))
                .findFirst();

        if (first.isEmpty())
            u.getAwsCustomPayee().add(new AwsCustomPayee(UNDERSCORE, transactionDesc));

        // transaction in different currency
        final var isAnotherCurrency = opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency);
        final var currencyIdByShortLetter = zenResponseMapper.findCurrencyIdByShortLetter(zenDiff, opCurrency);
        if (isAnotherCurrency) {
            mapDifferentCurrency(newZenTr, opAmount, currencyIdByShortLetter);
            final var newComment = additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + newZenTr.getComment();
            newZenTr.setComment(newComment);
        }

        // cash withdrawal
        if (regExpService.isCashWithdrawal(transactionDesc)) {
            final var maybeCashCurrency = zenResponseMapper.findCashAccountByCurrencyId(zenDiff, currencyIdByShortLetter);
            maybeCashCurrency.ifPresent(updateIncomeIfCashWithdrawal(newZenTr, opAmount));
            return newZenTr;
        }

        // parse transfer between own cards
        if (regExpService.isInternalTransfer(transactionDesc)) {
            newZenTr.setPayee(EMPTY);
            newZenTr.setOriginalPayee(EMPTY);

            final var cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
            final var maybeAcc = zenResponseMapper.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, pbCard);
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