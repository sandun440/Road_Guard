package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class AddFingerprintActivity extends AppCompatActivity {

    private static final String PREFS = "RoadGuardPrefs";
    private static final String KEY_FP_USER = "fingerprintUser";
    private static final String KEY_FP_ENABLED = "isFingerprintEnabled";

    private String username;
    private Button skipButton, enableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fingerprint);

        username = getIntent().getStringExtra("username");
        skipButton = findViewById(R.id.skipBtn);
        enableButton = findViewById(R.id.enableBtn);

        // Guard: username must be present to bind fingerprint to an account
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "No username provided for fingerprint setup", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // Check biometric availability on this device
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric not available", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // Skip → Go directly to login
        skipButton.setOnClickListener(v -> goToLogin());

        // Enable Fingerprint → start auth flow
        enableButton.setOnClickListener(v -> startFingerprintAuth());
    }

    private void startFingerprintAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(AddFingerprintActivity.this, "Fingerprint Added!", Toast.LENGTH_SHORT).show();

                        // Save fingerprint login preference bound to this username
                        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                        prefs.edit()
                                .putString(KEY_FP_USER, username)
                                .putBoolean(KEY_FP_ENABLED, true)
                                .apply();

                        goToLogin();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(AddFingerprintActivity.this, "Error: " + errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(AddFingerprintActivity.this, "Fingerprint not recognized.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Add Fingerprint")
                .setSubtitle("Place your finger to register")
                .setDescription("Use your fingerprint for faster login")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToLogin() {
        Intent intent = new Intent(AddFingerprintActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }
}
