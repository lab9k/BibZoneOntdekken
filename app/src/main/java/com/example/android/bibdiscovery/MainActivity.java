package com.example.android.bibdiscovery;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bibdiscovery.utils.CanvasView;
import com.example.android.bibdiscovery.utils.ShakeListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.anastr.speedviewlib.Speedometer;
import com.github.anastr.speedviewlib.components.note.Note;
import com.github.anastr.speedviewlib.components.note.TextNote;
import com.github.anastr.speedviewlib.util.OnSectionChangeListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;

    private ShakeListener mShaker;
    private Date currentTime;

    private CanvasView canvasView;

    private HashMap<String, List<Double>> drawZoneGridX = new HashMap<>();
    private HashMap<String, List<Double>> drawZoneGridY = new HashMap<>();
    private HashMap<String, Integer> zoneDiscovered = new HashMap<>();
    private List<String> zoneColors = Arrays.asList("#ADADA8", "#F2E106", "#AABC2A", "#ECC8D6", "#CBE7E5", "#EFAF11", "#E52134");

    private BeaconManager beaconManager;

    private BluetoothAdapter mBluetoothAdapter;
    private Snackbar snackbar;

    private Comparator<Beacon> comparator;

    private HashMap<String, String[][]> zonesGrid = new HashMap<>();
    private HashMap<String, List<Double>> centerCheck = new HashMap<>();
    private HashMap<String, List<Double>> founded = new HashMap<>();

    private Speedometer speedMeter;
    private FrameLayout frame;
    private CircularProgressBar circularProgressBar;
    private TextView progressText;
    private RelativeLayout content;

    private Animation animShake;

    private String zone = "0";

    private ShowcaseView showcaseView;
    private int counter = 0;
    private boolean start = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

//        Comparator om de beacons te sorteren volgens afstand
        comparator = new Comparator<Beacon>() {
            @Override
            public int compare(Beacon left, Beacon right) {
                int compare = 0;
                if (left.getDistance() < right.getDistance()) {
                    compare = -1;
                } else if (left.getDistance() > right.getDistance()) {
                    compare = 1;
                }
                return compare;
            }
        };

//        Initialize lists en arrays
        for (int i = 1; i < 7; i++) {
            zonesGrid.put(String.valueOf(i), new String[40][3]);
            zoneDiscovered.put(String.valueOf(i), 0);
        }

        getInfoFromJson();

        setUpView();

        setUpShowCase();

        canvasView.setZonesX(drawZoneGridX);
        canvasView.setZonesY(drawZoneGridY);

        checkAndRequestPermissions();
        checkBluetooth();

        beaconManager.bind(this);

//        Speeltijd berekenen, wanneer spel start wordt de start tijd bijgehouden
//        Eens alle zones gevonden zijn wordt de gespeelde tijd berekend
        currentTime = Calendar.getInstance().getTime();

    }

    private void setUpView() {
//        Uit de view halen
        canvasView = findViewById(R.id.canvas);
        circularProgressBar = findViewById(R.id.progress);
        speedMeter = findViewById(R.id.speedView);
        progressText = findViewById(R.id.progressText);
        content = findViewById(R.id.content);

//        progressStart
        progressText.setTextColor(Color.DKGRAY);
        progressText.setText("0 %");

//        Speedmeter
        speedMeter.speedTo(20, 1000);
        speedMeter.setLowSpeedColor(Color.parseColor("#BECC02"));
        speedMeter.setHighSpeedColor(Color.parseColor("#F90629"));
        speedMeter.setMediumSpeedColor(Color.parseColor("#FCE303"));
        speedMeter.setTextColor(Color.TRANSPARENT);

//        Wanneer je veel beweegt krijg je die melding en zal ze van de nog niet gevonden
//        zones percent afgaan (je hebt die dus nog minder ontdekt)
        final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
            public void onShake() {
                vibe.vibrate(100);
//                new AlertDialog.Builder(MainActivity.this)
//                        .setPositiveButton(android.R.string.ok, null)
//                        .setMessage("Je beweegt veel, op deze manier kan je de zones niet goed ontdekken! Je verliest enkele delen van je zones!")
//                        .show();
                speedMeter.speedTo(90, 2000);

                for (int i = 1; i < 7; i++) {
                    if (!founded.containsKey(Integer.toString(i))) {
                        Integer temp = zoneDiscovered.get(Integer.toString(i));
                        if (temp >= 20) {
                            temp -= 20;
                        } else
                            temp = 0;
                        zoneDiscovered.put(Integer.toString(i), temp);
                    }
                }
            }
        });

//        Animatie bij te snel bewegen ophalen uit xml
        animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

