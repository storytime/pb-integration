package com.github.storytime.service.misc;

import com.github.storytime.config.props.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.lanwen.verbalregex.VerbalExpression;

import static com.github.storytime.config.props.Constants.*;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Component
public class RegExpService {

    private static final int GROUP_2 = 2;
    private static final int GROUP_1 = 1;

    private final VerbalExpression cashWithdrawal;
    private final VerbalExpression internalTransfer;
    private final VerbalExpression internalTransferCard;
    private final VerbalExpression internalFrom;
    private final VerbalExpression internalTo;
    private final VerbalExpression internalTransferComment;
    private final VerbalExpression internalTransferAdditionalCheck;
    private final VerbalExpression moneyBackCheck;

    @Autowired
    public RegExpService(final VerbalExpression internalTransfer,
                         final VerbalExpression internalTransferCard,
                         final VerbalExpression internalFrom,
                         final VerbalExpression internalTo,
                         final VerbalExpression internalTransferAdditionalCheck,
                         final VerbalExpression internalTransferComment,
                         final VerbalExpression moneyBackCheck,
                         final VerbalExpression cashWithdrawal) {
        this.cashWithdrawal = cashWithdrawal;
        this.internalTransferCard = internalTransferCard;
        this.internalFrom = internalFrom;
        this.internalTo = internalTo;
        this.internalTransferComment = internalTransferComment;
        this.internalTransfer = internalTransfer;
        this.moneyBackCheck = moneyBackCheck;
        this.internalTransferAdditionalCheck = internalTransferAdditionalCheck;
    }

    public boolean isCashWithdrawal(final String comment) {
        return cashWithdrawal.test(comment);
    }

    /**
     *
     *[Statement[appcode='895500', trandate=2022-07-05, trantime=15:12:00, amount='10.00 UAH', cardamount='-10.00 UAH', terminal='PrivatBank, CS980400', description='На свою карту']],
     *
     */

    public boolean isInternalTransfer(final String comment) {
        return internalTransfer.test(comment);
    }

    /**
     *[Statement[value='' appcode='208227', trandate=2022-07-05, trantime=10:24:00, amount='428.00 UAH', cardamount='-433.00 UAH', terminal='Prom ua, 31001422', description='Cо своей карты']]
     */

    public boolean isInternalTransferAdditionalCheck(final String terminal) {
        return internalTransferAdditionalCheck.test(terminal);
    }

    public boolean isMoneyBack(final String str) {
        return moneyBackCheck.test(str);
    }

    public boolean isInternalFrom(final String comment) {
        return internalFrom.test(comment);
    }

    public boolean isInternalTo(final String comment) {
        return internalTo.test(comment);
    }

    public String getCardFirstDigits(final String comment) {
        return internalTransferCard.getText(comment, GROUP_1);
    }

    public String getCardLastDigits(final String comment) {
        return internalTransferCard.getText(comment, GROUP_2);
    }

    public String getCardDigits(final String comment) {
        return internalTransferComment.getText(comment, GROUP_1);
    }

    public String normalizeDescription(final String desc) {
        return ofNullable(desc).orElse(EMPTY)
                .replace(QUOT_BANK, Constants.QUOT)
                .replace(APOS_BANK, SPACE)
                .replace(GT_BANK, SPACE)
                .replace(LT_BANK, SPACE)
                .replace(TARGET_BANK, SPACE)
                .trim();
    }
}
