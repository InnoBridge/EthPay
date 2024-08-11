package com.innobridge.ethpay.model;

import lombok.Data;

@Data
public class SigninResponse {
    private String accessToken;
    private String refreshToken;

    public SigninResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
