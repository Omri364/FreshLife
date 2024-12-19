package com.example.freshlife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles user registration for the FreshLife app.
 * This activity provides a registration form for new users to sign up using email and password.
 * Upon successful registration, it saves the user's Firebase ID token in SharedPreferences for authentication.
 */
public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        // Set up registration button click listener
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * Registers a new user with Firebase Authentication using the provided email and password.
     * Validates the user input, displays progress, and handles success or failure scenarios.
     */
    private void registerUser() {
        // Get inputs from the user
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address");
            editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Retrieve Firebase ID token
                            user.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String idToken = tokenTask.getResult().getToken();

                                            // Save token to SharedPreferences
                                            SharedPreferences sharedPreferences = getSharedPreferences("FreshLifePrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("authToken", idToken);
                                            editor.apply();

                                            Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                            // Redirect to MainActivity or any other activity
                                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegistrationActivity.this, "Failed to get token: " + tokenTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}