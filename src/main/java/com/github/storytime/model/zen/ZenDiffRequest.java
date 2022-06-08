package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZenDiffRequest {

    private Long currentClientTimestamp;
    private long lastServerTimestamp;
    private List<AccountItem> account;
    private List<TransactionItem> transaction;
}
