package com.example.freshlife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodItemList;
    private OnDeleteClickListener onDeleteClickListener;

    public FoodAdapter(List<FoodItem> foodItemList, OnDeleteClickListener listener) {
        this.foodItemList = foodItemList;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);
        holder.foodNameTextView.setText(foodItem.getName());
        holder.foodExpirationTextView.setText(foodItem.getExpirationDate());

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(foodItem, position));
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    // ViewHolder class for the RecyclerView
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView;
        TextView foodExpirationTextView;
        ImageButton deleteButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodExpirationTextView = itemView.findViewById(R.id.foodExpirationTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // Interface for handling delete click events
    public interface OnDeleteClickListener {
        void onDeleteClick(FoodItem foodItem, int position);
    }
}
