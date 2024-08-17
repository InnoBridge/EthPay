package com.innobridge.ethpay.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private String toUserId;
    private Currency sourceCurrencyType;
    private BigDecimal amount;
    private Currency targetCurrencyType;
   private String description;
}
