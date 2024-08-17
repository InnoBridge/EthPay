package com.innobridge.ethpay.model;

import com.innobridge.ethpay.model.Constants.PaymentStatus;

import lombok.Data;

@Data
public class UpdatePaymentRequest {
    private String paymentId;
    private PaymentStatus status; // only accept OR reject
}
