package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.dao.UserDao;
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
@DisplayName("Transfer Controller Tests - CashTransfer")
public class TransferControllerTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private UserDao userDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TransferController transferController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // CT-01: Valid Transfer - Money sent to another user
    // ============================================================
    @Test
    @DisplayName("CT-01: Valid transfer to another user - both balances update")
    void testValidTransfer() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 10000.00);
        Account recipientAccount = new Account(2, 2, 5000.00);
        String recipientNumber = "09181234567";
        double transferAmount = 500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);
        when(accountDao.getAccountbyMobileNumber(recipientNumber)).thenReturn(recipientAccount);
        when(accountDao.updateBalance(eq(senderAccount.getId()), anyDouble())).thenReturn(true);
        when(accountDao.updateBalance(eq(recipientAccount.getId()), anyDouble())).thenReturn(true);

        User recipientUser = new User(2, "Jane", "Smith", "09181234567", "4321");
        when(userDao.getUserById(2)).thenReturn(recipientUser);

        // Act
        String result = transferController.handleTransfer(recipientNumber, transferAmount, model, session);

        // Assert
        assertEquals("transfer", result);
        assertTrue((Boolean) model.getAttribute("transferSuccess"));
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(9500.00, model.getAttribute("newBalance"));
        assertEquals(500.00, model.getAttribute("amount"));
        assertEquals("Jane Smith", model.getAttribute("recipientName"));
        assertEquals("09181234567", model.getAttribute("recipientNumber"));
        verify(accountDao).updateBalance(senderAccount.getId(), 9500.00);
        verify(accountDao).updateBalance(recipientAccount.getId(), 5500.00);
        verify(transactionDao).logTransaction(senderAccount.getId(), recipientAccount.getId(), transferAmount, "TRANSFER");
    }

    // ============================================================
    // CT-02: Self Transfer - Cannot transfer to own account
    // ============================================================
    @Test
    @DisplayName("CT-02: Cannot transfer to own account")
    void testSelfTransfer() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 10000.00);
        String selfNumber = "09171234567";

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);

        // Act
        String result = transferController.handleTransfer(selfNumber, 500.00, model, session);

        // Assert
        assertEquals("transfer", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("own account"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CT-03: Recipient Not Found
    // ============================================================
    @Test
    @DisplayName("CT-03: Recipient not found - show error")
    void testRecipientNotFound() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 10000.00);
        String invalidNumber = "09999999999";

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);
        when(accountDao.getAccountbyMobileNumber(invalidNumber)).thenReturn(null);

        // Act
        String result = transferController.handleTransfer(invalidNumber, 500.00, model, session);

        // Assert
        assertEquals("transfer", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("User does not exist"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CT-04: Insufficient Balance
    // ============================================================
    @Test
    @DisplayName("CT-04: Insufficient balance - show error")
    void testInsufficientBalance() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 100.00);
        Account recipientAccount = new Account(2, 2, 5000.00);
        String recipientNumber = "09181234567";
        double transferAmount = 500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);
        when(accountDao.getAccountbyMobileNumber(recipientNumber)).thenReturn(recipientAccount);

        // Act
        String result = transferController.handleTransfer(recipientNumber, transferAmount, model, session);

        // Assert
        assertEquals("transfer", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("Insufficient balance"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CT-05: Invalid Amount - Amount must be > 0
    // ============================================================
    @Test
    @DisplayName("CT-05: Invalid amount - amount must be greater than 0")
    void testInvalidAmount() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 10000.00);
        Account recipientAccount = new Account(2, 2, 5000.00);
        String recipientNumber = "09181234567";
        double invalidAmount = -100.00;

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);
        when(accountDao.getAccountbyMobileNumber(recipientNumber)).thenReturn(recipientAccount);

        // Act
        String result = transferController.handleTransfer(recipientNumber, invalidAmount, model, session);

        // Assert
        assertEquals("transfer", result);
        assertTrue(model.containsAttribute("errorMessage"));
        String error = (String) model.getAttribute("errorMessage");
        assertTrue(error.contains("Amount must be greater than 0"));
        verify(accountDao, never()).updateBalance(anyInt(), anyDouble());
        verify(transactionDao, never()).logTransaction(any(), any(), anyDouble(), anyString());
    }

    // ============================================================
    // CT-06: Show Transfer Page - User Logged In
    // ============================================================
    @Test
    @DisplayName("CT-06: Should show transfer page when user is logged in")
    void testShowTransferPage() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567","1234");
        Account account = new Account(1, 1, 10000.00);

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);

        // Act
        String result = transferController.showTransfer(session, model);

        // Assert
        assertEquals("transfer", result);
        assertEquals("John", model.getAttribute("userName"));
        assertEquals(10000.00, model.getAttribute("balance"));
    }

    // ============================================================
    // CT-07: Show Transfer Page - User Not Logged In
    // ============================================================
    @Test
    @DisplayName("CT-07: Should redirect to login when user is not logged in")
    void testShowTransferPage_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String result = transferController.showTransfer(session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(accountDao, never()).getAccountByUserId(anyInt());
    }

    // ============================================================
    // CT-08: Both Balances Update Correctly
    // ============================================================
    @Test
    @DisplayName("CT-08: Both sender and receiver balances update correctly")
    void testBothBalancesUpdate() {
        // Arrange
        User sender = new User(1, "John", "Doe", "09171234567", "1234");
        Account senderAccount = new Account(1, 1, 10000.00);
        Account recipientAccount = new Account(2, 2, 5000.00);
        String recipientNumber = "09181234567";
        double transferAmount = 500.00;

        double expectedSenderBalance = 9500.00;
        double expectedRecipientBalance = 5500.00;

        when(session.getAttribute("loggedInUser")).thenReturn(sender);
        when(accountDao.getAccountByUserId(sender.getId())).thenReturn(senderAccount);
        when(accountDao.getAccountbyMobileNumber(recipientNumber)).thenReturn(recipientAccount);
        when(accountDao.updateBalance(senderAccount.getId(), expectedSenderBalance)).thenReturn(true);
        when(accountDao.updateBalance(recipientAccount.getId(), expectedRecipientBalance)).thenReturn(true);

        User recipientUser = new User(2, "Jane", "Smith", "09181234567","4321");
        when(userDao.getUserById(2)).thenReturn(recipientUser);

        // Act
        transferController.handleTransfer(recipientNumber, transferAmount, model, session);

        // Assert
        verify(accountDao).updateBalance(senderAccount.getId(), 9500.00);
        verify(accountDao).updateBalance(recipientAccount.getId(), 5500.00);
        assertEquals(10000.00, model.getAttribute("oldBalance"));
        assertEquals(9500.00, model.getAttribute("newBalance"));
    }
}