//        Wanneer pijl in het rood komt, tekst weergeven en shaken
        speedMeter.setOnSectionChangeListener(new OnSectionChangeListener() {
            @Override
            public void onSectionChangeListener(byte oldSection, byte newSection) {
                if (newSection == Speedometer.HIGH_SECTION) {
//                    speedMeter.startAnimation(animShake);
                    content.startAnimation(animShake);

                    // Wanneer de indicator in het rode gedeelte komt, wordt een tekstballon weergegeven
                    TextNote note = new TextNote(getApplicationContext(), "Niet zo snel!")
                            .setPosition(Note.Position.CenterSpeedometer)
                            .setAlign(Note.Align.Bottom)
                            .setTextTypeFace(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC))
                            .setBackgroundColor(Color.parseColor("#ffffff"))
                            .setTextColor(Color.GRAY)
                            .setCornersRound(20f)
                            .setTextSize(speedMeter.dpTOpx(20f));
                    speedMeter.addNote(note);

                    // dan wordt er 2 seconden gewacht en gaat de indicator terug naar 30
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            speedMeter.speedTo(30, 4000);
                        }
                    }, 2000);
                }
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth(); // ((display.getWidth()*20)/100)
        int height = display.getHeight();// ((display.getHeight()*30)/100)

//        Layout voor de Speedmeter
        ViewGroup.MarginLayoutParams paramss = (ViewGroup.MarginLayoutParams) speedMeter.getLayoutParams();
        paramss.width = width / 5;
        paramss.rightMargin = 75;
        paramss.topMargin = 200;
        speedMeter.setLayoutParams(paramss);

