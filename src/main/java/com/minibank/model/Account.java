package com.minibank.model;

public class Account {
    private int id;
    private  int userId;
    private double balance;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Account(int id, int userId, double balance){
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getId() {return id;}
    public int getUserId() {return userId;}
    public double getBalance() {return balance;}
    public  void setBalance(double balance) {this.balance = balance;}
}
