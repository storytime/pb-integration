package com.github.storytime.function;

import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;

@FunctionalInterface
public interface CurrencyCommentFunction {
    void generate(StringBuilder c, CurrencyRates r, Statement s, String before, String after);
}