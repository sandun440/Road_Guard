package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class FirstPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.first_page);

        MaterialButton loginButton = findViewById(R.id.loginBtn);
        MaterialButton signupButton = findViewById(R.id.signupBtn);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(FirstPageActivity.this, LoginPageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            });
        }

        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(FirstPageActivity.this, SignUpPageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            });
        }
    }
}