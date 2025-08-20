package com.s23010305.roadguard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PolicePageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private LatLng currentLatLng;   // updated by live location
    private Marker myMarker;        // "You are here" marker

    private final String POLICE_NUMBER = "119"; // change if needed
    private final OkHttpClient http = new OkHttpClient();

    // Read your Places API key from res/values/strings.xml
    private String getPlacesApiKey() { return getString(R.string.my_api_key); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.police_page);

        ImageView backButton = findViewById(R.id.imageView9);
        backButton.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Live location request (high accuracy, ~3s)
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1500L)
                .setWaitForAccurateLocation(true)
                .build();

        locationCallback = new LocationCallback() {
            @Override public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc == null) return;

                currentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                if (mMap != null) {
                    if (myMarker == null) {
                        myMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("You are here"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                    } else {
                        myMarker.setPosition(currentLatLng);
                    }
                }
            }
        };

        Button nearestPoliceButton = findViewById(R.id.button5);
        Button sendAlertButton     = findViewById(R.id.button6);

        nearestPoliceButton.setOnClickListener(v -> findNearestPolice());
        sendAlertButton.setOnClickListener(v -> sendAlert());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        ensureLocationPermissionAndStart();
    }

    private void ensureLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }
        if (mMap != null) mMap.setMyLocationEnabled(true);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureLocationPermissionAndStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ensureLocationPermissionAndStart();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Button: find and show the single nearest police station with distance. */
    private void findNearestPolice() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        // If we don't yet have a live fix, do a one-shot
        if (currentLatLng == null) {
            CancellationTokenSource cts = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(loc -> {
                        if (loc == null) {
                            Toast.makeText(this, "Getting location… please try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                        queryNearestPoliceAndShow(currentLatLng, /*didFallback=*/false);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            queryNearestPoliceAndShow(currentLatLng, /*didFallback=*/false);
        }
    }

    private void queryNearestPoliceAndShow(LatLng center, boolean didFallback) {
        final double lat = center.latitude, lng = center.longitude;

        // Nearest-first
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + lat + "," + lng
                + "&rankby=distance"
                + "&type=police"
                + "&key=" + getPlacesApiKey();

        Request request = new Request.Builder().url(url).build();
        http.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!didFallback) { queryNearestPoliceFallbackTextSearch(center); return; }
                runOnUiThread(() ->
                        Toast.makeText(PolicePageActivity.this, "Places error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (!didFallback) { queryNearestPoliceFallbackTextSearch(center); return; }
                    runOnUiThread(() ->
                            Toast.makeText(PolicePageActivity.this, "Places error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.optJSONArray("results");
                    if (results == null || results.length() == 0) {
                        if (!didFallback) { queryNearestPoliceFallbackTextSearch(center); }
                        else { openMapsSearchForPolice(center); }
                        return;
                    }

                    JSONObject nearest = results.getJSONObject(0);
                    showNearestMarkerWithDistance(center, nearest);

                } catch (Exception e) {
                    if (!didFallback) { queryNearestPoliceFallbackTextSearch(center); return; }
                    runOnUiThread(() ->
                            Toast.makeText(PolicePageActivity.this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /** Fallback (one time) using Text Search. */
    private void queryNearestPoliceFallbackTextSearch(LatLng center) {
        final double lat = center.latitude, lng = center.longitude;

        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json"
                + "?query=police+station"
                + "&location=" + lat + "," + lng
                + "&radius=5000"
                + "&key=" + getPlacesApiKey();

        Request request = new Request.Builder().url(url).build();
        http.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Final fallback: open Google Maps app to ensure the user sees something
                openMapsSearchForPolice(center);
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) { openMapsSearchForPolice(center); return; }
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.optJSONArray("results");
                    if (results == null || results.length() == 0) { openMapsSearchForPolice(center); return; }

                    JSONObject nearest = results.getJSONObject(0);
                    showNearestMarkerWithDistance(center, nearest);

                } catch (Exception e) {
                    openMapsSearchForPolice(center);
                }
            }
        });
    }

    private void showNearestMarkerWithDistance(LatLng from, JSONObject place) throws JSONException {
        JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
        double nlat = loc.getDouble("lat");
        double nlng = loc.getDouble("lng");
        String name = place.optString("name", "Nearest Police Station");

        float meters = distanceMeters(from.latitude, from.longitude, nlat, nlng);
        String distLabel = formatDistance(meters);

        runOnUiThread(() -> {
            mMap.clear();

            // Re-add "you are here"
            if (currentLatLng != null) {
                myMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
            }

            LatLng nPos = new LatLng(nlat, nlng);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(nPos)
                    .title(name)
                    .snippet(distLabel));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nPos, 15f));
            if (marker != null) marker.showInfoWindow();

            Toast.makeText(PolicePageActivity.this,
                    "Nearest: " + name + " (" + distLabel + ")", Toast.LENGTH_LONG).show();
        });
    }

    private void openMapsSearchForPolice(LatLng center) {
        runOnUiThread(() -> {
            Uri uri = Uri.parse("geo:" + center.latitude + "," + center.longitude + "?q=police+station");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }

    private static float distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        float[] out = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, out);
        return out[0];
    }

    private static String formatDistance(float meters) {
        DecimalFormat kmFmt = new DecimalFormat("#.##");
        return (meters >= 1000f)
                ? kmFmt.format(meters / 1000f) + " km away"
                : Math.round(meters) + " m away";
    }

    /** Opens SMS with current GPS coords pre-filled (no SEND_SMS permission needed). */
    private void sendAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Couldn't get location. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
                    String message = "EMERGENCY: I need help. My location is: "
                            + lat + ", " + lng + "\n" + mapsLink;

                    Uri smsUri = Uri.parse("smsto:" + POLICE_NUMBER);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                    intent.putExtra("sms_body", message);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
