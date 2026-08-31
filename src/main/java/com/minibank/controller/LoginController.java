package com.minibank.controller;

import com.minibank.dao.UserDao;
import com.minibank.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // ============================================================
    // DEPENDENCY INJECTION - Constructor Injection
    // ============================================================
    private final UserDao userDao;

    @Autowired
    public LoginController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session, Model model) {

        // ============================================================
        // CHECK IF ACCOUNT IS LOCKED
        // ============================================================
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts != null && attempts >= 3) {
            model.addAttribute("error", "Account locked due to 3 failed login attempts.");
        }

        // ============================================================
        // RETURN VIEW
        // ============================================================
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("mobileNumber") String mobileNumber,
                              @RequestParam("pin") String pin,
                              HttpSession session,
                              Model model) {

        // ============================================================
        // GET CURRENT LOGIN ATTEMPTS
        // ============================================================
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        // ============================================================
        // VALIDATION 1: Check if account is locked
        // ============================================================
        if (attempts >= 3) {
            model.addAttribute("error", "Account locked due to 3 failed login attempts.");
            return "login";
        }

        // ============================================================
        // AUTHENTICATE USER
        // ============================================================
        User user = userDao.authenticate(mobileNumber, pin);

        // ============================================================
        // VALIDATION 2: Check if credentials are valid
        // ============================================================
        if (user != null) {
            // ============================================================
            // SUCCESS - Create session and redirect to dashboard
            // ============================================================
            session.setAttribute("loginAttempts", 0);
            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        } else {
            // ============================================================
            // FAILURE - Increment attempts and show error
            // ============================================================
            attempts++;
            session.setAttribute("loginAttempts", attempts);

            int remaining = 3 - attempts;
            if (remaining > 0) {
                model.addAttribute("error", "❗Invalid mobile number or PIN. Attempts remaining: " + remaining);
            } else {
                model.addAttribute("error", "Maximum login attempts reached. Your account is locked.");
            }
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // ============================================================
        // INVALIDATE SESSION
        // ============================================================
        session.invalidate();

        // ============================================================
        // REDIRECT TO LOGIN
        // ============================================================
        return "redirect:/login";
    }
}