package com.example.freshlife;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freshlife.utils.RecyclerViewSwipeDecorator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.recyclerview.widget.ItemTouchHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<ShoppingItem> shoppingItems = new ArrayList<>();
    private Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_shopping_list);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                Intent settingIntent = new Intent(ShoppingListActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_inventory) {
                Intent inventoryIntent = new Intent(ShoppingListActivity.this, MainActivity.class);
                startActivity(inventoryIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_shopping_list) {
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.shoppingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShoppingListAdapter(this, shoppingItems, this::showEditShoppingDialog);
        recyclerView.setAdapter(adapter);

        sortSpinner = findViewById(R.id.sortSpinner);
        setupSortSpinner();

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> showAddProductDialog());

        fetchShoppingItems();
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
                    updateRecyclerViewVisibility();

                    // Apply default sorting
                    sortShoppingItems(0); // Default to "Sort by A-Z"
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

    private void addShoppingItemToBackend(ShoppingItem newItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ShoppingItem> call = apiService.addShoppingItem(newItem);

        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShoppingItem addedItem = response.body();
                    shoppingItems.add(addedItem);
                    adapter.notifyItemInserted(shoppingItems.size() - 1);
                    updateRecyclerViewVisibility();
                    Toast.makeText(ShoppingListActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingItem> call, Throwable t) {
                Toast.makeText(ShoppingListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerViewVisibility() {
        TextView emptyShoppingListTextView = findViewById(R.id.emptyShoppingListTextView);

        if (shoppingItems.isEmpty()) {
            emptyShoppingListTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyShoppingListTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ShoppingItem itemToDelete = shoppingItems.get(position);
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

    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        EditText itemNameEditText = dialogView.findViewById(R.id.dialogItemNameEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);
        EditText quantityEditText = dialogView.findViewById(R.id.dialogQuantityEditText);

        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Product to Shopping List")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = itemNameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String quantityStr = quantityEditText.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                int quantity = Integer.parseInt(quantityStr);
                addOrUpdateShoppingItem(name, quantity, category);
                dialog.dismiss();
            }
        });

        sortShoppingItems(sortSpinner.getSelectedItemPosition());
    }

    private void addOrUpdateShoppingItem(String name, int quantity, String category) {
        ShoppingItem existingItem = null;
        for (ShoppingItem item : shoppingItems) {
            if (item.getName().equalsIgnoreCase(name)) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setCategory(category);
            updateShoppingItem(existingItem);
        } else {
            ShoppingItem newItem = new ShoppingItem(name, false, category, quantity);
            addShoppingItemToBackend(newItem);
        }

        sortShoppingItems(sortSpinner.getSelectedItemPosition());
    }

    private void deleteShoppingItem(String id, int position) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiService.deleteShoppingItem(id);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    shoppingItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateRecyclerViewVisibility();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ShoppingListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        });
    }

    private void updateShoppingItem(ShoppingItem item) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ShoppingItem> call = apiService.updateShoppingItem(item.getId(), item);

        call.enqueue(new Callback<ShoppingItem>() {
            @Override
            public void onResponse(Call<ShoppingItem> call, Response<ShoppingItem> response) {
                if (response.isSuccessful()) {
                    adapter.notifyDataSetChanged();

                    sortShoppingItems(sortSpinner.getSelectedItemPosition());

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

    private void showEditShoppingDialog(ShoppingItem shoppingItem, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        // Initialize dialog elements
        EditText itemNameEditText = dialogView.findViewById(R.id.dialogItemNameEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogCategorySpinner);
        EditText quantityEditText = dialogView.findViewById(R.id.dialogQuantityEditText);

        // Populate the fields with existing data
        itemNameEditText.setText(shoppingItem.getName());
        quantityEditText.setText(String.valueOf(shoppingItem.getQuantity()));

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

        // Override the PositiveButton to handle validation and update
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = itemNameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String quantityStr = quantityEditText.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter the item name", Toast.LENGTH_SHORT).show();
            } else if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Please enter the quantity", Toast.LENGTH_SHORT).show();
            } else {
                // Update the item
                int quantity = Integer.parseInt(quantityStr);
                shoppingItem.setName(name);
                shoppingItem.setCategory(category);
                shoppingItem.setQuantity(quantity);

                updateShoppingItem(shoppingItem);
                dialog.dismiss();
            }
        });

        sortShoppingItems(sortSpinner.getSelectedItemPosition());
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

    private void setupSortSpinner() {
        String[] sortOptions = {"Sort by A-Z", "Sort by Category"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortShoppingItems(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Apply default sorting
        sortShoppingItems(0); // Default to "Sort by A-Z"
    }

    private void sortShoppingItems(int sortOption) {
        List<ShoppingItem> uncheckedItems = new ArrayList<>();
        List<ShoppingItem> checkedItems = new ArrayList<>();

        // Separate checked and unchecked items
        for (ShoppingItem item : shoppingItems) {
            if (item.isChecked()) {
                checkedItems.add(item);
            } else {
                uncheckedItems.add(item);
            }
        }

        // Apply sorting based on the selected option
        Comparator<ShoppingItem> comparator;
        switch (sortOption) {
            case 0: // Sort by A-Z
                comparator = Comparator.comparing(ShoppingItem::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case 1: // Sort by Category
                comparator = Comparator.comparing(ShoppingItem::getCategory, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                comparator = (item1, item2) -> 0; // No sorting
        }

        uncheckedItems.sort(comparator);
        checkedItems.sort(comparator);

        // Combine unchecked and checked items back into the main list
        shoppingItems.clear();
        shoppingItems.addAll(uncheckedItems);
        shoppingItems.addAll(checkedItems);

        adapter.notifyDataSetChanged();
    }

}
