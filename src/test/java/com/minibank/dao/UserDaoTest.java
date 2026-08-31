package com.minibank.dao;

import com.minibank.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User DAO Tests")
public class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
    }

    // ============================================================
    // UD-01: Authenticate User - Valid Credentials
    // ============================================================
    @Test
    @DisplayName("UD-01: Should authenticate user with valid credentials")
    void testAuthenticate_ValidCredentials() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "1234";

        // Act
        User user = userDao.authenticate(mobileNumber, pin);

        // Assert
        assertNotNull(user, "User should not be null for valid credentials");
        assertEquals(mobileNumber, user.getMobileNumber(), "Mobile number should match");
        assertEquals(pin, user.getPin(), "PIN should match");
        assertNotNull(user.getFirstName(), "First name should not be null");
        assertNotNull(user.getLastName(), "Last name should not be null");
    }

    // ============================================================
    // UD-02: Authenticate User - Invalid Mobile Number
    // ============================================================
    @Test
    @DisplayName("UD-02: Should return null for invalid mobile number")
    void testAuthenticate_InvalidMobileNumber() {
        // Arrange
        String invalidMobile = "09999999999";
        String pin = "1234";

        // Act
        User user = userDao.authenticate(invalidMobile, pin);

        // Assert
        assertNull(user, "Should return null for invalid mobile number");
    }

    // ============================================================
    // UD-03: Authenticate User - Invalid PIN
    // ============================================================
    @Test
    @DisplayName("UD-03: Should return null for invalid PIN")
    void testAuthenticate_InvalidPin() {
        // Arrange
        String mobileNumber = "09171234567";
        String invalidPin = "9999";

        // Act
        User user = userDao.authenticate(mobileNumber, invalidPin);

        // Assert
        assertNull(user, "Should return null for invalid PIN");
    }

    // ============================================================
    // UD-04: Get User By ID - Valid ID
    // ============================================================
    @Test
    @DisplayName("UD-04: Should get user by valid ID")
    void testGetUserById_ValidId() {
        // Arrange
        int userId = 1;

        // Act
        User user = userDao.getUserById(userId);

        // Assert
        assertNotNull(user, "User should not be null for valid ID");
        assertEquals(userId, user.getId(), "User ID should match");
        assertNotNull(user.getFirstName(), "First name should not be null");
        assertNotNull(user.getLastName(), "Last name should not be null");
        assertNotNull(user.getMobileNumber(), "Mobile number should not be null");
    }

    // ============================================================
    // UD-05: Get User By ID - Invalid ID
    // ============================================================
    @Test
    @DisplayName("UD-05: Should return null for invalid user ID")
    void testGetUserById_InvalidId() {
        // Arrange
        int invalidUserId = 99999;

        // Act
        User user = userDao.getUserById(invalidUserId);

        // Assert
        assertNull(user, "Should return null for invalid user ID");
    }

    // ============================================================
    // UD-06: Authenticate User - Check Database Reference
    // ============================================================
    @Test
    @DisplayName("UD-06: Should verify user exists in database")
    void testAuthenticate_CheckDatabaseReference() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "1234";

        // Act
        User user = userDao.authenticate(mobileNumber, pin);

        // Assert - This verifies the user actually exists in the database
        assertNotNull(user, "User should exist in the database");
        assertEquals("09171234567", user.getMobileNumber(), "Mobile number should match database");

        // Verify the user has an account
        AccountDao accountDao = new AccountDao();
        assertNotNull(accountDao.getAccountByUserId(user.getId()),
                "User should have an associated account");
    }

    // ============================================================
    // UD-07: Get User By ID - Verify User Details
    // ============================================================
    @Test
    @DisplayName("UD-07: Should return complete user details")
    void testGetUserById_CompleteDetails() {
        // Arrange
        int userId = 1;

        // Act
        User user = userDao.getUserById(userId);

        // Assert
        assertNotNull(user, "User should not be null");
        assertTrue(user.getId() > 0, "User ID should be positive");
        assertNotNull(user.getFirstName(), "First name should not be null");
        assertNotNull(user.getLastName(), "Last name should not be null");
        assertNotNull(user.getMobileNumber(), "Mobile number should not be null");
        assertNotNull(user.getPin(), "PIN should not be null");
    }
}