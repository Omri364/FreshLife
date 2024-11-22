package com.example.freshlife;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingItem> shoppingItems;
    private Context context;
    private final OnEditClickListener editClickListener;

    public ShoppingListAdapter(Context context, List<ShoppingItem> shoppingItems, OnEditClickListener editClickListener) {
        this.context = context;
        this.shoppingItems = shoppingItems;
        this.editClickListener = editClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shopping_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = shoppingItems.get(position);

        holder.itemName.setText(item.getName());
        holder.radioButton.setChecked(item.isChecked());

        // Change appearance based on whether the item is checked
        if (item.isChecked()) {
            holder.itemName.setTextColor(context.getResources().getColor(R.color.secondaryTextColor, context.getTheme()));
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
        } else {
            holder.itemName.setTextColor(context.getResources().getColor(R.color.textColor, context.getTheme()));
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        // Handle radio button clicks
        holder.radioButton.setOnClickListener(v -> {
            // Toggle the current checked state
            boolean currentChecked = !item.isChecked();
            item.setChecked(currentChecked); // Update the item state
            moveCheckedItems(); // Reorganize the list (checked items at the bottom)
            notifyDataSetChanged(); // Refresh RecyclerView
        });

        // Handle item click for editing
        holder.itemView.setOnClickListener(v -> editClickListener.onEditClick(item, position));

        // Set quantity
        holder.quantityTextView.setText("Qty: " + item.getQuantity());

        //TODO: put in a helper class
        // Set category icon based on category
        switch (item.getCategory()) {
            case "Dairy":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_dairy);
                break;
            case "Drink":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_drink);
                break;
            case "Dry Food":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_dry_food);
                break;
            case "Fish":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_fish);
                break;
            case "Meat":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_meat);
                break;
            case "Other":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_food);
                break;
            case "Sauce":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_sauce);
                break;
            case "Vegetable":
                holder.categoryIcon.setImageResource(R.drawable.ic_category_vegtable);
                break;
            default:
                holder.categoryIcon.setImageResource(R.drawable.ic_category_food); // Default icon
                break;
        }
    }

    @Override
    public int getItemCount() {
        return shoppingItems.size();
    }

    private void moveCheckedItems() {
        List<ShoppingItem> uncheckedItems = new ArrayList<>();
        List<ShoppingItem> checkedItems = new ArrayList<>();

        for (ShoppingItem item : shoppingItems) {
            if (item.isChecked()) {
                checkedItems.add(item);
            } else {
                uncheckedItems.add(item);
            }
        }

        shoppingItems.clear();
        shoppingItems.addAll(uncheckedItems);
        shoppingItems.addAll(checkedItems);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        RadioButton radioButton;
        View itemContainer;
        TextView quantityTextView;
        ImageView categoryIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            radioButton = itemView.findViewById(R.id.itemCheckBox);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            quantityTextView = itemView.findViewById(R.id.shoppingQuantityTextView);
            categoryIcon = itemView.findViewById(R.id.shoppingCategoryIcon);
        }
    }

    // Interface for item edit click listener
    public interface OnEditClickListener {
        void onEditClick(ShoppingItem shoppingItem, int position);
    }
}
