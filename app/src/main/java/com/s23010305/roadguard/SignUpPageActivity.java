package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpPageActivity extends AppCompatActivity {
    private static final String TAG = "SignUpPageActivity";
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, usernameEditText, passwordEditText;
    private TextInputLayout firstNameInputLayout, lastNameInputLayout, emailInputLayout, usernameInputLayout, passwordInputLayout;
    private MaterialButton signUpButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_page);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize UI elements with null checks
        firstNameInputLayout = findViewById(R.id.fNameInput);
        if (firstNameInputLayout == null) {
            Log.e(TAG, "firstNameInputLayout is null. Check ID R.id.fNameInput in layout.");
            Toast.makeText(this, "UI Error: First Name field not found.", Toast.LENGTH_LONG).show();
            return;
        }
        firstNameEditText = findViewById(R.id.firstNameEditText);

        lastNameInputLayout = findViewById(R.id.lNameInput);
        if (lastNameInputLayout == null) {
            Log.e(TAG, "lastNameInputLayout is null. Check ID R.id.lNameInput in layout.");
            Toast.makeText(this, "UI Error: Last Name field not found.", Toast.LENGTH_LONG).show();
            return;
        }
        lastNameEditText = findViewById(R.id.lastNameEditText);

        emailInputLayout = findViewById(R.id.emailInput);
        if (emailInputLayout == null) {
            Log.e(TAG, "emailInputLayout is null. Check ID R.id.emailInput in layout.");
            Toast.makeText(this, "UI Error: Email field not found.", Toast.LENGTH_LONG).show();
            return;
        }
        emailEditText = findViewById(R.id.emailEditText);

        usernameInputLayout = findViewById(R.id.usernameInput);
        if (usernameInputLayout == null) {
            Log.e(TAG, "usernameInputLayout is null. Check ID R.id.usernameInput in layout.");
            Toast.makeText(this, "UI Error: Username field not found.", Toast.LENGTH_LONG).show();
            return;
        }
        usernameEditText = findViewById(R.id.usernameEditText);

        passwordInputLayout = findViewById(R.id.PasswordInput);
        if (passwordInputLayout == null) {
            Log.e(TAG, "passwordInputLayout is null. Check ID R.id.PasswordInput in layout.");
            Toast.makeText(this, "UI Error: Password field not found.", Toast.LENGTH_LONG).show();
            return;
        }
        passwordEditText = findViewById(R.id.passwordEditText);

        signUpButton = findViewById(R.id.btnSignUp);
        if (signUpButton == null) {
            Log.e(TAG, "signUpButton is null, check layout ID");
            Toast.makeText(this, "UI error, please restart the app", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpButton.setOnClickListener(v -> performSignup());

        TextView signUpText = findViewById(R.id.signuptxt);
        if (signUpText == null) {
            Log.e(TAG, "signuptxt is null, check layout ID");
            Toast.makeText(this, "UI error, please check the layout", Toast.LENGTH_SHORT).show();
        } else {
            signUpText.setOnClickListener(v -> {
                Log.d(TAG, "signuptxt clicked, navigating to LoginPageActivity");
                try {
                    Intent intent = new Intent(SignUpPageActivity.this, LoginPageActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed: " + e.getMessage());
                    Toast.makeText(this, "Navigation error, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void performSignup() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d(TAG, "Signup attempt - Username: " + username + ", Email: " + email);

        // Clear previous errors
        clearErrors();

        // Validate inputs
        if (firstName.isEmpty()) {
            firstNameInputLayout.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            lastNameInputLayout.setError("Last name is required");
            return;
        }

        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            return;
        }

        if (username.isEmpty()) {
            usernameInputLayout.setError("Username is required");
            return;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Password is required");
            return;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Invalid email address");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            return;
        }

        // Check if username is already taken
        if (databaseHelper.isUsernameTaken(username)) {
            usernameInputLayout.setError("Username is already taken");
            return;
        }

        // Check if email is already taken
        if (databaseHelper.isEmailTaken(email)) {
            emailInputLayout.setError("Email is already registered");
            return;
        }

        // Add user to database
        boolean isAdded = databaseHelper.addUser(firstName, lastName, email, username, password);

        if (isAdded) {
            Log.d(TAG, "User registered successfully: " + username);
            Toast.makeText(this, "Registration successful! Setting up fingerprint...", Toast.LENGTH_SHORT).show();

            // Print all users for debugging


            // Navigate to AddFingerprintActivity, passing user data
            Intent intent = new Intent(SignUpPageActivity.this, AddFingerprintActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else {
            Log.e(TAG, "Failed to register user: " + username);
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        firstNameInputLayout.setError(null);
        lastNameInputLayout.setError(null);
        emailInputLayout.setError(null);
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }
}