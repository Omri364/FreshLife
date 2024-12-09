package com.example.freshlife;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MainActivityTest {

    private MainActivity mainActivity;
    private Context context;
    private SharedPreferences sharedPreferences;

    @Mock
    private FirebaseAuth mockFirebaseAuth;

    @Mock
    private FirebaseUser mockFirebaseUser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Initialize context and shared preferences
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("FreshLifePrefs", Context.MODE_PRIVATE);

        // Initialize MainActivity with mocks
        mainActivity = new MainActivity();
//        mainActivity.firebaseAuth = mockFirebaseAuth;
//        mainActivity.sharedPreferences = sharedPreferences;
    }

    @Test
    public void testIsUserAuthenticated_ValidUserAndToken() {
        // Mock FirebaseAuth returning a valid user
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        // Mock SharedPreferences containing a valid token
        sharedPreferences.edit().putString("authToken", "validToken").apply();

        // Verify user authentication
        assertTrue(mainActivity.isUserAuthenticated());
    }

    @Test
    public void testIsUserAuthenticated_NoToken() {
        // Mock FirebaseAuth returning a valid user
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        // Mock SharedPreferences without a token
        sharedPreferences.edit().remove("authToken").apply();

        // Verify user authentication
        assertFalse(mainActivity.isUserAuthenticated());
    }

    @Test
    public void testIsUserAuthenticated_NoUser() {
        // Mock FirebaseAuth returning no user
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Mock SharedPreferences containing a valid token
        sharedPreferences.edit().putString("authToken", "validToken").apply();

        // Verify user authentication
        assertFalse(mainActivity.isUserAuthenticated());
    }

    @Test
    public void testIsUserAuthenticated_NoUserAndNoToken() {
        // Mock FirebaseAuth returning no user
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Mock SharedPreferences without a token
        sharedPreferences.edit().remove("authToken").apply();

        // Verify user authentication
        assertFalse(mainActivity.isUserAuthenticated());
    }

}