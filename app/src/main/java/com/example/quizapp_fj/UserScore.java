package com.example.quizapp_fj;

public class UserScore {
    private String userName;
    private int score;

    public UserScore() {
        // Constructeur vide
    }

    public UserScore(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }

    public String getUserName() { return userName; }
    public int getScore() { return score; }
}
