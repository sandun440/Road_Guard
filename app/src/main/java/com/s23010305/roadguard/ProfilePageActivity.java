package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfilePageActivity extends AppCompatActivity {

    TextView profileName, profileEmail, profilePhone;
    ImageView profileImageView, backBtn;
    Button editProfileBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        profileImageView = findViewById(R.id.profileImageView);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        backBtn = findViewById(R.id.backBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Load user data
        SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
        String username = prefs.getString("currentUsername", null);

        if (username != null) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            User user = dbHelper.getUserByUsername(username);
            if (user != null) {
                profileName.setText(user.getFirstName() + " " + user.getLastName());
                profileEmail.setText(user.getEmail());
                profilePhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Add phone number");
            }
        }

        // Button listeners
        editProfileBtn.setOnClickListener(v -> startActivity(new Intent(this, EditProfilePageActivity.class)));
        backBtn.setOnClickListener(v -> finish());

        logoutBtn.setOnClickListener(v -> {
            // Clear all saved user data
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Navigate back to FirstPageActivity and clear back stack
            Intent intent = new Intent(ProfilePageActivity.this, FirstPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optional: add transition animations
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }
}
