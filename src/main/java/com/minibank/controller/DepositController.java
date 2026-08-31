package com.minibank.controller;

import com.minibank.dao.AccountDao;
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

    public DepositController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @GetMapping("/deposit")
    public String showDeposit(HttpSession session, Model model) {

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

        return "deposit";
    }

    @PostMapping("/deposit")
    public String handleDeposit(@RequestParam("amount") double amount,
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

        model.addAttribute("userName", user.getFirstName());

        if (amount <= 0) {
            model.addAttribute("errorMessage", "Amount must be greater than 0.");
            model.addAttribute("balance", account.getBalance());
            model.addAttribute("amount", amount);
            return "deposit";
        }

        // Capture old balance before changing it
        double oldBalance = account.getBalance();
        double newBalance = oldBalance + amount;

        // Update and persist
        accountDao.updateBalance(account.getId(), newBalance);

        // Attributes for the receipt view
        model.addAttribute("depositSuccess", true);
        model.addAttribute("oldBalance", oldBalance);
        model.addAttribute("newBalance", newBalance);
        model.addAttribute("balance", newBalance);

        return "deposit";
    }
}