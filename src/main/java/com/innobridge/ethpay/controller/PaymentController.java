package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.Account;
import com.innobridge.ethpay.model.Crypto;
import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.model.Transaction;
import com.innobridge.ethpay.service.TransactionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.util.Utility.getAuthentication;

@RestController
@RequestMapping(TRANSACTION_URL)
public class PaymentController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Make Payment",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Transaction.class)))
    })
    public ResponseEntity<?> makePayment(
            @RequestParam String receiverEmail,
            @RequestParam Currency sourceCurrency,
            @RequestParam Currency targetCurrency,
            @RequestParam(required = false) Crypto substrateCrypto,
            @RequestParam double targetAmount,
            @RequestParam(required = false) String message) {
        try {
            return ResponseEntity.ok(
                    transactionService.createTransaction(
                            getAuthentication().getId(),
                            receiverEmail,
                            sourceCurrency,
                            targetCurrency,
                            substrateCrypto,
                            BigDecimal.valueOf(targetAmount),
                            message
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
