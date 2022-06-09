//package com.github.storytime.service;
//
//import com.github.storytime.BaseTestConfig;
//import com.github.storytime.model.db.CustomPayee;
//import com.github.storytime.service.utils.CustomPayeeService;
//import junitparams.Parameters;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.Set;
//
//import static com.github.storytime.config.props.Constants.EMPTY;
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class CustomPayeeServiceTest extends BaseTestConfig {
//
//    @Autowired
//    private CustomPayeeService customPayeeService;
//
//    @Autowired
//    private Set<CustomPayee> customPayeeValues;
//
//    @Before
//    public void init() {
//        final ArrayList<CustomPayee> testDbValues = new ArrayList<>();
//        testDbValues.add(new CustomPayee("Эпицентр", "SHOP EPITSENTR"));
//        testDbValues.add(new CustomPayee("OKKO", "AZS OKKO"));
//        testDbValues.add(new CustomPayee("WOG", "WOG"));
//
//        customPayeeValues.clear();
//        customPayeeValues.addAll(testDbValues);
//    }
//
//    @Test
//    @Parameters(method = "validData")
//    public void testWithValidData(final String in, final String expected) {
//        final String actual = customPayeeService.getNicePayee(in);
//        assertThat(actual).isNotNull();
//        assertThat(actual).isEqualTo(expected);
//    }
//
//    @Test
//    @Parameters(method = "notValidData")
//    public void testWithNotValidData(final String in, final String expected) {
//        final String actual = customPayeeService.getNicePayee(in);
//        assertThat(actual).isNotNull();
//        assertThat(actual).isNotEqualTo(expected);
//    }
//
//    private Object[] validData() {
//        final String randomTextString1 = getRandomTextString();
//        final String randomTextString2 = getRandomTextString();
//        return new Object[]{
//                new Object[]{EMPTY, EMPTY},
//                new Object[]{null, EMPTY},
//                new Object[]{randomTextString1, randomTextString1},
//                new Object[]{randomTextString2, randomTextString2},
//                new Object[]{"АЗС AZS OKKO 60 KYIV", "OKKO"},
//                new Object[]{"АЗС AZS OKKO 60 KYIV", "OKKO"},
//        };
//    }
//
//    private Object[] notValidData() {
//        return new Object[]{
//                new Object[]{getRandomTextString(), getRandomTextString()},
//                new Object[]{null, getRandomTextString()},
//                new Object[]{getRandomTextString(), getRandomTextString()}
//        };
//    }
//}
