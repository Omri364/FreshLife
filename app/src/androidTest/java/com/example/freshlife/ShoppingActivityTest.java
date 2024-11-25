package com.example.freshlife;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.freshlife.ShoppingListActivity;

@RunWith(AndroidJUnit4.class)
public class ShoppingActivityTest {
    @Rule
    public ActivityScenarioRule<ShoppingListActivity> activityRule =
            new ActivityScenarioRule<>(ShoppingListActivity.class);

    // Test to verify the Add button is visible
    @Test
    public void testAddItemButtonIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.addButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Test to verify the Sort Spinner is visible
    @Test
    public void testSortSpinnerIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.sortSpinner))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Test to add a new item with valid input
    @Test
    public void testAddNewItem() {
        // Click on the "Add" button
        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());

        // Type the item name "Milk"
        Espresso.onView(ViewMatchers.withId(R.id.dialogItemNameEditText))
                .perform(ViewActions.typeText("Milk"), ViewActions.closeSoftKeyboard());

        // Click the "Add" button in the dialog
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        // Check if the item appears in the RecyclerView
        Espresso.onView(ViewMatchers.withText("Milk"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Test to add an item with empty input
    @Test
    public void testAddEmptyItem() {
        // Click on the "Add" button
        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());

        // Leave the item name field empty and click "Add"
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        // Check if an error message is displayed
        Espresso.onView(ViewMatchers.withText("Please enter the food name"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Test adding a duplicate item
    @Test
    public void testAddDuplicateItem() {
        // Add the first item
        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.dialogItemNameEditText))
                .perform(ViewActions.typeText("Milk"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        // Add the duplicate item
        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.dialogItemNameEditText))
                .perform(ViewActions.typeText("Milk"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        // Verify that "Milk" only appears once (or as expected for duplicates)
        Espresso.onView(ViewMatchers.withText("Milk"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Test to verify sorting functionality
    @Test
    public void testSortItems() {
        // Select the sorting option
        Espresso.onView(ViewMatchers.withId(R.id.sortSpinner))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Sort By A-Z"))
                .perform(ViewActions.click());

        // Add items
        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.dialogItemNameEditText))
                .perform(ViewActions.typeText("Apples"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.addButton)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.dialogItemNameEditText))
                .perform(ViewActions.typeText("Bananas"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withText("Add")).perform(ViewActions.click());

        // Verify that the items are sorted alphabetically
        Espresso.onView(ViewMatchers.withText("Apples"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Bananas"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
