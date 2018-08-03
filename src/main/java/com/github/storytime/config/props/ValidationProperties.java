package com.github.storytime.config.props;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:text.properties")
public class ValidationProperties {

    @Value("${merch.id.null}")
    private String merchIdNull;

    @Value("${merch.id.format}")
    private String merchIdFormat;

    @Value("${password.null}")
    private String passwordNull;

    @Value("${password.length}")
    private String passwordLength;

    @Value("${start.date.null}")
    private String startDateNull;

    @Value("${start.date.format}")
    private String startDateFormat;

    @Value("${end.date.null}")
    private String endDateNull;

    @Value("${end.date.format}")
    private String endDateFormat;

    @Value("${card.null}")
    private String cardNull;

    @Value("${card.format}")
    private String cardFormat;

    public String getMerchIdNull() {
        return merchIdNull;
    }

    public String getMerchIdFormat() {
        return merchIdFormat;
    }

    public String getPasswordNull() {
        return passwordNull;
    }

    public String getPasswordLength() {
        return passwordLength;
    }

    public String getStartDateNull() {
        return startDateNull;
    }

    public String getStartDateFormat() {
        return startDateFormat;
    }

    public String getEndDateNull() {
        return endDateNull;
    }

    public String getEndDateFormat() {
        return endDateFormat;
    }

    public String getCardNull() {
        return cardNull;
    }

    public String getCardFormat() {
        return cardFormat;
    }
}
