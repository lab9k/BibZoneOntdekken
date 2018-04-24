package com.example.android.bibdiscovery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.android.bibdiscovery.models.Score;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NameActivity extends AppCompatActivity {

    private List<Score> namen = new ArrayList<>();
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        getNamen();

        TextView tx = findViewById(R.id.nameText);
        Button btn = findViewById(R.id.startBtn);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf");
        final EditText etx = findViewById(R.id.name);

        etx.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });
        etx.setTypeface(custom_font);
        tx.setTypeface(custom_font);
        btn.setTypeface(custom_font);

        final Intent intent = new Intent(NameActivity.this, MainActivity.class);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(etx)) {
                    saveNamen(etx);
                    startActivity(intent);
                    finish();
                }
            }
        });

        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (checkInput(etx)) {
                        saveNamen(etx);
                        startActivity(intent);
                        finish();
                    }
                }
                return true;
            }
        };
        etx.setOnEditorActionListener(enterListener);
        mPrefs = getPreferences(MODE_PRIVATE);
    }

    private boolean checkInput(EditText etx) {
        if (etx.getText().toString().trim().isEmpty()) {
            etx.setError("Vul hier je naam in!");
            return false;
        }
        return true;
    }

    public void saveNamen(EditText etx) {
        String naam = etx.getText().toString().trim().toLowerCase();
        Score score = containsName(naam);
        if (score == null) {
            namen.add(new Score(naam));
        }
        SharedPreferences.Editor editor;
        mPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
        editor = mPrefs.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(namen);
        editor.putString("namen", jsonFavorites);
        editor.putString("naam", naam);
        editor.apply();
    }

    public void getNamen() {
        // used for retrieving arraylist from json formatted string
        List namenTemp;
        mPrefs = getSharedPreferences("score", Context.MODE_PRIVATE);
        if (mPrefs.contains("namen")) {
            String jsonFavorites = mPrefs.getString("namen", null);
            Gson gson = new Gson();
            Score[] favoriteItems = gson.fromJson(jsonFavorites, Score[].class);
            namenTemp = Arrays.asList(favoriteItems);
            namen = new ArrayList<Score>(namenTemp);
        }
    }

    private Score containsName(String naam) {
        for (Score s : namen) {
            if (s.getName().equals(naam))
                return s;
        }
        return null;
    }
}
