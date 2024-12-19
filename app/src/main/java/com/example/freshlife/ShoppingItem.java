package com.example.freshlife;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Represents a shopping list item.
 * Each item includes a name, quantity, category, and a flag indicating if it's checked.
 */
public class ShoppingItem implements Serializable {
    @SerializedName("_id")  // Maps MongoDB's _id to id
    private String id;
    private String name;
    private boolean isChecked;
    private String category;
    private int quantity;

    /**
     * Constructs a new ShoppingItem instance.
     *
     * @param name      The name of the item.
     * @param isChecked Whether the item is checked off.
     * @param category  The category of the item.
     * @param quantity  The quantity of the item.
     */
    public ShoppingItem(String name, boolean isChecked, String category, int quantity) {
        this.name = name;
        this.isChecked = isChecked;
        this.category = category;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public int getQuantity() {return this.quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
