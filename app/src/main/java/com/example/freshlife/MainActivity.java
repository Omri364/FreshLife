package com.example.freshlife;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.freshlife.utils.DataFetcher;
import com.example.freshlife.utils.RecyclerViewSwipeDecorator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.Manifest;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * The `MainActivity` class serves as the entry point of the application.
 * It provides the login interface and handles user authentication via email/password
 * and Google Sign-In. Additionally, it includes navigation to the registration activity
 * and password recovery functionality.
 */
public class MainActivity extends AppCompatActivity {

    private TextView registerLink, forgotPasswordLink;
    private EditText emailEditText, passwordEditText;
    private LinearLayout loginButton, googleLoginButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 100; // Request code for Google Sign-In
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if a valid token exists in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);

        // If the user is authenticated but no valid token exists, sign them out
        if (firebaseAuth.getCurrentUser() != null && token == null) {
            firebaseAuth.signOut();
        }

        // If a valid token exists, navigate to InventoryActivity
        if (firebaseAuth.getCurrentUser() != null && token != null) {
            navigateToInventory();
        }

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleSignInButton);
        registerLink = findViewById(R.id.registerLinkTextView);
        forgotPasswordLink = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Handle register button click
        registerLink.setOnClickListener(v -> navigateToRegistration());

        // Forgot password
        forgotPasswordLink.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(MainActivity.this, "Enter your registered email", Toast.LENGTH_SHORT).show();
            } else {
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Google Sign-In
        googleLoginButton.setOnClickListener(v -> handleGoogleSignIn());
    }

    /**
     * Logs in the user using email and password.
     * Validates user input, authenticates with Firebase, and saves the ID token to SharedPreferences.
     */
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate email and password input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Sign in with Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Get the ID token
                        firebaseAuth.getCurrentUser().getIdToken(true)
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        // Save ID token to SharedPreferences
                                        String idToken = tokenTask.getResult().getToken();
                                        SharedPreferences sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("authToken", idToken); // Save ID token instead of UID
                                        editor.apply();

                                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                        navigateToInventory();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error fetching token: " + tokenTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Navigates to the InventoryActivity.
     */
    private void navigateToInventory() {
        Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the RegistrationActivity.
     */
    private void navigateToRegistration() {
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    /**
     * Handles Google Sign-In by launching the account picker dialog.
     */
    private void handleGoogleSignIn() {
        // Sign out from GoogleSignInClient to force account picker dialog
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign-In failed
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Authenticates the user with Firebase using Google credentials and saves the ID token.
     *
     * @param idToken The Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Successfully authenticated, retrieve Firebase ID token
                        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String firebaseToken = tokenTask.getResult().getToken();

                                        // Save the Firebase ID token to SharedPreferences
                                        SharedPreferences sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
                                        sharedPreferences.edit()
                                                .putString("authToken", firebaseToken)
                                                .apply();

                                        Log.d("FirebaseToken", "Firebase Token: " + firebaseToken);

                                        navigateToInventory();
                                    } else {
                                        Log.e("FirebaseToken", "Failed to get Firebase token", tokenTask.getException());
                                    }
                                });
                    } else {
                        Log.e("FirebaseAuth", "Firebase authentication failed", task.getException());
                    }
                });
    }

    /**
     * Public method for testing user authentication.
     *
     * @return True if the user is authenticated, false otherwise.
     */
    public boolean isUserAuthenticated() {
        SharedPreferences sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        return firebaseAuth.getCurrentUser() != null && token != null;
    }

}

