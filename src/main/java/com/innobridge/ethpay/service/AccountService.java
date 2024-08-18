package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.repository.AccountRepository;
import com.innobridge.ethpay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Account getAccount(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account doesn't exits, you can create a new account."));
    }

    public Account createAccount(String userId) {
        Account newAccount = new Account();
        accountRepository.findByUserId(userId)
                .ifPresentOrElse(
                        account -> {
                            throw new IllegalArgumentException("Account already exists.");
                } , () -> {
                            newAccount.setUserId(userId);
                            newAccount.setAutoAccept(true);
                            newAccount.setCreatedDate(new Date(System.currentTimeMillis()));
                            newAccount.setBalances(Map.of());
                            accountRepository.save(newAccount);
                });
        return accountRepository.save(newAccount);
    }

    public Account deposit(String userId, Currency currency, BigDecimal amount) {
        Account account = getAccount(userId);
        Map<Currency, Balance> balances = account.getBalances();
        if (balances.containsKey(currency)) {
            Balance balance = balances.get(currency);
            balance.setBalance(balance.getBalance().add(amount));
            balances.put(currency, balance);
        } else {
            balances.put(currency, new Balance(currency, amount, Map.of()));
        }
        Account savedAccount = accountRepository.save(account);
        updatePendingTransaction(userId, currency);
        return savedAccount;
    }

    public Account withdraw(String userId, Currency currency, BigDecimal amount) {
        Account account = getAccount(userId);
        Map<Currency, Balance> balances = account.getBalances();
        if (balances.containsKey(currency) && amount.compareTo(balances.get(currency).getAvailableFund()) <= 0){
            Balance balance = balances.get(currency);
            balance.setBalance(balance.getBalance().subtract(amount));
            balances.put(currency, balance);
        } else {
            throw new IllegalArgumentException("Insufficient funds.");
        }
        Account savedAccount = accountRepository.save(account);
        updatePendingTransaction(userId, currency);
        return savedAccount;
    }

    public Account setAutoAccept(String userId, boolean autoAccept) {
        Account account = getAccount(userId);
        account.setAutoAccept(autoAccept);
        return accountRepository.save(account);
    }

    public Account updatePendingTransaction(String userId, Currency currency) {
        List<Transaction> pendingTransactions = getSenderTransactions(
                userId,
                currency);

        Map<String, BigDecimal> pendingTransactionMap = pendingTransactions
                .stream()
                .map(transaction -> Map.entry(transaction.getId(), transaction.getSourceAmount()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Account account = getAccount(userId);

        account.getBalances().get(currency).setPendingTransactions(pendingTransactionMap);

        return accountRepository.save(account);
    }

    private List<Transaction> getSenderTransactions(String senderId, Currency sourceCurrency) {
        return transactionRepository.findBySenderIdAndStatusAndSourceCurrency(senderId, TransactionStatus.PENDING, sourceCurrency);
    }
}
