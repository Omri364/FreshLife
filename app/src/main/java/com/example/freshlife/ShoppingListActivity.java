package com.example.freshlife;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freshlife.utils.RecyclerViewSwipeDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;




public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_shopping_list);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                // Open SettingsActivity
                Intent settingIntent = new Intent(ShoppingListActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_inventory) {
                // Open MainActivity
                Intent inventoryIntent = new Intent(ShoppingListActivity.this, MainActivity.class);
                startActivity(inventoryIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_shopping_list) {
                // Stay in ShoppingListActivity
                return true;
            }
            return false;
        });


        recyclerView = findViewById(R.id.shoppingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShoppingListAdapter(this, shoppingItems, this::showEditShoppingDialog);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            // Show the dialog to add a product
            showAddProductDialog();
        });

        // Fetch shopping items from the backend
        fetchShoppingItems();

        // Setup swipe to delete
        setupSwipeToDelete();

    }

    private void fetchShoppingItems() {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<ShoppingItem>> call = apiService.getShoppingItems();

        call.enqueue(new Callback<List<ShoppingItem>>() {
            @Override
            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shoppingItems.clear();
                    shoppingItems.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("ShoppingListActivity", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                Log.e("ShoppingListActivity", "Failed to fetch shopping items", t);
            }
        });
    }


    private void addShoppingItem(ShoppingItem shoppingItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ShoppingItem> call = apiService.addShoppingItem(shoppingItem);

        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shoppingItems.add(response.body());
                    adapter.notifyItemInserted(shoppingItems.size() - 1);
                    Toast.makeText(ShoppingListActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingItem> call, Throwable t) {
                Toast.makeText(ShoppingListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteShoppingItem(String id, int position) {
        Log.d("ShoppingListActivity", "Deleting item with ID: " + id);

        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.deleteShoppingItem(id);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove the item from the list and notify the adapter
                    shoppingItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(ShoppingListActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position); // Revert swipe action
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ShoppingListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position); // Revert swipe action
            }
        });
    }

    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        // Initialize dialog elements
        EditText itemNameEditText = dialogView.findViewById(R.id.productNameInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);

        // Populate category spinner
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Build and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Product to Shopping List")
                .setView(dialogView)
                .setPositiveButton("Add", null)  // We'll override this button later for validation
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override the PositiveButton to handle validation and addition
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = itemNameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the item name", Toast.LENGTH_SHORT).show();
            } else {
                // Create a new ShoppingItem object
                ShoppingItem newItem = new ShoppingItem(name,false, category); // Assuming "false" for initial checked state

                // Add the item to the shopping list
                addShoppingItem(newItem);

                dialog.dismiss(); // Close the dialog
            }
        });
    }



    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // We are not handling drag & drop
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ShoppingItem itemToDelete = shoppingItems.get(position);

                // Call delete method
                deleteShoppingItem(itemToDelete.getId(), position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(ShoppingListActivity.this, R.color.deleteBackground))
                        .addActionIcon(R.drawable.ic_trash)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Show edit shopping item dialog
    private void showEditShoppingDialog(ShoppingItem shoppingItem, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        // Initialize dialog elements
        EditText itemNameEditText = dialogView.findViewById(R.id.productNameInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);

        // Populate the fields with existing data
        itemNameEditText.setText(shoppingItem.getName());

        // Populate category spinner
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(getCategoryIndex(shoppingItem.getCategory(), categories));

        // Build and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Shopping Item")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override the PositiveButton to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = itemNameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the item name", Toast.LENGTH_SHORT).show();
            } else {
                // Update the item
                shoppingItem.setName(name);
                shoppingItem.setCategory(category);

                updateShoppingItem(shoppingItem, position);
                dialog.dismiss();
            }
        });
    }

    // Update shopping item on the backend
    private void updateShoppingItem(ShoppingItem shoppingItem, int position) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ShoppingItem> call = apiService.updateShoppingItem(shoppingItem.getId(), shoppingItem);

        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful()) {
                    shoppingItems.set(position, shoppingItem);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(ShoppingListActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingItem> call, Throwable t) {
                Toast.makeText(ShoppingListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
}