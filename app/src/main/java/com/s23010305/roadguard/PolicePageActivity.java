package com.s23010305.roadguard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolicePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.police_page); // Replace with your XML layout name

        // Initialize the back button
        ImageView backButton = findViewById(R.id.imageView9);
        backButton.setOnClickListener(v -> finish()); // Closes the activity

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize buttons
        Button nearestPoliceButton = findViewById(R.id.button5);
        Button sendAlertButton = findViewById(R.id.button6);

        // Nearest Police button click listener
        nearestPoliceButton.setOnClickListener(v -> {
            // Placeholder: Find and display nearby police stations
            Toast.makeText(this, "Finding nearest police station...", Toast.LENGTH_SHORT).show();
            findNearestPolice();
        });

        // Send Alert button click listener
        sendAlertButton.setOnClickListener(v -> {
            // Placeholder: Send alert functionality
            Toast.makeText(this, "Alert sent!", Toast.LENGTH_SHORT).show();
            sendAlert();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Check for location permissions and enable My Location layer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // Show user's location
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Example: Add a marker and move camera to a default location
        LatLng defaultLocation = new LatLng(7.583460884012087, 80.68332185105507); // Example: San Francisco
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Sample Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void findNearestPolice() {
        // TODO: Implement logic to find nearby police stations
        // Example: Use Google Places API to search for nearby police stations
        // You can make an HTTP request to the Places API or use the Places SDK
        // For now, this is a placeholder
        LatLng policeStation = new LatLng(37.7849, -122.4294); // Example coordinates
        mMap.addMarker(new MarkerOptions().position(policeStation).title("Nearest Police Station"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(policeStation, 15));
    }

    private void sendAlert() {
        // TODO: Implement alert functionality
        // Example: Send a notification, SMS, or API call to an emergency service
        // This is a placeholder for your actual implementation
    }


}