package com.example.freshlife;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.freshlife.utils.DataFetcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private String userUid;
    private FirebaseAuth mAuth;
    private TextView userEmailTextView;
    private EditText notificationDaysInput;
    private TextView notificationTimeTextView;
    private SharedPreferences sharedPreferences;
    private SwitchMaterial darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // TODO: change it to token?
        // Retrieve the UID from SharedPreferences
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        userUid = sharedPreferences.getString("uid", null);

        if (userUid == null) {
            // If UID is null, redirect to Login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Set user email
        userEmailTextView = findViewById(R.id.userEmailTextView);
        if (currentUser != null) {
            userEmailTextView.setText(currentUser.getEmail());
        } else {
            userEmailTextView.setText("Guest");
        }

        // Logout Button setup
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Show a confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        // Redirect to LoginActivity
                        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                // Stay in SettingsActivity
                return true;
            } else if (item.getItemId() == R.id.navigation_inventory) {
                // Open MainActivity
                Intent inventoryIntent = new Intent(SettingsActivity.this, InventoryActivity.class);
                startActivity(inventoryIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_shopping_list) {
                // Open ShoppingListActivity
                Intent shoppingListIntent = new Intent(SettingsActivity.this, ShoppingListActivity.class);
                startActivity(shoppingListIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Initialize UI elements
        notificationDaysInput = findViewById(R.id.notificationDaysInput);
        notificationTimeTextView = findViewById(R.id.notificationTimeTextView);
        MaterialButton saveSettingsButton = findViewById(R.id.saveSettingsButton);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Load saved preferences
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        int savedDays = sharedPreferences.getInt("notificationDays", 3);
        int savedHour = sharedPreferences.getInt("notificationHour", 9);
        int savedMinute = sharedPreferences.getInt("notificationMinute", 0);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);

        // Set saved values
        notificationDaysInput.setText(String.valueOf(savedDays));
        notificationTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", savedHour, savedMinute));
        darkModeSwitch.setChecked(isDarkMode);

        // Apply dark mode based on saved preference
        applyDarkMode(isDarkMode);

        // Open TimePickerDialog when time text is clicked
        notificationTimeTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = savedHour;
            int minute = savedMinute;

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        notificationTimeTextView.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();
        });

        // Handle dark mode toggle
        darkModeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();

            applyDarkMode(isChecked);

            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Save preferences on button click
        saveSettingsButton.setOnClickListener(v -> {
            String daysInput = notificationDaysInput.getText().toString().trim();

            if (daysInput.isEmpty()) {
                Toast.makeText(this, "Please enter the number of days", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedDays = Integer.parseInt(daysInput);
            String[] timeParts = notificationTimeTextView.getText().toString().split(":");
            int selectedHour = Integer.parseInt(timeParts[0]);
            int selectedMinute = Integer.parseInt(timeParts[1]);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notificationDays", selectedDays);
            editor.putInt("notificationHour", selectedHour);
            editor.putInt("notificationMinute", selectedMinute);
            editor.apply();

            Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();

            // Fetch items and schedule notifications
            DataFetcher.fetchFoodItemsFromDatabase(this, foodItems -> {
                NotificationScheduler.scheduleNotifications(this, foodItems);
            }, userUid);
        });
    }

    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
