package com.github.storytime.model.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInfo {

    private BigDecimal balance;

    private String balanceStr;

    private String currencySymbol;

    private BigDecimal inUah;

    private String inUahStr;

    private String title;

    private BigDecimal percent;
}
