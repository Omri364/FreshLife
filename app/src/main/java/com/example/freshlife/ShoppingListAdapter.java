package com.example.freshlife;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    public ShoppingListAdapter(Context context, List<ShoppingItem> shoppingItems) {
        this.context = context;
        this.shoppingItems = shoppingItems;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            radioButton = itemView.findViewById(R.id.itemCheckBox);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }
    }
}
