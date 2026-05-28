package com.example.quizapp_fj;

public class Question {
    private String text;
    private int imageResId;
    private String option1;
    private String option2;
    private String correctAnswer;
    private String explanation;

    public Question(String text, int imageResId, String option1, String option2, String correctAnswer, String explanation) {
        this.text = text;
        this.imageResId = imageResId;
        this.option1 = option1;
        this.option2 = option2;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public String getText() {
        return text;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }
}