//        Layout voor de circleprogressbar
        frame = findViewById(R.id.frame);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) frame.getLayoutParams();
        params.width = width / 4;
        params.rightMargin = 500;
        params.bottomMargin = 200;
        frame.setLayoutParams(params);
    }

    private void setUpShowCase() {
        TextPaint title = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf"));
        title.setTextSize(150);
        title.setColor(Color.parseColor("#ED755C"));

        TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        text.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf"));
        text.setTextSize(80);
        text.setColor(Color.parseColor("#5D4D53"));

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(speedMeter))
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("Snelheid")
                .setContentText("Deze meter geeft jouw snelheid aan. In de bib mogen we niet lopen. Let op dat het pijltje niet in het rood komt,want dan gaat het percent van de zone naar beneden.")
                .setOnClickListener(this)
                .setContentTitlePaint(title)
                .setContentTextPaint(text)
                .build();
        showcaseView.setButtonText(getString(R.string.volgende));
    }

    @Override
    public void onClick(View v) {
        switch (counter) {
            case 0:
                showcaseView.setShowcase(new ViewTarget(findViewById(R.id.plattegrond)), true);
                showcaseView.setContentTitle("Plattegrond");
                showcaseView.setContentText("Dit is een plattegrond van de bib. Deze plekken moet je gaan ontdekken.");
                break;
            case 1:
                showcaseView.setShowcase(new ViewTarget(frame), true);
                showcaseView.setContentTitle("Vooruitgang");
                showcaseView.setContentText("Dit wiel geeft aan hoeveel je al van een zone ontdekt hebt. \nKan jij alle zones ontdekken?");
                showcaseView.setButtonText(getString(R.string.close));
                break;
            case 3:
                start = true;
                showcaseView.hide();
                break;
        }
        counter++;
    }

    @Override
    public void onResume() {
        mShaker.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mShaker.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    if (start)
                        if (founded.size() < 6) {
                            List<Beacon> lijstBeacons = new ArrayList<>(beacons);
                            Collections.sort(lijstBeacons, comparator);
                            if (beacons.size() > 3) {
                                String b1 = lijstBeacons.get(0).getId1().toString().toLowerCase();
                                String b2 = lijstBeacons.get(1).getId1().toString().toLowerCase();
                                String b3 = lijstBeacons.get(2).getId1().toString().toLowerCase();
                                String b4 = lijstBeacons.get(3).getId1().toString().toLowerCase();

                                if (!b1.isEmpty() && !b2.isEmpty() && !b3.isEmpty() && !b4.isEmpty()) {
                                    String tempZone = checkZone(b1, b2, b3, b4);
                                    if (!tempZone.equals(zone) && !tempZone.equals("0")) {
                                        zone = tempZone;
                                    }
                                    if (!founded.containsKey(tempZone) && !zone.equals("0")) {
                                        drawZoneContour(zone);
                                        checkTime(zone);
                                    }
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Geen zones gevonden", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        } else
                            printDifference();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(MainActivity.this, "Geen beacons gevonden", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private void drawZoneContour(final String tempZone) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canvasView.drawZoneContour(tempZone);
            }
        });
    }

    private void drawZoneProgress(final String tempZone) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circularProgressBar.setColor(Color.parseColor(zoneColors.get(Integer.valueOf(tempZone))));
                int animationDuration = 2500;
                circularProgressBar.setProgressWithAnimation(zoneDiscovered.get(tempZone), animationDuration);
                progressText.setTextColor(Color.parseColor(zoneColors.get(Integer.valueOf(tempZone))));
                progressText.setText(String.format("%s %%", zoneDiscovered.get(tempZone).toString()));
            }
        });

    }

    private String checkZone(String b1, String b2, String b3, String b4) {
//        Checken in welke zone je staat aan de hand van de beacons die gevonden worden
        for (Map.Entry<String, String[][]> entry : zonesGrid.entrySet()) {
            String key = entry.getKey();
            String[][] zoneGrid = entry.getValue();
            for (String[] t : zoneGrid) {
                if ((b1.equals(t[0]) && b2.equals(t[1]) && b3.equals(t[2])) || (b1.equals(t[0]) && b2.equals(t[1]) && b4.equals(t[2]))) {
                    return key;
                }
            }
        }
        return "0";
    }

    private void checkTime(final String tempZone) {
//        de opacity wordt aangepast telkens je in deze zone bent met 2, zo wordt hij langzamer zichtbaar
//        tot wanneer de zone volledig ontdekt is en dus opacity op max staat
        Integer temp = zoneDiscovered.get(tempZone);
        if (temp <= 99) {
            temp += 1;
            zoneDiscovered.put(tempZone, temp);
        } else {
            founded.put(tempZone, centerCheck.get(tempZone));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    canvasView.setFounded(founded);
//                    Toast.makeText(MainActivity.this, "Zone " + tempZone + " volledig ontdekt!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        drawZoneProgress(tempZone);
    }

    public void printDifference() {
//        Tijd berekenen van totale speeltijd nadat alle zones ontdekt werden
//        Een een dialoog tonen omdat je alles ontdekte
        Date endDate = Calendar.getInstance().getTime();

        long different = endDate.getTime() - currentTime.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        final long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        final long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        final long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        final long elapsedSeconds = different / secondsInMilli;

        beaconManager.unbind(this);

        long[] time = {elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds};
        final Intent intent = new Intent(MainActivity.this, EndActivity.class);
        intent.putExtra("time", time);
        startActivity(intent);

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                new AlertDialog.Builder(MainActivity.this)
//                        .setPositiveButton(android.R.string.ok, null)
//                        .setTitle(R.string.titel)
//                        .setMessage(String.format("%s%n%s", "Je hebt alle zones ontdekt, goed zo!",
//                                "Je deed dit in een tijd van " + elapsedHours + " uren " +
//                                        elapsedMinutes + " minuten en " + elapsedSeconds + " seconden."))
//                        .show();
//            }
//        });
    }

    public void getInfoFromJson() {
        Random r = new Random();
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("beacons.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconZones");
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject jsonObject = beaconsArray.getJSONObject(i);
                String zone = jsonObject.getString("zone");
                JSONArray zones = jsonObject.getJSONArray("zones");
                for (int j = 0; j < zones.length(); j++) {
                    JSONArray ob = zones.getJSONObject(j).getJSONArray("beacons");
                    String[][] temp = zonesGrid.get(zone);
                    temp[j][0] = ob.getString(0).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(0).toLowerCase();
                    temp[j][1] = ob.getString(1).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(1).toLowerCase();
                    temp[j][2] = ob.getString(2).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(2).toLowerCase();

                    zonesGrid.put(zone, temp);
                }

                JSONArray check = jsonObject.getJSONArray("check");
                List<Double> tempCheck = new ArrayList<>();
                for (int k = 0; k < check.length(); k++) {
                    tempCheck.add(check.getDouble(k));
                }
                centerCheck.put(zone, tempCheck);

                JSONArray drawZoneX = jsonObject.getJSONArray("drawZoneX");
                List<Double> tempDrawX = new ArrayList<>();
                for (int k = 0; k < drawZoneX.length(); k++) {
                    tempDrawX.add(drawZoneX.getDouble(k));
                }
                drawZoneGridX.put(zone, tempDrawX);

                JSONArray drawZoneY = jsonObject.getJSONArray("drawZoneY");
                List<Double> tempDrawY = new ArrayList<>();
                for (int k = 0; k < drawZoneY.length(); k++) {
                    tempDrawY.add(drawZoneY.getDouble(k));
                }
                drawZoneGridY.put(zone, tempDrawY);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public String loadJSONFromAsset(String filename) {
        String json;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean checkAndRequestPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                    }
                    builder.show();
                }
                break;
        }
    }

    public void checkBluetooth() {
        //check if bluetooth is on
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            snackbar = Snackbar.make(findViewById(android.R.id.content), "Apparaat ondersteunt geen bluetooth", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Bluetooth staat uit", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Zet aan", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setBluetooth();
                            }
                        });
                snackbar.show();
            }
        }
    }

    public void setBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            bluetoothAdapter.enable();
        }
    }
}

