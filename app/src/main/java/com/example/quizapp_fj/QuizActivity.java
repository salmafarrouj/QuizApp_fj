package com.example.quizapp_fj;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvQuestionNum, tvQuestionText;
    private ImageView ivQuestion, ivGoProfile;
    private ImageButton ibSpeak;
    private RadioGroup rg;
    private RadioButton rb1, rb2;
    private Button bNext;
    private ProgressBar quizProgress;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer timer;
    
    private ArrayList<String> corrections = new ArrayList<>();
    private FirebaseAuth mAuth;
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mAuth = FirebaseAuth.getInstance();
        
        // Initialisation de l'IA vocale (Gratuite)
        tts = new TextToSpeech(this, this);

        tvQuestionNum = findViewById(R.id.tvQuestionNum);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        ivQuestion = findViewById(R.id.ivQuestion);
        ivGoProfile = findViewById(R.id.ivGoProfile);
        ibSpeak = findViewById(R.id.ibSpeak);
        rg = findViewById(R.id.rg);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        bNext = findViewById(R.id.bNext);
        quizProgress = findViewById(R.id.quizProgress);

        loadQuestions();
        Collections.shuffle(questionList);
        
        quizProgress.setMax(questionList.size());
        
        // L'affichage de la première question se fera une fois l'IA prête (dans onInit)

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rg.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(QuizActivity.this, "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
                } else {
                    stopSpeaking();
                    checkAnswer();
                    goToNextStep();
                }
            }
        });

        ivGoProfile.setOnClickListener(v -> {
            stopSpeaking();
            Intent intent = new Intent(QuizActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        ibSpeak.setOnClickListener(v -> speakCurrentQuestion());

        updateProfileIcon();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.FRENCH);
            isTtsInitialized = true;
            
            // SYNCHRONISATION : Détecter quand l'IA a fini de parler
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {}

                @Override
                public void onDone(String utteranceId) {
                    // L'IA a fini, on lance le chrono !
                    runOnUiThread(() -> startTimer());
                }

                @Override
                public void onError(String utteranceId) {
                    // En cas d'erreur, on lance quand même le chrono pour ne pas bloquer
                    runOnUiThread(() -> startTimer());
                }
            });

            // On affiche la première question
            runOnUiThread(() -> displayQuestion());
        } else {
            displayQuestion();
        }
    }

    private void speakCurrentQuestion() {
        if (tts != null && isTtsInitialized) {
            Question q = questionList.get(currentQuestionIndex);
            String toSpeak = "Question. " + q.getText() + ". " +
                             "Option 1. " + q.getOption1() + ". " +
                             "Option 2. " + q.getOption2();
            
            // On donne un ID à ce discours pour que le listener sache quand il finit
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "QuestionSpeakID");
            tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params, "QuestionSpeakID");
        }
    }

    private void stopSpeaking() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                vibrateOnError();
                corrections.add("Question : " + questionList.get(currentQuestionIndex).getText() + 
                               "\n⏰ (Temps écoulé) Réponse correcte : " + questionList.get(currentQuestionIndex).getCorrectAnswer() +
                               "\n💡 " + questionList.get(currentQuestionIndex).getExplanation());
                goToNextStep();
            }
        }.start();
    }

    private void displayQuestion() {
        if (timer != null) timer.cancel();

        Question q = questionList.get(currentQuestionIndex);
        
        tvQuestionNum.setText("Question " + (currentQuestionIndex + 1) + " / " + questionList.size());
        tvQuestionText.setText(q.getText());
        ivQuestion.setImageResource(q.getImageResId());
        rb1.setText(q.getOption1());
        rb2.setText(q.getOption2());
        rg.clearCheck();
        
        quizProgress.setProgress(currentQuestionIndex + 1);

        // Au lieu de lancer le timer, on lance la voix
        if (isTtsInitialized) {
            speakCurrentQuestion();
        } else {
            startTimer(); // Si pas de voix, on lance direct
        }
    }

    private void updateProfileIcon() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(ivGoProfile);
        }
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();
        questionList.add(new Question(
                "A cette intersection, je laisse la priorité à droite :",
                R.drawable.q1, "Oui", "Non", "Non",
                "Explication : Vous êtes sur une route prioritaire comme l'indique le panneau losange jaune."));
        questionList.add(new Question(
                "Le panneau de danger indique une succession de virages dont le 1er est :",
                R.drawable.q2, "À droite", "À gauche", "À droite",
                "Explication : La flèche du panneau de danger s'oriente d'abord vers la droite."));
        questionList.add(new Question(
                "Avant de partir, je laisse tourner mon moteur pour qu'il monte en température :",
                R.drawable.q3, "Oui", "Non", "Non",
                "Explication : Il est inutile et polluant de laisser tourner le moteur à l'arrêt. Il vaut mieux rouler doucement."));
        questionList.add(new Question(
                "En tant qu'automobiliste, vous devez être plus vigilant lorsque :",
                R.drawable.q4, "Le tramway est arrêté", "Le tramway circule", "Le tramway est arrêté",
                "Explication : Des piétons peuvent descendre et traverser de manière imprévue quand le tramway est à l'arrêt."));
        questionList.add(new Question(
                "En conduisant, je peux utiliser mon portable pour écrire un texto ou composer un numéro :",
                R.drawable.q5, "Oui", "Non", "Non",
                "Explication : L'usage du téléphone tenu en main est strictement interdit et très dangereux pour la concentration."));
    }

    private void checkAnswer() {
        RadioButton selectedRb = findViewById(rg.getCheckedRadioButtonId());
        Question currentQ = questionList.get(currentQuestionIndex);
        String userAnswer = selectedRb.getText().toString();
        
        if (userAnswer.equals(currentQ.getCorrectAnswer())) {
            score++;
            playSuccessSound();
        } else {
            vibrateOnError();
            corrections.add("Question : " + currentQ.getText() + 
                           "\n❌ Votre réponse : " + userAnswer + 
                           "\n✅ Réponse correcte : " + currentQ.getCorrectAnswer() +
                           "\n💡 " + currentQ.getExplanation());
        }
    }

    private void playSuccessSound() {
        try {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrateOnError() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(200);
            }
        }
    }

    private void goToNextStep() {
        if (currentQuestionIndex < questionList.size() - 1) {
            currentQuestionIndex++;
            displayQuestion();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            if (timer != null) timer.cancel();
            Intent intent = new Intent(QuizActivity.this, Score.class);
            intent.putExtra("score", score);
            intent.putExtra("total", questionList.size());
            intent.putStringArrayListExtra("corrections", corrections);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileIcon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
