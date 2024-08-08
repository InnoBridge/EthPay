package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, CustomUserRepository {

    // Custom query to find employees by firstname
    @Query("{ 'username' : ?0 }")
    Optional<User> findByUsername(String username);

    // Custom query to find employee by email
//    @Query("{ 'email' : ?0 }")
//    public Optional<User> findByEmail(String email);

}
