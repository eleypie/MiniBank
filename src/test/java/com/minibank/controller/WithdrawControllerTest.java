package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.model.Account;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Withdraw Controller Tests - CashOut")
public class WithdrawControllerTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private WithdrawController withdrawController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // CO-01: Valid Withdraw - Amount > 0 and sufficient balance
    // ============================================================
    @Test
    @DisplayName("CO-01: Valid withdraw with sufficient balance")
    void testValidWithdraw() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double withdrawAmount = 500.00;
        double expectedNewBalance = 9500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(account.getId(), expectedNewBalance)).thenReturn(true);

        // Act
        String result = withdrawController.handleWithdraw(withdrawAmount, model, session);

        // Assert
        assertEquals("withdraw", result);
        assertTrue((Boolean) model.getAttribute("withdrawSuccess"));
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(9500.00, model.getAttribute("newBalance"));
        assertEquals(500.00, model.getAttribute("amount"));
        verify(accountDao).updateBalance(account.getId(), expectedNewBalance);
        verify(transactionDao).logTransaction(account.getId(), null, withdrawAmount, "WITHDRAW");
    }

    // ============================================================
    // CO-02: Invalid Withdraw - Amount ≤ 0
    // ============================================================
    @Test
    @DisplayName("CO-02: Invalid withdraw with amount ≤ 0")
    void testInvalidAmount() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double invalidAmount = -100.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = withdrawController.handleWithdraw(invalidAmount, model, session);

        // Assert
        assertEquals("withdraw", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("Amount must be greater than 0"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CO-03: Insufficient Balance
    // ============================================================
    @Test
    @DisplayName("CO-03: Insufficient balance")
    void testInsufficientBalance() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 100.00);
        double withdrawAmount = 500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = withdrawController.handleWithdraw(withdrawAmount, model, session);

        // Assert
        assertEquals("withdraw", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("Insufficient balance"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CO-04: Balance Update Verification
    // ============================================================
    @Test
    @DisplayName("CO-04: Balance update verification")
    void testBalanceUpdate() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double withdrawAmount = 500.00;
        double expectedNewBalance = 9500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(account.getId(), expectedNewBalance)).thenReturn(true);

        // Act
        withdrawController.handleWithdraw(withdrawAmount, model, session);

        // Assert
        verify(accountDao).updateBalance(account.getId(), 9500.00);
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(9500.00, model.getAttribute("newBalance"));
    }

    // ============================================================
    // CO-05: Transaction Logging Verification
    // ============================================================
    @Test
    @DisplayName("CO-05: Transaction logging verification")
    void testTransactionLogging() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double withdrawAmount = 500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(anyInt(), anyDouble())).thenReturn(true);

        // Act
        withdrawController.handleWithdraw(withdrawAmount, model, session);

        // Assert
        verify(transactionDao).logTransaction(
                eq(account.getId()),
                eq(null),
                eq(withdrawAmount),
                eq("WITHDRAW")
        );
    }

    // ============================================================
    // CO-06: Show Withdraw Page - User Logged In
    // ============================================================
    @Test
    @DisplayName("CO-06: Should show withdraw page when user is logged in")
    void testShowWithdrawPage() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = withdrawController.showWithdraw(session, model);

        // Assert
        assertEquals("withdraw", result);
        assertEquals("John Doe", model.getAttribute("userName"));
        assertEquals(10000.00, model.getAttribute("balance"));
    }

    // ============================================================
    // CO-07: Show Withdraw Page - User Not Logged In
    // ============================================================
    @Test
    @DisplayName("CO-07: Should redirect to login when user is not logged in")
    void testShowWithdrawPage_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String result = withdrawController.showWithdraw(session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(accountDao, never()).getAccountByUserId(anyInt());
    }
}