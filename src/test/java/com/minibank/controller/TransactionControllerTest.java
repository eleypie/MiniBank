package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.User;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Controller Tests - Transactions")
public class TransactionControllerTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TransactionController transactionController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // TR-01: Show All Transactions
    // ============================================================
    @Test
    @DisplayName("TR-01: Should show all transactions when filter is ALL")
    void testShowAllTransactions() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, null, 1, 1000.00, "DEPOSIT", null, null, null),
                new Transaction(2, 1, null, 500.00, "WITHDRAW", null, null, null),
                new Transaction(3, 1, 2, 200.00, "TRANSFER", null, null, null)
        );

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = transactionController.showTransactions("ALL", session, model);

        // Assert
        assertEquals("transaction", result);
        assertEquals("John Doe", model.getAttribute("userName"));
        assertEquals(account.getId(), model.getAttribute("accountId"));
        assertEquals(10000.00, model.getAttribute("accountBalance"));
        assertEquals("ALL", model.getAttribute("filter"));

        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertEquals(3, resultTransactions.size());
        assertEquals("DEPOSIT", resultTransactions.get(0).getType());
        assertEquals("WITHDRAW", resultTransactions.get(1).getType());
        assertEquals("TRANSFER", resultTransactions.get(2).getType());
    }

    // ============================================================
    // TR-02: Filter by DEPOSIT
    // ============================================================
    @Test
    @DisplayName("TR-02: Should filter transactions by DEPOSIT")
    void testFilterDeposits() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, null, 1, 1000.00, "DEPOSIT", null, null, null),
                new Transaction(2, 1, null, 500.00, "WITHDRAW", null, null, null),
                new Transaction(3, 1, 2, 200.00, "TRANSFER", null, null, null)
        );

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = transactionController.showTransactions("DEPOSIT", session, model);

        // Assert
        assertEquals("transaction", result);
        assertEquals("DEPOSIT", model.getAttribute("filter"));

        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertEquals(1, resultTransactions.size());
        assertEquals("DEPOSIT", resultTransactions.get(0).getType());
    }

    // ============================================================
    // TR-03: Filter by WITHDRAW
    // ============================================================
    @Test
    @DisplayName("TR-03: Should filter transactions by WITHDRAW")
    void testFilterWithdrawals() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, null, 1, 1000.00, "DEPOSIT", null, null, null),
                new Transaction(2, 1, null, 500.00, "WITHDRAW", null, null, null),
                new Transaction(3, 1, 2, 200.00, "TRANSFER", null, null, null)
        );

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = transactionController.showTransactions("WITHDRAW", session, model);

        // Assert
        assertEquals("transaction", result);
        assertEquals("WITHDRAW", model.getAttribute("filter"));

        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertEquals(1, resultTransactions.size());
        assertEquals("WITHDRAW", resultTransactions.get(0).getType());
    }

    // ============================================================
    // TR-04: Filter by TRANSFER
    // ============================================================
    @Test
    @DisplayName("TR-04: Should filter transactions by TRANSFER")
    void testFilterTransfers() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, null, 1, 1000.00, "DEPOSIT", null, null, null),
                new Transaction(2, 1, null, 500.00, "WITHDRAW", null, null, null),
                new Transaction(3, 1, 2, 200.00, "TRANSFER", null, null, null)
        );

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = transactionController.showTransactions("TRANSFER", session, model);

        // Assert
        assertEquals("transaction", result);
        assertEquals("TRANSFER", model.getAttribute("filter"));

        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertEquals(1, resultTransactions.size());
        assertEquals("TRANSFER", resultTransactions.get(0).getType());
    }

    // ============================================================
    // TR-05: Empty Transactions - Show Empty State
    // ============================================================
    @Test
    @DisplayName("TR-05: Should show empty state when no transactions")
    void testEmptyTransactions() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList();

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = transactionController.showTransactions("ALL", session, model);

        // Assert
        assertEquals("transaction", result);
        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertTrue(resultTransactions.isEmpty());
    }

    // ============================================================
    // TR-06: User Not Logged In - Redirect to Login
    // ============================================================
    @Test
    @DisplayName("TR-06: Should redirect to login when user is not logged in")
    void testUserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String result = transactionController.showTransactions("ALL", session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(accountDao, never()).getAccountByUserId(anyInt());
        verify(transactionDao, never()).getTransactionsByAccountId(anyInt());
    }

    // ============================================================
    // TR-07: Account Not Found - Redirect to Dashboard
    // ============================================================
    @Test
    @DisplayName("TR-07: Should redirect to dashboard when account not found")
    void testAccountNotFound() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(null);

        // Act
        String result = transactionController.showTransactions("ALL", session, model);

        // Assert
        assertEquals("redirect:/dashboard", result);
        verify(transactionDao, never()).getTransactionsByAccountId(anyInt());
    }
}