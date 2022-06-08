package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReminderMarkerItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeAccount")
    private String outcomeAccount;

    @JsonProperty("reminder")
    private String reminder;

    @JsonProperty("incomeAccount")
    private String incomeAccount;

    @JsonProperty("merchant")
    private Object merchant;

    @JsonProperty("notify")
    private boolean notify;

    @JsonProperty("payee")
    private Object payee;

    @JsonProperty("outcomeInstrument")
    private int outcomeInstrument;

    @JsonProperty("comment")
    private Object comment;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("tag")
    private List<String> tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("incomeInstrument")
    private int incomeInstrument;

    @JsonProperty("outcome")
    private int outcome;

    @JsonProperty("changed")
    private int changed;

    @Override
    public String toString() {
        return
                "ReminderMarkerItem{" +
                        "date = '" + date + '\'' +
                        ",income = '" + income + '\'' +
                        ",outcomeAccount = '" + outcomeAccount + '\'' +
                        ",reminder = '" + reminder + '\'' +
                        ",incomeAccount = '" + incomeAccount + '\'' +
                        ",merchant = '" + merchant + '\'' +
                        ",notify = '" + notify + '\'' +
                        ",payee = '" + payee + '\'' +
                        ",outcomeInstrument = '" + outcomeInstrument + '\'' +
                        ",comment = '" + comment + '\'' +
                        ",id = '" + id + '\'' +
                        ",state = '" + state + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",incomeInstrument = '" + incomeInstrument + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}