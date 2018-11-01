package com.github.storytime.model.jaxb.statement.response.ok;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public Response.Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Response.Merchant value) {
        this.merchant = value;
    }

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
            "oper",
            "info"
    })
    public static class Data {

        @XmlElement(required = true)
        protected String oper;
        @XmlElement(required = true)
        protected Response.Data.Info info;

        public String getOper() {
            return oper;
        }


        public void setOper(String value) {
            this.oper = value;
        }

        public Response.Data.Info getInfo() {
            return info;
        }

        public void setInfo(Response.Data.Info value) {
            this.info = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "statements"
        })
        public static class Info {

            @XmlElement(required = true)
            protected Response.Data.Info.Statements statements;

            public Response.Data.Info.Statements getStatements() {
                return statements;
            }

            public void setStatements(Response.Data.Info.Statements value) {
                this.statements = value;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "statement"
            })
            public static class Statements {

                protected List<Response.Data.Info.Statements.Statement> statement;
                @XmlAttribute(name = "status")
                protected String status;
                @XmlAttribute(name = "credit")
                protected Float credit;
                @XmlAttribute(name = "debet")
                protected Float debet;

                public List<Response.Data.Info.Statements.Statement> getStatement() {
                    if (statement == null) {
                        statement = new ArrayList<>();
                    }
                    return this.statement;
                }

                public String getStatus() {
                    return status;
                }

                public void setStatus(String value) {
                    this.status = value;
                }

                public Float getCredit() {
                    return credit;
                }

                public void setCredit(Float value) {
                    this.credit = value;
                }

                public Float getDebet() {
                    return debet;
                }

                public void setDebet(Float value) {
                    this.debet = value;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                        "value"
                })
                public static class Statement {

                    @XmlValue
                    protected String value;
                    @XmlAttribute(name = "card")
                    protected Long card;
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

                    @XmlAttribute(name = "description", required = false)
                    protected String customComment;

                    public String getCustomComment() {
                        return customComment;
                    }

                    public Statement setCustomComment(String customComment) {
                        this.customComment = customComment;
                        return this;
                    }

                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }

                    public Long getCard() {
                        return card;
                    }

                    public void setCard(Long value) {
                        this.card = value;
                    }

                    public String getAppcode() {
                        return appcode;
                    }

                    public void setAppcode(String value) {
                        this.appcode = value;
                    }

                    public XMLGregorianCalendar getTrandate() {
                        return trandate;
                    }

                    public void setTrandate(XMLGregorianCalendar value) {
                        this.trandate = value;
                    }

                    public XMLGregorianCalendar getTrantime() {
                        return trantime;
                    }

                    public void setTrantime(XMLGregorianCalendar value) {
                        this.trantime = value;
                    }

                    public String getAmount() {
                        return amount;
                    }

                    public void setAmount(String value) {
                        this.amount = value;
                    }

                    public String getCardamount() {
                        return cardamount;
                    }

                    public void setCardamount(String value) {
                        this.cardamount = value;
                    }

                    public String getRest() {
                        return rest;
                    }

                    public void setRest(String value) {
                        this.rest = value;
                    }

                    public String getTerminal() {
                        return terminal;
                    }

                    public void setTerminal(String value) {
                        this.terminal = value;
                    }

                    public String getDescription() {
                        return description;
                    }

                    public void setDescription(String value) {
                        this.description = value;
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(getCard(), getAppcode(), getTrandate(), getTrantime(), getAmount(),
                                getCardamount(), getRest(), getTerminal(), getDescription(), getCustomComment());
                    }

                    @Override
                    public String toString() {
                        return ("card = " + card) +
                                "appcode = " + appcode +
                                "trandate = " + trandate +
                                "trantime = " + trantime +
                                "amount = " + amount +
                                "cardamount = " + cardamount +
                                "rest = " + rest +
                                "terminal = " + terminal +
                                "description = " + description +
                                "customComment = " + customComment;
                    }
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
