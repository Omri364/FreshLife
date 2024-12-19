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

/**
 * The `SettingsActivity` class allows users to manage application settings, such as notification preferences and dark mode.
 * It also provides a logout feature and navigational links to other parts of the app.
 */
public class SettingsActivity extends AppCompatActivity {

    private String userToken;
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

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Retrieve the user's authentication token
        sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        userToken = sharedPreferences.getString("authToken", null);

        if (userToken == null) {
            redirectToLogin();
        }

        // Initialize UI components
        initializeUIComponents();

        // Load and apply saved preferences
        loadPreferences();
    }

    /**
     * Initializes UI components and sets up their behavior.
     */
    private void initializeUIComponents() {
        // Email display
        userEmailTextView = findViewById(R.id.userEmailTextView);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userEmailTextView.setText(currentUser != null ? currentUser.getEmail() : "Guest");

        // Logout functionality
        setupLogoutButton();

        // Bottom navigation
        setupBottomNavigation();

        // Notification time picker
        notificationTimeTextView = findViewById(R.id.notificationTimeTextView);
        setupTimePicker();

        // Dark mode toggle
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setOnCheckedChangeListener(this::toggleDarkMode);

        // Save button
        MaterialButton saveSettingsButton = findViewById(R.id.saveSettingsButton);
        saveSettingsButton.setOnClickListener(v -> savePreferences());
    }

    /**
     * Redirects the user to the login screen.
     */
    private void redirectToLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Loads and applies saved preferences for notifications and dark mode.
     */
    private void loadPreferences() {
        int savedDays = sharedPreferences.getInt("notificationDays", 3);
        int savedHour = sharedPreferences.getInt("notificationHour", 9);
        int savedMinute = sharedPreferences.getInt("notificationMinute", 0);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);

        notificationDaysInput = findViewById(R.id.notificationDaysInput);
        notificationDaysInput.setText(String.valueOf(savedDays));

        notificationTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", savedHour, savedMinute));
        darkModeSwitch.setChecked(isDarkMode);

        applyDarkMode(isDarkMode);
    }

    /**
     * Sets up the logout button and its behavior.
     */
    private void setupLogoutButton() {
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", null)
                .show());
    }

    /**
     * Logs out the user and redirects to the login screen.
     */
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Configures the bottom navigation bar.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_settings) return true;
            if (item.getItemId() == R.id.navigation_inventory) navigateToActivity(InventoryActivity.class);
            if (item.getItemId() == R.id.navigation_shopping_list) navigateToActivity(ShoppingListActivity.class);
            return false;
        });
    }

    /**
     * Navigates to a specified activity.
     *
     * @param activityClass The target activity class.
     */
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Configures the time picker for notification time.
     */
    private void setupTimePicker() {
        notificationTimeTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, hour, minute) -> notificationTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true).show();
        });
    }

    /**
     * Toggles dark mode and saves the preference.
     */
    private void toggleDarkMode(CompoundButton button, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("darkMode", isChecked);
        editor.apply();
        applyDarkMode(isChecked);
        Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    /**
     * Applies the dark mode theme.
     *
     * @param isDarkMode True to enable dark mode, false to disable.
     */
    private void applyDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Saves the user's preferences for notifications and schedules notifications.
     */
    private void savePreferences() {
        String daysInput = notificationDaysInput.getText().toString().trim();
        if (daysInput.isEmpty()) {
            Toast.makeText(this, "Please enter the number of days", Toast.LENGTH_SHORT).show();
            return;
        }

        int days = Integer.parseInt(daysInput);
        String[] timeParts = notificationTimeTextView.getText().toString().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("notificationDays", days);
        editor.putInt("notificationHour", hour);
        editor.putInt("notificationMinute", minute);
        editor.apply();

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();

        DataFetcher.fetchFoodItemsFromDatabase(this, foodItems -> NotificationScheduler.scheduleNotifications(this, foodItems));
    }
}
