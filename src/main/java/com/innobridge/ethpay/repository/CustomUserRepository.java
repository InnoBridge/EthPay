package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.User;

public interface CustomUserRepository {
    User updateUser(String id, User user);
}
