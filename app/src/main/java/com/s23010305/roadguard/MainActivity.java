package com.s23010305.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loading_page); // This is your loading page

        // Delay for 2 seconds, then open FirstPageActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
            startActivity(intent);
            finish(); // Optional: close the loading activity
        }, 2000); // 2000 milliseconds = 2 seconds

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loadingpage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
