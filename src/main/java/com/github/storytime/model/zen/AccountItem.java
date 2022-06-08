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
public class AccountItem {

    @JsonProperty("private")
    private boolean jsonMemberPrivate;

    @JsonProperty("role")
    private Object role;

    @JsonProperty("payoffInterval")
    private Object payoffInterval;

    @JsonProperty("instrument")
    private int instrument;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("percent")
    private Object percent;

    @JsonProperty("enableSMS")
    private boolean enableSMS;

    @JsonProperty("balance")
    private double balance;

    @JsonProperty("payoffStep")
    private Object payoffStep;

    @JsonProperty("creditLimit")
    private double creditLimit;

    @JsonProperty("company")
    private Integer company;

    @JsonProperty("endDateOffset")
    private Object endDateOffset;

    @JsonProperty("id")
    private String id;

    @JsonProperty("savings")
    private Boolean savings;

    @JsonProperty("startBalance")
    private double startBalance;

    @JsonProperty("inBalance")
    private boolean inBalance;

    @JsonProperty("enableCorrection")
    private boolean enableCorrection;

    @JsonProperty("archive")
    private boolean archive;

    @JsonProperty("syncID")
    private List<String> syncID;

    @JsonProperty("capitalization")
    private Object capitalization;

    @JsonProperty("endDateOffsetInterval")
    private Object endDateOffsetInterval;

    @JsonProperty("user")
    private int user;

    @JsonProperty("startDate")
    private Object startDate;

    @JsonProperty("changed")
    private long changed;

    @Override
    public String toString() {
        return
                "AccountItem{" +
                        "private = '" + jsonMemberPrivate + '\'' +
                        ",role = '" + role + '\'' +
                        ",payoffInterval = '" + payoffInterval + '\'' +
                        ",instrument = '" + instrument + '\'' +
                        ",type = '" + type + '\'' +
                        ",title = '" + title + '\'' +
                        ",percent = '" + percent + '\'' +
                        ",enableSMS = '" + enableSMS + '\'' +
                        ",balance = '" + balance + '\'' +
                        ",payoffStep = '" + payoffStep + '\'' +
                        ",creditLimit = '" + creditLimit + '\'' +
                        ",company = '" + company + '\'' +
                        ",endDateOffset = '" + endDateOffset + '\'' +
                        ",id = '" + id + '\'' +
                        ",savings = '" + savings + '\'' +
                        ",startBalance = '" + startBalance + '\'' +
                        ",inBalance = '" + inBalance + '\'' +
                        ",enableCorrection = '" + enableCorrection + '\'' +
                        ",archive = '" + archive + '\'' +
                        ",syncID = '" + syncID + '\'' +
                        ",capitalization = '" + capitalization + '\'' +
                        ",endDateOffsetInterval = '" + endDateOffsetInterval + '\'' +
                        ",user = '" + user + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}