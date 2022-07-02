package com.github.storytime.model.api;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class CustomPayee {

    @NotNull
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

    @Override
    public boolean equals(final Object obj) {
        final CustomPayee that = (CustomPayee) obj;
        return that.getId().equals(this.getId());
    }
}
