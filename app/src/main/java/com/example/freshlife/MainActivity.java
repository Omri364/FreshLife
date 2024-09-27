package com.example.freshlife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView foodItemsRecyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        foodItemsRecyclerView = findViewById(R.id.foodItemsRecyclerView);
        foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Add Food Button
        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {
            // Start AddFoodItemActivity when button is clicked
            Intent intent = new Intent(MainActivity.this, AddFoodItemActivity.class);
            startActivity(intent);
        });

        // Fetch and display food items from the backend
        fetchFoodItems();
    }

    private void fetchFoodItems() {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<FoodItem>> call = apiService.getFoodItems();

        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful()) {
                    foodItems = response.body();
                    Log.d("MainActivity", "Fetched items: " + foodItems);

                    // Set up adapter and assign it to RecyclerView
                    foodAdapter = new FoodAdapter(foodItems);
                    foodItemsRecyclerView.setAdapter(foodAdapter);
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
}
