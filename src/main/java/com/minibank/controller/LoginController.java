package com.minibank.controller;

import com.minibank.dao.UserDao;
import com.minibank.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserDao userDao = new UserDao();

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session, Model model) {
        // Show lock message on GET requests if already locked out
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts != null && attempts >= 3) {
            model.addAttribute("error", "Account locked due to 3 failed login attempts.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("mobileNumber") String mobileNumber,
                              @RequestParam("pin") String pin,
                              HttpSession session,
                              Model model) {

        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= 3) {
            model.addAttribute("error", "Account locked due to 3 failed login attempts.");
            return "login";
        }

        User user = userDao.authenticate(mobileNumber, pin);

        if (user != null) {
            session.setAttribute("loginAttempts", 0);
            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        } else {
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
        session.invalidate();
        return "redirect:/login";
    }
}