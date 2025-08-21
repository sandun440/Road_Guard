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

    private SharedPreferences prefs;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        profileImageView = findViewById(R.id.profileImageView);
        profileName      = findViewById(R.id.profileName);
        profileEmail     = findViewById(R.id.profileEmail);
        profilePhone     = findViewById(R.id.profilePhone);
        editProfileBtn   = findViewById(R.id.editProfileBtn);
        backBtn          = findViewById(R.id.backBtn);
        logoutBtn        = findViewById(R.id.logoutBtn);

        prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
        db    = DatabaseHelper.getInstance(this);

        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfilePageActivity.class))
        );
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePageActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish(); // optional: close ProfilePageActivity so it’s not left in the stack
        });

        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("logged_in_username");
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            Intent intent = new Intent(ProfilePageActivity.this, FirstPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUser();
    }

    private void bindUser() {
        String username = prefs.getString("logged_in_username", null);
        if (username == null) {
            profileName.setText("Guest");
            profileEmail.setText("");
            profilePhone.setText("Add phone number");
            ProfileImageUtils.loadInto(this, profileImageView, R.drawable.profile);
            return;
        }

        User user = db.getUserByUsername(username);
        if (user != null) {
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName()  != null && !user.getLastName().isEmpty() ? " " + user.getLastName() : "");
            profileName.setText(fullName.isEmpty() ? user.getUsername() : fullName);
            profileEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            String phoneDisplay = (user.getPhone() != null && !user.getPhone().trim().isEmpty())
                    ? user.getPhone()
                    : "Add phone number";
            profilePhone.setText(phoneDisplay);
        } else {
            profileName.setText("Unknown User");
            profileEmail.setText("");
            profilePhone.setText("Add phone number");
        }

        // Load the same saved profile image
        ProfileImageUtils.loadInto(this, profileImageView, R.drawable.profile);
    }
}
