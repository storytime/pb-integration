//package com.github.storytime.config;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//
//@Configuration
//@PropertySource(value = "classpath:app-services.properties", encoding = "UTF-8")
//public class AppServicesConfig {
//
//    @Value("${users.ms.get.by.id.url}")
//    private String usersMsGetByIdUrl;
//
//    @Value("${users.ms.get.all}")
//    private String usersMmGetALlUrl;
//
//    @Value("${users.ms.save}")
//    private String saveUserUrl;
//
//    public String getUsersMsGetByIdUrl() {
//        return usersMsGetByIdUrl;
//    }
//
//    public String getUsersMmGetALlUrl() {
//        return usersMmGetALlUrl;
//    }
//
//    public String getSaveUserUrl() {
//        return saveUserUrl;
//    }
//}
