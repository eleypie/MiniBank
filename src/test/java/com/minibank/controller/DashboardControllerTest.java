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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard Controller Tests")
public class DashboardControllerTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private DashboardController dashboardController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // TEST CB-01: Show Dashboard When User Is Logged In
    // ============================================================
    @Test
    @DisplayName("CB-01: Should show dashboard when user is logged in")
    void testShowDashboard_UserLoggedIn() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList();

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = dashboardController.showDashboard(session, model);

        // Assert
        assertEquals("dashboard", result);
        assertEquals("John Doe", model.getAttribute("userName"));
        assertEquals("09171234567", model.getAttribute("mobileNumber"));
        assertEquals(10000.00, model.getAttribute("balance"));
        assertEquals(transactions, model.getAttribute("transactions"));
    }

    // ============================================================
    // TEST CB-02: Redirect to Login When User Not Logged In
    // ============================================================
    @Test
    @DisplayName("CB-02: Should redirect to login when user is not logged in")
    void testShowDashboard_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        // Act
        String result = dashboardController.showDashboard(session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(accountDao, never()).getAccountByUserId(anyInt());
        verify(transactionDao, never()).getTransactionsByAccountId(anyInt());
    }

    // ============================================================
    // TEST CB-03: Show Zero Balance When Account Not Found
    // ============================================================
    @Test
    @DisplayName("CB-03: Should show zero balance when account not found")
    void testShowDashboard_AccountNotFound() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567","1234");

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(null);

        // Act
        String result = dashboardController.showDashboard(session, model);

        // Assert
        assertEquals("dashboard", result);
        assertEquals("John Doe", model.getAttribute("userName"));
        assertEquals("09171234567", model.getAttribute("mobileNumber"));
        assertEquals(0.00, model.getAttribute("balance"));
        assertTrue(((List<?>) model.getAttribute("transactions")).isEmpty());
    }

    // ============================================================
    // TEST CB-04: Load Transactions Correctly
    // ============================================================
    @Test
    @DisplayName("CB-04: Should load transactions correctly")
    void testShowDashboard_WithTransactions() {
        // Arrange
        User user = new User(1, "John", "Doe", "09171234567", "1234");
        Account account = new Account(1, 1, 10000.00);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, null, 1, 1000.00, "DEPOSIT", null, null, null),
                new Transaction(2, 1, null, 500.00, "WITHDRAW", null, null, null)
        );

        when(session.getAttribute("loggedInUser")).thenReturn(user);
        when(accountDao.getAccountByUserId(user.getId())).thenReturn(account);
        when(transactionDao.getTransactionsByAccountId(account.getId())).thenReturn(transactions);

        // Act
        String result = dashboardController.showDashboard(session, model);

        // Assert
        assertEquals("dashboard", result);
        List<Transaction> resultTransactions = (List<Transaction>) model.getAttribute("transactions");
        assertEquals(2, resultTransactions.size());
        assertEquals("DEPOSIT", resultTransactions.get(0).getType());
        assertEquals("WITHDRAW", resultTransactions.get(1).getType());
    }
}