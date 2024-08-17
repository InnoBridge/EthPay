package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.Account;
import com.innobridge.ethpay.model.Balance;
import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

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
                            newAccount.setAutoAccept(false);
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
        return accountRepository.save(account);
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
        return accountRepository.save(account);
    }

    public Account setAutoAccept(String userId, boolean autoAccept) {
        Account account = getAccount(userId);
        account.setAutoAccept(autoAccept);
        return accountRepository.save(account);
    }
}
