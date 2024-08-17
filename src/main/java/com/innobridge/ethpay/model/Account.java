package com.innobridge.ethpay.model;

import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.Map;

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
}