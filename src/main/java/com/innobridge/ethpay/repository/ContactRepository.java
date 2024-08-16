package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.Contacts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ContactRepository extends MongoRepository<Contacts, String> {

    @Query("{ 'email' : ?0 }")
    Optional<Contacts> findByEmail(String email);

}