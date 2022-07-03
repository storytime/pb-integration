package com.github.storytime.api;

import com.github.storytime.service.info.VersionService;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.text.TextProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VersionController.class)
class VersionControllerTest {

    private final TextProducer textProducer;
    @MockBean
    private VersionService versionService;
    @Autowired
    private MockMvc mockMvc;

    public VersionControllerTest() {
        this.textProducer = Fairy.create().textProducer();
    }

    @Test
    void testIfVersionWorks() throws Exception {
        String mockString = textProducer.latinWord(1);

        Mockito.when(versionService.readVersion()).thenReturn(CompletableFuture.supplyAsync(() -> mockString));
        final MvcResult request = createValidRequest();

        mockMvc.perform(asyncDispatch(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_PLAIN_VALUE))
                .andExpect(content().string(mockString));
    }

    private MvcResult createValidRequest() throws Exception {
        return mockMvc.perform(get("/app/api/version"))
                .andExpect(request().asyncStarted())
                .andDo(log())
                .andReturn();
    }
}

