package com.innobridge.ethpay.model;

import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String userId;
    private Map<Currency, Balance> balances;
    @CreatedDate
    private Date createdDate;
    private boolean autoAccept;

    public AccountResponse toAccountResponse(Map<String, TransactionResponse> transactionResponseMap) {
        Map<Currency, BalanceResponse> balanceResponseMap = new HashMap<>();
        balances.forEach(
                (currency, balance) -> balanceResponseMap.put(currency, balance.toBalanceResponse(transactionResponseMap))
        );
        return new AccountResponse(
                id,
                userId,
                balanceResponseMap,
                createdDate,
                autoAccept);
    }
}