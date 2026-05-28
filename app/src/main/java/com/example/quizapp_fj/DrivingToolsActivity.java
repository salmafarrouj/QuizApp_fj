package com.example.quizapp_fj;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class DrivingToolsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_tools);

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbarTools);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnFindDrivingSchool = findViewById(R.id.btnFindDrivingSchool);

        btnFindDrivingSchool.setOnClickListener(v -> {
            // Utilisation d'un Intent pour ouvrir Google Maps avec une recherche
            // "geo:0,0?q=auto-école" demande à Maps de chercher "auto-école" autour de la position actuelle
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=auto-école");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            
            // Vérifier si Google Maps est installé
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Si Maps n'est pas installé, ouvrir dans le navigateur
                Uri webUri = Uri.parse("https://www.google.com/maps/search/auto-école");
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });
    }
}
