package com.innobridge.ethpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "contacts")
@Data
public class Contacts {
    @Id
    private String id;
    private String email;
    private Set<String> contacts;

    public Contacts(String email, Set<String> contacts) {
        this.email = email;
        this.contacts = contacts;
    }
}
