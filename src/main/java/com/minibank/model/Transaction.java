package com.minibank.model;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private double amount;
    private String transactionType;
    private Timestamp transactionDate;

    public Transaction(int id, Integer senderAccountId, Integer receiverAccountId, double amount, String transactionType, Timestamp transactionDate){
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
    }

    public  int getId(){ return  id;}
    public Integer getSenderAccountId(){ return senderAccountId;}
    public Integer getReceiverAccountId() { return receiverAccountId;}
    public double getAmount(){return  amount;}
    public String getTransactionType(){return transactionType;}
    public Timestamp getTransactionDate(){return transactionDate;}
}
