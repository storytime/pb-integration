package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class BudgetItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeLock")
    private boolean outcomeLock;

    @JsonProperty("incomeLock")
    private boolean incomeLock;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("outcome")
    private int outcome;

    @JsonProperty("changed")
    private int changed;


    @Override
    public String toString() {
        return
                "BudgetItem{" +
                        "date = '" + date + '\'' +
                        ",income = '" + income + '\'' +
                        ",outcomeLock = '" + outcomeLock + '\'' +
                        ",incomeLock = '" + incomeLock + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}