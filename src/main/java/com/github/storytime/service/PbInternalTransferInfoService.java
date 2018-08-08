package com.github.storytime.service;

import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.repository.PbInternalTransferInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.storytime.config.props.Constants.CARD_TWO_DIGITS;
import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.right;

@Component
public class PbInternalTransferInfoService {

    private final PbInternalTransferInfoRepository pbInternalTransferInfoRepository;
    private final RegExpService regExpService;

    @Autowired
    public PbInternalTransferInfoService(final PbInternalTransferInfoRepository pbInternalTransferInfoRepository,
                                         final RegExpService regExpService) {
        this.pbInternalTransferInfoRepository = pbInternalTransferInfoRepository;
        this.regExpService = regExpService;
    }

    public String generateIdForFromTransfer(final User u, final Statement cardNum, final Float opAmount, final String comment) {
        final String card = String.valueOf(cardNum.getCard());
        return u.getId() +
                left(card, CARD_TWO_DIGITS) +
                right(card, CARD_TWO_DIGITS) +
                regExpService.getCardFirstDigits(comment) +
                regExpService.getCardLastDigits(comment) +
                opAmount +
                cardNum.getTrandate();

    }

    public String generateIdForToTransfer(final User u, final Statement cardNum, final Float opAmount, final String comment) {
        final String card = String.valueOf(cardNum.getCard());
        return u.getId() +
                regExpService.getCardFirstDigits(comment) +
                regExpService.getCardLastDigits(comment) +
                left(card, CARD_TWO_DIGITS) +
                right(card, CARD_TWO_DIGITS) +
                opAmount +
                cardNum.getTrandate();
    }

    public void save(final String id) {
        pbInternalTransferInfoRepository.save(id);
    }

    public boolean isAlreadyHandled(final String id) {
        return pbInternalTransferInfoRepository.isExist(id);
    }

}
