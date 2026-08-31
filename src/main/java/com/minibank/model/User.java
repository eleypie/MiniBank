package com.minibank.model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String pin;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public User(int id, String firstName, String lastName, String mobileNumber, String pin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.pin = pin;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobileNumber() { return mobileNumber; }
    public String getPin() { return pin; }
}