package com.innobridge.ethpay.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.innobridge.ethpay.model.Payment;
import com.innobridge.ethpay.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

}
