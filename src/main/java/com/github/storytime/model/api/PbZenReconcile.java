package com.github.storytime.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PbZenReconcile {
    @NonNull
    private String accountName;

    @NonNull
    private String bankAmount;

    @NonNull
    private String zenAmount;

    @NonNull
    private String diff;
}
