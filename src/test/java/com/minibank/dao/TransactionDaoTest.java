package com.minibank.dao;

import com.minibank.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction DAO Tests")
public class TransactionDaoTest {

    private TransactionDao transactionDao;
    private AccountDao accountDao;

    @BeforeEach
    void setUp() {
        transactionDao = new TransactionDao();
        accountDao = new AccountDao();
    }

    // ============================================================
    // TD-01: Log Deposit Transaction
    // ============================================================
    @Test
    @DisplayName("TD-01: Should log deposit transaction successfully")
    void testLogDepositTransaction() {
        // Arrange
        Integer senderAccountId = null; // Deposit has no sender
        Integer receiverAccountId = 1;   // Assuming account with ID 1 exists
        double amount = 1000.00;
        String transactionType = "DEPOSIT";

        // Act - Log the transaction
        transactionDao.logTransaction(senderAccountId, receiverAccountId, amount, transactionType);

        // Assert - Verify transaction was logged
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(receiverAccountId);
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Should have at least one transaction");

        // Find the newly added transaction
        Transaction latestTransaction = transactions.get(0);
        assertEquals("DEPOSIT", latestTransaction.getType(), "Transaction type should be DEPOSIT");
        assertEquals(amount, latestTransaction.getAmount(), 0.01, "Amount should match");
        assertEquals(receiverAccountId, latestTransaction.getReceiverAccountId(), "Receiver account ID should match");
        assertNull(latestTransaction.getSenderAccountId(), "Sender account ID should be null for deposit");
    }

    // ============================================================
    // TD-02: Log Withdraw Transaction
    // ============================================================
    @Test
    @DisplayName("TD-02: Should log withdraw transaction successfully")
    void testLogWithdrawTransaction() {
        // Arrange
        Integer senderAccountId = 1;   // Assuming account with ID 1 exists
        Integer receiverAccountId = null; // Withdrawal has no receiver
        double amount = 500.00;
        String transactionType = "WITHDRAW";

        // Act - Log the transaction
        transactionDao.logTransaction(senderAccountId, receiverAccountId, amount, transactionType);

        // Assert - Verify transaction was logged
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(senderAccountId);
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Should have at least one transaction");

        // Find the newly added transaction
        Transaction latestTransaction = transactions.get(0);
        assertEquals("WITHDRAW", latestTransaction.getType(), "Transaction type should be WITHDRAW");
        assertEquals(amount, latestTransaction.getAmount(), 0.01, "Amount should match");
        assertEquals(senderAccountId, latestTransaction.getSenderAccountId(), "Sender account ID should match");
        assertNull(latestTransaction.getReceiverAccountId(), "Receiver account ID should be null for withdrawal");
    }

    // ============================================================
    // TD-03: Log Transfer Transaction
    // ============================================================
    @Test
    @DisplayName("TD-03: Should log transfer transaction successfully")
    void testLogTransferTransaction() {
        // Arrange
        Integer senderAccountId = 1;   // Assuming account with ID 1 exists
        Integer receiverAccountId = 2; // Assuming account with ID 2 exists
        double amount = 200.00;
        String transactionType = "TRANSFER";

        // Act - Log the transaction
        transactionDao.logTransaction(senderAccountId, receiverAccountId, amount, transactionType);

        // Assert - Verify transaction was logged
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(senderAccountId);
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Should have at least one transaction");

        // Find the newly added transaction
        Transaction latestTransaction = transactions.get(0);
        assertEquals("TRANSFER", latestTransaction.getType(), "Transaction type should be TRANSFER");
        assertEquals(amount, latestTransaction.getAmount(), 0.01, "Amount should match");
        assertEquals(senderAccountId, latestTransaction.getSenderAccountId(), "Sender account ID should match");
        assertEquals(receiverAccountId, latestTransaction.getReceiverAccountId(), "Receiver account ID should match");
    }

