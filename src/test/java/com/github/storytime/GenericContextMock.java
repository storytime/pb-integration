package com.github.storytime;

import com.github.storytime.other.PrintAllConfigProperties;
import com.github.storytime.scheduler.PbSyncSchedulerExecutor;
import io.codearte.jfairy.Fairy;
import org.flywaydb.core.Flyway;
import org.springframework.boot.test.mock.mockito.MockBean;

public class GenericContextMock {

    @MockBean
    public Flyway flyway;

    @MockBean
    public PrintAllConfigProperties printAllConfigProperties;

    @MockBean
    PbSyncSchedulerExecutor pbSyncSchedulerExecutor;

    public static String getRandomTextString() {
        return Fairy.create().textProducer().sentence(2);
    }

}
