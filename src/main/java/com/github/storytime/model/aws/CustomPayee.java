package com.github.storytime.model.aws;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class CustomPayee {

    @DynamoDBAttribute(attributeName = "payee")
    private String payee;

    @DynamoDBAttribute(attributeName = "containsValue")
    private String containsValue;
}
