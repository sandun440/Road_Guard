package com.s23010305.roadguard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class EditProfilePageActivity extends AppCompatActivity {
    EditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    ImageView profileImage;
    Button saveBtn;
    TextView changePhotoText;

    TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout;

    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private boolean pickingCamera = false;

    private DatabaseHelper db;
    private SharedPreferences prefs;

    private String currentUsername;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_page);

        // Views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        profileImage = findViewById(R.id.profileImage);
        saveBtn = findViewById(R.id.saveBtn);
        changePhotoText = findViewById(R.id.changePhotoText);

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        db = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("RoadGuardPrefs", MODE_PRIVATE);

        currentUsername = prefs.getString("logged_in_username", null);
        if (currentUsername == null) {
            currentUsername = getIntent().getStringExtra("username");
        }

        if (currentUsername != null) {
            currentUser = db.getUserByUsername(currentUsername);
        }

        // Prefill text fields from DB
        if (currentUser != null) {
            String fullName = (currentUser.getFirstName() != null ? currentUser.getFirstName() : "") +
                    (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty()
                            ? (" " + currentUser.getLastName()) : "");
            nameEditText.setText(fullName.trim());
            emailEditText.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            phoneEditText.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
            passwordEditText.setText(currentUser.getPassword() != null ? currentUser.getPassword() : "");
        } else {
            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
        }

        // Load saved profile image into the preview
        ProfileImageUtils.loadInto(this, profileImage, R.drawable.profile);

        // Image picker (Camera + Gallery)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (pickingCamera && cameraImageUri != null) {
                            profileImage.setImageURI(cameraImageUri);
                            selectedImageUri = cameraImageUri;
                        } else if (!pickingCamera && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            profileImage.setImageURI(imageUri);
                            selectedImageUri = imageUri;
                        }
                    }
                });

        // Permission launcher (single permission flow kept as-is)
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        showImagePickerOptions();
                    } else {
                        String permission = getPermissionString();
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            showSettingsDialog();
                        } else {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        changePhotoText.setOnClickListener(v -> requestPermissionAndPick());
        saveBtn.setOnClickListener(v -> onSaveClicked());
    }

    private void onSaveClicked() {
        if (currentUsername == null) {
            Toast.makeText(this, "No logged-in user—cannot save.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName  = nameEditText.getText().toString().trim();
        String email     = emailEditText.getText().toString().trim();
        String phone     = phoneEditText.getText().toString().trim();
        String password  = passwordEditText.getText().toString().trim();

        String firstName = "";
        String lastName  = "";
        if (!fullName.isEmpty()) {
            String[] parts = fullName.split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) lastName = parts[1];
        }

        if (currentUser != null) {
            if (firstName.isEmpty()) firstName = currentUser.getFirstName();
            if (lastName.isEmpty())  lastName  = currentUser.getLastName();
            if (email.isEmpty())     email     = currentUser.getEmail();
            if (phone.isEmpty())     phone     = currentUser.getPhone();
            if (password.isEmpty())  password  = currentUser.getPassword();
        }

        boolean ok;
        try {
            ok = db.updateUser(currentUsername, firstName, lastName, email, phone, password);
        } catch (Exception ex) {
            Toast.makeText(this, "Update failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!ok) {
            Toast.makeText(this, "No changes saved.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the chosen image Uri (shared across the app)
        if (selectedImageUri != null) {
            ProfileImageUtils.saveUriString(this, selectedImageUri.toString());
        }

        Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();

        currentUser = db.getUserByUsername(currentUsername);

        startActivity(new Intent(this, ProfilePageActivity.class));
        finish();
    }

    private String getPermissionString() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    private void requestPermissionAndPick() {
        String mediaPermission = getPermissionString();
        String cameraPermission = Manifest.permission.CAMERA;

        if (ContextCompat.checkSelfPermission(this, mediaPermission) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            showImagePickerOptions();
        } else {
            requestPermissionLauncher.launch(mediaPermission);
            requestPermissionLauncher.launch(cameraPermission);
        }
    }

    private void showImagePickerOptions() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image From")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else pickImage();
                })
                .show();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("You have denied the permission permanently. Please go to app settings to enable it.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickImage() {
        pickingCamera = false;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void openCamera() {
        pickingCamera = true;
        try {
            File imageFile = File.createTempFile(
                    "profile_" + System.currentTimeMillis(), ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            imagePickerLauncher.launch(intent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }
}
