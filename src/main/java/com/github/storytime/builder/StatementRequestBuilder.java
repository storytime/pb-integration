package com.github.storytime.builder;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.statement.request.Request;
import com.github.storytime.service.SignatureGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.storytime.config.props.Constants.*;

@Service
public class StatementRequestBuilder {

    private final SignatureGeneratorService signatureGeneratorService;

    @Autowired
    public StatementRequestBuilder(final SignatureGeneratorService signatureGeneratorService) {
        this.signatureGeneratorService = signatureGeneratorService;
    }

    private void buildDataPaymentProperties(final List<Request.Data.Payment.Prop> prop,
                                            final String sd, final String en,
                                            final String cardNumber) {

        final Request.Data.Payment.Prop startDate = new Request.Data.Payment.Prop();
        startDate.setName(START_DATE);
        startDate.setValue(sd);
        prop.add(startDate);

        final Request.Data.Payment.Prop endDate = new Request.Data.Payment.Prop();
        endDate.setName(END_DATE);
        endDate.setValue(en);
        prop.add(endDate);

        final Request.Data.Payment.Prop card = new Request.Data.Payment.Prop();
        card.setName(CARD);
        card.setValue(cardNumber);
        prop.add(card);
    }

    private Request.Data buildRequestData() {
        final Request.Data requestData = new Request.Data();
        requestData.setOper(CMT);
        requestData.setWait(PB_WAIT);
        requestData.setTest(TEST);
        return requestData;
    }

    private Request.Data.Payment buildDataPayment() {
        final Request.Data.Payment payment = new Request.Data.Payment();
        payment.setId(EMPTY);
        return payment;
    }

    private Request.Merchant buildMerchant(final Integer id, final String signature) {
        final Request.Merchant merchant = new Request.Merchant();
        merchant.setId(id);
        merchant.setSignature(signature);
        return merchant;
    }

    private Request buildRequestRoot(final Request.Merchant merchant, final Request.Data data) {
        final Request request = new Request();
        request.setVersion(XML_VERSION);
        request.setMerchant(merchant);
        request.setData(data);
        return request;
    }

    public Request buildStatementRequest(final MerchantInfo m,
                                         final String startDate,
                                         final String endDate) {
        final Integer merchantId = m.getMerchantId();
        final String password = m.getPassword();
        final String card = m.getCardNumber();

        final Request.Data.Payment payment = buildDataPayment();
        buildDataPaymentProperties(payment.getProp(), startDate, endDate, card);
        final Request.Data data = buildRequestData();
        data.setPayment(payment);

        final String signature = signatureGeneratorService.generateSignature(startDate, endDate, card, password);
        final Request.Merchant merchant = buildMerchant(merchantId, signature);

        return buildRequestRoot(merchant, data);
    }

}
