package com.github.storytime.model.aws;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class CustomPayee {

    @DynamoDBAttribute(attributeName = "id")
    private String id;

    @NotNull
    @DynamoDBAttribute(attributeName = "payee")
    private String payee;

    @NotNull
    @DynamoDBAttribute(attributeName = "containsValue")
    private String containsValue;

    @DynamoDBAttribute(attributeName = "createDate")
    private Long createDate;
}
