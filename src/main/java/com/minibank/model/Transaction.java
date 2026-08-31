package com.minibank.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Transaction {

    private int id;
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private double amount;
    private String transactionType;
    private Timestamp transactionDate;

    // Display fields (not stored in database)
    private String senderName;
    private String receiverName;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    // Base constructor without names
    public Transaction(int id, Integer senderAccountId, Integer receiverAccountId,
                       double amount, String transactionType, Timestamp transactionDate) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
    }

    // Full constructor with sender and receiver names
    public Transaction(int id, Integer senderAccountId, Integer receiverAccountId,
                       double amount, String transactionType, Timestamp transactionDate,
                       String senderName, String receiverName) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    // ============================================================
    // GETTERS
    // ============================================================
    public int getId() {return id;}
    public Integer getSenderAccountId() {return senderAccountId;}
    public Integer getReceiverAccountId() {return receiverAccountId;}
    public double getAmount() {return amount;}
    public String getTransactionType() {return transactionType;}
    public Timestamp getTransactionDate() {return transactionDate;}
    public String getSenderName() {return senderName != null ? senderName : "Unknown";}
    public String getReceiverName() {return receiverName != null ? receiverName : "Unknown";}

    // ============================================================
    // THYMELEAF CONVENIENCE METHODS
    // ============================================================

    // Alias for transactionType
    public String getType() {
        return transactionType;}

    // Format date for display: "MMM d, yyyy - h:mm a"
    public String getDateFormatted() {
        if (transactionDate == null) return "";
        return new SimpleDateFormat("MMM d, yyyy - h:mm a").format(transactionDate);
    }

    // Format amount with commas: "1,500.00"
    public String getAmountFormatted() {
        return String.format("%,.2f", amount);
    }

    // Generate reference number: "#TX-0001"
    public String getReference() {
        return String.format("#TX-%04d", id);
    }
}