package com.innobridge.ethpay.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.innobridge.ethpay.model.Constants.PaymentStatus;
import com.innobridge.ethpay.model.Payment;
import com.innobridge.ethpay.model.PaymentRequest;
import com.innobridge.ethpay.model.UpdatePaymentRequest;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.service.PaymentService;
import com.innobridge.ethpay.service.UserService;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-payment")
    public ResponseEntity<?> payment(@RequestBody PaymentRequest paymentRequest) {

        Payment payment = new Payment();

        payment.setSourceUserId(paymentRequest.getUserId());
        payment.setTargetUserId(paymentRequest.getToUserId());
        payment.setSourceCurrencyType(paymentRequest.getSourceCurrencyType());
        payment.setSourceCurrencyAmount(paymentRequest.getAmount());
        payment.setTargetCurrencyType(paymentRequest.getTargetCurrencyType());
        // TODO: Probably need to use the changes in
        // https://github.com/InnoBridge/EthPay/pull/9/files#diff-3ffc269ef683660c723166f292f5c06446eb2a5f9c2a4a4eb4c54458856b91e9
        // to get the correct value here??
        // using same amount for now
        payment.setTargetCurrencyAmount(paymentRequest.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDescription(paymentRequest.getDescription());

        // Perform payment
        Payment savedPayment = paymentService.savePayment(payment);

        return ResponseEntity.ok("Payment successful: " + savedPayment);
    }

    @PostMapping("/update-payment")
    public ResponseEntity<?> updatePayment(@RequestBody UpdatePaymentRequest updateRequest) {
        // 1. Find the Payment Record
        Optional<Payment> payment = paymentService.findById(updateRequest.getPaymentId());
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
        }

        // 2. Handle Rejection
        if (PaymentStatus.REJECTED.equals(updateRequest.getStatus())) {
            // Update Payment Status to "rejected"
            payment.get().setStatus(PaymentStatus.REJECTED);
            paymentService.savePayment(payment.get());

            // TODO: Send rejection notification to notification service

            // Notify Sender (User A)
            // Notification notification = new Notification(
            // payment.getFromUser(),
            // "Your payment of " + payment.getAmount() + " " + payment.getCurrency() +
            // " to " + payment.getToUser() + " was rejected.",
            // payment.getId(),
            // "unread",
            // new Date()
            // );
            // notificationRepository.save(notification);

            return ResponseEntity.ok("Payment rejected.");
        }

        // 3. Handle Acceptance
        if (PaymentStatus.SUCCESS.equals(updateRequest.getStatus())) {
            payment.get().setStatus(PaymentStatus.SUCCESS);
            paymentService.savePayment(payment.get());

            // Update User Balances
            User fromUser = userService.getById(payment.get().getSourceUserId()).orElseThrow();
            User toUser = userService.getById(payment.get().getTargetUserId()).orElseThrow();

            // Deduct balance from Sender (User A)
            BigDecimal currentFromBalance = fromUser.getBalance().getOrDefault(payment.get().getSourceCurrencyType(),
                    BigDecimal.ZERO);
            BigDecimal newFromBalance = currentFromBalance.subtract(payment.get().getSourceCurrencyAmount());
            fromUser.getBalance().put(payment.get().getSourceCurrencyType(), newFromBalance);

            // Add balance to Recipient (User B)
            BigDecimal currentToBalance = toUser.getBalance().getOrDefault(payment.get().getTargetCurrencyType(),
                    BigDecimal.ZERO);
            BigDecimal newToBalance = currentToBalance.add(payment.get().getTargetCurrencyAmount());
            toUser.getBalance().put(payment.get().getTargetCurrencyType(), newToBalance);

            userService.saveUser(fromUser);
            userService.saveUser(toUser);

            // Notify Sender (User A)
            // Notification notification = new Notification(
            // payment.getFromUser(),
            // "Your payment of " + payment.getConvertedAmount() + " " +
            // payment.getConvertedCurrency() +
            // " to " + payment.getToUser() + " was accepted.",
            // payment.getId(),
            // "unread",
            // new Date()
            // );
            // notificationRepository.save(notification);

            return ResponseEntity.ok("Payment accepted and processed.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status value");
    }

}
