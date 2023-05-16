package com.example.AnonymousChat.model;

public enum Report {
    SALES("Sales Report"),
    FINANCIAL("Financial Report"),
    INVENTORY("Inventory Report"),
    CUSTOMER("Customer Report");

    private final String displayName;

    Report(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
