package com.example.freshlife;

public class FoodItem {

    private String name;
    private int quantity;
    private String expirationDate;

    // Constructor
    public FoodItem(String name, int quantity, String expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}

