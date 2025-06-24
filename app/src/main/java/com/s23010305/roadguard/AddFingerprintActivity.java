package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class AddFingerprintActivity extends AppCompatActivity {
    private static final String TAG = "AddFingerprintActivity";
    private ProgressBar progressBar;
    private TextView promptText, statusText;
    private Button retryBtn, cancelBtn, addFingerprintBtn;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_fingerprint);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        promptText = findViewById(R.id.promptText);
        statusText = findViewById(R.id.statusText);
        retryBtn = findViewById(R.id.retryBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        addFingerprintBtn = findViewById(R.id.addFingerprintBtn);

        // Check biometric availability
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_LONG).show();
            statusText.setText("Biometric authentication is not available on this device.");
            statusText.setVisibility(View.VISIBLE);
            addFingerprintBtn.setEnabled(false);
            retryBtn.setEnabled(false);
            cancelBtn.setVisibility(View.VISIBLE);
            return;
        }

        // Set up button listeners
        retryBtn.setOnClickListener(v -> startAuthentication());
        cancelBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
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
        addFingerprintBtn.setVisibility(View.GONE);
        statusText.setText("");

        // Set up BiometricPrompt
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + errString);
                    statusText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                    addFingerprintBtn.setVisibility(View.VISIBLE);
                    promptText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Fingerprint successfully added!");
                    statusText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.GONE);
                    addFingerprintBtn.setVisibility(View.GONE);
                    cancelBtn.setVisibility(View.VISIBLE);

                    // Save fingerprint authentication status to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isFingerprintEnabled", true);
                    editor.apply();

                    // Optionally save to database
                    boolean saved = databaseHelper.saveFingerprintStatus(true);
                    if (saved) {
                        Log.d(TAG, "Fingerprint status saved to database");
                        Toast.makeText(AddFingerprintActivity.this,
                                "Fingerprint authentication enabled",
                                Toast.LENGTH_SHORT).show();

                        // Delay to show success message before finishing
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(AddFingerprintActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }, 1500);
                    } else {
                        Log.e(TAG, "Failed to save fingerprint status to database");
                        statusText.setText("Error saving fingerprint data");
                        retryBtn.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Authentication failed. Please try again.");
                    statusText.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                    addFingerprintBtn.setVisibility(View.VISIBLE);
                    promptText.setVisibility(View.VISIBLE);
                });
            }
        });

        // Create BiometricPrompt.PromptInfo
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Add Fingerprint")
                .setSubtitle("Scan your fingerprint to enable authentication")
                .setDescription("Place your finger on the sensor")
                .setNegativeButtonText("Cancel")
                .build();

        // Start authentication
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up database helper
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}