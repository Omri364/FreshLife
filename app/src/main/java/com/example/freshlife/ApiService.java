package com.example.freshlife;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.PUT;

public interface ApiService {

    @GET("/food-items")
    Call<List<FoodItem>> getFoodItems();

    @POST("/food-items")
    Call<FoodItem> addFoodItem(@Body FoodItem foodItem);

    @DELETE("/food-items/{id}")
    Call<Void> deleteFoodItem(@Path("id") String foodItemId);

    @PUT("/food-items/{id}")
    Call<FoodItem> updateFoodItem(@Path("id") String id, @Body FoodItem foodItem);

    @GET("/shopping-items")
    Call<List<ShoppingItem>> getShoppingItems();

    @POST("/shopping-items")
    Call<ShoppingItem> addShoppingItem(@Body ShoppingItem shoppingItem);

    @DELETE("/shopping-items/{id}")
    Call<Void> deleteShoppingItem(@Path("id") String id);

    @PUT("/shopping-items/{id}")
    Call<ShoppingItem> updateShoppingItem(@Path("id") String id, @Body ShoppingItem shoppingItem);

}

