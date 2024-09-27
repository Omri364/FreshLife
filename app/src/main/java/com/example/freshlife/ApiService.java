package com.example.freshlife;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("/food-items")
    Call<List<FoodItem>> getFoodItems();

    @POST("/food-items")
    Call<FoodItem> addFoodItem(@Body FoodItem foodItem);
}

