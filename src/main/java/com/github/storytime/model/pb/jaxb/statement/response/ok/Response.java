package com.github.storytime.model.pb.jaxb.statement.response.ok;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "merchant",
        "data"
})
@XmlRootElement(name = "response")
public class Response {

    @XmlElement(required = true)
    protected Response.Merchant merchant;

    @XmlElement(required = true)
    protected Response.Data data;

    @XmlAttribute(name = "version")
    protected Float version;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "oper",
            "info"
    })

    @lombok.Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {

        @XmlElement(required = true)
        protected String oper;

        @XmlElement(required = true)
        protected Response.Data.Info info;


        @lombok.Data
        @Accessors(chain = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "statements"
        })
        public static class Info {

            @XmlElement
            protected Response.Data.Info.Statements statements;

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "statement"
            })
            public static class Statements {

                protected List<Response.Data.Info.Statements.Statement> statement;

                @XmlAttribute(name = "status")
                protected String status;

                @XmlAttribute(name = "credit")
                protected String credit;

                @XmlAttribute(name = "debet")
                protected String debet;

                public List<Response.Data.Info.Statements.Statement> getStatement() {
                    if (statement == null) {
                        statement = new ArrayList<>();
                    }
                    return this.statement;
                }

                @lombok.Data
                @Accessors(chain = true)
                @AllArgsConstructor
                @NoArgsConstructor
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                        "value"
                })
                public static class Statement {

                    @XmlValue
                    protected String value;

                    @XmlAttribute(name = "card")
                    protected String card;

                    @XmlAttribute(name = "appcode")
                    protected String appcode;

                    @XmlAttribute(name = "trandate")
                    @XmlSchemaType(name = "date")
                    protected XMLGregorianCalendar trandate;

                    @XmlAttribute(name = "trantime")
                    @XmlSchemaType(name = "time")
                    protected XMLGregorianCalendar trantime;

                    @XmlAttribute(name = "amount")
                    protected String amount;

                    @XmlAttribute(name = "cardamount")
                    protected String cardamount;

                    @XmlAttribute(name = "rest")
                    protected String rest;

                    @XmlAttribute(name = "terminal")
                    protected String terminal;

                    @XmlAttribute(name = "description")
                    protected String description;

                    @XmlAttribute(name = "customComment")
                    protected String customComment;


                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (!(o instanceof Statement statement)) return false;
                        if (!getValue().equals(statement.getValue())) return false;
                        if (!getCard().equals(statement.getCard())) return false;
                        if (!getAppcode().equals(statement.getAppcode())) return false;
                        if (!getTrandate().equals(statement.getTrandate())) return false;
                        if (!getTrantime().equals(statement.getTrantime())) return false;
                        if (!getAmount().equals(statement.getAmount())) return false;
                        if (!getCardamount().equals(statement.getCardamount())) return false;
                        if (!getRest().equals(statement.getRest())) return false;
                        if (!getTerminal().equals(statement.getTerminal())) return false;
                        return getDescription() != null ? getDescription().equals(statement.getDescription()) : statement.getDescription() == null;
                    }

                    @Override
                    public int hashCode() {
                        int result = getValue().hashCode();
                        result = 31 * result + getCard().hashCode();
                        result = 31 * result + getAppcode().hashCode();
                        result = 31 * result + getTrandate().hashCode();
                        result = 31 * result + getTrantime().hashCode();
                        result = 31 * result + getAmount().hashCode();
                        result = 31 * result + getCardamount().hashCode();
                        result = 31 * result + getRest().hashCode();
                        result = 31 * result + getTerminal().hashCode();
                        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
                        return result;
                    }

                    @Override
                    public String toString() {
                        return new StringJoiner(", ", Statement.class.getSimpleName() + "[", "]")
                                .add("value='" + value + "'")
                                .add("card=" + card)
                                .add("appcode='" + appcode + "'")
                                .add("trandate=" + trandate)
                                .add("trantime=" + trantime)
                                .add("amount='" + amount + "'")
                                .add("cardamount='" + cardamount + "'")
                                .add("rest='" + rest + "'")
                                .add("terminal='" + terminal + "'")
                                .add("description='" + description + "'")
                                .toString();
                    }
                }
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
