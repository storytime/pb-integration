package com.github.storytime.model.pb.jaxb.request;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public Request createRequest() {
        return new Request();
    }

    public Request.Data createRequestData() {
        return new Request.Data();
    }

    public Request.Data.Payment createRequestDataPayment() {
        return new Request.Data.Payment();
    }

    public Request.Merchant createRequestMerchant() {
        return new Request.Merchant();
    }

    public Request.Data.Payment.Prop createRequestDataPaymentProp() {
        return new Request.Data.Payment.Prop();
    }

}
