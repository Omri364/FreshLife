package com.example.freshlife;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFoodItemActivity extends AppCompatActivity {

    private EditText nameEditText, quantityEditText, expirationDateEditText;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_item);

        // Initialize EditTexts and Button
        nameEditText = findViewById(R.id.nameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        expirationDateEditText = findViewById(R.id.expirationDateEditText);
        addButton = findViewById(R.id.addButton);

        // Set click listener for the add button
        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            String expirationDate = expirationDateEditText.getText().toString();

            // Create new FoodItem object
            FoodItem foodItem = new FoodItem(name, quantity, expirationDate);

            // Call method to add the food item
            addFoodItem(foodItem);
        });
    }

    private void addFoodItem(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<FoodItem> call = apiService.addFoodItem(foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddFoodItemActivity.this, "Food item added successfully!", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity and return to the main screen
                } else {
                    Toast.makeText(AddFoodItemActivity.this, "Failed to add food item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Toast.makeText(AddFoodItemActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

