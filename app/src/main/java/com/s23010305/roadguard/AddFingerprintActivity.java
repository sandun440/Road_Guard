package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddFingerprintActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView promptText, statusText;
    private Button retryBtn, cancelBtn, addFingerprintBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fingerprint);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        promptText = findViewById(R.id.promptText);
        statusText = findViewById(R.id.statusText);
        retryBtn = findViewById(R.id.retryBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        addFingerprintBtn = findViewById(R.id.addFingerprintBtn);

        // Set up button listeners
        retryBtn.setOnClickListener(v -> startAuthentication());
        cancelBtn.setOnClickListener(v -> finish());
        addFingerprintBtn.setOnClickListener(v -> startAuthentication());

        // Start fingerprint authentication automatically
        startAuthentication();
    }

    private void startAuthentication() {
        // Reset UI
        progressBar.setVisibility(View.VISIBLE);
        promptText.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        addFingerprintBtn.setVisibility(View.GONE); // Hide during authentication
        statusText.setText("");

        // Set up Executor for BiometricPrompt
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Authentication failed: " + errString);
                    statusText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                    addFingerprintBtn.setVisibility(View.VISIBLE); // Show again after failure
                    promptText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Fingerprint added successfully!");
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    statusText.setVisibility(View.VISIBLE);
                    // Navigate to HomePageActivity
                    new android.os.Handler().postDelayed(() -> {
                        Intent intent = new Intent(AddFingerprintActivity.this, HomePageActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }, 1000);
                });
            }

            @Override
            public void onAuthenticationFailed() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Authentication failed. Try again.");
                    statusText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                    addFingerprintBtn.setVisibility(View.VISIBLE); // Show again after failure
                    promptText.setVisibility(View.VISIBLE);
                });
            }
        });

        // Show BiometricPrompt
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Road Guard Fingerprint Setup")
                .setDescription("Add your fingerprint to proceed")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}