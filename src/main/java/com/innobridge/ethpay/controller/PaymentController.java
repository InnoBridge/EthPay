package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.service.TransactionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.util.Utility.getAuthentication;

@RestController
@RequestMapping(TRANSACTION_URL)
public class PaymentController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieve user's transactions",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = TransactionResponse.class)))
    })
    public ResponseEntity<?> getTransactions(@RequestParam(required = false) TransactionStatus status) {
        try {
            return ResponseEntity.ok(transactionService.getTransactions(getAuthentication().getId(), status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

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
