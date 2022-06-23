package com.github.storytime.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInfoResponse {

    @NonNull
    private List<SavingsInfo> savings;

    @NonNull
    private String total;

}
