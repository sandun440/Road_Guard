package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilePageActivity extends AppCompatActivity {

    TextView profileName, profileEmail, profilePhone;
    ImageView profileImageView, backBtn;
    Button editProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        backBtn = findViewById(R.id.backBtn);

        // Example values - replace with real user data if needed
        profileName.setText("Sandun Sameera");
        profileEmail.setText("sandun@example.com");
        profilePhone.setText("+94 77 123 4567");

        // Navigate to Edit Profile Page
        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePageActivity.this, EditProfilePageActivity.class);
            startActivity(intent);
        });

        // Handle Back Button
        backBtn.setOnClickListener(v -> {
            finish(); // Go back to the previous screen
        });
    }
}
