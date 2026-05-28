package com.example.quizapp_fj;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ChatbotActivity extends AppCompatActivity {

    private TextView tvChatHistory;
    private EditText etChatMessage;
    private ScrollView scrollChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        MaterialToolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvChatHistory = findViewById(R.id.tvChatHistory);
        etChatMessage = findViewById(R.id.etChatMessage);
        scrollChat = findViewById(R.id.scrollChat);
        ImageButton btnSendChat = findViewById(R.id.btnSendChat);

        MaterialButton btnSugPriority = findViewById(R.id.btnSugPriority);
        MaterialButton btnSugSigns = findViewById(R.id.btnSugSigns);
        MaterialButton btnSugSpeed = findViewById(R.id.btnSugSpeed);
        MaterialButton btnSugAlcohol = findViewById(R.id.btnSugAlcohol);

        btnSendChat.setOnClickListener(v -> sendMessage());

        btnSugPriority.setOnClickListener(v -> {
            addMessage("Vous: Quelles sont les règles de priorité ?");
            getBotResponse("priorité");
        });
        btnSugSigns.setOnClickListener(v -> {
            addMessage("Vous: Parlez-moi des panneaux.");
            getBotResponse("panneau");
        });
        btnSugSpeed.setOnClickListener(v -> {
            addMessage("Vous: Quelles sont les limitations de vitesse ?");
            getBotResponse("vitesse");
        });
        btnSugAlcohol.setOnClickListener(v -> {
            addMessage("Vous: Quelles sont les limites d'alcool ?");
            getBotResponse("alcool");
        });
        btnSugAlcohol.setOnClickListener(v -> {
            addMessage("Vous: Quelles sont les limites d'alcool ?");
            getBotResponse("alcool");
        });

    }

    private void sendMessage() {
        String query = etChatMessage.getText().toString().trim();
        if (!query.isEmpty()) {
            addMessage("Vous: " + query);
            etChatMessage.setText("");
            getBotResponse(query.toLowerCase());
        }
    }

    private void getBotResponse(String userText) {
        String response;

        if (userText.contains("priorité") || userText.contains("droite")) {
            response = "La règle d'or est la priorité à droite. Attention aux panneaux 'Cédez le passage', 'Stop' ou aux ronds-points qui modifient cet ordre.";
        } else if (userText.contains("panneau") || userText.contains("signalisation")) {
            response = "Il y a 4 types principaux : Triangle (Danger), Rond (Interdiction/Obligation), Carré (Indication) et Flèche (Direction).";
        } else if (userText.contains("vitesse") || userText.contains("limite") || userText.contains("km/h")) {
            response = "Ville : 50 km/h | Route : 80 km/h | Autoroute : 130 km/h (110 s'il pleut).";
        } else if (userText.contains("alcool") || userText.contains("boire")) {
            response = "Limite standard : 0,5 g/L (0,2 g/L pour les jeunes conducteurs). Un verre standard = environ 0,20g/L.";
        } else if (userText.contains("bonjour") || userText.contains("salut")) {
            response = "Bonjour ! Je suis là pour vous aider dans vos révisions du code de la route.";
        } else if (userText.contains("merci")) {
            response = "Je vous en prie ! Autre chose ?";
        } else {
            response = "Désolé, je n'ai pas compris. Essayez de me parler de 'priorité', 'panneaux' ou 'vitesse'.";
        }

        scrollChat.postDelayed(() -> addMessage("Assistant: " + response), 500);
    }

    private void addMessage(String text) {
        tvChatHistory.append("\n" + text + "\n");
        scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
    }
}

