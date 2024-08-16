package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.Exchange;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ExchangeRepository extends MongoRepository<Exchange, String> {

    @Query("{ 'crypto' : ?0 }")
    Optional<Exchange> findByCrypto(String crypto);
}
