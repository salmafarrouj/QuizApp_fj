package com.example.quizapp_fj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    EditText etLogin, etPassword;
    Button bLogin;
    TextView tvRegister;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLogin = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        bLogin = findViewById(R.id.bLogin);
        tvRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();

        // Si déjà connecté, on va directement au Quiz (et non au Chatbot)
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, QuizActivity.class));
            finish();
        }

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = etLogin.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),"Champs vides",Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                                    // Direction le Quiz directement
                                    startActivity(new Intent(MainActivity.this, QuizActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        tvRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Register.class)));
    }
}
