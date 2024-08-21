package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.service.AccountService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.innobridge.ethpay.util.Utility.getAuthentication;
import static com.innobridge.ethpay.constants.HTTPConstants.*;

@RestController
@RequestMapping(ACCOUNT_URL)
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Retrieve user's account",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccountResponse.class)))
    })
    public ResponseEntity<?> getAccount() {

        try {
            Account account = accountService.getAccount(getAuthentication().getId());
            return ResponseEntity.ok(convertAccountToAccountResponse(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Create a new account",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccountResponse.class)))
    })
    public ResponseEntity<?> createAccount() {
        try {
            Account account = accountService.createAccount(getAuthentication().getId());
            return ResponseEntity.ok(convertAccountToAccountResponse(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "DepositCash",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccountResponse.class)))
    })
    public ResponseEntity<?> depositCash(@RequestParam Currency currency, @RequestParam Double amount) {
        try {
            Account account = accountService.deposit(getAuthentication().getId(), currency, BigDecimal.valueOf(amount));
            return ResponseEntity.ok(convertAccountToAccountResponse(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "WithdrawCash",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccountResponse.class)))
    })
    public ResponseEntity<?> withdrawCash(@RequestParam Currency currency, @RequestParam Double amount) {
        try {
            Account account = accountService.withdraw(getAuthentication().getId(), currency, BigDecimal.valueOf(amount));
            return ResponseEntity.ok(convertAccountToAccountResponse(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/autoaccept")
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Set Auto Accept",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = Account.class)))
    })
    public ResponseEntity<?> setAutoAccept(@RequestParam boolean autoAccept) {
        try {
            return ResponseEntity.ok(accountService.setAutoAccept(getAuthentication().getId(), autoAccept));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private AccountResponse convertAccountToAccountResponse(Account account) {
        Map<String, TransactionResponse> pendingTransactions = transactionService
                .getTransactions(account.getUserId(), TransactionStatus.PENDING)
                .stream()
                .collect(Collectors.toMap(TransactionResponse::getId, transactionResponse -> transactionResponse));
        return account.toAccountResponse(pendingTransactions);
    }
}
