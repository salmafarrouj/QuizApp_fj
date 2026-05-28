package com.example.quizapp_fj;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private TextView tvUserName, tvUserEmail;
    private SharedPreferences sharedPreferences;
    private FirebaseUser currentUser;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfile = findViewById(R.id.ivProfile);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        
        MaterialButton bChangePhoto = findViewById(R.id.bChangePhoto);
        MaterialButton bDeletePhoto = findViewById(R.id.bDeletePhoto);
        MaterialButton bChatbot = findViewById(R.id.bChatbot); 
        MaterialButton bDrivingTools = findViewById(R.id.bDrivingTools); // Bouton GPS
        MaterialButton bGlobalRanking = findViewById(R.id.bGlobalRanking);
        MaterialButton bBack = findViewById(R.id.bBack);
        MaterialButton bLogout = findViewById(R.id.bLogout);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = getSharedPreferences("QuizAppLocalPrefs", Context.MODE_PRIVATE);

        if (savedInstanceState != null) {
            String savedUri = savedInstanceState.getString("photoUri");
            if (savedUri != null) photoUri = Uri.parse(savedUri);
        }

        updateUI();

        // Ouvrir l'Assistant IA
        bChatbot.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatbotActivity.class));
        });

        // Ouvrir les Outils de Conduite (GPS)
        bDrivingTools.setOnClickListener(v -> {
            startActivity(new Intent(this, DrivingToolsActivity.class));
        });

        // Ouvrir le Classement
        bGlobalRanking.setOnClickListener(v -> {
            startActivity(new Intent(this, TopScoresActivity.class));
        });

        bChangePhoto.setOnClickListener(v -> showImageOptions());
        bDeletePhoto.setOnClickListener(v -> deletePhoto());
        bBack.setOnClickListener(v -> finish());
        bLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void updateUI() {
        if (currentUser == null) return;
        tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Utilisateur");
        tvUserEmail.setText(currentUser.getEmail());
        loadProfileImage();
    }

    private void loadProfileImage() {
        String localUri = sharedPreferences.getString("profile_uri_" + currentUser.getUid(), null);
        Object imageSource = (localUri != null) ? Uri.parse(localUri) : (currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl() : R.drawable.baseline_person_28);
        Glide.with(this).load(imageSource).circleCrop().placeholder(R.drawable.baseline_person_28).into(ivProfile);
    }

    private void showImageOptions() {
        String[] options = {"Galerie", "Appareil photo"};
        new AlertDialog.Builder(this).setTitle("Changer la photo")
            .setItems(options, (dialog, which) -> {
                if (which == 0) chooseFromGallery();
                else checkCameraPermission();
            }).show();
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Sélectionner"), PICK_IMAGE_REQUEST);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        else openCamera();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            } catch (IOException ignored) {}
        }
    }

    private void deletePhoto() {
        new AlertDialog.Builder(this).setTitle("Supprimer").setMessage("Supprimer votre photo ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    sharedPreferences.edit().remove("profile_uri_" + currentUser.getUid()).apply();
                    updateFirebaseProfile(null);
                }).setNegativeButton("Non", null).show();
    }

    private void updateFirebaseProfile(Uri uri) {
        currentUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
                .addOnCompleteListener(task -> currentUser.reload().addOnCompleteListener(t -> updateUI()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = (requestCode == PICK_IMAGE_REQUEST && data != null) ? data.getData() : photoUri;
            if (uri != null) detectFaceAndProcess(uri);
        }
    }

    private void detectFaceAndProcess(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            FaceDetection.getClient(new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).build())
                .process(image).addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        sharedPreferences.edit().putString("profile_uri_" + currentUser.getUid(), uri.toString()).apply();
                        updateFirebaseProfile(uri);
                    } else {
                        new AlertDialog.Builder(this).setTitle("Photo invalide").setMessage("Aucun visage détecté. Veuillez réessayer.")
                            .setPositiveButton("Réessayer", (d, w) -> showImageOptions()).show();
                    }
                }).addOnFailureListener(e -> updateFirebaseProfile(uri));
        } catch (IOException e) { updateFirebaseProfile(uri); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
    }
}
