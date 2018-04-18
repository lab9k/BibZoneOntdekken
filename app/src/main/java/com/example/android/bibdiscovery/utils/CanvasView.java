package com.example.android.bibdiscovery.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.android.bibdiscovery.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lottejespers.
 */

public class CanvasView extends View {

    private Paint wallpaint;
    private Path wallpath;
    private Path foundpath;
    private Paint foundpaint;

    private List<Double> x = new ArrayList<>();
    private List<Double> y = new ArrayList<>();


    private HashMap<String, List<Double>> founded = new HashMap<>();

    private HashMap<String, List<Double>> zonesX = new HashMap<>();
    private HashMap<String, List<Double>> zonesY = new HashMap<>();

    private List<String> foundedZones = new ArrayList<>();

    public CanvasView(Context context) {
        super(context, null);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        wallpaint = new Paint();
        foundpaint = new Paint();
        wallpath = new Path();
        foundpath = new Path();
        wallpaint.setColor(Color.DKGRAY);
        wallpaint.setStyle(Paint.Style.STROKE);
        wallpaint.setStrokeWidth(15);

        foundpaint.setColor(Color.DKGRAY);
        foundpaint.setStyle(Paint.Style.FILL_AND_STROKE);
        foundpaint.setStrokeWidth(1);
        foundpaint.setAlpha(215);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

//      Draw pentagon
        wallpath.reset();

        if (x.size() > 0) {
            wallpath.moveTo((float) (x.get(0) / 259.2756208) * width, (float) (y.get(0) / 162.8976568) * height);
            for (int i = 0; i < x.size(); i++) {
                Double tempx = x.get(i);
                Double tempy = y.get(i);
                wallpath.lineTo((float) (tempx / 259.2756208) * width, (float) (tempy / 162.8976568) * height);
            }
            wallpath.close();

            canvas.drawPath(wallpath, wallpaint);
        }

        if (founded.size() > 0)
            for (Map.Entry<String, List<Double>> entry : founded.entrySet()) {
                String key = entry.getKey();
                List<Double> value = entry.getValue();
//                if (!foundedZones.contains(key)) {
                    foundedZones.add(key);

                    foundpath.reset();
                    foundpath.moveTo((float) (zonesX.get(key).get(0) / 259.2756208) * width, (float) (zonesY.get(key).get(0) / 162.8976568) * height);
                    for (int i = 0; i < zonesX.get(key).size(); i++) {
                        foundpath.lineTo((float) (zonesX.get(key).get(i) / 259.2756208) * width, (float) (zonesY.get(key).get(i) / 162.8976568) * height);
                    }
                    foundpath.close();

                    canvas.drawPath(foundpath, foundpaint);

                    Drawable d = getResources().getDrawable(R.drawable.check_pijl);
                    d.setBounds((int) (value.get(0) / 259.2756208 * width),
                            (int) (value.get(1) / 162.8976568 * height),
                            (int) (value.get(2) / 259.2756208 * width),
                            (int) (value.get(3) / 162.8976568 * height));
                    d.draw(canvas);
//                }
            }

    }

    public void drawZoneContour(String zone) {
        this.x = zonesX.get(zone);
        this.y = zonesY.get(zone);
        invalidate();
    }

    public void setFounded(HashMap<String, List<Double>> founded) {
        this.founded = founded;
        invalidate();
    }

    public void setZonesX(HashMap<String, List<Double>> zonesX) {
        this.zonesX = zonesX;
    }

    public void setZonesY(HashMap<String, List<Double>> zonesY) {
        this.zonesY = zonesY;
    }
}
