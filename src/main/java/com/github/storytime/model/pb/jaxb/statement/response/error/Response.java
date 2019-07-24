package com.github.storytime.model.pb.jaxb.statement.response.error;

import javax.xml.bind.annotation.*;

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


    public Response.Data getData() {
        return data;
    }

    public void setData(Response.Data value) {
        this.data = value;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float value) {
        this.version = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "error"
    })
    public static class Data {

        @XmlElement(required = true)
        protected Response.Data.Error error;

        public Response.Data.Error getError() {
            return error;
        }

        public void setError(Response.Data.Error value) {
            this.error = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "value"
        })
        public static class Error {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "message")
            protected String message;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String value) {
                this.message = value;
            }

        }
    }
}
