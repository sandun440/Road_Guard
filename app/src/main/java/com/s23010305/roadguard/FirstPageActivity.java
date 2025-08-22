package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class FirstPageActivity extends AppCompatActivity {

    private static final String PREFS = "RoadGuardPrefs";
    private static final String KEY_FP_USER = "fingerprintUser";
    private static final String KEY_FP_ENABLED = "isFingerprintEnabled";
    private static final String KEY_CURRENT_USER = "currentUsername"; // set this after password login

    private Button loginBtn, signupBtn;
    private ImageView fingerprintIcon;

    private String fingerprintUser;
    private boolean isFingerprintEnabled;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_page);

        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);
        fingerprintIcon = findViewById(R.id.fingerprintIcon);

        // Load fingerprint prefs
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        fingerprintUser = prefs.getString(KEY_FP_USER, null);
        isFingerprintEnabled = prefs.getBoolean(KEY_FP_ENABLED, false);

        // Buttons for normal flow
        loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginPageActivity.class)));
        signupBtn.setOnClickListener(v -> startActivity(new Intent(this, SignUpPageActivity.class)));

        // Check biometric availability; if not available, disable the fingerprint icon
        BiometricManager bm = BiometricManager.from(this);
        int canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        boolean biometricAvailable = (canAuth == BiometricManager.BIOMETRIC_SUCCESS);

        if (!biometricAvailable) {
            if (fingerprintIcon != null) fingerprintIcon.setEnabled(false);
        }

        // Prepare prompt
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(FirstPageActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(FirstPageActivity.this, "Fingerprint recognized!", Toast.LENGTH_SHORT).show();

                        // Navigate directly to HomePage with the fingerprint-bound user
                        Intent intent = new Intent(FirstPageActivity.this, HomePageActivity.class);
                        intent.putExtra("username", fingerprintUser);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(FirstPageActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        // Fingerprint login button behavior
        if (fingerprintIcon != null) {
            fingerprintIcon.setOnClickListener(v -> {
                if (!biometricAvailable) {
                    Toast.makeText(this, "Biometric not available on this device", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isFingerprintEnabled || fingerprintUser == null || fingerprintUser.trim().isEmpty()) {
                    Toast.makeText(this, "No fingerprint login available", Toast.LENGTH_SHORT).show();
                } else {
                    biometricPrompt.authenticate(promptInfo);
                }
            });
        }
    }
}
