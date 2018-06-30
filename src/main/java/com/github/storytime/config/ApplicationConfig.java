package com.github.storytime.config;


import com.github.storytime.other.RequestLoggerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

@Configuration
public class ApplicationConfig {

    @Bean
    public StringWriter stringWriter() {
        return new StringWriter();
    }

    @Bean
    public RestTemplate restTemplate() {
        // TODO: req time out
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(singletonList(new RequestLoggerInterceptor()));
        return restTemplate;
    }

    @Bean
    public Marshaller jaxbMarshaller() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(
                com.github.storytime.model.jaxb.history.response.ok.Response.class
        );
        return jaxbContext.createMarshaller();
    }

    @Bean
    public Unmarshaller jaxbHistoryErrorUnmarshaller() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext
                .newInstance(com.github.storytime.model.jaxb.history.response.error.Response.class);
        return jaxbContext.createUnmarshaller();
    }

    @Bean
    public Unmarshaller jaxbHistoryOkUnmarshaller() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext
                .newInstance(com.github.storytime.model.jaxb.history.response.ok.Response.class);
        return jaxbContext.createUnmarshaller();
    }

    @Bean
    public Set<String> pbTransferInfo() {
        return new HashSet<>();
    }

}
