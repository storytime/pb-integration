package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class ZenResponse {

    @JsonProperty("country")
    private List<CountryItem> country;

    @JsonProperty("reminderMarker")
    private List<ReminderMarkerItem> reminderMarker;

    @JsonProperty("reminder")
    private List<ReminderItem> reminder;

    @JsonProperty("serverTimestamp")
    private int serverTimestamp;

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

    public List<CountryItem> getCountry() {
        return country;
    }

    public void setCountry(List<CountryItem> country) {
        this.country = country;
    }

    public List<ReminderMarkerItem> getReminderMarker() {
        return reminderMarker;
    }

    public void setReminderMarker(List<ReminderMarkerItem> reminderMarker) {
        this.reminderMarker = reminderMarker;
    }

    public List<ReminderItem> getReminder() {
        return reminder;
    }

    public void setReminder(List<ReminderItem> reminder) {
        this.reminder = reminder;
    }

    public int getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(int serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public List<MerchantItem> getMerchant() {
        return merchant;
    }

    public void setMerchant(List<MerchantItem> merchant) {
        this.merchant = merchant;
    }

    public List<InstrumentItem> getInstrument() {
        return instrument;
    }

    public void setInstrument(List<InstrumentItem> instrument) {
        this.instrument = instrument;
    }

    public List<CompanyItem> getCompany() {
        return company;
    }

    public void setCompany(List<CompanyItem> company) {
        this.company = company;
    }

    public List<TagItem> getTag() {
        return tag;
    }

    public void setTag(List<TagItem> tag) {
        this.tag = tag;
    }

    public List<UserItem> getUser() {
        return user;
    }

    public void setUser(List<UserItem> user) {
        this.user = user;
    }

    public List<AccountItem> getAccount() {
        return account;
    }

    public void setAccount(List<AccountItem> account) {
        this.account = account;
    }

    public List<TransactionItem> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<TransactionItem> transaction) {
        this.transaction = transaction;
    }

    public List<BudgetItem> getBudget() {
        return budget;
    }

    public void setBudget(List<BudgetItem> budget) {
        this.budget = budget;
    }

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