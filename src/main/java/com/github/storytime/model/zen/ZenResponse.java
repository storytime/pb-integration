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
public class ZenResponse {

    @JsonProperty("country")
    private List<CountryItem> country;

    @JsonProperty("reminderMarker")
    private List<ReminderMarkerItem> reminderMarker;

    @JsonProperty("reminder")
    private List<ReminderItem> reminder;

    @JsonProperty("serverTimestamp")
    private long serverTimestamp;

    @JsonProperty("merchant")
    private List<MerchantItem> merchant;

    @JsonProperty("instrument")
    private List<InstrumentItem> instrument;

    @JsonProperty("company")
    private List<CompanyItem> company;

    @JsonProperty("tag")
    private List<TagItem> tag;

    @JsonProperty("user")
    private List<UserItem> user;

    @JsonProperty("account")
    private List<AccountItem> account;

    @JsonProperty("transaction")
    private List<TransactionItem> transaction;

    @JsonProperty("budget")
    private List<BudgetItem> budget;

    @Override
    public String toString() {
        return
                "ZenResponse{" +
                        "country = '" + country + '\'' +
                        ",reminderMarker = '" + reminderMarker + '\'' +
                        ",reminder = '" + reminder + '\'' +
                        ",serverTimestamp = '" + serverTimestamp + '\'' +
                        ",merchant = '" + merchant + '\'' +
                        ",instrument = '" + instrument + '\'' +
                        ",company = '" + company + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",account = '" + account + '\'' +
                        ",transaction = '" + transaction + '\'' +
                        ",budget = '" + budget + '\'' +
                        "}";
    }
}