package com.innobridge.ethpay.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.innobridge.ethpay.model.Constants.Currency;
import com.innobridge.ethpay.model.Constants.PaymentStatus;

import lombok.Data;

@Document(collection = "activepayments")
@Data
public class Payment {
    @Id
    private String id;
    private String sourceUserId;
    private String targetUserId;
    private Currency sourceCurrencyType;
    private BigDecimal sourceCurrencyAmount;
    private Currency targetCurrencyType;
    private BigDecimal targetCurrencyAmount;
    private PaymentStatus status;
    private String description;

}
