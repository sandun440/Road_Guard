package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpPageActivity extends AppCompatActivity {

    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, usernameEditText, passwordEditText;
    private TextInputLayout firstNameInputLayout, lastNameInputLayout, emailInputLayout, usernameInputLayout, passwordInputLayout;
    private MaterialButton signUpButton;
    private DatabaseHelper databaseHelper;
    private TextView logintxt; // 👈 Added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        databaseHelper = DatabaseHelper.getInstance(this);

        firstNameInputLayout = findViewById(R.id.fNameInput);
        lastNameInputLayout = findViewById(R.id.lNameInput);
        emailInputLayout = findViewById(R.id.emailInput);
        usernameInputLayout = findViewById(R.id.usernameInput);
        passwordInputLayout = findViewById(R.id.PasswordInput);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        signUpButton = findViewById(R.id.btnSignUp);
        logintxt = findViewById(R.id.signuptxt); // 👈 Link to TextView in XML

        signUpButton.setOnClickListener(v -> performSignup());

        // 👇 Navigate to Login
        logintxt.setOnClickListener(v -> {
            startActivity(new Intent(SignUpPageActivity.this, LoginPageActivity.class));
            finish();
        });
    }

    private void performSignup() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (firstName.isEmpty()) { firstNameInputLayout.setError("Required"); return; }
        if (lastName.isEmpty()) { lastNameInputLayout.setError("Required"); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInputLayout.setError("Valid email required"); return; }
        if (username.isEmpty()) { usernameInputLayout.setError("Required"); return; }
        if (password.isEmpty() || password.length() < 6) { passwordInputLayout.setError("6+ chars required"); return; }

        if (databaseHelper.isUsernameTaken(username)) { usernameInputLayout.setError("Username taken"); return; }
        if (databaseHelper.isEmailTaken(email)) { emailInputLayout.setError("Email registered"); return; }

        boolean added = databaseHelper.addUser(firstName, lastName, email, username, password);
        if (added) {
            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

            // 👉 Directly navigate to AddFingerprintActivity
            Intent intent = new Intent(SignUpPageActivity.this, AddFingerprintActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
