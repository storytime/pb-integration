package com.github.storytime.builder;

import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.pb.jaxb.request.Request;
import com.github.storytime.model.pb.jaxb.request.Request.Data;
import com.github.storytime.model.pb.jaxb.request.Request.Data.Payment;
import com.github.storytime.model.pb.jaxb.request.Request.Data.Payment.Prop;
import com.github.storytime.model.pb.jaxb.request.Request.Merchant;
import com.github.storytime.service.utils.PbSignatureGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.storytime.config.props.Constants.*;
import static java.util.List.of;

@Service
public class PbRequestBuilder {

    private final PbSignatureGeneratorService pbSignatureGeneratorService;

    @Autowired
    public PbRequestBuilder(final PbSignatureGeneratorService pbSignatureGeneratorService) {
        this.pbSignatureGeneratorService = pbSignatureGeneratorService;
    }

    private Data buildRequestData(Payment payment) {
        return Data.builder().oper(CMT).wait(PB_WAIT).payment(payment).test(TEST).build();
    }

    private Payment buildDataPayment(List<Prop> props) {
        return Payment.builder().id(EMPTY).prop(props).build();
    }

    private Merchant buildMerchant(final Integer id, final String signature) {
        return Merchant.builder().signature(signature).id(id).build();
    }

    private List<Prop> buildAccountDataPaymentPropertiesForStatements(final String sd, final String en, final String cardNumber) {
        final Prop startProp = Prop.builder().name(START_DATE).value(sd).build();
        final Prop endProp = Prop.builder().name(END_DATE).value(en).build();
        final Prop cardProp = Prop.builder().name(CARD).value(cardNumber).build();
        return of(startProp, endProp, cardProp);
    }

    private List<Prop> buildAccountDataPaymentPropertiesForBalance(final String cardNumber) {
        final Prop cardNum = Prop.builder().name(CARDNUM).value(cardNumber).build();
        final Prop card = Prop.builder().name(COUNTRY).value(UA).build();
        return of(cardNum, card);
    }

    public Request buildAccountRequest(final PbMerchant pbMerchant) {
        final Integer merchantId = pbMerchant.getMerchantId();
        final String password = pbMerchant.getPassword();
        final String card = pbMerchant.getCardNumber();

        final List<Prop> props = buildAccountDataPaymentPropertiesForBalance(card);
        final Payment payment = buildDataPayment(props);
        final Data data = buildRequestData(payment);

        final String signature = pbSignatureGeneratorService.generateAccountSignature(card, password);
        final Merchant merchant = buildMerchant(merchantId, signature);

        return Request.builder().version(XML_VERSION).merchant(merchant).data(data).build();
    }

    public Request buildStatementRequest(final PbMerchant pbMerchant,
                                         final String startDate,
                                         final String endDate) {

        final Integer merchantId = pbMerchant.getMerchantId();
        final String password = pbMerchant.getPassword();
        final String card = pbMerchant.getCardNumber();

        final List<Prop> props = buildAccountDataPaymentPropertiesForStatements(startDate, endDate, card);
        final Payment payment = buildDataPayment(props);
        final Data data = buildRequestData(payment);

        final String signature = pbSignatureGeneratorService.generateStatementSignature(startDate, endDate, card, password);
        final Merchant merchant = buildMerchant(merchantId, signature);
        return Request.builder().version(XML_VERSION).merchant(merchant).data(data).build();
    }
}
