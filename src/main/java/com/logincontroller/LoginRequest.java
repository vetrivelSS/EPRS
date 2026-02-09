package com.logincontroller;

public class LoginRequest {
    private String identifier; // Email or Phone
    private String password;

    // Default Constructor
    public LoginRequest() {}

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
