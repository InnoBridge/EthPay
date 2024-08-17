package com.innobridge.ethpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Unwrapped;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    public String id;
    public String senderId;
    public String receiverId;
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
