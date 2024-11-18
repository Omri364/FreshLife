package com.example.freshlife;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements FoodAdapter.OnDeleteClickListener {

    private RecyclerView foodItemsRecyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems = new ArrayList<>();

    // Define the ActivityResultLauncher
    private final ActivityResultLauncher<Intent> addFoodItemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the new FoodItem from the result
                    FoodItem newFoodItem = (FoodItem) result.getData().getSerializableExtra("newFoodItem");

                    // Add the new item to the list and notify the adapter
                    if (newFoodItem != null) {
                        foodItems.add(newFoodItem);
                        foodAdapter.notifyItemInserted(foodItems.size() - 1);  // Notify the adapter
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foodItemsRecyclerView = findViewById(R.id.foodItemsRecyclerView);
        foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodAdapter = new FoodAdapter(foodItems, this);
        foodItemsRecyclerView.setAdapter(foodAdapter);

        // Fetch and display existing food items from the backend
        fetchFoodItems();

        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {
            // Launch AddFoodItemActivity and wait for result
            Intent intent = new Intent(MainActivity.this, AddFoodItemActivity.class);
            addFoodItemLauncher.launch(intent);
        });
    }

    private void fetchFoodItems() {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<FoodItem>> call = apiService.getFoodItems();

        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful()) {
                    foodItems.clear();
                    foodItems.addAll(response.body());
                    foodAdapter.notifyDataSetChanged();
                } else {
                    Log.e("MainActivity", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                Log.e("MainActivity", "Failed to fetch food items", t);
            }
        });
    }


    @Override
    public void onDeleteClick(FoodItem foodItem, int position) {
        Log.d("MainActivity", "Attempting to delete item with ID: " + foodItem.getId());
        // Show a confirmation dialog before deleting the item
        new AlertDialog.Builder(this)
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> deleteFoodItem(foodItem, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteFoodItem(FoodItem foodItem, int position) {
        Log.d("MainActivity", "Deleting item with id: " + foodItem.getId()); // Log the id

        // Make DELETE request to the backend
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.deleteFoodItem(foodItem.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    foodItems.remove(position);
                    foodAdapter.notifyItemRemoved(position);
                    Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("MainActivity", "Delete request failed with code: " + response.code());
                    Toast.makeText(MainActivity.this, "Delete failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("MainActivity", "Failed to delete item", t);
            }
        });
    }
}

