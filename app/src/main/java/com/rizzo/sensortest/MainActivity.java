package com.rizzo.sensortest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rizzo.sensortest.opengl.OpenGLRenderer;
import com.rizzo.sensortest.opengl.OpenGlView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
    private TextView txt_compass;
    private TextView txt_inclinazione, inclinazioneOttima, orientamentoOttimo, irradianza;
    int mAzimuth;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private int gradi;
    private ProgressBar progressBar;
    private TextView latlogText;
    private Double Lat, Lng;

    private Coordinate lista_taubd;

    private LineChart anyChartView;

    public static Animation btnAnim;
    private static DrawerLayout drawerLayout;
    private static OpenGlView openGlView;

    private Button worksButton,creditsButton,helpButton;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.works:
                        Intent intent = new Intent(getApplicationContext(), why_it_works.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;
                    case R.id.credits:
                        Intent intent2 = new Intent(getApplicationContext(), credits.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        break;
                    case R.id.help:
                        Intent intent3 = new Intent(getApplicationContext(), IntroActivity.class);
                        intent3.putExtra("main",true);
                        startActivity(intent3);
                        break;
                }
                return false;
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.imageView);
        txt_compass = (TextView) findViewById(R.id.graditext);
        txt_inclinazione = (TextView) findViewById(R.id.ydegree);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        latlogText = (TextView) findViewById(R.id.latlog);
        inclinazioneOttima = (TextView) findViewById(R.id.yOptimaldegree);
        orientamentoOttimo = (TextView) findViewById(R.id.gradiOptimaltext);
        //irradianza = (TextView) findViewById(R.id.irradianza);
        anyChartView = (LineChart) findViewById(R.id.any_chart_view);
        //anyChartView.setProgressBar(progressBar);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);


        Button button = (Button) findViewById(R.id.button);
        button.setAnimation(btnAnim);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Lat != null && Lng != null) {
                    /*MegaFunzione a = new MegaFunzione(Lat, Lng, inclinazioneOttima, orientamentoOttimo,irradianza,anyChartView, lista_taubd.all_tauB,lista_taubd.all_tauD);
                    a.start();*/
                    String id = "";
                    PVGsAPI task = new PVGsAPI("https://re.jrc.ec.europa.eu/api/seriescalc?lat=" + Lat.toString() + "&lon=" + Lng.toString() + "&optimalangles=1&outputformat=json&startyear=2013&endyear=2016&pvtechchoice=CIS&pvcalculation=1&peakpower=1&loss=1", inclinazioneOttima, orientamentoOttimo, false, progressBar, anyChartView);
                    Thread a = new Thread(task);
                    //a.start();

                    progressBar.setVisibility(View.VISIBLE);
                        /*inclinazioneOttima.setText(valori[0]);
                        orientamentoOttimo.setText(valori[1]);*/

                } else {
                    Toast.makeText(getApplicationContext(), "Latitudine e Longitudine non ancora pronti",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //registro l'accellerometro per avere l'inclinazione
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(accelelistner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        requestPermission();
        start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_actions, menu);
        return true;
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            /*if(msg.what==SendReceive.DISCONNECT)
            {
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }*/
        }
    } ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_button:
                if(!drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.openDrawer(Gravity.RIGHT);
                else
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                break;
        }
        return true;
    }

    //event listener per ottenere i dati sull'angolo di inclinazione
    private SensorEventListener accelelistner = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //angolo di inclinazione
            float[] g2 = new float[3];
            g2 = event.values.clone();
            double norm_Of_g = Math.sqrt(g2[0] * g2[0] + g2[1] * g2[1] + g2[2] * g2[2]);
            // Normalize the accelerometer vector
            g2[0] = g2[0] / (float) norm_Of_g;
            g2[1] = g2[1] / (float) norm_Of_g;
            g2[2] = g2[2] / (float) norm_Of_g;
            int inclination_raw = (int) Math.toDegrees(Math.acos(g2[2]));
            //OpenGLRenderer.Instance.setX(inclination_raw);
            int inclination = Math.round(inclination_raw);
            gradi = inclination;
            txt_inclinazione.setText(gradi + "°");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //listener per orientamento bussola
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }
        /*OpenGLRenderer.Instance.setZ(mAzimuth);
        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);*/

        String where = "NW";

        if(mAzimuth>180)
        {
            mAzimuth-=360;
        }

        /*if (mAzimuth >= 350 || mAzimuth <= 10)
            where = "S";
        if (mAzimuth < 350 && mAzimuth > 280)
            where = "SE";
        if (mAzimuth <= 280 && mAzimuth > 260)
            where = "E";
        if (mAzimuth <= 260 && mAzimuth > 190)
            where = "NE";
        if (mAzimuth <= 190 && mAzimuth > 170)
            where = "N";
        if (mAzimuth <= 170 && mAzimuth > 100)
            where = "NW";
        if (mAzimuth <= 100 && mAzimuth > 80)
            where = "W";
        if (mAzimuth <= 80 && mAzimuth > 10)
            where = "SW";
         */
        txt_compass.setText(mAzimuth + "° S");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //inizializzazione sensori
    public void start() {

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            } else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    //deregistro i listener
    public void stop() {
        if (haveSensor) {
            mSensorManager.unregisterListener(this, mRotationV);
            mSensorManager.unregisterListener(accelelistner, mAccelerometer);
        } else {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(accelelistner, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(accelelistner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        start();
    }

    public void requestPermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                getcurrentLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
    }

    //Ottiene la posizione corrente tramite la libreria di google
    public void getcurrentLocation() {
        progressBar.setVisibility(View.VISIBLE);
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(300);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int lastestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(lastestLocationIndex).getLatitude();
                    double longitudine = locationResult.getLocations().get(lastestLocationIndex).getLongitude();
                    latlogText.setText(String.format("Lat %s Lng %s", latitude, longitudine));
                    Lat = latitude;
                    Lng = longitudine;
                    lista_taubd = ottieniTau();
                }
                progressBar.setVisibility(View.GONE);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());

    }

    public Coordinate ottieniTau()
    {
        ArrayList<String> righe_raw = new ArrayList<>();
        ArrayList<Coordinate> location = new ArrayList<>();
        InputStream file = this.getResources().openRawResource(R.raw.taub_taud_latlog);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        try {
            String line = reader.readLine();
            while (line != null) {
                righe_raw.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i=0;
        for (String ele:righe_raw)
        {
            //Log.v("test",""+ele.split("\\t").length);
            String[] elems= ele.split("\\t");
            int len = elems.length;
            if(len==17 && i>4)
            {
                String nome =elems[0];
                String lat_raw = elems[len-2];
                String lng_raw = elems[len-1];
                String taub_raw = elems[8];
                String[] elems_taud = righe_raw.get(i-1).split("\\t");
                String taud_raw = elems_taud[8];
                Double[] tutti_tau_B = new Double[12];
                Double[] tutti_tau_D = new Double[12];
                int k=0;
                for(int j=3;j<15;j++)
                {
                    tutti_tau_B[k]=Double.parseDouble(elems_taud[j]);
                    tutti_tau_D[k]=Double.parseDouble(elems[j]);
                    k++;
                }
                Double taud,taub;
                taub=Double.parseDouble(taub_raw);
                taud=Double.parseDouble(taud_raw);
                lat_raw = lat_raw.replaceFirst(",",".");
                lng_raw= lng_raw.replaceFirst(",",".");
                Double lan = Double.parseDouble(lat_raw);
                Double lng = Double.parseDouble(lng_raw);
                Coordinate coord = new Coordinate(nome,lan,lng,taud,taub,tutti_tau_B,tutti_tau_D);
                location.add(coord);
            }
            i++;
        }
        //Lambda function
        Coordinate res = Collections.min(location, Comparator.comparing(x->x.distanza_euclidea(Lat,Lng)));
        return res;
    }

}

