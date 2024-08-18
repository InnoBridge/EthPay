package com.innobridge.ethpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private String userId;
    private String userName;
    private String email;
}
