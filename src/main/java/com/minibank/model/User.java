package com.minibank.model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
    private String pin;

    public User(int id, String firstName, String lastName, String mobileNumber, String email, String pin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.pin = pin;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobileNumber() { return mobileNumber; }
    public String getPin() { return pin; }
}