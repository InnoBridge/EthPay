package com.innobridge.ethpay.model;

public enum Crypto {
    BITCOIN("BTC"),
    ETHEREUM("ETH"),
    LITECOIN("LTC"),
    RIPPLE("XRP"),
    DOGECOIN("DOGE");

    private final String symbol;

    Crypto(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
