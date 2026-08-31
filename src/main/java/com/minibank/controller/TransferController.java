package com.minibank.controller;

import com.minibank.dao.AccountDao;
import com.minibank.dao.TransactionDao;
import com.minibank.dao.UserDao;
import com.minibank.model.Account;
import com.minibank.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TransferController {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final UserDao userDao;

    public TransferController(AccountDao accountDao, TransactionDao transactionDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.userDao = userDao;
    }

    @GetMapping("/transfer")
    public String showTransfer(HttpSession session, Model model) {

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

        return "transfer";
    }

    @PostMapping("/transfer")
    public String handleTransfer(@RequestParam("recipientNumber") String recipientNumber,
                                 @RequestParam("amount") double amount,
                                 Model model,
                                 HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Account senderAccount = accountDao.getAccountByUserId(user.getId());
        if (senderAccount == null) {
            return "redirect:/dashboard";
        }

        // Common model attributes
        model.addAttribute("userName", user.getFirstName());
        model.addAttribute("balance", senderAccount.getBalance());
        model.addAttribute("recipientNumber", recipientNumber);
        model.addAttribute("amount", amount);

        // ============================================================
        // VALIDATION 1: Cannot transfer to your own account
        // ============================================================
        if (recipientNumber.equals(user.getMobileNumber())) {
            model.addAttribute("errorMessage", "You cannot transfer money to your own account.");
            return "transfer";
        }

        // ============================================================
        // VALIDATION 2: Find recipient account by mobile number
        // ============================================================
        Account recipientAccount = accountDao.getAccountbyMobileNumber(recipientNumber);
        if (recipientAccount == null) {
            model.addAttribute("errorMessage", "User does not exist. Please check the mobile number.");
            return "transfer";
        }

        // ============================================================
        // VALIDATION 3: Amount must be greater than 0
        // ============================================================
        if (amount <= 0) {
            model.addAttribute("errorMessage", "Amount must be greater than 0.");
            return "transfer";
        }

        // ============================================================
        // VALIDATION 4: Insufficient balance
        // ============================================================
        double oldBalance = senderAccount.getBalance();
        if (amount > oldBalance) {
            model.addAttribute("errorMessage", "Insufficient balance. You have ₱" +
                    String.format("%.2f", oldBalance) + " available.");
            return "transfer";
        }

        // ============================================================
        // PROCESS TRANSFER
        // ============================================================
        double newSenderBalance = oldBalance - amount;
        double newRecipientBalance = recipientAccount.getBalance() + amount;

        // Update both accounts
        boolean senderUpdated = accountDao.updateBalance(senderAccount.getId(), newSenderBalance);
        boolean recipientUpdated = accountDao.updateBalance(recipientAccount.getId(), newRecipientBalance);

        if (!senderUpdated || !recipientUpdated) {
            model.addAttribute("errorMessage", "Transfer failed. Please try again.");
            return "transfer";
        }

        // Log transaction - matches the database schema
        transactionDao.logTransaction(senderAccount.getId(), recipientAccount.getId(), amount, "TRANSFER");

        // ============================================================
        // Get Recipient Name
        // ============================================================
        String recipientName = "Unknown User";
        try {
            User recipientUser = userDao.getUserById(recipientAccount.getUserId());
            if (recipientUser != null) {
                recipientName = recipientUser.getFirstName() + " " + recipientUser.getLastName();
            }
        } catch (Exception e) {
            recipientName = recipientNumber;
        }

        // ============================================================
        // SUCCESS - Return receipt data
        // ============================================================
        model.addAttribute("transferSuccess", true);
        model.addAttribute("oldBalance", oldBalance);
        model.addAttribute("newBalance", newSenderBalance);
        model.addAttribute("balance", newSenderBalance);
        model.addAttribute("amount", amount);
        model.addAttribute("recipientName", recipientName);
        model.addAttribute("recipientNumber", recipientNumber);

        return "transfer";
    }
}