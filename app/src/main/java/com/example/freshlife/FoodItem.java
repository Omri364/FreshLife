package com.example.freshlife;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FoodItem implements Serializable {
    @SerializedName("_id")  // Maps MongoDB's _id to id
    private String id;
    private String name;
    private int quantity;
    private String expirationDate;
    private String category;

    // Constructor
    public FoodItem(String name, int quantity, String expirationDate, String category) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.category = category;
    }

    public FoodItem(String id, String name, int quantity, String expirationDate, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.category = category;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getCategory() { return category; }
}

