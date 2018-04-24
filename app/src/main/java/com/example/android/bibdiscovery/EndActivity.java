package com.example.android.bibdiscovery;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class EndActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        long[] time = getIntent().getLongArrayExtra("time");

        TextView endText = findViewById(R.id.endText);
        endText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf"));
        endText.setText(String.format("%s%n%s%n%s", "Proficiat!", "Je hebt alle zones ontdekt!",
                "Je deed er " + time[1] + " uren, " +
                        time[2] + " minuten en " + time[3] + " seconden over."));

        final KonfettiView konfettiView = findViewById(R.id.konfettiView);
        konfettiView.build()
                .addColors(Color.parseColor("#ED755C"), Color.parseColor("#5D4D53"), Color.parseColor("#653332"))
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .stream(300, 3000L);

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.winning);
        mp.start();

    }
}
