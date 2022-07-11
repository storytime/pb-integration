package com.github.storytime.model.api;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class CustomPayee {

    @DynamoDBAttribute(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "payee")
    private String payee;

    @DynamoDBAttribute(attributeName = "containsValue")
    private String containsValue;

    @DynamoDBAttribute(attributeName = "createDate")
    private Long createDate;

    @Override
    public boolean equals(final Object obj) {
        final CustomPayee that = (CustomPayee) obj;
        return that.id.equals(this.id) && that.payee.equals(this.payee);
    }

    @Override
    public String toString() {
        return "id: " + id + ", payee: " + payee + ", contains value: " + containsValue;
    }
}
