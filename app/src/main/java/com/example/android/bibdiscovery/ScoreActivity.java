package com.example.android.bibdiscovery;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.android.bibdiscovery.models.Score;
import com.example.android.bibdiscovery.utils.ScoreAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreActivity extends AppCompatActivity {

    private List<Score> namen;
    private SharedPreferences mPrefs;
    private String naam;
    private String score;
    private ListView listView;
    private ScoreAdapter adapter;
    private Score tempScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        score = getIntent().getStringExtra("score");

        getNamen();

        saveNamen();

        Comparator<Score> comparator = new Comparator<Score>() {
            @Override
            public int compare(Score left, Score right) {
                int compare = 0;
                if (Integer.valueOf(left.getScore().replaceAll(":", "")) < Integer.valueOf(right.getScore().replaceAll(":", ""))) {
                    compare = -1;
                } else if (Integer.valueOf(left.getScore().replaceAll(":", "")) > Integer.valueOf(right.getScore().replaceAll(":", ""))) {
                    compare = 1;
                }
                return compare;
            }
        };

        Collections.sort(namen, comparator);

        listView = findViewById(R.id.list);
        adapter = new ScoreAdapter(namen, this);
        listView.setAdapter(adapter);
    }

    public void saveNamen() {
        tempScore = containsName();

        if (tempScore != null) {
            tempScore.setScore(score);
//            namen.add(tempScore);
        }
        SharedPreferences.Editor editor;
        mPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
        editor = mPrefs.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(namen);
        editor.putString("namen", jsonFavorites);
        editor.apply();
    }

    private Score containsName() {
        for (Score s : namen) {
            if (s.getName().equals(naam))
                return s;
        }
        return null;
    }

    public void getNamen() {
        // used for retrieving arraylist from json formatted string
        List namenTemp;
        mPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
        naam = mPrefs.getString("naam", null);
        if (mPrefs.contains("namen")) {
            String jsonFavorites = mPrefs.getString("namen", null);
            Gson gson = new Gson();
            Score[] favoriteItems = gson.fromJson(jsonFavorites, Score[].class);
            namenTemp = Arrays.asList(favoriteItems);
            namen = new ArrayList<Score>(namenTemp);
        }
    }
}
