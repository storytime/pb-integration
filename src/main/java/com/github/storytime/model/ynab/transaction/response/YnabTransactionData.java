package com.github.storytime.model.ynab.transaction.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class YnabTransactionData {

    @JsonProperty("transaction_ids")
    private List<String> transactionIds;

    @JsonProperty("transactions")
    private List<YnabTransactions> transactions;

    @JsonProperty("duplicate_import_ids")
    private List<String> duplicateImportIds;

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }

    public List<YnabTransactions> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<YnabTransactions> transactions) {
        this.transactions = transactions;
    }

    public List<String> getDuplicateImportIds() {
        return duplicateImportIds;
    }

    public void setDuplicateImportIds(List<String> duplicateImportIds) {
        this.duplicateImportIds = duplicateImportIds;
    }

    @Override
    public String toString() {
        return
                "YnabAccountData{" +
                        "transaction_ids = '" + transactionIds + '\'' +
                        ",transactions = '" + transactions + '\'' +
                        ",duplicate_import_ids = '" + duplicateImportIds + '\'' +
                        "}";
    }
}