package com.github.storytime.model.jaxb.history.request;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "merchant",
        "data"
})
@XmlRootElement(name = "request")
public class Request {

    @XmlElement(required = true)
    protected Request.Merchant merchant;
    @XmlElement(required = true)
    protected Request.Data data;
    @XmlAttribute(name = "version")
    protected Integer version;

    public Request.Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Request.Merchant value) {
        this.merchant = value;
    }

    public Request.Data getData() {
        return data;
    }

    public void setData(Request.Data value) {
        this.data = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer value) {
        this.version = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "oper",
            "wait",
            "test",
            "payment"
    })
    public static class Data {

        @XmlElement(required = true)
        protected String oper;
        protected int wait;
        protected int test;
        @XmlElement(required = true)
        protected Request.Data.Payment payment;


        public String getOper() {
            return oper;
        }

        public void setOper(String value) {
            this.oper = value;
        }

        public int getWait() {
            return wait;
        }

        public void setWait(int value) {
            this.wait = value;
        }

        public int getTest() {
            return test;
        }

        public void setTest(int value) {
            this.test = value;
        }

        public Request.Data.Payment getPayment() {
            return payment;
        }

        public void setPayment(Request.Data.Payment value) {
            this.payment = value;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "prop"
        })
        public static class Payment {

            @XmlElement(required = true)
            protected List<Request.Data.Payment.Prop> prop;
            @XmlAttribute(name = "id")
            protected String id;

            public List<Request.Data.Payment.Prop> getProp() {
                if (prop == null) {
                    prop = new ArrayList<>();
                }
                return this.prop;
            }

            public String getId() {
                return id;
            }

            public void setId(String value) {
                this.id = value;
            }


            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Prop {

                @XmlAttribute(name = "name")
                protected String name;
                @XmlAttribute(name = "value")
                protected String value;

                public String getName() {
                    return name;
                }

                public void setName(String value) {
                    this.name = value;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "id",
            "signature"
    })
    public static class Merchant {

        protected int id;
        @XmlElement(required = true)
        protected String signature;

        public int getId() {
            return id;
        }

        public void setId(int value) {
            this.id = value;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String value) {
            this.signature = value;
        }
    }
}
