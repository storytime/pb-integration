//package com.github.storytime;
//
//import com.github.storytime.builder.HistoryRequestBuilder;
//import com.github.storytime.model.jaxb.history.request.Request;
//import org.apache.http.entity.ContentType;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.security.NoSuchAlgorithmException;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.apache.http.client.fluent.Request.Post;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class Pb24ProxyApplicationTests {
//
//    @Autowired
//    private StringWriter stringWriter;
//
//    @Autowired
//    private Marshaller jaxbMarshaller;
//
//    @Autowired
//    private HistoryRequestBuilder historyRequestBuilder;
//
//    @Test
//    public void contextLoads() throws JAXBException, IOException, NoSuchAlgorithmException {
//        final Request request1 = historyRequestBuilder
//                .buildStatementRequest(133800, "G7156LLiWn7100nKq4SvtrBC0OVZPKJ9", "01.01.2018", "01.02.2018", "4731185613017182");
//
//        jaxbMarshaller.marshal(request1, stringWriter);
//        final String xmlString1 = stringWriter.toString();
//        System.out.println(xmlString1);
//
//        final String s = Post("https://api.privatbank.ua/p24api/rest_fiz")
//                .bodyString(xmlString1, ContentType.APPLICATION_FORM_URLENCODED)
//                .generate().returnContent().asString(UTF_8);
//
//        System.out.println(s);
//    }
//
//}
