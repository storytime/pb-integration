package com.github.storytime.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class CustomConfig {

    @Value("${pb.transactions.url}")
    private String pbTransactionsUrl;

    @Value("${pb.account.url}")
    private String pbAccountsUrl;

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

    @Value("${scheduler.executor.core.pool.size}")
    private Integer schedulerCorePoolSize;

    @Value("${scheduler.executor.thread.prefix}")
    private String schedulerThreadPrefix;

    @Value("${verbal.regexp.pb.comment.separator}")
    private String pbCommentSeparator;

    @Value("${verbal.regexp.pb.cash.withdrawal}")
    private String pbCashWithdrawal;

    @Value("${verbal.regexp.pb.cash.withdrawal.new}")
    private String pbCashWithdrawalNew;

    @Value("${verbal.regexp.pb.cash.withdrawal.cash.machine}")
    private String pbCashWithdrawalCashMachine;

    @Value("${verbal.regexp.pb.transfer.internal.to}")
    private String pbInternalTransferTo;

    @Value("${verbal.regexp.pb.transfer.internal.to.new}")
    private String pbInternalTransferNew;

    @Value("${verbal.regexp.pb.transfer.internal.from}")
    private String pbInternalTransferFrom;

    @Value("${verbal.regexp.pb.transfer.internal.from.new}")
    private String pbInternalTransferFromNew;

    @Value("${verbal.regexp.pb.transfer.internal.from.special}")
    private String pbInternalTransferFromSpecial;

    @Value("${verbal.regexp.pb.transfer.terminal}")
    private String transferCheckByTerminal;

    @Value("${verbal.regexp.pb.money.back}")
    private String moneyBack;

    @Value("${verbal.regexp.pb.transfer.digit.separator}")
    private String pbInternalTransferSeparator;

    @Value("${pushed.pb.zen.transaction.storage.clean.older.millis}")
    private Integer pushedPbZenTransactionStorageCleanOlderMillis;

    @Value("${pb.invalid.signature.rollback.period.hours}")
    private Integer pbRollBackPeriod;

    @Value("${cf.executor.core.pool.size}")
    private Integer cfCorePoolSize;

    @Value("${cf.executor.max.pool.size}")
    private Integer cfMaxPoolSize;

    @Value("${cf.executor.thread.prefix}")
    private String cfThreadPrefix;

    @Value("${pb.invalid.signature.max.rollback.period.ms}")
    private Long maxRollbackPeriod;
}
