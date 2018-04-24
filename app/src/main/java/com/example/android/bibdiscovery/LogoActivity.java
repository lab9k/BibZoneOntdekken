package com.example.android.bibdiscovery;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        final ImageView krook = findViewById(R.id.krook);
        krook.startAnimation(fadeIn);

        final ImageView lab9k = findViewById(R.id.lab9k);
        lab9k.startAnimation(fadeIn);

        final ImageView digipolis = findViewById(R.id.digipolis);
        digipolis.startAnimation(fadeIn);

        final ImageView gent = findViewById(R.id.gent);
        gent.startAnimation(fadeIn);

        final Intent startIntent = new Intent(LogoActivity.this, StartActivity.class);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                krook.startAnimation(fadeOut);
                lab9k.startAnimation(fadeOut);
                digipolis.startAnimation(fadeOut);
                gent.startAnimation(fadeOut);
            }
        }, 4000);

        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(startIntent);
            }
        }, 7000);
    }
}
