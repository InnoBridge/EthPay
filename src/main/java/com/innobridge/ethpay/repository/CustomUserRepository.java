package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.User;

public interface CustomUserRepository {
    User updateUser(String id, User user);
    void updateTokens(String id, String accessToken, String refreshToken);
}
