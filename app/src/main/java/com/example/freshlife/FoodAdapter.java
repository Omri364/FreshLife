package com.example.freshlife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodItemList;
    private OnDeleteClickListener onDeleteClickListener;
    private Context context;
    private final OnEditClickListener editClickListener;

    public FoodAdapter(Context context, List<FoodItem> foodItemList, OnDeleteClickListener listener, OnEditClickListener editClickListener) {
        this.context = context;
        this.foodItemList = foodItemList;
        this.onDeleteClickListener = listener;
        this.editClickListener = editClickListener;
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

        // Calculate days until expiration
        String expirationInfo = calculateDaysUntilExpiration(foodItem.getExpirationDate());
        holder.expirationInfoTextView.setText(expirationInfo);

        // Set background color based on expiration
        int backgroundColor = determineBackgroundColor(foodItem.getExpirationDate());
        holder.itemView.setBackgroundColor(backgroundColor);

        // Set category icon based on category
        switch (foodItem.getCategory()) {
            case "Dairy":
                holder.categoryIcon.setImageResource(R.drawable.ic_dairy);
                break;
            case "Vegetable":
                holder.categoryIcon.setImageResource(R.drawable.ic_vegtable);
                break;
            case "Meat":
                holder.categoryIcon.setImageResource(R.drawable.ic_meat);
                break;
            case "Drink":
                holder.categoryIcon.setImageResource(R.drawable.ic_drink);
                break;
            default:
                holder.categoryIcon.setImageResource(R.drawable.ic_food); // Default icon
                break;
        }

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(foodItem, position));

        // Handle item click for editing
        holder.itemView.setOnClickListener(v -> editClickListener.onEditClick(foodItem, position));
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    // ViewHolder class for the RecyclerView
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView;
        TextView expirationInfoTextView;
        ImageButton deleteButton;
        ImageView categoryIcon;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            expirationInfoTextView = itemView.findViewById(R.id.expirationInfoTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
        }
    }

    // Interface for handling delete click events
    public interface OnDeleteClickListener {
        void onDeleteClick(FoodItem foodItem, int position);
    }

    // Interface for item edit click listener
    public interface OnEditClickListener {
        void onEditClick(FoodItem foodItem, int position);
    }

    private String calculateDaysUntilExpiration(String expirationDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Parse the expiration date
            Date expiry = sdf.parse(expirationDate);

            // Get today's date without time (to only compare dates)
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();

            // Calculate the difference in days
            long diff = expiry.getTime() - today.getTime();
            long days = diff / (24 * 60 * 60 * 1000); // Convert milliseconds to days

            if (days > 0) {
                return "Expires in " + days + " days";
            } else if (days == 0) {
                return "Expires today";
            } else {
                return "Expired " + Math.abs(days) + " days ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }

    private int determineBackgroundColor(String expirationDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date expiry = sdf.parse(expirationDate);
            Date today = new Date();
            long diff = expiry.getTime() - today.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            if (days < 0) {
                // Expired (light red)
                return ContextCompat.getColor(context, R.color.light_red);
            } else if (days <= 3) {
                // Expiring in 3 days (light yellow)
                return ContextCompat.getColor(context, R.color.light_yellow);
            } else {
                // Default background (white)
                return ContextCompat.getColor(context, R.color.white);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ContextCompat.getColor(context, R.color.white); // Default color
        }
    }

    // Updates the food item list and refreshes the RecyclerView
    public void updateList(List<FoodItem> newFoodItemList) {
        this.foodItemList = new ArrayList<>(newFoodItemList);
        notifyDataSetChanged();
    }
}
