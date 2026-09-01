package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.model.Account;
import com.minibank.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DepositController {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public DepositController(AccountDao accountDao, TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    @GetMapping("/deposit")
    public String showDeposit(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        // ============================================================
        // VALIDATION: Check if user is logged in
        // ============================================================
        if (user == null) {
            return "redirect:/login";
        }

        Account account = accountDao.getAccountByUserId(user.getId());
        if (account == null) {
            return "redirect:/dashboard";
        }

        // ============================================================
        // SET MODEL ATTRIBUTES FOR VIEW
        // ============================================================
        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("balance", account.getBalance());

        // ============================================================
        // RETURN VIEW
        // ============================================================
        return "deposit";
    }

    @PostMapping("/deposit")
    public String handleDeposit(@RequestParam("amount") double amount,
                                Model model,
                                HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        // ============================================================
        // VALIDATION: Check if user is logged in
        // ============================================================
        if (user == null) {
            return "redirect:/login";
        }

        Account account = accountDao.getAccountByUserId(user.getId());
        if (account == null) {
            return "redirect:/dashboard";
        }

        // Common model attributes
        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("balance", account.getBalance());
        model.addAttribute("amount", amount);

        // ============================================================
        // VALIDATION 1: Amount must be greater than 0
        // ============================================================
        if (amount <= 0) {
            model.addAttribute("errorMessage", "⚠️ Please enter a valid amount greater than zero.");
            return "deposit";
        }

        // ============================================================
        // PROCESS DEPOSIT
        // ============================================================
        double oldBalance = account.getBalance();
        double newBalance = oldBalance + amount;

        // Update account balance
        accountDao.updateBalance(account.getId(), newBalance);

        // Log transaction: no sender (money entering the system), receiver = this account
        transactionDao.logTransaction(null, account.getId(), amount, "DEPOSIT");

        // ============================================================
        // SUCCESS - Return receipt data
        // ============================================================
        model.addAttribute("depositSuccess", true);
        model.addAttribute("oldBalance", oldBalance);
        model.addAttribute("newBalance", newBalance);
        model.addAttribute("balance", newBalance);
        model.addAttribute("amount", amount);

        return "deposit";
    }
}