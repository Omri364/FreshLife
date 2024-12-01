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
import com.google.firebase.auth.FirebaseAuth;

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

public class InventoryActivity extends AppCompatActivity implements FoodAdapter.OnDeleteClickListener {

    private String userUid;
    private String token;
    private RecyclerView foodItemsRecyclerView;
    private Spinner sortSpinner;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodItems = new ArrayList<>();
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private List<String> locations = new ArrayList<>(Arrays.asList("All", "Fridge", "Pantry"));
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
        setContentView(R.layout.activity_inventory);

        // Retrieve the UID from SharedPreferences
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        userUid = sharedPreferences.getString("uid", null);
        token = "Bearer " + sharedPreferences.getString("authToken", null);

        if (userUid == null) {
            // If UID is null, redirect to Login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Setup RecyclerView and Adapter
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

        // set storage locations
        setupLocationButtons();

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
                Intent settingsIntent = new Intent(InventoryActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_inventory) {
                // Stay in MainActivity (Inventory tab)
                return true;
            } else if (item.getItemId() == R.id.navigation_shopping_list) {
                // Open ShoppingListActivity
                Intent shoppingListIntent = new Intent(InventoryActivity.this, ShoppingListActivity.class);
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
                        .addBackgroundColor(ContextCompat.getColor(InventoryActivity.this, R.color.deleteBackground))
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
            Log.d("InventoryActivity", "Scheduling notifications for fetched items.");
            NotificationScheduler.scheduleNotifications(this, foodItems);
        }, userUid);
    }

    @Override
    public void onDeleteClick(FoodItem foodItem, int position) {
        Log.d("InventoryActivity", "Attempting to delete item with ID: " + foodItem.getId());
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
        Call<Void> call = apiService.deleteFoodItem(token, foodItem.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove the item from the main list
                    foodItems.removeIf(item -> item.getId().equals(foodItem.getId()));

                    // Apply filtering and sorting again
                    filterAndSortFoodItems();

                    Toast.makeText(InventoryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InventoryActivity.this, "Failed to delete item: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(InventoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                foodNameEditText.setError("Please enter the food name");
                foodNameEditText.requestFocus();
            }
            if (quantityStr.isEmpty()) {
                quantityEditText.setError("Please enter the quantity");
                quantityEditText.requestFocus();
            }
            if (expirationDate.isEmpty()) {
                expirationDateTextView.setError("Please enter expiration date");
                expirationDateTextView.requestFocus();
            }
            if (!name.isEmpty() && !quantityStr.isEmpty() && !expirationDate.isEmpty()) {
                // All inputs are valid; proceed with adding the item
                int quantity = Integer.parseInt(quantityStr);
                addFoodItem(new FoodItem(userUid, name, quantity, expirationDate, category ,replenishAutomatically, location));
                dialog.dismiss(); // Close the dialog
            }
        });
    }

    private void addFoodItem(FoodItem foodItem) {
        //TODO: change to simpler way

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();

                // Proceed with adding the food item using the token
                ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
                Call<FoodItem> call = apiService.addFoodItem("Bearer " + token, foodItem);

                call.enqueue(new Callback<FoodItem>() {
                    @Override
                    public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                        if (response.isSuccessful()) {
                            FoodItem addedItem = response.body();
                            if (addedItem != null) {
                                foodItems.add(addedItem);
                                filterAndSortFoodItems();
                                foodAdapter.notifyDataSetChanged();
                                Toast.makeText(InventoryActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(InventoryActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FoodItem> call, Throwable t) {
                        Toast.makeText(InventoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("InventoryActivity", "Failed to get token", task.getException());
                Toast.makeText(InventoryActivity.this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Add item to shopping list
    private void addToShoppingList(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        ShoppingItem shoppingItem = new ShoppingItem(foodItem.getName(), false, foodItem.getCategory(), foodItem.getQuantity());

//        String token = "Bearer " + sharedPreferences.getString("authToken", "");

        Call<ShoppingItem> call = apiService.addShoppingItem(token, shoppingItem);
        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InventoryActivity.this, "Added to shopping list", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InventoryActivity.this, "Failed to add to shopping list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingItem> call, Throwable t) {
                Toast.makeText(InventoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                foodNameEditText.setError("Please enter the food name");
                foodNameEditText.requestFocus();
            }
            if (quantityStr.isEmpty()) {
                quantityEditText.setError("Please enter the quantity");
                quantityEditText.requestFocus();
            }
            if (expirationDate.isEmpty()) {
                expirationDateTextView.setError("Please enter the quantity");
                expirationDateTextView.requestFocus();
            }
            if (!name.isEmpty() && !quantityStr.isEmpty() && !expirationDate.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
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
        foodItem.setUid(userUid); // Attach UID to the food item

//        String token = "Bearer " + sharedPreferences.getString("authToken", "");

        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<FoodItem> call = apiService.updateFoodItem(token, foodItem.getId(), foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int position = foodItems.indexOf(foodItem);
                    foodItems.set(position, foodItem);
                    filterAndSortFoodItems(); // Update view
                    foodAdapter.notifyItemChanged(position);
                    Toast.makeText(InventoryActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InventoryActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Toast.makeText(InventoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void setupLocationButtons() {
        Button buttonAll = findViewById(R.id.buttonAll);
        Button buttonFridge = findViewById(R.id.buttonFridge);
        Button buttonPantry = findViewById(R.id.buttonPantry);

        View.OnClickListener locationClickListener = v -> {
            Button clickedButton = (Button) v;
            selectedLocation = clickedButton.getText().toString();

            // Update styles for all buttons
            buttonAll.setBackgroundColor(ContextCompat.getColor(this, selectedLocation.equals("All")
                    ? R.color.selectedButtonLocationBackground
                    : R.color.buttonLocationBackground));

            buttonFridge.setBackgroundColor(ContextCompat.getColor(this, selectedLocation.equals("Fridge")
                    ? R.color.selectedButtonLocationBackground
                    : R.color.buttonLocationBackground));

            buttonPantry.setBackgroundColor(ContextCompat.getColor(this, selectedLocation.equals("Pantry")
                    ? R.color.selectedButtonLocationBackground
                    : R.color.buttonLocationBackground));


            // Filter and sort the items based on the selected location
            filterAndSortFoodItems();
        };

        // Attach click listeners
        buttonAll.setOnClickListener(locationClickListener);
        buttonFridge.setOnClickListener(locationClickListener);
        buttonPantry.setOnClickListener(locationClickListener);

        filterAndSortFoodItems();
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}