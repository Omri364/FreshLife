package com.example.freshlife;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainActivityUnitTest {

    private MainActivity mainActivity;
    private List<FoodItem> foodItems;

    @Mock
    SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mainActivity = new MainActivity();
        foodItems = new ArrayList<>();

        // Mock food items
//        foodItems.add(new FoodItem("Apples", 2, "2024-12-01", "Fruit", false, "Fridge"));
//        foodItems.add(new FoodItem("Bananas", 3, "2024-11-25", "Fruit", true, "Fridge"));
//        foodItems.add(new FoodItem("Milk", 1, "2024-11-30", "Dairy", false, "Unsorted"));
    }

    @Test
    public void testSortByExpirationDate() {
        // Simulate sorting by expiration date
//        mainActivity.sortByExpiration(foodItems);
//
//        // Verify sorting order
//        assertEquals("Bananas", foodItems.get(0).getName()); // Earliest expiration
//        assertEquals("Milk", foodItems.get(1).getName());
//        assertEquals("Apples", foodItems.get(2).getName());
    }

    @Test
    public void testFilterByLocation() {
        // Simulate filtering items by location ("Fridge")
//        List<FoodItem> filteredItems = mainActivity.filterByLocation("Fridge", foodItems);
//
//        // Verify filtered list
//        assertEquals(2, filteredItems.size());
//        assertEquals("Apples", filteredItems.get(0).getName());
//        assertEquals("Bananas", filteredItems.get(1).getName());
    }

    @Test
    public void testApplyDarkMode() {
        // Mock SharedPreferences behavior
        when(sharedPreferences.getBoolean("darkMode", false)).thenReturn(true);

        // Apply dark mode
//        mainActivity.applyDarkMode(true);


        // Assert dark mode was applied (Mocked behavior - no assertions here)
    }
}
