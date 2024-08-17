package com.innobridge.ethpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "exchange")
@Data
public class Exchange {
    @Id
    private String id;
    @Indexed(unique = true)
    private Crypto crypto;
    private Map<Currency, Double> currencyRates;

    public Exchange(Crypto crypto) {
        this.crypto = crypto;
        this.currencyRates = new HashMap<>();
    }
}
