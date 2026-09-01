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
public class WithdrawController {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public WithdrawController(AccountDao accountDao, TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    @GetMapping("/withdraw")
    public String showWithdraw(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Account account = accountDao.getAccountByUserId(user.getId());
        if (account == null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("balance", account.getBalance());

        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String handleWithdraw(@RequestParam("amount") double amount,
                                 Model model,
                                 HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
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
            model.addAttribute("errorMessage", "⚠️ Amount must be greater than 0.");
            return "withdraw";
        }

        // ============================================================
        // VALIDATION 2: Insufficient balance
        // ============================================================
        double oldBalance = account.getBalance();
        if (amount > oldBalance) {
            model.addAttribute("errorMessage", "⚠️ Insufficient balance. You have ₱" +
                    String.format("%.2f", oldBalance) + " available.");
            return "withdraw";
        }

        // ============================================================
        // PROCESS WITHDRAWAL
        // ============================================================
        double newBalance = oldBalance - amount;

        // Update account balance
        accountDao.updateBalance(account.getId(), newBalance);

        // Log transaction: sender = this account, no receiver (money leaving the system)
        transactionDao.logTransaction(account.getId(), null, amount, "WITHDRAW");

        // ============================================================
        // SUCCESS - Return receipt data
        // ============================================================
        model.addAttribute("withdrawSuccess", true);
        model.addAttribute("oldBalance", oldBalance);
        model.addAttribute("newBalance", newBalance);
        model.addAttribute("balance", newBalance);
        model.addAttribute("amount", amount);

        return "withdraw";
    }
}