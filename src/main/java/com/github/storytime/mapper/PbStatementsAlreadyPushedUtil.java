package com.github.storytime.mapper;

import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;

public class PbStatementsAlreadyPushedUtil {

    private PbStatementsAlreadyPushedUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateUniqString(final Statement pbSt) {
        final var terminal = pbSt.getTerminal();
        final var cardAmount = pbSt.getAmount();
        final var amount = pbSt.getAmount();
        return terminal + cardAmount + amount;
    }
}
