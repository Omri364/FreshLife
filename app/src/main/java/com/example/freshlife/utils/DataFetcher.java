package com.example.freshlife.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.freshlife.ApiService;
import com.example.freshlife.FoodItem;
import com.example.freshlife.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class for fetching data from the backend.
 */
public class DataFetcher {

    /**
     * Callback interface for handling the fetched food items.
     */
    public interface FoodItemsCallback {
        /**
         * Called when food items are fetched from the database.
         *
         * @param foodItems The list of fetched food items.
         */
        void onFoodItemsFetched(List<FoodItem> foodItems);
    }

    /**
     * Fetches food items for the currently authenticated user from the backend.
     *
     * @param context  The application context used to retrieve the shared preferences.
     * @param callback A callback that handles the fetched food items.
     */
    public static void fetchFoodItemsFromDatabase(Context context, FoodItemsCallback callback) {
        String token = context.getSharedPreferences("FreshLifePrefs", Context.MODE_PRIVATE).getString("authToken", "");
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<FoodItem>> call = apiService.getFoodItems("Bearer " + token);
        call.enqueue(new Callback<List<FoodItem>>() {
            @Override
            public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onFoodItemsFetched(response.body());
                    Log.d("DataFetcher", "Fetched food items successfully.");
                } else {
                    Log.e("DataFetcher", "Failed to fetch food items: " + response.message());
                    callback.onFoodItemsFetched(new ArrayList<>()); // Return empty list on failure
                }
            }

            @Override
            public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                Log.e("DataFetcher", "Error: " + t.getMessage());
                callback.onFoodItemsFetched(new ArrayList<>()); // Return empty list on failure
            }
        });
    }
}


