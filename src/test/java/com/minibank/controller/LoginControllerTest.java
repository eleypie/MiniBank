package com.minibank.controller;

import com.minibank.dao.UserDao;
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
@DisplayName("Login Controller Tests - UserAuthentication")
public class LoginControllerTest {

    @Mock
    private UserDao userDao;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginController loginController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ============================================================
    // UA-01: Valid Login - Correct credentials
    // ============================================================
    @Test
    @DisplayName("UA-01: Valid login with correct credentials")
    void testValidLogin() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "1234";
        User expectedUser = new User(1, "John", "Doe", "09171234567", "1234");

        when(userDao.authenticate(mobileNumber, pin)).thenReturn(expectedUser);

        // Act
        String result = loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        assertEquals("redirect:/dashboard", result);
        verify(session).setAttribute("loggedInUser", expectedUser);
        verify(session).setAttribute("loginAttempts", 0);
    }

    // ============================================================
    // UA-02: Invalid Login - Wrong mobile number
    // ============================================================
    @Test
    @DisplayName("UA-02: Invalid login with wrong mobile number")
    void testInvalidMobileLogin() {
        // Arrange
        String mobileNumber = "09999999999";
        String pin = "1234";

        when(userDao.authenticate(mobileNumber, pin)).thenReturn(null);

        // Act
        String result = loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        assertEquals("login", result);
        assertTrue(model.containsAttribute("error"));
        verify(session, never()).setAttribute(eq("loggedInUser"), any());
        // Verify that loginAttempts was set (we don't care about the exact value here)
        verify(session, atLeastOnce()).setAttribute(eq("loginAttempts"), anyInt());
    }

    // ============================================================
    // UA-03: Invalid Login - Wrong PIN
    // ============================================================
    @Test
    @DisplayName("UA-03: Invalid login with wrong PIN")
    void testInvalidPinLogin() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "9999";

        when(userDao.authenticate(mobileNumber, pin)).thenReturn(null);

        // Act
        String result = loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        assertEquals("login", result);
        assertTrue(model.containsAttribute("error"));
        verify(session, never()).setAttribute(eq("loggedInUser"), any());
        // Verify that loginAttempts was set (we don't care about the exact value here)
        verify(session, atLeastOnce()).setAttribute(eq("loginAttempts"), anyInt());
    }

    // ============================================================
    // UA-04: Account Lockout - 3 failed attempts
    // ============================================================
    @Test
    @DisplayName("UA-04: Account lockout after 3 failed attempts")
    void testAccountLockout() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "9999";

        when(session.getAttribute("loginAttempts")).thenReturn(3);

        // Act
        String result = loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        assertEquals("login", result);
        assertTrue(model.containsAttribute("error"));
        String error = (String) model.getAttribute("error");
        assertTrue(error.contains("locked"));
        verify(userDao, never()).authenticate(anyString(), anyString());
    }

    // ============================================================
    // UA-05: Session Creation on Successful Login
    // ============================================================
    @Test
    @DisplayName("UA-05: Session created on successful login")
    void testSessionCreation() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "1234";
        User expectedUser = new User(1, "John", "Doe", "09171234567", "1234");

        when(userDao.authenticate(mobileNumber, pin)).thenReturn(expectedUser);

        // Act
        loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        verify(session).setAttribute("loggedInUser", expectedUser);
        verify(session).setAttribute("loginAttempts", 0);
    }

    // ============================================================
    // UA-06: Show Login Page with Lock Message
    // ============================================================
    @Test
    @DisplayName("UA-06: Show login page with lock message when locked")
    void testShowLoginPageWithLock() {
        // Arrange
        when(session.getAttribute("loginAttempts")).thenReturn(3);

        // Act
        String result = loginController.showLoginPage(session, model);

        // Assert
        assertEquals("login", result);
        assertTrue(model.containsAttribute("error"));
        String error = (String) model.getAttribute("error");
        assertTrue(error.contains("locked"));
    }

    // ============================================================
    // UA-07: Show Login Page Without Lock
    // ============================================================
    @Test
    @DisplayName("UA-07: Show login page without lock message")
    void testShowLoginPageWithoutLock() {
        // Arrange
        when(session.getAttribute("loginAttempts")).thenReturn(0);

        // Act
        String result = loginController.showLoginPage(session, model);

        // Assert
        assertEquals("login", result);
        assertFalse(model.containsAttribute("error"));
    }

    // ============================================================
    // UA-08: Logout - Session invalidated
    // ============================================================
    @Test
    @DisplayName("UA-08: Logout invalidates session")
    void testLogout() {
        // Act
        String result = loginController.logout(session);

        // Assert
        assertEquals("redirect:/login", result);
        verify(session).invalidate();
    }

    // ============================================================
    // UA-09: Index redirects to login
    // ============================================================
    @Test
    @DisplayName("UA-09: Index page redirects to login")
    void testIndexRedirect() {
        // Act
        String result = loginController.index();

        // Assert
        assertEquals("redirect:/login", result);
    }

    // ============================================================
    // UA-10: Failed login increments attempts
    // ============================================================
    @Test
    @DisplayName("UA-10: Failed login increments attempt counter")
    void testFailedLoginIncrementsAttempts() {
        // Arrange
        String mobileNumber = "09171234567";
        String pin = "9999";

        when(session.getAttribute("loginAttempts")).thenReturn(null);
        when(userDao.authenticate(mobileNumber, pin)).thenReturn(null);

        // Act
        loginController.handleLogin(mobileNumber, pin, session, model);

        // Assert
        verify(session).setAttribute("loginAttempts", 1);
    }
}