package com.example.quizapp_fj;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopScoresActivity extends AppCompatActivity {

    private RecyclerView rvTopScores;
    private TopScoresAdapter adapter;
    private List<UserScore> scoreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_scores);

        // Configuration de la barre d'outils
        MaterialToolbar toolbar = findViewById(R.id.toolbarTopScores);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes Records Personnels");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Configuration du RecyclerView
        rvTopScores = findViewById(R.id.rvTopScores);
        rvTopScores.setLayoutManager(new LinearLayoutManager(this));
        
        scoreList = new ArrayList<>();
        adapter = new TopScoresAdapter(scoreList);
        rvTopScores.setAdapter(adapter);

        loadLocalScores();
    }

    private void loadLocalScores() {
        SharedPreferences pref = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        String history = pref.getString("history", "");
        
        if (!history.isEmpty()) {
            String[] scores = history.split(",");
            List<Integer> intScores = new ArrayList<>();
            for (String s : scores) {
                try {
                    intScores.add(Integer.parseInt(s));
                } catch (NumberFormatException ignored) {}
            }
            
            // Trier pour afficher les meilleurs scores en premier
            Collections.sort(intScores, Collections.reverseOrder());
            
            scoreList.clear();
            // On affiche les 10 meilleurs scores
            int limit = Math.min(intScores.size(), 10);
            for (int i = 0; i < limit; i++) {
                scoreList.add(new UserScore("Record #" + (i + 1), intScores.get(i)));
            }
            adapter.notifyDataSetChanged();
        }
    }
}
