package com.innobridge.ethpay.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Unwrapped;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Data
public class TransactionResponse {
    public String id;
    public String senderEmail;
    public String receiverEmail;
    public Currency sourceCurrency;
    public BigDecimal sourceAmount;
    public Currency targetCurrency;
    public BigDecimal targetAmount;
    @Unwrapped.Nullable
    public Crypto substrateCrypto;
    public Date createdDate;
    public Date completedDate;
    public TransactionStatus status;
    public String description;
}