package com.innobridge.ethpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.innobridge.ethpay.model.Payment;
import com.innobridge.ethpay.model.PaymentRequest;
import com.innobridge.ethpay.service.PaymentService;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<?> payment(@RequestBody PaymentRequest paymentRequest) {

        Payment payment = new Payment();

        payment.setFromEmail(paymentRequest.getFromEmail());
        payment.setToEmail(paymentRequest.getToEmail());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency()); // TODO: Validate currency is valid.
        payment.setTargetCurrency(paymentRequest.getTargetCurrency()); // TODO: Decide if this is even needed..
        payment.setDescription(paymentRequest.getDescription());

        // Perform payment
        Payment savedPayment = paymentService.savePayment(payment);

        return ResponseEntity.ok("Payment successful: " + savedPayment);
    }

}
