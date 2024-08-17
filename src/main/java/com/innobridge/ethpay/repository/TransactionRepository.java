package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.model.Transaction;
import com.innobridge.ethpay.model.TransactionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    @Query(value = "{ 'senderId' : ?0, 'status' : ?1, 'sourceCurrency' : ?2}", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findBySenderIdAndStatusAndSourceCurrency(String userId, TransactionStatus status, Currency sourceCurrency);
}
