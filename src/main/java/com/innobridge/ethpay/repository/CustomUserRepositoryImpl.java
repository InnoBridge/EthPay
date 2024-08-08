package com.innobridge.ethpay.repository;

import com.innobridge.ethpay.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Update;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public User updateUser(String id, User user) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update()
                .set("username", user.getUsername())
//                .set("roles", user.getRoles())
                .set("password", user.getPassword())
                .set("refreshToken", user.getRefreshToken());

        return mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                User.class);
    }
}