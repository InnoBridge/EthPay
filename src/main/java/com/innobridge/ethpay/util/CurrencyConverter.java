package com.innobridge.ethpay.util;

import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.model.Exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverter {
    public static BigDecimal convertCurrency(BigDecimal amount,
                                             Currency sourceCurrency,
                                             Currency targetCurrency,
                                             Exchange exchangeRate) {
        BigDecimal sourceCurrencyExchangeRate;
        BigDecimal targetCurrencyExchangeRate;
        if (exchangeRate.getCurrencyRates().containsKey(sourceCurrency)) {
            sourceCurrencyExchangeRate = BigDecimal.valueOf(exchangeRate.getCurrencyRates().get(sourceCurrency));
        } else {
            throw new RuntimeException("Source currency not found in exchange rates");
        }

        if (exchangeRate.getCurrencyRates().containsKey(targetCurrency)) {
            targetCurrencyExchangeRate = BigDecimal.valueOf(exchangeRate.getCurrencyRates().get(targetCurrency));
        } else {
            throw new RuntimeException("Source currency not found in exchange rates");
        }

        return amount.divide(sourceCurrencyExchangeRate, 100, RoundingMode.HALF_UP)
                .multiply(targetCurrencyExchangeRate);
    }
}
