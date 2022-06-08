package com.github.storytime.model.pb.jaxb.statement.response.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.*;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "data"
})
@XmlRootElement(name = "response")
public class Response {

    @XmlElement(required = true)
    protected Response.Data data;

    @XmlAttribute(name = "version")
    protected Float version;

    @lombok.Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "error"
    })
    public static class Data {

        @XmlElement(required = true)
        protected Response.Data.Error error;

        @lombok.Data
        @Accessors(chain = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "value"
        })
        public static class Error {

            @XmlValue
            protected String value;

            @XmlAttribute(name = "message")
            protected String message;

        }
    }
}
