package com.github.storytime.mapper.response;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.PbAccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PbAccountBalanceResponseMapper {

    private final PbStatementResponseMapper pbStatementResponseMapper;

    @Autowired
    public PbAccountBalanceResponseMapper(final PbStatementResponseMapper pbStatementResponseMapper) {
        this.pbStatementResponseMapper = pbStatementResponseMapper;
    }

    public PbAccountBalance mapResponse(final MerchantInfo m, final ResponseEntity<String> body) {
        final BigDecimal bigDecimal = pbStatementResponseMapper.mapAccountRequestBody(body);
        return new PbAccountBalance(m.getShortDesc(), bigDecimal);
    }
}
