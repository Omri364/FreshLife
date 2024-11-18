package com.example.freshlife;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShoppingListActivity extends AppCompatActivity {

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
    }
}