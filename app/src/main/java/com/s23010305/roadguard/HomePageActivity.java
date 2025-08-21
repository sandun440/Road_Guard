package com.s23010305.roadguard;

import android.Manifest;
import android.content.Intent;
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
    private ImageView profileIcon; // reference to the profile icon on the home screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_page);

        // Emergency Call - direct call to 119
        Button emgCallBtn = findViewById(R.id.emgCallBtn);
        emgCallBtn.setOnClickListener(v -> {
            numberToCall = "119";
            makeDirectCall();
        });

        // Fire Rescue - direct call to 110
        Button fireRescueBtn = findViewById(R.id.firerescuebtn);
        fireRescueBtn.setOnClickListener(v -> {
            numberToCall = "110";
            makeDirectCall();
        });

        // Gas Store (already opens Google Maps search)
        Button gasStoreBtn = findViewById(R.id.gasStoreBtn);
        gasStoreBtn.setOnClickListener(v -> openMapSearch("gas station near me"));

        // Police (kept as-is: opens your Police page)
        ImageView policeCard = findViewById(R.id.police);
        policeCard.setOnClickListener(v ->
                startActivity(new Intent(HomePageActivity.this, PolicePageActivity.class)));

        // Ambulance - direct call to 1990
        ImageView ambulanceCard = findViewById(R.id.ambulance);
        ambulanceCard.setOnClickListener(v -> {
            numberToCall = "1990";
            makeDirectCall();
        });

        // Repair Shop -> open Google Maps search
        ImageView repairShopCard = findViewById(R.id.repairshop);
        repairShopCard.setOnClickListener(v -> openMapSearch("repair shop near me"));

        // Towing -> open Google Maps search
        ImageView towingCard = findViewById(R.id.towing);
        towingCard.setOnClickListener(v -> openMapSearch("towing service near me"));

        // Profile
        profileIcon = findViewById(R.id.profile);
        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(HomePageActivity.this, ProfilePageActivity.class)));

        // Initial load (in case of returning from cold start)
        ProfileImageUtils.loadInto(this, profileIcon, R.drawable.profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the profile picture every time we return to Home
        ProfileImageUtils.loadInto(this, profileIcon, R.drawable.profile);
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

    private void openMapSearch(String query) {
        // Prefer the Maps app if available
        Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geo);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to browser
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
            startActivity(new Intent(Intent.ACTION_VIEW, web));
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
