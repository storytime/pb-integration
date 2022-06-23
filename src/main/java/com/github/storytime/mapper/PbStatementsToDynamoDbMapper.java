package com.github.storytime.mapper;

import com.github.storytime.model.pb.jaxb.statement.response.ok.Response;

public class PbStatementsToDynamoDbMapper {

    private PbStatementsToDynamoDbMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateUniqString(Response.Data.Info.Statements.Statement pbSt) {
        return pbSt.getAppcode() + pbSt.getTerminal() + pbSt.getCardamount() + pbSt.getAmount();
    }
}
