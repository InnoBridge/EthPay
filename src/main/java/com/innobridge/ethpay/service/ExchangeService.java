package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.Crypto;
import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.model.Exchange;
import com.innobridge.ethpay.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;

    public Exchange getExchange(Crypto crypto) {
        return exchangeRepository
                .findByCrypto(crypto.name())
                .orElse(new Exchange(crypto));
    }

    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public Exchange updateExchange(Crypto crypto, Currency currency, Double rate) {
        Exchange exchange = getExchange(crypto);
        exchange.getCurrencyRates().put(currency, rate);
        return exchangeRepository.save(exchange);
    }
}
