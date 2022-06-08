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
public class ReminderItem {

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeAccount")
    private String outcomeAccount;

    @JsonProperty("endDate")
    private Object endDate;

    @JsonProperty("incomeAccount")
    private String incomeAccount;

    @JsonProperty("merchant")
    private Object merchant;

    @JsonProperty("notify")
    private boolean notify;

    @JsonProperty("points")
    private List<Integer> points;

    @JsonProperty("payee")
    private Object payee;

    @JsonProperty("outcomeInstrument")
    private int outcomeInstrument;

    @JsonProperty("step")
    private int step;

    @JsonProperty("interval")
    private Object interval;

    @JsonProperty("comment")
    private Object comment;

    @JsonProperty("id")
    private String id;

    @JsonProperty("tag")
    private List<String> tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("incomeInstrument")
    private int incomeInstrument;

    @JsonProperty("outcome")
    private int outcome;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("changed")
    private int changed;


    @Override
    public String toString() {
        return
                "ReminderItem{" +
                        "income = '" + income + '\'' +
                        ",outcomeAccount = '" + outcomeAccount + '\'' +
                        ",endDate = '" + endDate + '\'' +
                        ",incomeAccount = '" + incomeAccount + '\'' +
                        ",merchant = '" + merchant + '\'' +
                        ",notify = '" + notify + '\'' +
                        ",points = '" + points + '\'' +
                        ",payee = '" + payee + '\'' +
                        ",outcomeInstrument = '" + outcomeInstrument + '\'' +
                        ",step = '" + step + '\'' +
                        ",interval = '" + interval + '\'' +
                        ",comment = '" + comment + '\'' +
                        ",id = '" + id + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",incomeInstrument = '" + incomeInstrument + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}