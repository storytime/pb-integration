package com.github.storytime.model.pb.jaxb.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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

    @lombok.Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
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


        @lombok.Data
        @Accessors(chain = true)
        @AllArgsConstructor
        @NoArgsConstructor
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

            @lombok.Data
            @Accessors(chain = true)
            @AllArgsConstructor
            @NoArgsConstructor
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Prop {

                @XmlAttribute(name = "name")
                protected String name;

                @XmlAttribute(name = "value")
                protected String value;
            }
        }
    }

    @lombok.Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "id",
            "signature"
    })
    public static class Merchant {

        protected int id;
        @XmlElement(required = true)

        protected String signature;
    }
}
