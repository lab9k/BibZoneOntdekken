package com.example.android.bibdiscovery;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        TextView tx = findViewById(R.id.introText);
        Button btn = findViewById(R.id.startBtn);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf");

        tx.setTypeface(custom_font);
        btn.setTypeface(custom_font);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}