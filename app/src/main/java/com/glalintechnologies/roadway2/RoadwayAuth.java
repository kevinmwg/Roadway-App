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
import com.google.firebase.auth.FirebaseUser;

public class RoadwayAuth extends AppCompatActivity {

    // UI Components
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadway_auth);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Handle Login Button Click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Handle Register Text Click
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(RoadwayAuth.this, register.class);
                startActivity(registerIntent);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Authenticate the user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, get the current user
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Prepare data to send back
                        Intent returnIntent = new Intent(RoadwayAuth.this, OperatorsregistrationActivity.class);
                        returnIntent.putExtra("operatorName", getIntent().getStringExtra("operatorName"));
                        returnIntent.putExtra("contactNumber", getIntent().getStringExtra("contactNumber"));
                        returnIntent.putExtra("vehicleModel", getIntent().getStringExtra("vehicleModel"));
                        returnIntent.putExtra("licensePlate", getIntent().getStringExtra("licensePlate"));
                        returnIntent.putExtra("description", getIntent().getStringExtra("description"));
                        returnIntent.putExtra("emergencyContact", getIntent().getStringExtra("emergencyContact"));
                        returnIntent.putExtra("vehicleType", getIntent().getStringExtra("vehicleType"));
                        returnIntent.putExtra("serviceType", getIntent().getStringExtra("serviceType"));
                        returnIntent.putExtra("isActive", getIntent().getBooleanExtra("isActive", false));

                        // Navigate back to OperatorsregistrationActivity
                        startActivity(returnIntent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(RoadwayAuth.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Navigate to Dashboard
            Intent dashboardIntent = new Intent(RoadwayAuth.this, MainActivity.class);
            startActivity(dashboardIntent);
            finish(); // Prevent going back to login
        }
    }
}


