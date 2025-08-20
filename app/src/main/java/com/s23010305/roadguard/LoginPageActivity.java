package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginPageActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton;
    private DatabaseHelper databaseHelper;
    private TextView signuptxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        databaseHelper   = DatabaseHelper.getInstance(this);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton      = findViewById(R.id.btnlogin);
        signuptxt        = findViewById(R.id.logintxt);

        loginButton.setOnClickListener(v -> performLogin());

        // Go to Signup
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
            SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // 🔑 Save the logged-in user with the same key used in signup & profile screens
            editor.putString("logged_in_username", username);
            editor.putBoolean("isLoggedIn", true);
            editor.putBoolean("isFingerprintEnabled", true); // optional flag
            editor.apply();

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Navigate to Home
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            passwordEditText.setText("");
        }
    }
}
