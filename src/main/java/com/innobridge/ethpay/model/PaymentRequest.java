package com.innobridge.ethpay.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class PaymentRequest {
    @Email
    private String fromEmail;
    @Email
    private String toEmail;
    private String amount;
    private String currency;
    private String targetCurrency;
    private String description;

}
