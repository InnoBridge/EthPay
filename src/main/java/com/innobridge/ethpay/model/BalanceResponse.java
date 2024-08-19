package com.innobridge.ethpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal availableFund;
    private List<TransactionResponse> pendingTransactions;
}