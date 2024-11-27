package com.example.freshlife;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {
    // Retrieve food items for the user
    @GET("food-items")
    Call<List<FoodItem>> getFoodItems(@Header("Authorization") String token);

    @POST("food-items")
    Call<FoodItem> addFoodItem(@Header("Authorization") String token, @Body FoodItem foodItem);

    @PUT("food-items/{id}")
    Call<FoodItem> updateFoodItem(@Header("Authorization") String token, @Path("id") String id, @Body FoodItem foodItem);

    @DELETE("food-items/{id}")
    Call<Void> deleteFoodItem(@Header("Authorization") String token, @Path("id") String id);

    // Retrieve shopping items for the user
    @GET("/shopping-items")
    Call<List<ShoppingItem>> getShoppingItems(@Header("Authorization") String token);

    // Add a new shopping item
    @POST("/shopping-items")
    Call<ShoppingItem> addShoppingItem(@Header("Authorization") String token, @Body ShoppingItem shoppingItem);

    // Delete a specific shopping item
    @DELETE("/shopping-items/{id}")
    Call<Void> deleteShoppingItem(@Header("Authorization") String token, @Path("id") String id);

    // Update an existing shopping item
    @PUT("/shopping-items/{id}")
    Call<ShoppingItem> updateShoppingItem(@Header("Authorization") String token, @Path("id") String id, @Body ShoppingItem shoppingItem);

}

