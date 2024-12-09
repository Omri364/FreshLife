package com.example.freshlife;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.contrib.RecyclerViewActions;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;


@RunWith(AndroidJUnit4.class)
public class InventoryActivityTest {

    @Before
    public void setup() throws InterruptedException {
        // Disable animations
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                Context context = ApplicationProvider.getApplicationContext();
                ContentResolver contentResolver = context.getContentResolver();

                Settings.Global.putFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
                Settings.Global.putFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 0f);
                Settings.Global.putFloat(contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 0f);
            } catch (SecurityException e) {
                Log.e("TestSetup", "Failed to disable animations programmatically", e);
            }
        }

        // Authenticate the test user and save the token
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("FreshLifePrefs", Context.MODE_PRIVATE);

        CountDownLatch latch = new CountDownLatch(1); // Latch to wait for async operations

        firebaseAuth.signInWithEmailAndPassword("omriroth6@gmail.com", "Omri1234")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseAuth.getCurrentUser().getIdToken(true)
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String authToken = tokenTask.getResult().getToken();
                                        sharedPreferences.edit()
                                                .putString("authToken", authToken)
                                                .apply();
                                        latch.countDown(); // Signal completion
                                    } else {
                                        throw new RuntimeException("Failed to retrieve token for test user");
                                    }
                                });
                    } else {
                        throw new RuntimeException("Authentication failed for test user");
                    }
                });

        latch.await(); // Wait for the token to be saved before proceeding
    }


    @Rule
    public ActivityTestRule<InventoryActivity> activityRule =
            new ActivityTestRule<>(InventoryActivity.class);

    @Test
    public void testAddAndDeleteItem() throws InterruptedException {
        final String testItemName = "Add/Delete Test Item";

        // Step 1: Add a new item to the fridge
        onView(withId(R.id.addFoodButton)).perform(click());
        onView(withId(R.id.dialogFoodNameEditText)).perform(replaceText(testItemName));
        onView(withId(R.id.dialogQuantityEditText)).perform(replaceText("2"));
        onView(withId(R.id.dialogExpirationDateTextView)).perform(click());
        onView(withText("אישור")).perform(click()); // Adjust based on the date picker button text
        onView(withId(R.id.buttonToggleFridge)).perform(click());
        onView(withText("Add")).perform(click());
        Thread.sleep(2000); // Wait for RecyclerView to refresh

        // Step 2: Verify the item is in the fridge list
        onView(withId(R.id.buttonFridge)).perform(click());
        onView(withText(testItemName)).check(matches(isDisplayed()));

        // Step 3: Verify the item is not in the pantry list
        onView(withId(R.id.buttonPantry)).perform(click());
        onView(withText(testItemName)).check(matches(Matchers.not(isDisplayed())));

        // Step 4: Delete the item
        onView(withId(R.id.buttonFridge)).perform(click());
        // Swipe the item at position 0 to delete it
        onView(withId(R.id.foodItemsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));

        Thread.sleep(2000); // Wait for deletion to complete

        // Step 5: Verify the item is not in any list
        onView(withId(R.id.buttonAll)).perform(click());
        onView(withText(testItemName)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.buttonFridge)).perform(click());
        onView(withText(testItemName)).check(matches(Matchers.not(isDisplayed())));
    }

    @Test
    public void testEditFoodItem() throws InterruptedException {
        String testItemName = "Edit Test Item";

        // Step 1: Add a new item
        onView(withId(R.id.addFoodButton)).perform(click());
        onView(withId(R.id.dialogFoodNameEditText)).perform(replaceText(testItemName));
        onView(withId(R.id.dialogQuantityEditText)).perform(replaceText("2"));
        onView(withId(R.id.dialogExpirationDateTextView)).perform(click());
        onView(withText("אישור")).perform(click());
        onView(withText("Add")).perform(click());

        // Wait for the item to be added
        Thread.sleep(2000);

        // Step 2: Click on the item to edit it
        onView(withText(testItemName)).perform(click());

        // Edit the item's fields
        testItemName = "Edited Item";
        onView(withId(R.id.dialogFoodNameEditText)).perform(replaceText(testItemName));
        onView(withId(R.id.dialogQuantityEditText)).perform(replaceText("5"));
        onView(withId(R.id.dialogExpirationDateTextView)).perform(click());
        onView(withText("אישור")).perform(click());

        // Save the edited item
        onView(withText("Save")).perform(click());

        // Wait for the item to be updated
        Thread.sleep(2000);

        // Step 3: Verify the updated details
        onView(withText(testItemName)).check(matches(withText("Edited Item")));

        // Ensure the changes are reflected
        onView(withText("Qty: 5")).check(matches(withText("Qty: 5"))); // Quantity

        // Step 4: Delete the item
        onView(withId(R.id.foodItemsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));
        // Wait for the item to be deleted
        Thread.sleep(2000);

        Thread.sleep(2000); // Wait for deletion to complete

        // Step 5: Verify the item is not in any list
        onView(withId(R.id.buttonAll)).perform(click());
        onView(withText(testItemName)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.buttonFridge)).perform(click());
        onView(withText(testItemName)).check(matches(Matchers.not(isDisplayed())));
    }

    @After
    public void logOutUser() {
        // Log the user out from Firebase
        FirebaseAuth.getInstance().signOut();
        FirebaseApp.getInstance().delete();

        // Clear the token and other data from SharedPreferences
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("FreshLifePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .remove("authToken") // Remove the token
                .apply();

        Log.d("InventoryActivityTest", "Signed out after test");

    }


    // Custom matcher to find RecyclerView items
    private static Matcher<View> hasDescendant(final Matcher<View> matcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    if (matcher.matches(recyclerView.getChildAt(i))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("RecyclerView should contain a view that matches: ");
                matcher.describeTo(description);
            }
        };
    }

}