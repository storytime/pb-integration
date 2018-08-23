package com.github.storytime.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:custom.properties", encoding = "UTF-8")
public class CustomConfig {

    @Value("${pb.exchange.url}")
    private String pbExchangeUrl;

    @Value("${minfin.exchange.url}")
    private String minExchangeUrl;

    @Value("${minfin.access.token}")
    private String minfinToken;

    @Value("${pb.transactions.url}")
    private String pbTransactionsUrl;

    @Value("${zen.diff.url}")
    private String zenDiffUrl;

    @Value("${pb.cash.url}")
    private String pbCashUrl;

    @Value("${filter.new.transactions.start.time.millis}")
    private Integer filterTimeMillis;

    @Value("${pb.bank.signature.error}")
    private String pbBankSignature;

    @Value("${async.executor.core.pool.size}")
    private Integer asyncCorePoolSize;

    @Value("${async.executor.max.pool.size}")
    private Integer asyncMaxPoolSize;

    @Value("${async.executor.thread.prefix}")
    private String asyncThreadPrefix;

    @Value("${verbal.regexp.pb.comment.separator}")
    private String pbCommentSeparator;

    @Value("${verbal.regexp.pb.cash.withdrawal}")
    private String pbCashWithdrawal;

    @Value("${verbal.regexp.pb.transfer.internal.to}")
    private String pbInternalTransferTo;

    @Value("${verbal.regexp.pb.transfer.internal.from}")
    private String pbInternalTransferFrom;

    @Value("${verbal.regexp.pb.transfer.digit.separator}")
    private String pbInternalTransferSeparator;

    @Value("${pushed.pb.zen.transaction.storage.clean.older.millis}")
    private Integer pushedPbZenTransactionStorageCleanOlderMillis;

    public String getPbExchangeUrl() {
        return pbExchangeUrl;
    }

    public String getMinExchangeUrl() {
        return minExchangeUrl + minfinToken + "/";
    }

    public String getPbTransactionsUrl() {
        return pbTransactionsUrl;
    }

    public String getZenDiffUrl() {
        return zenDiffUrl;
    }

    public Integer getFilterTimeMillis() {
        return filterTimeMillis;
    }

    public String getPbBankSignature() {
        return pbBankSignature;
    }

    public Integer getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    public Integer getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    public String getAsyncThreadPrefix() {
        return asyncThreadPrefix;
    }


    public String getPbCommentSeparator() {
        return pbCommentSeparator.trim();
    }

    public String getPbCashWithdrawal() {
        return pbCashWithdrawal.trim();
    }

    public String getPbInternalTransferTo() {
        return pbInternalTransferTo.trim();
    }

    public String getPbInternalTransferFrom() {
        return pbInternalTransferFrom.trim();
    }

    public String getPbInternalTransferSeparator() {
        return pbInternalTransferSeparator.trim();
    }

    public String getPbCashUrl() {
        return pbCashUrl;
    }

    public Integer getPushedPbZenTransactionStorageCleanOlderMillis() {
        return pushedPbZenTransactionStorageCleanOlderMillis;
    }
}
