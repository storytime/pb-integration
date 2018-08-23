package com.github.storytime.service;

import com.github.storytime.BaseTestConfig;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.ExpiredTransactionItem;
import com.github.storytime.model.zen.TransactionItem;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

public class PushedPbZenTransactionStorageServiceTest extends BaseTestConfig {

    public static final long TRANSACTION_ITEM_TIME1 = 1534971600000L;
    public static final long TRANSACTION_ITEM_TIME = 1532293200000L;
    public static final int EXPECTED = 2;

    @Autowired
    private PushedPbZenTransactionStorageService pushedPbZenTransactionStorageService;

    @Autowired
    private Set<ExpiredTransactionItem> pushedPbZenTransactionStorage;

    @Autowired
    private CustomConfig customConfig;

    @Before
    public void init() {
        pushedPbZenTransactionStorage
                .add(new ExpiredTransactionItem(TRANSACTION_ITEM_TIME1, new TransactionItem().setIncome(100F)));
        pushedPbZenTransactionStorage
                .add(new ExpiredTransactionItem(TRANSACTION_ITEM_TIME, new TransactionItem().setIncome(50F)));
        pushedPbZenTransactionStorage
                .add(new ExpiredTransactionItem(now().toEpochMilli() - 1 - customConfig.getPushedPbZenTransactionStorageCleanOlderMillis(),
                        new TransactionItem().setIncome(500F)));
        pushedPbZenTransactionStorage
                .add(new ExpiredTransactionItem(now().toEpochMilli() + 1 - customConfig.getPushedPbZenTransactionStorageCleanOlderMillis(),
                        new TransactionItem().setIncome(501F)));
        pushedPbZenTransactionStorage
                .add(new ExpiredTransactionItem(now().toEpochMilli(), new TransactionItem().setIncome(1000F)));
    }

    @Test
    public void testCleanUp() {
        pushedPbZenTransactionStorageService.cleanOldPbToZenTransactionStorage();
        assertThat(pushedPbZenTransactionStorage).isNotNull();
        assertThat(pushedPbZenTransactionStorage.size()).isEqualTo(EXPECTED);
    }

}
