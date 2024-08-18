package com.innobridge.ethpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class AccountResponse {
    @Id
    private String id;
    private String userId;
    private Map<Currency, BalanceResponse> balances;
    @CreatedDate
    private Date createdDate;
    private boolean autoAccept;
}
