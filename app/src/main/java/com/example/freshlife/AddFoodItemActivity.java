package com.example.freshlife;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Calendar;
import java.util.Locale;

public class AddFoodItemActivity extends AppCompatActivity {

    private EditText nameEditText, quantityEditText;
    private TextView expirationDateTextView;
    private Spinner categorySpinner;
    private Button addButton;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_item);

        nameEditText = findViewById(R.id.nameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        expirationDateTextView = findViewById(R.id.expirationDateTextView);
        categorySpinner = findViewById(R.id.categorySpinner);
        addButton = findViewById(R.id.addButton);

        // Populate category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Get current date for initial date picker values
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Set up the DatePickerDialog when the expiration date field is clicked
        expirationDateTextView.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddFoodItemActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // Update the TextView with the selected date
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                        expirationDateTextView.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay));
                    }, selectedYear, selectedMonth, selectedDay);
            datePickerDialog.show();
        });

        // When the Add button is pressed, collect the data and send it back
        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            String expirationDate = expirationDateTextView.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();

            FoodItem foodItem = new FoodItem(name, quantity, expirationDate, category, false);
            addFoodItem(foodItem);
        });
    }

    private void addFoodItem(FoodItem foodItem) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<FoodItem> call = apiService.addFoodItem(foodItem);

        call.enqueue(new Callback<FoodItem>() {
            @Override
            public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                if (response.isSuccessful()) {
                    // The backend returns the newly created item with the generated id
                    FoodItem createdFoodItem = response.body();

                    // Pass the new item with id back to MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newFoodItem", createdFoodItem);
                    setResult(RESULT_OK, resultIntent);
                    finish();  // Close AddFoodItemActivity
                } else {
                    Toast.makeText(AddFoodItemActivity.this, "Failed to add food item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodItem> call, Throwable t) {
                Toast.makeText(AddFoodItemActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

