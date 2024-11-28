package com.glalintechnologies.roadway2;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class register extends AppCompatActivity {

    // UI Components
    private EditText registerEmail, registerPassword, registerConfirmPassword;
    private Button registerButton;
    private TextView loginRedirect;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI components
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword);
        registerButton = findViewById(R.id.registerButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        // Handle Register Button Click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Handle Login Redirect Click
        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(register.this, RoadwayAuth.class);
                startActivity(loginIntent);
                finish(); // Prevent back navigation to registration
            }
        });
    }

    private void registerUser() {
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String confirmPassword = registerConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            registerEmail.setError("Email is required");
            registerEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            registerPassword.setError("Password is required");
            registerPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            registerPassword.setError("Password must be at least 6 characters");
            registerPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerConfirmPassword.setError("Passwords do not match");
            registerConfirmPassword.requestFocus();
            return;
        }

        // Register user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        Toast.makeText(register.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Login Screen
                        Intent loginIntent = new Intent(register.this, RoadwayAuth.class);
                        startActivity(loginIntent);
                        finish(); // Prevent back navigation to registration
                    } else {
                        // Registration failed
                        Toast.makeText(register.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
