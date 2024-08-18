package com.innobridge.ethpay.service;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.model.Currency;
import com.innobridge.ethpay.repository.TransactionRepository;
import com.innobridge.ethpay.util.CurrencyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

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

    public TransactionResponse getTransactionById(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!transaction.getSenderId().equals(userId) && !transaction.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Transaction not found");
        }
        User sender = userService.getById(transaction.getSenderId()).get();
        User receiver = userService.getById(transaction.getReceiverId()).get();
        return transaction.toTransactionResponse(sender.getEmail(), receiver.getEmail());
    }

    public List<TransactionResponse> getTransactions(String userId, TransactionStatus status) {
        List<TransactionStatus> statuses = status == null
                ? List.of(TransactionStatus.PENDING, TransactionStatus.FILLED, TransactionStatus.CANCELLED)
                : List.of(status);

        List<Transaction> transactions = transactionRepository.findBySenderIdOrReceiverIdAndStatusIn(userId, statuses);

        return convertToTransactionResponse(userId, transactions);
    }

    public List<TransactionResponse> getSenderTransactions(String userId, TransactionStatus status, Currency sourceCurrency) {
        List<TransactionStatus> statuses = status == null
                ? List.of(TransactionStatus.PENDING, TransactionStatus.FILLED, TransactionStatus.CANCELLED)
                : List.of(status);

        List<Transaction> transactions = sourceCurrency == null
                ? transactionRepository.findBySenderIdAndStatusIn(userId, statuses):
                transactionRepository.findBySenderIdAndStatusInAndSourceCurrency(userId, statuses, sourceCurrency);

        return convertToTransactionResponse(userId, transactions);
    }

    public List<TransactionResponse> getReceiverTransactions(String userId, TransactionStatus status, Currency sourceCurrency) {
        List<TransactionStatus> statuses = status == null
                ? List.of(TransactionStatus.PENDING, TransactionStatus.FILLED, TransactionStatus.CANCELLED)
                : List.of(status);

        List<Transaction> transactions = sourceCurrency == null
                ? transactionRepository.findByReceiverIdAndStatusIn(userId, statuses):
                transactionRepository.findByReceiverIdAndStatusInAndSourceCurrency(userId, statuses, sourceCurrency);

        return convertToTransactionResponse(userId, transactions);
    }

    public TransactionResponse createTransaction(String senderId,
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
        return savedTransaction.toTransactionResponse(sourceUser.getEmail(), targetUser.getEmail());
    }

    public TransactionResponse acceptTransaction(String transactionId, String receiverId) {
        // TODO Auto-generated method stub
        // Get transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        String senderId = transaction.getSenderId();

        if (!transaction.getReceiverId().equals(receiverId)) {
            throw new IllegalArgumentException("Invalid Transaction for sender " + receiverId);
        }

        // perform withdraw from the sender account
        accountService.withdraw(senderId, transaction.sourceCurrency, transaction.sourceAmount);
        accountService.deposit(receiverId, transaction.targetCurrency, transaction.targetAmount);

        transaction.setStatus(TransactionStatus.FILLED);
        transaction.setCompletedDate(new Date(System.currentTimeMillis()));
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountService.updatePendingTransaction(senderId, transaction.sourceCurrency);

        User sourceUser = userService.getById(senderId).get();
        User targetUser = userService.getById(receiverId).get();
        return savedTransaction.toTransactionResponse(sourceUser.getEmail(), targetUser.getEmail());
    }

    public TransactionResponse rejectSenderTransaction(String transactionId, String senderId, String message) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        if (!transaction.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("Invalid Transaction for sender " + senderId);
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setCompletedDate(new Date(System.currentTimeMillis()));
        transaction.setDescription(message);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountService.updatePendingTransaction(transaction.senderId, transaction.sourceCurrency);

        User sourceUser = userService.getById(senderId).get();
        User targetUser = userService.getById(transaction.getReceiverId()).get();
        return savedTransaction.toTransactionResponse(sourceUser.getEmail(), targetUser.getEmail());
    }

    public TransactionResponse rejectReceiverTransaction(String transactionId, String receiverId, String message) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (!transaction.getReceiverId().equals(receiverId)) {
            throw new IllegalArgumentException("Invalid Transaction for receiver " + receiverId);
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setCompletedDate(new Date(System.currentTimeMillis()));
        transaction.setDescription(message);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountService.updatePendingTransaction(transaction.senderId, transaction.sourceCurrency);
        User sourceUser = userService.getById(transaction.getSenderId()).get();
        User targetUser = userService.getById(receiverId).get();
        return savedTransaction.toTransactionResponse(sourceUser.getEmail(), targetUser.getEmail());
    }

    private List<TransactionResponse> convertToTransactionResponse(String userId, List<Transaction> transactions) {
        Set<String> userIds = transactions.stream()
                .map(transaction -> transaction.getSenderId().equals(userId) ? transaction.getReceiverId() : transaction.getSenderId())
                .collect(Collectors.toSet());
        userIds.add(userId);
        List<User> users = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, String> userIdEmailMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));

        return transactions.stream()
                .map(transaction ->
                        transaction.toTransactionResponse(
                                userIdEmailMap.get(transaction.getSenderId()),
                                userIdEmailMap.get(transaction.getReceiverId())))
                .toList();
    }

}
