package com.example.freshlife;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FoodItem implements Serializable {
    @SerializedName("_id")  // Maps MongoDB's _id to id
    private String id;
    private String uid; // Firebase user id
    private String name;
    private int quantity;
    private String expirationDate;
    private String category;
    private boolean replenishAutomatically;
    private String location;

    // Constructor
    public FoodItem(String uid, String name, int quantity, String expirationDate, String category,
                    boolean replenishAutomatically, String location) {
        this.uid = uid;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.category = category;
        this.replenishAutomatically = replenishAutomatically;
        this.location = location;
    }

    public FoodItem(String id, String uid, String name, int quantity, String expirationDate, String category,
                    boolean replenishAutomatically, String location) {
        this.uid = uid;
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.category = category;
        this.replenishAutomatically = replenishAutomatically;
        this.location = location;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getUid() { return uid; }
    public String getName() {
        return name;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public boolean getReplenishAutomatically() { return replenishAutomatically; }
    public String getLocation() {
        return location;
    }
    public String getCategory() { return category; }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setUid(String uid) {this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public void setCategory(String category) { this.category = category; }
    public void setReplenishAutomatically(boolean replenishAutomatically) { this.replenishAutomatically = replenishAutomatically; }
    public void setLocation(String location) {
        this.location = location;
    }
}

