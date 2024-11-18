package com.example.freshlife;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

        adapter = new ShoppingListAdapter(this, shoppingItems);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Product to Shopping List");

        // Inflate a custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        EditText productNameInput = dialogView.findViewById(R.id.productNameInput);
        EditText productCategoryInput = dialogView.findViewById(R.id.productCategoryInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String productName = productNameInput.getText().toString().trim();
            String productCategory = productCategoryInput.getText().toString().trim();

            if (!productName.isEmpty() && !productCategory.isEmpty()) {
                addShoppingItem(new ShoppingItem(productName, false, productCategory));
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
}