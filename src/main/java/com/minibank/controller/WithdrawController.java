package com.minibank.controller;

import com.minibank.dao.AccountDao;
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
public class WithdrawController {

    private final AccountDao accountDao;

    public WithdrawController(Account accountDao){
        this.accountDao = accountDao;
    }

    @GetMapping("/withdraw")
    public String showWithdraw(HttpSession session, Model model){
        User user = (User) session.getAttribute("loogedInUser");
        if (user == null){
            return "redirect:/login";
        }

        Account account = accountDao.getAccountByUserId(user.getId());

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
            return "redirect:/withdraw";
        }

        Account account = AccountDao.getAccountByUserId(user.getId());
        if (account == null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("userName", user.getFirstName());

        if (amount <= 0) {
            model.addAttribute("errorMessage", "Amount must be greater than 0.");
            model.addAttribute("balance", account.getBalance());
            model.addAttribute("amount", amount);
            return "withdraw";
        }

        // Capture old balance before changing it
        double oldBalance = account.getBalance();
        double newBalance = oldBalance + amount;
        }
    }
}
