package com.example.freshlife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The `ShoppingListAdapter` class is the RecyclerView adapter for displaying shopping list items.
 * It manages the list of items, their display, and user interactions such as checking off items or editing them.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingItem> shoppingItems;
    private Context context;
    private final OnEditClickListener editClickListener;

    /**
     * Constructor for the `ShoppingListAdapter`.
     *
     * @param context           The context in which the adapter is used.
     * @param shoppingItems     The list of shopping items to display.
     * @param editClickListener The listener for edit click events.
     */
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
            holder.itemName.setTextColor(context.getResources().getColor(R.color.itemSecondaryTextColor, context.getTheme()));
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.checkedItem));
        } else {
            holder.itemName.setTextColor(context.getResources().getColor(R.color.itemTextColor, context.getTheme()));
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.defaultItem));
        }

        // Handle radio button clicks
        holder.radioButton.setOnClickListener(v -> {
            // Toggle the current checked state
            boolean currentChecked = !item.isChecked();
            item.setChecked(currentChecked); // Update the item state
            moveCheckedItems(); // Reorganize the list (checked items at the bottom)
            // Call the sorting logic
            if (context instanceof ShoppingListActivity) {
                ((ShoppingListActivity) context).sortShoppingList();
                ((ShoppingListActivity) context).updateShoppingItemBackend(item);
            }
            notifyDataSetChanged(); // Refresh RecyclerView
        });

        // Handle item click for editing
        holder.itemView.setOnClickListener(v -> editClickListener.onEditClick(item, position));

        // Set quantity
        holder.quantityTextView.setText("Qty: " + item.getQuantity());

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

    /**
     * Updates lists of checked and unchecked items
     */
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

    /**
     * Interface for item edit click listener
     */
    public interface OnEditClickListener {
        void onEditClick(ShoppingItem shoppingItem, int position);
    }
}
