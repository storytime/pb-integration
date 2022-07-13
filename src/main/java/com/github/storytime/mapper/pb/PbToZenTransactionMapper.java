package com.github.storytime.mapper.pb;

import com.github.storytime.mapper.CustomPayeeMapper;
import com.github.storytime.mapper.response.ZenResponseMapper;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.MerchantItem;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.misc.AdditionalCommentService;
import com.github.storytime.service.misc.CustomPayeeService;
import com.github.storytime.service.misc.DateService;
import com.github.storytime.service.misc.RegExpService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.*;
import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenTransactionMapper {

    private static final Logger LOGGER = getLogger(PbToZenTransactionMapper.class);

    private final DateService dateService;
    private final RegExpService regExpService;
    private final CustomPayeeMapper customPayeeMapper;
    private final CustomPayeeService customPayeeService;
    private final AdditionalCommentService additionalCommentService;
    private final ZenResponseMapper zenResponseMapper;

    @Autowired
    public PbToZenTransactionMapper(final DateService dateService,
                                    final RegExpService regExpService,
                                    final CustomPayeeMapper customPayeeMapper,
                                    final CustomPayeeService customPayeeService,
                                    final AdditionalCommentService additionalCommentService,
                                    final ZenResponseMapper zenResponseMapper) {
        this.dateService = dateService;
        this.regExpService = regExpService;
        this.customPayeeMapper = customPayeeMapper;
        this.additionalCommentService = additionalCommentService;
        this.zenResponseMapper = zenResponseMapper;
        this.customPayeeService = customPayeeService;
    }

    public List<TransactionItem> mapPbTransactionToZen(final List<Statement> statementList,
                                                       final ZenResponse zenDiff,
                                                       final AppUser u) {

        return statementList
                .stream()
                .map(pbStatement -> parseTransactionItem(zenDiff, u, pbStatement))
                .filter(Objects::nonNull)
                .toList();
    }

    private String createIdForZen(final String userId,
                                  final Double amount,
                                  final byte[] trDateBytes,
                                  final String card,
                                  final String appcode,
                                  final String terminal) {
        final var userIdBytes = userId.getBytes();
        final var trAmountByes = String.valueOf(amount).getBytes();
        final var cardBytes = card.getBytes();
        final var appCodeBytes = appcode.getBytes();
        final var descBytes = terminal.getBytes();
        final var capacity = userIdBytes.length + trDateBytes.length +
                trAmountByes.length + cardBytes.length + appCodeBytes.length + descBytes.length;

        final var idBytes = ByteBuffer.allocate(capacity)
                .put(userIdBytes)
                .put(trDateBytes)
                .put(trAmountByes)
                .put(cardBytes)
                .put(appCodeBytes)
                .put(descBytes)
                .array();

        return UUID.nameUUIDFromBytes(idBytes).toString();
    }

    private String createIdForZenForOwnTransfer(final String userId,
                                                final long createdTimeArg,
                                                final String terminal) {
        final var userIdBytes = userId.getBytes();
        final var terminalBytes = terminal.getBytes();
        final var crTimeBytes = String.valueOf(createdTimeArg).getBytes();
        final var capacity = userIdBytes.length + crTimeBytes.length + terminalBytes.length;

        //  LOGGER.debug("PBM transfer id generation, user id: [{}], terminal: [{}], createdTimeArg: [{}]", userId, terminal, createdTimeArg);

        final var idBytes = ByteBuffer.allocate(capacity)
                .put(userIdBytes)
                .put(crTimeBytes)
                .put(terminalBytes)
                .array();

        return UUID.nameUUIDFromBytes(idBytes).toString();
    }

    public TransactionItem parseTransactionItem(final ZenResponse zenDiff, final AppUser user, final Statement pbTr) {
        final var userId = user.getId();

        LOGGER.debug("Mapping tr: [{}], for user: [{}]", pbTr, userId);

        final TransactionItem.TransactionItemBuilder newZenBuilder = TransactionItem.builder();
        final var transactionDesc = regExpService.normalizeDescription(pbTr.getDescription());
        final var opAmount = Double.parseDouble(substringBefore(pbTr.getAmount(), SPACE));
        final var opCurrency = substringAfter(pbTr.getAmount(), SPACE);
        final var cardAmount = parseDouble(substringBefore(pbTr.getCardamount(), SPACE));
        final var cardCurrency = substringAfter(pbTr.getCardamount(), SPACE);
        final var pbCard = pbTr.getCard();
        final var accountIdArg = zenResponseMapper.findAccountIdByPbCard(zenDiff, pbCard);
        final var currencyArg = zenResponseMapper.findCurrencyIdByShortLetter(zenDiff, cardCurrency);
        final var trDateArg = dateService.toZenFormat(pbTr.getTrandate(), pbTr.getTrantime(), user.getTimeZone());
        final var appCode = ofNullable(pbTr.getAppcode()).orElse(EMPTY);
        final var createdTimeArg = dateService.xmlDateTimeToZoned(pbTr.getTrandate(), pbTr.getTrantime(), user.getTimeZone()).toInstant().getEpochSecond();
        final var terminal = pbTr.getTerminal();
        final var idTrArg = createIdForZen(userId, opAmount, trDateArg.getBytes(), pbTr.getCard(), pbTr.getAppcode(), terminal);
        final var userIdArg = zenResponseMapper.findUserId(zenDiff);

        //builder arg preparation
        final var incomeBankIdArg = cardAmount > EMPTY_AMOUNT ? appCode : EMPTY;
        final var outcomeBankIdArg = cardAmount < EMPTY_AMOUNT ? appCode : EMPTY;
        final var customCommentArg = pbTr.getCustomComment();
        final var incomeArg = cardAmount > EMPTY_AMOUNT ? cardAmount : EMPTY_AMOUNT;
        final var outComeArg = cardAmount < EMPTY_AMOUNT ? -cardAmount : EMPTY_AMOUNT;

        newZenBuilder.incomeBankID(incomeBankIdArg);
        newZenBuilder.outcomeBankID(outcomeBankIdArg);
        newZenBuilder.id(idTrArg);
        newZenBuilder.changed(NOT_CHANGED);
        newZenBuilder.created(createdTimeArg);
        newZenBuilder.user(userIdArg);
        newZenBuilder.deleted(false);
        newZenBuilder.comment(customCommentArg);
        newZenBuilder.date(trDateArg);
        newZenBuilder.incomeAccount(accountIdArg);
        newZenBuilder.income(incomeArg);
        newZenBuilder.outcomeAccount(accountIdArg);
        newZenBuilder.outcome(outComeArg);
        newZenBuilder.incomeInstrument(currencyArg);
        newZenBuilder.outcomeInstrument(currencyArg);
        newZenBuilder.viewed(false);

        // transaction in different currency
        final var isAnotherCurrencyFlag = opAmount != EMPTY_AMOUNT && !opCurrency.equalsIgnoreCase(cardCurrency);
        final var currencyIdByShortLetterArg = zenResponseMapper.findCurrencyIdByShortLetter(zenDiff, opCurrency);
        final var inAnotherCurrencyPositiveFlag = isAnotherCurrencyFlag && opAmount > EMPTY_AMOUNT;
        final var inAnotherCurrencyNegativeFlag = isAnotherCurrencyFlag && opAmount > EMPTY_AMOUNT;
        final var maybeCashAccountInCurrency = zenResponseMapper.findCashAccountByCurrencyId(zenDiff, currencyIdByShortLetterArg);
        final var cashFlag = regExpService.isCashWithdrawal(transactionDesc);
        final var isCashAndAccountFlag = cashFlag && maybeCashAccountInCurrency.isPresent();

        final var isOwnTransferFlag = regExpService.isInternalTransfer(transactionDesc);
        final var isPrivateTerminal = regExpService.isInternalTransferAdditionalCheck(terminal);
        final var internalFromFlag = regExpService.isInternalFrom(transactionDesc);
        final var internalToFlag = regExpService.isInternalTo(transactionDesc);
        final var isOwnTransferForSureFlag = isOwnTransferFlag && isPrivateTerminal;
        final var isMoneyBackFlag = regExpService.isMoneyBack(transactionDesc);

        //is in another currency
        if (inAnotherCurrencyPositiveFlag) {
            newZenBuilder.opIncome(abs(cardAmount));
            newZenBuilder.opIncomeInstrument(currencyIdByShortLetterArg);
            newZenBuilder.comment(additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + customCommentArg);
        }

        if (inAnotherCurrencyNegativeFlag) {
            newZenBuilder.outcome(abs(cardAmount));
            newZenBuilder.opOutcomeInstrument(currencyIdByShortLetterArg);
            newZenBuilder.comment(additionalCommentService.exchangeInfoComment(opAmount, opCurrency, cardAmount) + customCommentArg);
        }

        // cash withdrawal
        if (isCashAndAccountFlag) {
            newZenBuilder.income(opAmount);
            newZenBuilder.incomeAccount(maybeCashAccountInCurrency.get().getId());
            // LOGGER.debug("PBM cash withdrawal tr: [{}], user: [{}]", pbTr, userId);
            return newZenBuilder.build();
        }

        // parse transfer between own cards
        if (isOwnTransferForSureFlag) {
            final var idForZenForOwnTransfer = createIdForZenForOwnTransfer(userId, createdTimeArg, terminal);
            newZenBuilder.id(idForZenForOwnTransfer);
            final var cardLastDigits = regExpService.getCardLastDigits(transactionDesc);
            final var maybeAcc = zenResponseMapper.findAccountIdByTwoCardDigits(zenDiff, cardLastDigits, pbCard);
            final var isAccountExists = maybeAcc.isPresent();

            if (internalFromFlag && isAccountExists) {
                newZenBuilder.outcome(opAmount);
                newZenBuilder.incomeBankID(null);
                newZenBuilder.outcomeAccount(maybeAcc.get());
            } else if (internalToFlag && isAccountExists) {
                newZenBuilder.income(opAmount);
                newZenBuilder.outcome(opAmount);
                newZenBuilder.outcomeBankID(null);
                newZenBuilder.incomeAccount(maybeAcc.get());
            } else {
                LOGGER.warn("PBM not able to parse internal transfer cannot get to/from account tr id: [{}], user: [{}], id tr: [{}]", idForZenForOwnTransfer, userId, pbTr);
            }
            // LOGGER.debug("PBM local transfer tr id: [{}], user: [{}], tr: [{}]", idForZenForOwnTransfer, userId, pbTr);
        }

        //add payee
        if (!cashFlag && !isOwnTransferForSureFlag && !isMoneyBackFlag) {
            final var originalDesc = getTransactionItem(transactionDesc, terminal, isPrivateTerminal, isOwnTransferFlag);
            final var maybeNicePayee = customPayeeMapper.getNicePayee(originalDesc, user);
            final var merchant = zenResponseMapper.findMerchantByNicePayee(zenDiff, maybeNicePayee);
            addMerchant(originalDesc, maybeNicePayee, newZenBuilder, merchant);
            customPayeeService.updatePayeeForUser(user, originalDesc);
            LOGGER.debug("Nice payee is: [{}] for original: [{}], merchant: [{}]", maybeNicePayee, originalDesc, merchant.map(MerchantItem::getId).orElse(EMPTY));
        }

        return newZenBuilder.build();
    }

    private String getTransactionItem(final String transactionDesc, final String terminal, boolean isPrivateTerminal, boolean isOwnTransferFlag) {
        if (isPrivateTerminal) {
            return transactionDesc;
        } else if (isOwnTransferFlag) {
            return terminal;
        } else {
            return transactionDesc;
        }
    }

    private void addMerchant(final String transactionDesc,
                             final String maybeNicePayee,
                             final TransactionItem.TransactionItemBuilder newZenBuilder,
                             final Optional<MerchantItem> merchant) {
        newZenBuilder.originalPayee(transactionDesc);
        newZenBuilder.payee(merchant.map(MerchantItem::getTitle).orElse(maybeNicePayee));
        newZenBuilder.merchant(merchant.map(MerchantItem::getId).orElse(null));
    }
}