package com.example.freshlife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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

public class SettingsActivity extends AppCompatActivity {

    private SeekBar notificationDaysSeekBar;
    private TextView notificationDaysText;
    private TimePicker notificationTimePicker;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) {
                // Stay in SettingsActivity
                return true;
            }
            else if (item.getItemId() == R.id.navigation_inventory) {
                // Open MainActivity
                Intent inventoryIntent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(inventoryIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (item.getItemId() == R.id.navigation_shopping_list) {
                // Open ShoppingListActivity
                Intent shoppingListIntent = new Intent(SettingsActivity.this, ShoppingListActivity.class);
                startActivity(shoppingListIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        notificationDaysSeekBar = findViewById(R.id.notificationDaysSeekBar);
        notificationDaysText = findViewById(R.id.notificationDaysText);
        notificationTimePicker = findViewById(R.id.notificationTimePicker);
        Button saveSettingsButton = findViewById(R.id.saveSettingsButton);

        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);

        // Load saved preferences
        int savedDays = sharedPreferences.getInt("notificationDays", 3);
        int savedHour = sharedPreferences.getInt("notificationHour", 9);
        int savedMinute = sharedPreferences.getInt("notificationMinute", 0);

        notificationDaysSeekBar.setProgress(savedDays);
        notificationDaysText.setText("Days: " + savedDays);
        notificationTimePicker.setHour(savedHour);
        notificationTimePicker.setMinute(savedMinute);

        // Check the user's locale or preference for 24-hour format and set it
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(this);
        notificationTimePicker.setIs24HourView(is24HourFormat);

        // Update days text when SeekBar is adjusted
        notificationDaysSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notificationDaysText.setText("Days: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Save preferences on button click
        saveSettingsButton.setOnClickListener(v -> {
            int selectedDays = notificationDaysSeekBar.getProgress();
            int selectedHour = notificationTimePicker.getHour();
            int selectedMinute = notificationTimePicker.getMinute();

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