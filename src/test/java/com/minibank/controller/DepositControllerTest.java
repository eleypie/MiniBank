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
@DisplayName("Deposit Controller Tests - CashIn")
public class DepositControllerTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private DepositController depositController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // CI-01: Valid Deposit - Amount > 0
    // ============================================================
    @Test
    @DisplayName("CI-01: Valid deposit with amount > 0 - Balance should increase")
    void testValidDeposit() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double depositAmount = 500.00;
        double expectedNewBalance = 10500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(account.getId(), expectedNewBalance)).thenReturn(true);

        // Act
        String result = depositController.handleDeposit(depositAmount, model, session);

        // Assert
        assertEquals("deposit", result);
        assertTrue((Boolean) model.getAttribute("depositSuccess"));
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(10500.00, model.getAttribute("newBalance"));
        assertEquals(500.00, model.getAttribute("amount"));
        verify(accountDao).updateBalance(account.getId(), expectedNewBalance);
        verify(transactionDao).logTransaction(null, account.getId(), depositAmount, "DEPOSIT");
    }

    // ============================================================
    // CI-02: Invalid Deposit - Amount ≤ 0
    // ============================================================
    @Test
    @DisplayName("CI-02: Invalid deposit with amount ≤ 0 - Should show error")
    void testInvalidAmount() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double invalidAmount = -100.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = depositController.handleDeposit(invalidAmount, model, session);

        // Assert
        assertEquals("deposit", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("valid amount"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CI-03: Balance Update Verification
    // ============================================================
    @Test
    @DisplayName("CI-03: Balance should update correctly after deposit")
    void testBalanceUpdate() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double depositAmount = 500.00;
        double expectedNewBalance = 10500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(account.getId(), expectedNewBalance)).thenReturn(true);

        // Act
        depositController.handleDeposit(depositAmount, model, session);

        // Assert
        verify(accountDao).updateBalance(account.getId(), 10500.00);
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(10500.00, model.getAttribute("newBalance"));
    }

    // ============================================================
    // CI-04: Transaction Logging Verification
    // ============================================================
    @Test
    @DisplayName("CI-04: Transaction should be logged after successful deposit")
    void testTransactionLogging() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        double depositAmount = 500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(accountDao.updateBalance(anyInt(), anyDouble())).thenReturn(true);

        // Act
        depositController.handleDeposit(depositAmount, model, session);

        // Assert
        verify(transactionDao).logTransaction(
                eq(null),
                eq(account.getId()),
                eq(depositAmount),
                eq("DEPOSIT")
        );
    }

    // ============================================================
    // CI-05: Show Deposit Page - User Logged In
    // ============================================================
    @Test
    @DisplayName("CI-05: Should show deposit page when user is logged in")
    void testShowDepositPage() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = depositController.showDeposit(session, model);

        // Assert
        assertEquals("deposit", result);
        assertEquals("John Doe", model.getAttribute("userName"));
        assertEquals(10000.00, model.getAttribute("balance"));
    }

    // ============================================================
    // CI-06: Show Deposit Page - User Not Logged In
    // ============================================================
    @Test
    @DisplayName("CI-06: Should redirect to login when user is not logged in")
    void testShowDepositPage_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String result = depositController.showDeposit(session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(accountDao, never()).getAccountByUserId(anyInt());
    }
}