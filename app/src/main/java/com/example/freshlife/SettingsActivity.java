package com.example.freshlife;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.freshlife.utils.DataFetcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private EditText notificationDaysInput;
    private TextView notificationTimeTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                // Stay in SettingsActivity
                return true;
            } else if (item.getItemId() == R.id.navigation_inventory) {
                // Open MainActivity
                Intent inventoryIntent = new Intent(SettingsActivity.this, MainActivity.class);
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

        // Load saved preferences
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        int savedDays = sharedPreferences.getInt("notificationDays", 3);
        int savedHour = sharedPreferences.getInt("notificationHour", 9);
        int savedMinute = sharedPreferences.getInt("notificationMinute", 0);

        // Set saved values
        notificationDaysInput.setText(String.valueOf(savedDays));
        notificationTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", savedHour, savedMinute));

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
            });
        });
    }
}