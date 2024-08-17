package com.innobridge.ethpay.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Unwrapped;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
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

    public Transaction(String id,
                       String senderId,
                       String receiverId,
                       Currency sourceCurrency,
                       BigDecimal sourceAmount,
                       Currency targetCurrency,
                       BigDecimal targetAmount,
                       Crypto substrateCrypto,
                       Date createdDate,
                       Date completedDate,
                       TransactionStatus status,
                       String description) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.sourceCurrency = sourceCurrency;
        this.sourceAmount = sourceAmount;
        this.targetCurrency = targetCurrency;
        this.targetAmount = targetAmount;
        this.substrateCrypto = substrateCrypto;
        this.createdDate = createdDate;
        this.completedDate = completedDate;
        this.status = status;
        this.description = description;
    }

    public TransactionResponse toTransactionResponse(String senderEmail, String receiverEmail) {
        return TransactionResponse.builder()
                .id(this.id)
                .senderEmail(senderEmail)
                .receiverEmail(receiverEmail)
                .sourceCurrency(this.sourceCurrency)
                .sourceAmount(this.sourceAmount)
                .targetCurrency(this.targetCurrency)
                .targetAmount(this.targetAmount)
                .substrateCrypto(this.substrateCrypto)
                .createdDate(this.createdDate)
                .completedDate(this.completedDate)
                .status(this.status)
                .description(this.description)
                .build();
    }
}
