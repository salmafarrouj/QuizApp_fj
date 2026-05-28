package com.example.quizapp_fj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class Score extends AppCompatActivity {
    Button bLogout, bTry;
    MaterialButton bGoProfile, bTopScores;
    ProgressBar progressBar;
    TextView tvScore, tvCorrections;
    ImageView ivTrophy;
    LinearLayout layoutHistory;
    int score, total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        tvScore = findViewById(R.id.tvScore);
        tvCorrections = findViewById(R.id.tvCorrections);
        progressBar = findViewById(R.id.progressBar);
        layoutHistory = findViewById(R.id.layoutHistory);
        ivTrophy = findViewById(R.id.ivTrophy);
        bLogout = findViewById(R.id.bLogout);
        bTry = findViewById(R.id.bTry);
        bGoProfile = findViewById(R.id.bGoProfile);
        bTopScores = findViewById(R.id.bTopScores);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        total = intent.getIntExtra("total", 5);
        ArrayList<String> corrections = intent.getStringArrayListExtra("corrections");

        // 1. Calcul et affichage du score actuel
        int percentage = (total > 0) ? (score * 100) / total : 0;
        tvScore.setText(percentage + " %");
        progressBar.setProgress(percentage);

        // Animation si score parfait
        if (percentage == 100) {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            ivTrophy.startAnimation(pulse);
            Toast.makeText(this, "🏆 EXCELLENT ! SCORE PARFAIT ! 🏆", Toast.LENGTH_LONG).show();
        }

        // 2. Gestion de l'historique local et du record personnel
        saveScoreAndDrawGraph(percentage);
        saveHighScoreLocally(percentage);

        // 3. Affichage des corrections détaillées
        if (corrections != null && !corrections.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : corrections) {
                sb.append("• ").append(s).append("\n\n");
            }
            tvCorrections.setText(sb.toString());
        } else {
            tvCorrections.setText("Aucune erreur, félicitations !\nVotre maîtrise du code est parfaite.");
            tvCorrections.setTextColor(Color.parseColor("#2E7D32"));
        }

        bLogout.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Merci de votre Participation !", Toast.LENGTH_SHORT).show();
            finishAffinity();
        });

        bTry.setOnClickListener(v -> {
            startActivity(new Intent(Score.this, QuizActivity.class));
            finish();
        });

        bGoProfile.setOnClickListener(v -> {
            startActivity(new Intent(Score.this, ProfileActivity.class));
        });

        bTopScores.setOnClickListener(v -> {
            startActivity(new Intent(Score.this, TopScoresActivity.class));
        });
    }

    private void saveHighScoreLocally(int percentage) {
        SharedPreferences pref = getSharedPreferences("LocalTopScores", MODE_PRIVATE);
        int currentBest = pref.getInt("best_score", 0);
        if (percentage > currentBest) {
            pref.edit().putInt("best_score", percentage).apply();
            Toast.makeText(this, "🎉 Nouveau record personnel !", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveScoreAndDrawGraph(int currentPercentage) {
        SharedPreferences pref = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        String history = pref.getString("history", "");
        
        if (!history.isEmpty()) {
            history += ",";
        }
        history += currentPercentage;
        
        String[] scores = history.split(",");
        if (scores.length > 10) {
            StringBuilder sb = new StringBuilder();
            for (int i = scores.length - 10; i < scores.length; i++) {
                if (sb.length() > 0) sb.append(",");
                sb.append(scores[i]);
            }
            history = sb.toString();
            scores = history.split(",");
        }
        
        pref.edit().putString("history", history).apply();

        layoutHistory.removeAllViews();
        for (String s : scores) {
            try {
                int val = Integer.parseInt(s);
                View bar = new View(this);
                
                float density = getResources().getDisplayMetrics().density;
                int heightPx = (int) (val * 0.6 * density); 
                if (heightPx < 5) heightPx = 5;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, heightPx);
                params.weight = 1;
                params.setMargins(4, 0, 4, 0);
                bar.setLayoutParams(params);
                
                if (val < 50) bar.setBackgroundColor(Color.parseColor("#D32F2F"));
                else if (val < 80) bar.setBackgroundColor(Color.parseColor("#FBC02D"));
                else bar.setBackgroundColor(Color.parseColor("#388E3C"));
                
                layoutHistory.addView(bar);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
