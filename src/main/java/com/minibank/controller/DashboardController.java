package com.minibank.controller;

// Updated imports with full package names
import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class DashboardController {

    private final AccountDao accountDao = new AccountDao();
    private final TransactionDao transactionDao = new TransactionDao();

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        // Protect route: Redirect to login if user session doesn't exist
        if (user == null) {
            return "redirect:/login";
        }

        // Fetch live account and transaction history from DB using DAOs
        Account account = accountDao.getAccountByUserId(user.getId());
        List<Transaction> history = (account != null)
                ? transactionDao.getTransactionsByAccountId(account.getId())
                : List.of();

        // Pass database attributes into the HTML UI
        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("mobileNumber", user.getMobileNumber());
        model.addAttribute("balance", (account != null) ? account.getBalance() : 0.00);
        model.addAttribute("transactions", history);

        return "dashboard"; // Renders src/main/resources/templates/dashboard.html
    }
}