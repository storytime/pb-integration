package com.github.storytime.model.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInfo {

    @NonNull
    private BigDecimal balance;

    @NonNull
    private String balanceStr;

    @NonNull
    private String currencySymbol;

    @NonNull
    private BigDecimal inUah;

    @NonNull
    private String inUahStr;

    @NonNull
    private String title;

    @NonNull
    private BigDecimal percent;
}
