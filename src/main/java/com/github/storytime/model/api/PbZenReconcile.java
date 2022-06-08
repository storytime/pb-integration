package com.github.storytime.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PbZenReconcile {
    private String accountName;
    private String bankAmount;
    private String zenAmount;
    private String diff;
}
