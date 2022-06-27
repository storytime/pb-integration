package com.github.storytime.model.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInfoResponse {

    @NonNull
    private List<SavingsInfo> savings;

    @NonNull
    private String total;

}
