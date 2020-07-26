package com.github.storytime.mapper.response;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.PbAccountBalance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PbAccountBalanceResponseMapper {

    public PbAccountBalance buildSimpleObject(final BigDecimal bal, final MerchantInfo m) {
        return new PbAccountBalance(m.getShortDesc(), bal);
    }
}
