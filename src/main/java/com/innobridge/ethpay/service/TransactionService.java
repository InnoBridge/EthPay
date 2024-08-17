package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.repository.TransactionRepository;
import com.innobridge.ethpay.util.CurrencyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private ExchangeService exchangeService;
    @Autowired
    private AccountService accountService;

    public Transaction createTransaction(String senderId,
                                         String receiverEmail,
                                         Currency sourceCurrency,
                                         Currency targetCurrency,
                                         Crypto substrateCrypto,
                                         BigDecimal targetAmount,
                                         String message) {

        User sourceUser = userService.getById(senderId).get();
        // Check if the receiver exists
        User targetUser = userService.getByEmail(receiverEmail).orElseThrow(
                () -> new IllegalArgumentException("User with email " + receiverEmail + " not found")
        );

        BigDecimal sourceAmount;
        /**
         * If the source and target currency are the same, no need to convert the currency.
         */
        if (sourceCurrency.equals(targetCurrency)) {
            sourceAmount = targetAmount;
            substrateCrypto = null;
        } else {
            Exchange exchange = exchangeService.getExchange(substrateCrypto);
            /**
             * We would want to get the amount for the source currency.
             * This is a reverse calculation in which the targetCurrency
             * is the source currency and the sourceCurrency is the target currency
             * for the CurrencyConverter.
             */
            sourceAmount = CurrencyConverter.convertCurrency(
                    targetAmount,
                    targetCurrency,
                    sourceCurrency,
                    exchange);
        }

        Account sourceAccount = accountService.getAccount(senderId);
        Account targetAccount = accountService.getAccount(targetUser.getId());

        /**
         * Check if the source account has enough available funds to make the transaction.
         */
        if (sourceAccount.getBalances().get(sourceCurrency).getAvailableFund().compareTo(sourceAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Transaction.TransactionBuilder transaction = Transaction.builder()
                .senderId(senderId)
                .receiverId(targetUser.getId())
                .sourceCurrency(sourceCurrency)
                .sourceAmount(sourceAmount)
                .targetCurrency(targetCurrency)
                .targetAmount(targetAmount)
                .substrateCrypto(substrateCrypto)
                .createdDate(new Date(System.currentTimeMillis()))
                .description(message);

        /**
         * If the target account is set to auto accept, the transaction is completed immediately.
         */
        if (targetAccount.isAutoAccept()) {
            accountService.withdraw(senderId, sourceCurrency, sourceAmount);
            accountService.deposit(targetUser.getId(), targetCurrency, targetAmount);
            transaction.completedDate(new Date(System.currentTimeMillis())).status(TransactionStatus.FILLED);
        } else {
            transaction.status(TransactionStatus.PENDING);
        }
        Transaction savedTransaction = transactionRepository.save(transaction.build());
        accountService.updatePendingTransaction(senderId, sourceCurrency);
        return savedTransaction;
    }


}
