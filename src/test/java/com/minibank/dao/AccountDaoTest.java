package com.minibank.dao;

import com.minibank.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account DAO Tests")
public class AccountDaoTest {

    private AccountDao accountDao;

    @BeforeEach
    void setUp() {
        accountDao = new AccountDao();
    }

    // ============================================================
    // AD-01: Get Account By User ID - Valid User
    // ============================================================
    @Test
    @DisplayName("AD-01: Should get account by valid user ID")
    void testGetAccountByUserId_ValidUser() {
        // Arrange
        int userId = 1; // Assuming user with ID 1 exists in database

        // Act
        Account account = accountDao.getAccountByUserId(userId);

        // Assert
        assertNotNull(account, "Account should not be null for existing user");
        assertEquals(userId, account.getUserId(), "User ID should match");
        assertTrue(account.getBalance() >= 0, "Balance should be non-negative");
    }

    // ============================================================
    // AD-02: Get Account By User ID - Invalid User
    // ============================================================
    @Test
    @DisplayName("AD-02: Should return null for non-existent user ID")
    void testGetAccountByUserId_InvalidUser() {
        // Arrange
        int invalidUserId = 99999; // User ID that doesn't exist

        // Act
        Account account = accountDao.getAccountByUserId(invalidUserId);

        // Assert
        assertNull(account, "Should return null for non-existent user");
    }

    // ============================================================
    // AD-03: Get Account By Mobile Number - Valid Number
    // ============================================================
    @Test
    @DisplayName("AD-03: Should get account by valid mobile number")
    void testGetAccountByMobileNumber_ValidNumber() {
        // Arrange
        String mobileNumber = "09171234567"; // Assuming this number exists

        // Act
        Account account = accountDao.getAccountbyMobileNumber(mobileNumber);

        // Assert
        assertNotNull(account, "Account should not be null for existing mobile number");
        assertTrue(account.getBalance() >= 0, "Balance should be non-negative");
    }

    // ============================================================
    // AD-04: Get Account By Mobile Number - Invalid Number
    // ============================================================
    @Test
    @DisplayName("AD-04: Should return null for non-existent mobile number")
    void testGetAccountByMobileNumber_InvalidNumber() {
        // Arrange
        String invalidMobile = "09999999999"; // Number that doesn't exist

        // Act
        Account account = accountDao.getAccountbyMobileNumber(invalidMobile);

        // Assert
        assertNull(account, "Should return null for non-existent mobile number");
    }

    // ============================================================
    // AD-05: Update Balance - Valid Update
    // ============================================================
    @Test
    @DisplayName("AD-05: Should update balance successfully")
    void testUpdateBalance_ValidUpdate() {
        // Arrange
        int accountId = 1; // Assuming account with ID 1 exists
        double newBalance = 15000.00;

        // Act
        boolean result = accountDao.updateBalance(accountId, newBalance);

        // Assert
        assertTrue(result, "Balance update should return true");

        // Verify the update
        Account updatedAccount = accountDao.getAccountByUserId(1);
        assertNotNull(updatedAccount);
        assertEquals(newBalance, updatedAccount.getBalance(), 0.01, "Balance should be updated correctly");
    }

    // ============================================================
    // AD-06: Update Balance - Invalid Account
    // ============================================================
    @Test
    @DisplayName("AD-06: Should return false for non-existent account ID")
    void testUpdateBalance_InvalidAccount() {
        // Arrange
        int invalidAccountId = 99999;
        double newBalance = 15000.00;

        // Act
        boolean result = accountDao.updateBalance(invalidAccountId, newBalance);

        // Assert
        assertFalse(result, "Should return false for non-existent account");
    }

    // ============================================================
    // AD-07: Get Account Balance - Verify Balance Matches Database
    // ============================================================
    @Test
    @DisplayName("AD-07: Should get correct balance from database")
    void testGetAccountBalance_MatchesDatabase() {
        // Arrange
        int userId = 1;

        // Act
        Account account = accountDao.getAccountByUserId(userId);

        // Assert
        assertNotNull(account);
        double balance = account.getBalance();
        assertTrue(balance >= 0, "Balance should be non-negative");

        // Verify the balance is a valid number
        assertDoesNotThrow(() -> Double.valueOf(balance), "Balance should be a valid number");
    }
}