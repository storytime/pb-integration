package com.github.storytime;


import com.github.storytime.scheduler.CustomPayeeSchedulerExecutor;
import com.github.storytime.scheduler.PbSyncSchedulerExecutor;
import com.github.storytime.scheduler.PushedToZenStorageSchedulerExecutor;
import io.codearte.jfairy.Fairy;
import junitparams.JUnitParamsRunner;
import org.flywaydb.core.Flyway;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@SpringBootTest
@RunWith(JUnitParamsRunner.class)
public class BaseTestConfig {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    /*
       We need to mock these classes in order to prevent schedule execution during tests
     */
    @MockBean
    private CustomPayeeSchedulerExecutor customPayeeSchedulerExecutor;

    @MockBean
    private PbSyncSchedulerExecutor pbSyncSchedulerExecutor;

    @MockBean
    private PushedToZenStorageSchedulerExecutor pushedToZenStorageSchedulerExecutor;

    @MockBean
    private Flyway flyway;

    public String getRandomTextString() {
        return Fairy.create().textProducer().sentence(5);
    }

}

