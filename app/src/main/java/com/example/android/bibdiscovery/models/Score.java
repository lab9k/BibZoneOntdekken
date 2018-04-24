package com.example.android.bibdiscovery.models;

/**
 * Created by lottejespers.
 */
public class Score {

    private String name;
    private String score;

    public Score(String name) {
        this.name = name;
        this.score = "00:00:00";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
