package com.innobridge.ethpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;
    private String username;
//    private List<Role> roles;
    private String password;
    private List<String> refreshToken;
}