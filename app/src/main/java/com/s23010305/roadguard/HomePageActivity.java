package com.s23010305.roadguard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomePageActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private String numberToCall = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_page);

        // Logout
        ImageView logoutImageView = findViewById(R.id.logOutbtn);
        logoutImageView.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(HomePageActivity.this, LoginPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        // Emergency Call - direct call to 119
        Button emgCallBtn = findViewById(R.id.emgCallBtn);
        emgCallBtn.setOnClickListener(v -> {
            numberToCall = "119";
            makeDirectCall();
        });

        // Gas Store
        Button gasStoreBtn = findViewById(R.id.gasStoreBtn);
        gasStoreBtn.setOnClickListener(v -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=gas+station+near+me"));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        // Police
        ImageView policeCard = findViewById(R.id.police);
        policeCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, PolicePageActivity.class);
            startActivity(intent);
        });

        // Ambulance - direct call to 1990
        ImageView ambulanceCard = findViewById(R.id.ambulance);
        ambulanceCard.setOnClickListener(v -> {
            numberToCall = "1990";
            makeDirectCall();
        });

        // Repair Shop
        ImageView repairShopCard = findViewById(R.id.repairshop);
        repairShopCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, RepairShopPageActivity.class);
            startActivity(intent);
        });

        // Towing
        ImageView towingCard = findViewById(R.id.towing);
        towingCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, TowingPageActivity.class);
            startActivity(intent);
        });

        // Profile
        ImageView profileIcon = findViewById(R.id.profile);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ProfilePageActivity.class);
            startActivity(intent);
        });
    }

    private void makeDirectCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numberToCall));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeDirectCall();
            } else {
                Toast.makeText(this, "Permission Denied to make calls", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
