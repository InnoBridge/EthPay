package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        var user = userRepository.findByUsername(username);
        return user;
    }
}
