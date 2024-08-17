package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {
    @Query("{ 'userId' : ?0 }")
    Optional<Account> findByUserId(String userId);
}
