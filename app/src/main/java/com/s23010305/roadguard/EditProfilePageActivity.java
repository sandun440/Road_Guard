package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfilePageActivity extends AppCompatActivity {
    EditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    ImageView profileImage;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_page);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        profileImage = findViewById(R.id.profileImage);
        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // TODO: Save data in SharedPreferences or Firebase

            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to Profile Page
            Intent intent = new Intent(EditProfilePageActivity.this, ProfilePageActivity.class);
            startActivity(intent);
            finish(); // optional: close EditProfilePageActivity
        });

        // Optional: logic for picking/changing profile photo
    }
}
