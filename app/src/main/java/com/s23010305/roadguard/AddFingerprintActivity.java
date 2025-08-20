package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class AddFingerprintActivity extends AppCompatActivity {

    private String username;
    private Button skipButton, enableButton;
    private ImageView fingerprintIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fingerprint);

        username = getIntent().getStringExtra("username");
        skipButton = findViewById(R.id.skipBtn);
        enableButton = findViewById(R.id.enableBtn);
        fingerprintIcon = findViewById(R.id.fingerprintIcon);

        // Check biometric availability
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric not available", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // Skip → Go directly to login
        skipButton.setOnClickListener(v -> goToLogin());

        // Enable Fingerprint → start auth
        enableButton.setOnClickListener(v -> startFingerprintAuth());
    }

    private void startFingerprintAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(AddFingerprintActivity.this, "Fingerprint Added!", Toast.LENGTH_SHORT).show();

                        // Save fingerprint login preference
                        SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("fingerprintUser", username);
                        editor.putBoolean("isFingerprintEnabled", true);
                        editor.apply();

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
                });

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
