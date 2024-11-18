package com.example.freshlife;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ShoppingItem implements Serializable {
    @SerializedName("_id")  // Maps MongoDB's _id to id
    private String id;
    private String name;
    private boolean isChecked;
    private String category;

    public ShoppingItem(String name, boolean isChecked, String category) {
        this.name = name;
        this.isChecked = isChecked;
        this.category = category;
    }

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
}
