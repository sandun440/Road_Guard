package com.s23010305.roadguard;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginPageActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton;
    private DatabaseHelper databaseHelper;
    private TextView signuptxt; // 👈 Added


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        databaseHelper = DatabaseHelper.getInstance(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.btnlogin);
        signuptxt = findViewById(R.id.logintxt); // 👈 Link to TextView in XML

        loginButton.setOnClickListener(v -> performLogin());

        // 👇 Navigate to Signup
        signuptxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginPageActivity.this, SignUpPageActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValidUser = databaseHelper.checkUser(username, password);

        if (isValidUser) {
            // Store login info and enable fingerprint
            SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("currentUsername", username);
            editor.putBoolean("isLoggedIn", true);
            editor.putBoolean("isFingerprintEnabled", true); // enable fingerprint after login
            editor.apply();

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            passwordEditText.setText("");
        }
    }
}
