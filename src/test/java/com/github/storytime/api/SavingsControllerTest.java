package com.github.storytime.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.storytime.model.api.SavingsInfo;
import com.github.storytime.model.api.SavingsInfoResponse;
import io.codearte.jfairy.producer.text.TextProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static io.codearte.jfairy.Fairy.create;
import static java.util.List.of;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SavingsController.class)
class SavingsControllerTest {

    private static final int COUNT = 1;

    @MockBean
    private SavingsController savingsController;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    private final TextProducer textProducer;

    public SavingsControllerTest() {
        this.textProducer = create().textProducer();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testIfSavingsTableWorks() throws Exception {
        String mockResponse = textProducer.latinWord(COUNT);
        String mockId = textProducer.latinWord(COUNT);

        when(savingsController.getAllSavingsAsTable(mockId)).thenReturn(CompletableFuture.supplyAsync(() -> mockResponse));
        final MvcResult request = createValidRequest(mockId, "/info");

        mockMvc.perform(asyncDispatch(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_PLAIN_VALUE))
                .andExpect(content().string(mockResponse));
    }

    @Test
    void testIfSavingsJsonWorks() throws Exception {
        final var mockResponse = textProducer.latinWord(COUNT);
        final var mockId = textProducer.latinWord(COUNT);
        final var savingsInfo = SavingsInfo.builder()
                .balanceStr(textProducer.latinWord(COUNT))
                .inUahStr(textProducer.latinWord(COUNT))
                .currencySymbol(textProducer.latinWord(COUNT))
                .title(textProducer.latinWord(COUNT))
                .inUah(new BigDecimal(100))
                .percent(new BigDecimal(100))
                .balance(new BigDecimal(100))
                .build();

        final var objectForResponse = SavingsInfoResponse.builder().savings(of(savingsInfo)).total(mockResponse).build();
        final var responseEntity = new ResponseEntity<>(objectForResponse, OK);
        final var expectedJson = objectMapper.writeValueAsString(objectForResponse);

        when(savingsController.getAllSavingsAsJson(mockId)).thenReturn(CompletableFuture.supplyAsync(() -> responseEntity));
        final MvcResult request = createValidRequest(mockId, "/json");

        mockMvc.perform(asyncDispatch(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(content().string(expectedJson));
    }

    private MvcResult createValidRequest(final String id, final String prefix) throws Exception {
        return mockMvc.perform(get("/app/api/savings/" + id + prefix))
                .andExpect(request().asyncStarted())
                .andDo(log())
                .andReturn();
    }
}

