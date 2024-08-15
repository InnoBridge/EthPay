package com.innobridge.ethpay.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "payments")
@Data
public class Payment {
    @Id
    private String id;
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
    private String currency;
    private String targetCurrency;
    private String description;

}
