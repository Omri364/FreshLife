package com.example.freshlife;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.freshlife.utils.DataFetcher;
import com.example.freshlife.utils.RecyclerViewSwipeDecorator;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements FoodAdapter.OnDeleteClickListener {

    private RecyclerView foodItemsRecyclerView;
    private Spinner locationFilterSpinner;
    private Spinner sortSpinner;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems = new ArrayList<>();
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private List<String> locations = new ArrayList<>(Arrays.asList("All", "Unsorted", "Fridge"));
    private String selectedLocation = "All";
    private static final String LOCATIONS_KEY = "locations_key";
    private SharedPreferences sharedPreferences;


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
    protected void onStart() {
        super.onStart();

        // Fetch items and schedule notifications
        fetchFoodItems();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foodItemsRecyclerView = findViewById(R.id.foodItemsRecyclerView);
        foodItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodAdapter = new FoodAdapter(this, foodItems, this ,this::showEditFoodDialog);
        foodItemsRecyclerView.setAdapter(foodAdapter);

        // Check and request POST_NOTIFICATIONS permission for Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Spinner for sorting
        sortSpinner = findViewById(R.id.sortSpinner);
        String[] sortOptions = getResources().getStringArray(R.array.sort_options); // Sorting options array in strings.xml
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        // apply dark mode if selected
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        applyDarkMode(isDarkMode);

        // load storage locations
        loadLocations();
        LinearLayout locationButtonContainer = findViewById(R.id.locationButtonContainer);
        generateLocationButtons(locationButtonContainer);

        // Handle sorting selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortFoodItems();
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

        // Add swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // No drag-and-drop functionality
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                FoodItem foodItem = foodItems.get(position);

                // Delete item and check if it should be added to the shopping list
                deleteFoodItem(foodItem, position);

                if (foodItem.getReplenishAutomatically()) {
                    addToShoppingList(foodItem);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.deleteBackground))
                        .addActionIcon(R.drawable.ic_trash)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        itemTouchHelper.attachToRecyclerView(foodItemsRecyclerView);
    }

    private void fetchFoodItems() {
        DataFetcher.fetchFoodItemsFromDatabase(this, foodItems -> {
            // Clear and update the list
            this.foodItems.clear();
            this.foodItems.addAll(foodItems);

            // Apply filtering and sorting after fetching
            filterAndSortFoodItems();

            // Notify adapter of data changes
            foodAdapter.notifyDataSetChanged();

            // Schedule notifications for fetched items
            Log.d("MainActivity", "Scheduling notifications for fetched items.");
            NotificationScheduler.scheduleNotifications(this, foodItems);
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
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.deleteFoodItem(foodItem.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove the item from the main list
                    foodItems.removeIf(item -> item.getId().equals(foodItem.getId()));

                    // Apply filtering and sorting again
                    filterAndSortFoodItems();

                    Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete item: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        Spinner locationSpinner = dialogView.findViewById(R.id.dialogLocationSpinner);
        CheckBox replenishCheckBox = dialogView.findViewById(R.id.dialogReplenishCheckBox);

        // Set up category spinner
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Populate location spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locations.subList(1, locations.size()) // Exclude "All"
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(spinnerAdapter);

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
            String location = locationSpinner.getSelectedItem().toString();
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
                addFoodItem(new FoodItem(name, quantity, expirationDate, category ,replenishAutomatically, location));
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
                    FoodItem addedItem = response.body();
                    if (addedItem != null) {
                        // Add the new item to the current food items list
                        foodItems.add(addedItem);
                        // Filter and sort the list based on current location and sorting options
                        filterAndSortFoodItems();
                        // Notify the adapter to update the RecyclerView
                        foodAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(MainActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
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
        ShoppingItem shoppingItem = new ShoppingItem(foodItem.getName(), false, foodItem.getCategory(), foodItem.getQuantity());

        Call<ShoppingItem> call = apiService.addShoppingItem(shoppingItem);
        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Added to shopping list", Toast.LENGTH_SHORT).show();
//                    fetchShoppingItems(); // Refresh the shopping list
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


    // Show edit food item dialog
    private void showEditFoodDialog(FoodItem foodItem, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_food, null);

        // Initialize dialog elements
        EditText foodNameEditText = dialogView.findViewById(R.id.dialogFoodNameEditText);
        EditText quantityEditText = dialogView.findViewById(R.id.dialogQuantityEditText);
        TextView expirationDateTextView = dialogView.findViewById(R.id.dialogExpirationDateTextView);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);
        Spinner locationSpinner = dialogView.findViewById(R.id.dialogLocationSpinner);
        CheckBox replenishCheckBox = dialogView.findViewById(R.id.dialogReplenishCheckBox);

        // Populate the fields with existing data
        foodNameEditText.setText(foodItem.getName());
        quantityEditText.setText(String.valueOf(foodItem.getQuantity()));
        expirationDateTextView.setText(foodItem.getExpirationDate());
        replenishCheckBox.setChecked(foodItem.getReplenishAutomatically());

        // Populate category spinner
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(getCategoryIndex(foodItem.getCategory(), categories));

        // Populate location spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locations.subList(1, locations.size()) // Exclude "All"
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(spinnerAdapter);

        // Pre-select the correct location for editing
        locationSpinner.setSelection(spinnerAdapter.getPosition(foodItem.getLocation()));

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
                .setTitle("Edit Food Item")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override the PositiveButton to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = foodNameEditText.getText().toString().trim();
            String quantityStr = quantityEditText.getText().toString().trim();
            String expirationDate = expirationDateTextView.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String location = locationSpinner.getSelectedItem().toString();
            boolean replenishAutomatically = replenishCheckBox.isChecked();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the food name", Toast.LENGTH_SHORT).show();
            } else if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Please enter the quantity", Toast.LENGTH_SHORT).show();
            } else if (expirationDate.isEmpty()) {
                Toast.makeText(this, "Please select an expiration date", Toast.LENGTH_SHORT).show();
            } else {
                int quantity = Integer.parseInt(quantityStr);

                // Update the item
                foodItem.setName(name);
                foodItem.setQuantity(quantity);
                foodItem.setExpirationDate(expirationDate);
                foodItem.setCategory(category);
                foodItem.setLocation(location);
                foodItem.setReplenishAutomatically(replenishAutomatically);

                updateFoodItem(foodItem);
                dialog.dismiss();
            }
        });
    }

    // Update food item on the backend
    private void updateFoodItem(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<FoodItem> call = apiService.updateFoodItem(foodItem.getId(), foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int position = foodItems.indexOf(foodItem);
                    foodItems.set(position, foodItem);
                    filterAndSortFoodItems(); // Update view
                    foodAdapter.notifyItemChanged(position);

//                    // Print food items list
//                    Log.d("FoodItemsList", "Current foodItems list:");
//                    for (int i = 0; i < foodItems.size(); i++) {
//                        FoodItem item = foodItems.get(i);
//                        Log.d("FoodItemsList", String.format("Position %d: %s (Quantity: %d, Category: %s, Expiration: %s)",
//                                i, item.getName(), item.getQuantity(), item.getCategory(), item.getExpirationDate()));
//                    }

                    Toast.makeText(MainActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get category index for spinner
    private int getCategoryIndex(String category, String[] categories) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) {
                return i;
            }
        }
        return 0; // Default to the first category
    }

    private void filterByLocation(String location) {
        List<FoodItem> filteredItems;
        if (location.equals("All Locations")) {
            filteredItems = new ArrayList<>(foodItems);
        } else {
            filteredItems = new ArrayList<>();
            for (FoodItem item : foodItems) {
                if (item.getLocation().equals(location)) {
                    filteredItems.add(item);
                }
            }
        }
        foodAdapter.updateList(filteredItems);
    }

    private void filterAndSortFoodItems() {
        List<FoodItem> filteredItems = new ArrayList<>();

        // Filter by location
        if (selectedLocation.equals("All")) {
            filteredItems.addAll(foodItems); // Show all items
        } else {
            for (FoodItem item : foodItems) {
                if (item.getLocation().equalsIgnoreCase(selectedLocation)) {
                    filteredItems.add(item);
                }
            }
        }

        // Apply sorting
        String selectedSortOption = sortSpinner.getSelectedItem().toString();
        switch (selectedSortOption) {
            case "Sort By A-Z":
                Collections.sort(filteredItems, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                break;
            case "Sort By Category":
                Collections.sort(filteredItems, (o1, o2) -> o1.getCategory().compareToIgnoreCase(o2.getCategory()));
                break;
            case "Sort By Expiration":
                filteredItems.sort((o1, o2) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date1 = sdf.parse(o1.getExpirationDate());
                        Date date2 = sdf.parse(o2.getExpirationDate());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                });
                break;
        }

        // Update the adapter
        foodAdapter.updateList(filteredItems);

        // Toggle visibility of the "No items to show" message
        TextView emptyListTextView = findViewById(R.id.emptyListTextView);
        if (filteredItems.isEmpty()) {
            emptyListTextView.setVisibility(View.VISIBLE);
            foodItemsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.GONE);
            foodItemsRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void generateLocationButtons(LinearLayout locationButtonContainer) {
        locationButtonContainer.removeAllViews();

        // Add the edit button at the beginning
        FloatingActionButton editButton = new FloatingActionButton(this);
        editButton.setImageResource(R.drawable.ic_edit);
        editButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.editLocationButtonBackground)));
        editButton.setColorFilter(ContextCompat.getColor(this, R.color.editLocationsIcon), PorterDuff.Mode.SRC_IN);
        editButton.setSize(FloatingActionButton.SIZE_MINI); // Smaller size for better alignment
        editButton.setOnClickListener(v -> showEditLocationsDialog());

        // Create a layout for proper padding/margin for the edit button
        LinearLayout.LayoutParams editButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editButtonParams.setMargins(8, 0, 16, 0); // Add some spacing
        locationButtonContainer.addView(editButton, editButtonParams);

        // Add location buttons
        for (String location : locations) {
            Button button = new Button(this);
            button.setText(location);
            button.setTextColor(ContextCompat.getColor(this, R.color.locationButtonText));
            button.setBackgroundResource(R.drawable.default_button_background);

            // Highlight the selected button
            if (location.equals(selectedLocation)) {
                button.setBackgroundResource(R.drawable.selected_button_background);
            }

            button.setOnClickListener(v -> {
                selectedLocation = location;

                // Highlight the selected button and reset others
                updateButtonStyles(locationButtonContainer);
                filterAndSortFoodItems();
            });

            // Add proper spacing for location buttons
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(16, 0, 16, 0); // Add space between buttons
            locationButtonContainer.addView(button, buttonParams);
        }
    }


    private void updateButtonStyles(LinearLayout locationButtonContainer) {
        for (int i = 0; i < locationButtonContainer.getChildCount(); i++) {
            View view = locationButtonContainer.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;

                if (button.getText().toString().equals(selectedLocation)) {
                    button.setBackgroundResource(R.drawable.selected_button_background);
                } else {
                    button.setBackgroundResource(R.drawable.default_button_background);
                }
            }
        }
    }


    private int getLocationIndex(String location, String[] locations) {
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].equalsIgnoreCase(location)) {
                return i;
            }
        }
        return 0; // Default to the first location
    }

    private void showEditLocationsDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_locations, null);

        // Initialize the dialog elements
        RecyclerView locationsRecyclerView = dialogView.findViewById(R.id.locationsRecyclerView);
        EditText newLocationEditText = dialogView.findViewById(R.id.newLocationEditText);
        Button addLocationButton = dialogView.findViewById(R.id.addLocationButton);

        // Filter out "All" and "Unsorted" from the locations
        List<String> editableLocations = new ArrayList<>();
        for (String location : locations) {
            if (!location.equals("All") && !location.equals("Unsorted")) {
                editableLocations.add(location);
            }
        }

        // RecyclerView setup for editing locations
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the LocationAdapter with filtered locations
        LocationAdapter locationAdapter = new LocationAdapter(this, editableLocations, position -> {
            // Remove the location from the list
            String deletedLocation = editableLocations.get(position);
            editableLocations.remove(position);
            locations.remove(deletedLocation); // Update the main locations list
            // Notify the adapter that the data has changed
            locationsRecyclerView.getAdapter().notifyItemRemoved(position);

            // Handle items from deleted location
            moveItemsToUnsorted(deletedLocation);

            // Regenerate the location buttons dynamically
            generateLocationButtons((LinearLayout) findViewById(R.id.locationButtonContainer));
        });
        // Set the adapter to the RecyclerView
        locationsRecyclerView.setAdapter(locationAdapter);

        // Add new location
        addLocationButton.setOnClickListener(v -> {
            String newLocation = newLocationEditText.getText().toString().trim();
            if (!newLocation.isEmpty() && !locations.contains(newLocation)) {
                locations.add(newLocation);
                editableLocations.add(newLocation); // Also add to the filtered list
                locationAdapter.notifyItemInserted(editableLocations.size() - 1);
                generateLocationButtons((LinearLayout) findViewById(R.id.locationButtonContainer));
            }
        });

        // Show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Locations")
                .setView(dialogView)
                .setPositiveButton("Done", (dialog, which) -> {
                    saveLocations(); // Save updated locations to SharedPreferences
                })
                .show();
    }


    /**
     * Move items from a deleted location to "Unsorted".
     */
    private void moveItemsToUnsorted(String deletedLocation) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        // Iterate through all items and update those in the deleted location
        for (FoodItem item : foodItems) {
            if (item.getLocation().equalsIgnoreCase(deletedLocation)) {
                item.setLocation("Unsorted");

                // Update item in backend
                Call<FoodItem> call = apiService.updateFoodItem(item.getId(), item);
                call.enqueue(new Callback<FoodItem>() {
                    @Override
                    public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                        if (response.isSuccessful()) {
                            Log.d("MainActivity", "Item moved to Unsorted: " + item.getName());
                        } else {
                            Log.e("MainActivity", "Failed to update item: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<FoodItem> call, Throwable t) {
                        Log.e("MainActivity", "Error moving item to Unsorted", t);
                    }
                });
            }
        }
    }

    // Save locations to SharedPreferences
    private void saveLocations() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder locationsString = new StringBuilder();
        for (String location : locations) {
            locationsString.append(location).append(",");
        }
        // Remove the trailing comma
        if (locationsString.length() > 0) {
            locationsString.setLength(locationsString.length() - 1);
        }
        editor.putString(LOCATIONS_KEY, locationsString.toString());
        editor.apply();
    }

    // Load locations from SharedPreferences
    private void loadLocations() {
        String savedLocations = sharedPreferences.getString(LOCATIONS_KEY, null);
        locations.clear();
        if (savedLocations != null) {
            Collections.addAll(locations, savedLocations.split(","));
        } else {
            // Default locations
            locations.add("All");
            locations.add("Unsorted");
            locations.add("Fridge");
        }
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}

