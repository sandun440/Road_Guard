package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class FirstPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.first_page);

        // Initialize buttons
        MaterialButton loginButton = findViewById(R.id.loginBtn);
        MaterialButton signupButton = findViewById(R.id.signupBtn);
        ImageView fingerprintIcon = findViewById(R.id.fingerprintIcon);

        // Check if biometric authentication is available
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric authentication is not available", Toast.LENGTH_SHORT).show();
            fingerprintIcon.setEnabled(false); // Disable fingerprint icon if biometrics are not available
        }

        // Set up login button click listener
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(FirstPageActivity.this, LoginPageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            });
        }

        // Set up signup button click listener
        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(FirstPageActivity.this, SignUpPageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            });
        }

        // Set up fingerprint icon click listener
        if (fingerprintIcon != null) {
            fingerprintIcon.setOnClickListener(v -> authenticateWithBiometric());
        }
    }

    private void authenticateWithBiometric() {
        // Create a BiometricPrompt instance
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Verify your identity with your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(FirstPageActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(FirstPageActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                // Navigate to HomePageActivity
                Intent intent = new Intent(FirstPageActivity.this, HomePageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
                finish(); // Optional: Close FirstPageActivity
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(FirstPageActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the biometric prompt
        biometricPrompt.authenticate(promptInfo);
    }
}