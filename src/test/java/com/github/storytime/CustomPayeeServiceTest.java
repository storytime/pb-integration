package com.github.storytime;


import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.repository.CustomPayeeRepository;
import com.github.storytime.service.CustomPayeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Generate nice payee")
public class CustomPayeeServiceTest extends GenericContextMock {

    public static final String ORIGINAL_PAYEE_1 = "AZK KOG";
    public static final String ORIGINAL_PAYEE_2 = "AZK KOG CN";
    public static final String ORIGINAL_PAYEE_3 = "azk kog KV";
    public static final String CONTAINS_VALUE = "KOG";
    public static final String CHANGED_PAYEE = "Kog";

    @MockBean
    private CustomPayeeRepository customPayeeRepository;

    @Autowired
    @InjectMocks
    private CustomPayeeService customPayeeService;

    private static Stream<Arguments> getData() {
        final String randomTextString = getRandomTextString();
        return Stream.of(
                Arguments.of(EMPTY, EMPTY),
                Arguments.of(null, EMPTY),
                Arguments.of(ORIGINAL_PAYEE_1, CHANGED_PAYEE),
                Arguments.of(ORIGINAL_PAYEE_2, CHANGED_PAYEE),
                Arguments.of(ORIGINAL_PAYEE_3, CHANGED_PAYEE),
                Arguments.of(randomTextString, randomTextString),
                Arguments.of(SPACE, EMPTY)
        );
    }

    @BeforeEach
    public void setUp() {
        final ArrayList<CustomPayee> mockDbData = new ArrayList<>();
        mockDbData.add(new CustomPayee().setContainsValue(CONTAINS_VALUE).setPayee(CHANGED_PAYEE));
        mockDbData.add(new CustomPayee().setContainsValue(CONTAINS_VALUE.toLowerCase()).setPayee(CHANGED_PAYEE));
        when(customPayeeRepository.findAll()).thenReturn(mockDbData);
    }

    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @MethodSource("getData")
    public void testNicePayee(final String in, final String expected) {
        final String nicePayee = customPayeeService.getNicePayee(in);
        assertNotNull(nicePayee);
        assertEquals(nicePayee, expected);
        verify(customPayeeRepository, atLeastOnce()).findAll();
    }

}