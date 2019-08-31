package com.github.storytime.model.pb.jaxb.statement.response.ok;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public Response createResponse() {
        return new Response();
    }

    public Response.Data createResponseData() {
        return new Response.Data();
    }

    public Response.Data.Info createResponseDataInfo() {
        return new Response.Data.Info();
    }

    public Response.Data.Info.Statements createResponseDataInfoStatements() {
        return new Response.Data.Info.Statements();
    }

    public Response.Merchant createResponseMerchant() {
        return new Response.Merchant();
    }

    public Response.Data.Info.Statements.Statement createResponseDataInfoStatementsStatement() {
        return new Response.Data.Info.Statements.Statement();
    }

}
