package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TransactionController {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public TransactionController(AccountDao accountDao, TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    @GetMapping("/transactions")
    public String showTransactions(@RequestParam(defaultValue = "ALL") String filter,
                                   HttpSession session,
                                   Model model) {

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
        // FETCH TRANSACTIONS FROM DATABASE
        // ============================================================
        List<Transaction> allTransactions = transactionDao.getTransactionsByAccountId(account.getId());

        // ============================================================
        // APPLY FILTER
        // ============================================================
        List<Transaction> filtered = "ALL".equalsIgnoreCase(filter)
                ? allTransactions
                : allTransactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase(filter))
                .collect(Collectors.toList());

        // ============================================================
        // SET MODEL ATTRIBUTES FOR VIEW
        // ============================================================
        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("accountId", account.getId());
        model.addAttribute("accountBalance", account.getBalance());
        model.addAttribute("filter", filter);
        model.addAttribute("transactions", filtered);

        // ============================================================
        // RETURN VIEW
        // ============================================================
        return "transaction";
    }
}