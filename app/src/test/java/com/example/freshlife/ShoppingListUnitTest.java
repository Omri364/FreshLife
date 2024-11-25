package com.example.freshlife;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingListUnitTest {
    private List<ShoppingItem> shoppingItems;

    @Before
    public void setUp() {
        // Initialize a sample shopping list
        shoppingItems = new ArrayList<>();
        shoppingItems.add(new ShoppingItem("Apples", false, "Fruit", 1));
        shoppingItems.add(new ShoppingItem("Bananas", true, "Fruit", 3));
        shoppingItems.add(new ShoppingItem("Milk", false, "Dairy", 2));
        shoppingItems.add(new ShoppingItem("Eggs", true, "Dairy", 1));
    }

    @Test
    public void testSortAlphabetically() {
        // Apply sorting alphabetically
        shoppingItems.sort(Comparator.comparing(ShoppingItem::getName));

        // Verify the order
        assertEquals("Apples", shoppingItems.get(0).getName());
        assertEquals("Bananas", shoppingItems.get(1).getName());
        assertEquals("Eggs", shoppingItems.get(2).getName());
        assertEquals("Milk", shoppingItems.get(3).getName());
    }

    @Test
    public void testSortByCategory() {
        // Apply sorting by category
        shoppingItems.sort(Comparator.comparing(ShoppingItem::getCategory));

        // Verify the order
        assertEquals("Milk", shoppingItems.get(0).getName()); // Dairy
        assertEquals("Eggs", shoppingItems.get(1).getName()); // Dairy
        assertEquals("Apples", shoppingItems.get(2).getName()); // Fruit
        assertEquals("Bananas", shoppingItems.get(3).getName()); // Fruit
    }

    @Test
    public void testMoveCheckedItemsToBottom() {
        // Separate checked and unchecked items
        List<ShoppingItem> uncheckedItems = new ArrayList<>();
        List<ShoppingItem> checkedItems = new ArrayList<>();

        for (ShoppingItem item : shoppingItems) {
            if (item.isChecked()) {
                checkedItems.add(item);
            } else {
                uncheckedItems.add(item);
            }
        }

        // Merge lists: unchecked first, checked last
        List<ShoppingItem> sortedItems = new ArrayList<>(uncheckedItems);
        sortedItems.addAll(checkedItems);

        // Verify the order
        assertEquals("Apples", sortedItems.get(0).getName());
        assertEquals("Milk", sortedItems.get(1).getName());
        assertEquals("Bananas", sortedItems.get(2).getName());
        assertEquals("Eggs", sortedItems.get(3).getName());
    }
}
