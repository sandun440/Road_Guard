package com.s23010305.roadguard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * PolicePageActivity:
 * - Shows Google Map with user's current location (blue dot)
 * - Button 5: Opens Google Maps and searches for "police station near me"
 * - Button 6: Sends an SMS with the user's live GPS location to police (119)
 */
public class PolicePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String POLICE_NUMBER = "119"; // Emergency number

    private GoogleMap mMap; // Google Map instance
    private FusedLocationProviderClient fusedLocationClient; // For GPS location

    // Launcher for requesting location permission at runtime
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    // If permission granted → enable blue dot & center map on user
                    enableMyLocationOnMapIfReady();
                    centerMapOnCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.police_page);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Back button to close the activity
        ImageView backButton = findViewById(R.id.imageView9);
        backButton.setOnClickListener(v -> finish());

        // Load map fragment into container view
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Buttons
        Button nearestPoliceButton = findViewById(R.id.button5);
        Button sendAlertButton     = findViewById(R.id.button6);

        // Open Google Maps app with "police station near me"
        nearestPoliceButton.setOnClickListener(v -> openMapsSearchForPolice());

        // Send SMS to 119 with current location
        sendAlertButton.setOnClickListener(v -> sendAlertSmsWithLocation());
    }

    // ====== Google Map Callbacks ======
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true); // enable zoom buttons
        enableMyLocationOnMapIfReady();
        centerMapOnCurrentLocation();  // Move camera to current location
    }

    /** Enable "blue dot" on the map if permission granted */
    @SuppressLint("MissingPermission") // Safe because we check permission before calling
    private void enableMyLocationOnMapIfReady() {
        if (mMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Request permission if not yet granted
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /** Center the camera on the user's current location once */
    @SuppressLint("MissingPermission")
    private void centerMapOnCurrentLocation() {
        if (mMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return; // If no permission, skip
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) return;
                    LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 16f));
                })
                .addOnFailureListener(e -> {
                    // Not critical; blue dot will appear once Maps self-updates
                });
    }

    /** Helper: check if app has fine location permission */
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ====== Button 5: Open Google Maps search for police station ======
    private void openMapsSearchForPolice() {
        // geo:0,0?q= → lets Maps app search using device's current location
        Uri uri = Uri.parse("geo:0,0?q=police+station+near+me");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        // Fallback: if Google Maps not installed, open any maps app
        if (intent.resolveActivity(getPackageManager()) == null) {
            intent.setPackage(null);
        }
        startActivity(intent);
    }

    // ====== Button 6: Send SMS alert to police with GPS location ======
    @SuppressLint("MissingPermission")
    private void sendAlertSmsWithLocation() {
        if (!hasFineLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Couldn't get location. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    composeAndOpenSms(location);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Compose SMS with current coordinates + Google Maps link */
    private void composeAndOpenSms(@NonNull Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Create a shareable Google Maps link
        String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
        String message = "EMERGENCY: I need help. My location is: " + lat + ", " + lng + "\n" + mapsLink;

        // "smsto:" scheme → opens SMS app with recipient pre-filled
        //noinspection SpellCheckingInspection
        Uri smsUri = Uri.parse("smsto:" + POLICE_NUMBER);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }
}
