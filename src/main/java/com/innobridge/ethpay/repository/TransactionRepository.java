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

    @Query(value = "{ '$or': [ { 'senderId': ?0 }, { 'receiverId': ?0 } ], 'status': { '$in': ?1 } }", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findBySenderIdOrReceiverIdAndStatusIn(String userId, List<TransactionStatus> statuses);

    @Query(value = "{ 'senderId': ?0, 'status': { '$in': ?1 }}", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findBySenderIdAndStatusIn(String userId, List<TransactionStatus> statuses);

    @Query(value = "{ 'senderId': ?0, 'status': { '$in': ?1 }, 'sourceCurrency': ?2 }", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findBySenderIdAndStatusInAndSourceCurrency(String userId, List<TransactionStatus> statuses, Currency sourceCurrency);

    @Query(value = "{ 'receiverId': ?0, 'status': { '$in': ?1 }}", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findByReceiverIdAndStatusIn(String userId, List<TransactionStatus> statuses);

    @Query(value = "{ 'receiverId': ?0, 'status': { '$in': ?1 }, 'sourceCurrency': ?2 }", sort = "{ 'createdDate' : -1 }")
    List<Transaction> findByReceiverIdAndStatusInAndSourceCurrency(String userId, List<TransactionStatus> statuses, Currency sourceCurrency);
}
