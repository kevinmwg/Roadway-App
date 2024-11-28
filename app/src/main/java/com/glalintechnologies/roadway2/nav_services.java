package com.glalintechnologies.roadway2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class nav_services extends AppCompatActivity {

    private FloatingActionButton fabPromotions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_service);  // Ensure this layout file matches your XML

        // Initialize the promotion FloatingActionButton
        fabPromotions = findViewById(R.id.fab_promotions);

        // Set OnClickListener for fab_promotions to open OperatorsRegistrationActivity
        fabPromotions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to navigate to OperatorsRegistrationActivity
                Intent intent = new Intent(nav_services.this, OperatorsregistrationActivity.class);
                startActivity(intent);
            }
        });
    }
}