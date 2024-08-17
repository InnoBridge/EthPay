package com.innobridge.ethpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    private Currency currency;
    private BigDecimal balance;
    private Map<String, BigDecimal> pendingTransactions;

    public BigDecimal getAvailableFund() {
        BigDecimal availableFund = balance;
        pendingTransactions.values().stream().forEach(availableFund::subtract);
        return availableFund;
    }

    public BalanceResponse toBalanceResponse(Map<String, TransactionResponse> transactionResponseMap) {
        return new BalanceResponse(
                currency,
                balance,
                pendingTransactions.keySet().stream()
                        .map(transactionResponseMap::get)
                        .toList());
    }
}