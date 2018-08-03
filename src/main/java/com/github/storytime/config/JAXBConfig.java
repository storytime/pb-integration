package com.github.storytime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;

@Configuration
public class JAXBConfig {


    @Bean
    public StringWriter stringWriter() {
        return new StringWriter();
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
}
