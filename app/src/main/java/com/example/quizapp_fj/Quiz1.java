package com.example.quizapp_fj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Quiz1 extends AppCompatActivity {
    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    TextView tvTimer;
    int score = 0;
    String RepCorrect = "Non";
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz1);

        rg = findViewById(R.id.rg);
        bNext = findViewById(R.id.bNext);
        tvTimer = findViewById(R.id.tvTimer);

        // Initialisation du Timer (15 secondes)
        timer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // On n'affiche plus le temps écoulé
            }

            @Override
            public void onFinish() {
                // Passage direct à la question suivante sans Toast
                goToNextQuestion();
            }
        }.start();

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rg.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
                } else {
                    rb = findViewById(rg.getCheckedRadioButtonId());
                    if (rb.getText().toString().equals(RepCorrect)) {
                        score += 1;
                    }
                    goToNextQuestion();
                }
            }
        });
    }

    private void goToNextQuestion() {
        if (timer != null) {
            timer.cancel();
        }
        Intent intent = new Intent(Quiz1.this, Quiz2.class);
        intent.putExtra("score", score);
        startActivity(intent);
        overridePendingTransition(R.anim.exit, R.anim.entry);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
