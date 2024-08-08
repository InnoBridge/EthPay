package com.innobridge.ethpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "employees")
@Data
public class Employee {
    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String email;
}