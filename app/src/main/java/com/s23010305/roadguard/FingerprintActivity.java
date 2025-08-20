package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class FingerprintActivity extends AppCompatActivity {

    private TextView promptText, statusText;
    private ImageView fingerprintIcon;
    private Button retryBtn, cancelBtn;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fingerprint);

        // Initialize views

        fingerprintIcon = findViewById(R.id.fingerprintIcon);


        // Get username passed from signup/login
        username = getIntent().getStringExtra("username");

        // Button listeners
        retryBtn.setOnClickListener(v -> startAuthentication());
        cancelBtn.setOnClickListener(v -> finish());

        fingerprintIcon.setOnClickListener(v -> startAuthentication());

        // Start authentication automatically
        startAuthentication();
    }

    private void startAuthentication() {
        statusText.setText("");
        retryBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        statusText.setText("Fingerprint authenticated successfully!");

                        // Save username and login flag
                        SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("currentUsername", username);
                        editor.putBoolean("isLoggedIn", true);
                        editor.putBoolean("isFingerprintEnabled", true);
                        editor.apply();

                        // Navigate to HomePageActivity
                        new android.os.Handler().postDelayed(() -> {
                            startActivity(new Intent(FingerprintActivity.this, HomePageActivity.class));
                            finish();
                        }, 1000);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        statusText.setText("Error: " + errString);
                        retryBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        statusText.setText("Fingerprint not recognized. Try again.");
                        retryBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Road Guard Fingerprint Login")
                .setSubtitle("Use your fingerprint to login")
                .setDescription("Place your finger on the sensor")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
