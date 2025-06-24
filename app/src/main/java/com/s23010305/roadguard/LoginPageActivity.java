package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginPageActivity extends AppCompatActivity {
    private static final String TAG = "LoginPageActivity";
    private TextInputEditText usernameEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView signupText;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.btnlogin);
        signupText = findViewById(R.id.signuptxt);

        loginButton.setOnClickListener(v -> performLogin());

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPageActivity.this, SignUpPageActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        // For debugging: print all users when login page opens

    }

    private void performLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d(TAG, "Login attempt - Username: " + username + ", Password: " + password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check credentials against database
        boolean isValidUser = databaseHelper.checkUser(username, password);

        if (isValidUser) {
            // Get user information
            User user = databaseHelper.getUserByUsername(username);

            if (user != null) {
                Log.d(TAG, "Login successful for user: " + user.toString());
                Toast.makeText(this, "Welcome back, " + user.getFirstName() + "!", Toast.LENGTH_SHORT).show();

                // Store user information in SharedPreferences for later use
                SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("firstName", user.getFirstName());
                editor.putString("lastName", user.getLastName());
                editor.putString("email", user.getEmail());
                editor.putString("username", user.getUsername());
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                // Navigate to HomePageActivity
                Intent intent = new Intent(LoginPageActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.enter, R.anim.exit);
            } else {
                Log.e(TAG, "User data not found after successful authentication");
                Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Login failed - Invalid credentials");
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();

            // Clear password field for security
            passwordEditText.setText("");
        }
    }
}