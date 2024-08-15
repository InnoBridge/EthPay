package com.innobridge.ethpay.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.innobridge.ethpay.model.Payment;
import com.innobridge.ethpay.model.User;

public interface PaymentRepository extends MongoRepository<Payment, String>, CustomPaymentRepository {
    @Query("{ 'email' : ?0 }")
    Optional<User> findByEmail(String email);
}
