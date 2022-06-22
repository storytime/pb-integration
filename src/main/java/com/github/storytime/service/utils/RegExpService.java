package com.github.storytime.service.utils;

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

    @Autowired
    public RegExpService(final VerbalExpression internalTransfer,
                         final VerbalExpression internalTransferCard,
                         final VerbalExpression internalFrom,
                         final VerbalExpression internalTo,
                         final VerbalExpression internalTransferComment,
                         final VerbalExpression cashWithdrawal) {
        this.cashWithdrawal = cashWithdrawal;
        this.internalTransferCard = internalTransferCard;
        this.internalFrom = internalFrom;
        this.internalTo = internalTo;
        this.internalTransferComment = internalTransferComment;
        this.internalTransfer = internalTransfer;
    }

    public boolean isCashWithdrawal(String comment) {
        return cashWithdrawal.test(comment);
    }

    public boolean isInternalTransfer(String comment) {
        return internalTransfer.test(comment);
    }

    public boolean isInternalFrom(String comment) {
        return internalFrom.test(comment);
    }

    public boolean isInternalTo(String comment) {
        return internalTo.test(comment);
    }

    public String getCardFirstDigits(String comment) {
        return internalTransferCard.getText(comment, GROUP_1);
    }

    public String getCardLastDigits(String comment) {
        return internalTransferCard.getText(comment, GROUP_2);
    }

    public String getCardDigits(String comment) {
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