    // ============================================================
    // TD-04: Get Transactions By Account ID
    // ============================================================
    @Test
    @DisplayName("TD-04: Should get all transactions for an account")
    void testGetTransactionsByAccountId() {
        // Arrange
        int accountId = 1; // Assuming account with ID 1 exists

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(accountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");
        // Check that all transactions belong to the account
        for (Transaction transaction : transactions) {
            boolean isSender = transaction.getSenderAccountId() != null &&
                    transaction.getSenderAccountId().equals(accountId);
            boolean isReceiver = transaction.getReceiverAccountId() != null &&
                    transaction.getReceiverAccountId().equals(accountId);
            assertTrue(isSender || isReceiver,
                    "Transaction should involve account " + accountId);
        }
    }

    // ============================================================
    // TD-05: Get Transactions - With Sender and Receiver Names
    // ============================================================
    @Test
    @DisplayName("TD-05: Should return transactions with sender and receiver names")
    void testGetTransactionsWithNames() {
        // Arrange
        int accountId = 1;

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(accountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");

        // Check that names are populated for transactions
        for (Transaction transaction : transactions) {
            if (transaction.getSenderAccountId() != null) {
                // Sender name should be either "Unknown" or actual name
                assertNotNull(transaction.getSenderName(), "Sender name should not be null");
            }
            if (transaction.getReceiverAccountId() != null) {
                // Receiver name should be either "Unknown" or actual name
                assertNotNull(transaction.getReceiverName(), "Receiver name should not be null");
            }
        }
    }

    // ============================================================
    // TD-06: Get Transactions - Verify Date Formatting
    // ============================================================
    @Test
    @DisplayName("TD-06: Should format transaction dates correctly")
    void testTransactionDateFormatted() {
        // Arrange
        int accountId = 1;

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(accountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");

        if (!transactions.isEmpty()) {
            Transaction transaction = transactions.get(0);
            String dateFormatted = transaction.getDateFormatted();
            assertNotNull(dateFormatted, "Formatted date should not be null");
            assertFalse(dateFormatted.isEmpty(), "Formatted date should not be empty");
            // Check format: "MMM d, yyyy - h:mm a"
            assertTrue(dateFormatted.matches("\\w{3} \\d{1,2}, \\d{4} - \\d{1,2}:\\d{2} [AP]M"),
                    "Date should be in format: 'MMM d, yyyy - h:mm a'");
        }
    }

    // ============================================================
    // TD-07: Get Transactions - Verify Amount Formatting
    // ============================================================
    @Test
    @DisplayName("TD-07: Should format transaction amounts correctly")
    void testTransactionAmountFormatted() {
        // Arrange
        int accountId = 1;

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(accountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");

        if (!transactions.isEmpty()) {
            Transaction transaction = transactions.get(0);
            String amountFormatted = transaction.getAmountFormatted();
            assertNotNull(amountFormatted, "Formatted amount should not be null");
            assertFalse(amountFormatted.isEmpty(), "Formatted amount should not be empty");
            // Check format: "1,500.00"
            assertTrue(amountFormatted.matches("\\d{1,3}(,\\d{3})*\\.\\d{2}"),
                    "Amount should be in format: '1,500.00'");
        }
    }

    // ============================================================
    // TD-08: Get Transactions - Verify Reference Generation
    // ============================================================
    @Test
    @DisplayName("TD-08: Should generate reference numbers correctly")
    void testTransactionReference() {
        // Arrange
        int accountId = 1;

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(accountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");

        if (!transactions.isEmpty()) {
            Transaction transaction = transactions.get(0);
            String reference = transaction.getReference();
            assertNotNull(reference, "Reference should not be null");
            assertFalse(reference.isEmpty(), "Reference should not be empty");
            // Check format: "#TX-0001"
            assertTrue(reference.matches("#TX-\\d{4}"),
                    "Reference should be in format: '#TX-0001'");
        }
    }

    // ============================================================
    // TD-09: Get Transactions - Empty List for New Account
    // ============================================================
    @Test
    @DisplayName("TD-09: Should return empty list for account with no transactions")
    void testGetTransactions_EmptyList() {
        // Arrange
        int newAccountId = 99999; // Assuming this account has no transactions

        // Act
        List<Transaction> transactions = transactionDao.getTransactionsByAccountId(newAccountId);

        // Assert
        assertNotNull(transactions, "Transactions list should not be null");
        assertTrue(transactions.isEmpty(), "Transactions list should be empty for account with no transactions");
    }
}