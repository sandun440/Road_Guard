package com.s23010305.roadguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class FirstPageActivity extends AppCompatActivity {

    private Button loginBtn, signupBtn;
    private ImageView fingerprintIcon;
    private DatabaseHelper databaseHelper;
    private String lastLoggedInUsername;
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

        databaseHelper = DatabaseHelper.getInstance(this);

        // Load last logged-in username and fingerprint flag
        SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
        lastLoggedInUsername = prefs.getString("currentUsername", null);
        isFingerprintEnabled = prefs.getBoolean("isFingerprintEnabled", false);

        // Login button
        loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginPageActivity.class)));

        // Signup button
        signupBtn.setOnClickListener(v -> startActivity(new Intent(this, SignUpPageActivity.class)));

        // Setup biometric authentication
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
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

                        // ✅ Navigate directly to HomePage
                        Intent intent = new Intent(FirstPageActivity.this, HomePageActivity.class);
                        intent.putExtra("username", lastLoggedInUsername);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(FirstPageActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        // Fingerprint login button
        if (fingerprintIcon != null) {
            fingerprintIcon.setOnClickListener(v -> {
                if (!isFingerprintEnabled || lastLoggedInUsername == null) {
                    Toast.makeText(this, "No fingerprint login available", Toast.LENGTH_SHORT).show();
                } else {
                    biometricPrompt.authenticate(promptInfo);
                }
            });
        }
    }
}
