package com.example.freshlife;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.view.LayoutInflater;


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

        foodAdapter = new FoodAdapter(this, foodItems, this);
        foodItemsRecyclerView.setAdapter(foodAdapter);

        // Spinner for sorting
        Spinner sortSpinner = findViewById(R.id.sortSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options, // String array defined in res/values/strings.xml
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);



        // Handle sorting selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Alphabetical
                        sortAlphabetically();
                        break;
                    case 1: // By Category
                        sortByCategory();
                        break;
                    case 2: // By Expiration Date
                        sortByExpiration();
                        break;
                }
                foodAdapter.notifyDataSetChanged(); // Refresh RecyclerView
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Fetch and display existing food items from the backend
        fetchFoodItems();

        // Set default sorting to "Sort by Expiration" (index 2 in the string array)
        sortSpinner.setSelection(2); // Set default selection (0-based index)

        FloatingActionButton addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> showAddFoodDialog());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_inventory);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_inventory) {
                // Stay in MainActivity (Inventory tab)
                return true;
            } else if (item.getItemId() == R.id.navigation_shopping_list) {
                // Open ShoppingListActivity
                Intent shoppingListIntent = new Intent(MainActivity.this, ShoppingListActivity.class);
                startActivity(shoppingListIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
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
                    sortByExpiration(); // apply default sorting
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteFoodItem(foodItem, position);
                    // If replenishAutomatically is checked, add to shopping list
                    if (foodItem.getReplenishAutomatically()) {
                        addToShoppingList(foodItem);
                    }
                })
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

    // Sorting methods
    private void sortAlphabetically() {
        Collections.sort(foodItems, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    }

    private void sortByCategory() {
        Collections.sort(foodItems, (o1, o2) -> o1.getCategory().compareToIgnoreCase(o2.getCategory()));
    }

    private void sortByExpiration() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Collections.sort(foodItems, (o1, o2) -> {
            try {
                Date date1 = sdf.parse(o1.getExpirationDate());
                Date date2 = sdf.parse(o2.getExpirationDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    private void showAddFoodDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_food, null);

        // Initialize dialog elements
        EditText foodNameEditText = dialogView.findViewById(R.id.dialogFoodNameEditText);
        EditText quantityEditText = dialogView.findViewById(R.id.dialogQuantityEditText);
        TextView expirationDateTextView = dialogView.findViewById(R.id.dialogExpirationDateTextView);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);
        CheckBox replenishCheckBox = dialogView.findViewById(R.id.dialogReplenishCheckBox);

        // Set up category spinner
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set up expiration date picker
        expirationDateTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        expirationDateTextView.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Build and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Food Item")
                .setView(dialogView)
                .setPositiveButton("Add", null) // Set to null initially
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override the PositiveButton to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = foodNameEditText.getText().toString().trim();
            String quantityStr = quantityEditText.getText().toString().trim();
            String expirationDate = expirationDateTextView.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            boolean replenishAutomatically = replenishCheckBox.isChecked();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the food name", Toast.LENGTH_SHORT).show();
            } else if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Please enter the quantity", Toast.LENGTH_SHORT).show();
            } else if (expirationDate.isEmpty()) {
                Toast.makeText(this, "Please select an expiration date", Toast.LENGTH_SHORT).show();
            } else if (category.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            } else {
                // All inputs are valid; proceed with adding the item
                int quantity = Integer.parseInt(quantityStr);
                addFoodItem(new FoodItem(name, quantity, expirationDate, category ,replenishAutomatically));
                dialog.dismiss(); // Close the dialog
            }
        });
    }

    private void addFoodItem(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<FoodItem> call = apiService.addFoodItem(foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful()) {
                    foodItems.add(response.body());
                    foodAdapter.notifyItemInserted(foodItems.size() - 1);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add item to shopping list
    private void addToShoppingList(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        ShoppingItem shoppingItem = new ShoppingItem(foodItem.getName(),false, foodItem.getCategory());

        Call<ShoppingItem> call = apiService.addShoppingItem(shoppingItem);
        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Added to shopping list", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add to shopping list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingItem> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

