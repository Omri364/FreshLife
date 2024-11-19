package com.example.freshlife.utils;

import android.content.Context;
import android.util.Log;

import com.example.freshlife.ApiService;
import com.example.freshlife.FoodItem;
import com.example.freshlife.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataFetcher {
    public interface FoodItemsCallback {
        void onFoodItemsFetched(List<FoodItem> foodItems);
    }

    public static void fetchFoodItemsFromDatabase(Context context, FoodItemsCallback callback) {
        ApiService apiService = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        Call<List<FoodItem>> call = apiService.getFoodItems();
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


