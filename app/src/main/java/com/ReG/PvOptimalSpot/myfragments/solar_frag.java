package com.ReG.PvOptimalSpot.myfragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.ReG.PvOptimalSpot.Coordinate;
import com.ReG.PvOptimalSpot.PVGsAPI;
import com.ReG.PvOptimalSpot.R;
import com.ReG.PvOptimalSpot.myTimerTask;
import com.ReG.PvOptimalSpot.opengl.OpenGlView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static android.content.Context.SENSOR_SERVICE;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class solar_frag extends Fragment implements SensorEventListener {

    ImageView compass_img;
    private TextView txt_compass;
    private TextView txt_inclinazione, inclinazioneOttima, orientamentoOttimo;
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

    private LineChart plot;

    public static Animation btnAnim;

    private static OpenGlView openGlView;
    private solar_viewModel myModel;

    public AtomicBoolean gps_position=new AtomicBoolean(false);
    public AtomicBoolean thread_started = new AtomicBoolean(false);
    /*public solar_frag(solar_viewModel model)
    {
        this.myModel=model;
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1, container, false);
        myModel =  new ViewModelProvider(requireActivity()).get(solar_viewModel.class);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) view.findViewById(R.id.imageView);
        txt_compass = (TextView) view.findViewById(R.id.graditext);
        txt_inclinazione = (TextView) view.findViewById(R.id.ydegree);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        latlogText = (TextView) view.findViewById(R.id.latlog);
        inclinazioneOttima = (TextView) view.findViewById(R.id.yOptimaldegree);
        orientamentoOttimo = (TextView) view.findViewById(R.id.gradiOptimaltext);
        plot = (LineChart) view.findViewById(R.id.plot);
        btnAnim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_animation);
        Button button = (Button) view.findViewById(R.id.button);
        button.setAnimation(btnAnim);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Lat != null && Lng != null ) {
                    if(!thread_started.get()) {
                        String id = "";
                        thread_started.set(true);
                        PVGsAPI task = new PVGsAPI(thread_started, "https://re.jrc.ec.europa.eu/api/seriescalc?lat=" + Lat.toString() + "&lon=" + Lng.toString() + "&optimalangles=1&outputformat=json&startyear=2013&endyear=2016&pvtechchoice=CIS&pvcalculation=1&peakpower=1&loss=1", inclinazioneOttima, orientamentoOttimo, false, progressBar, plot, null);
                        Thread a = new Thread(task);
                        a.start();
                        plot.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    turnGPSOn().show();
                }
            }
        });
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(accelelistner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        requestPermission();
        start();
        return view;
    }
    public void setChart()
    {
        List<Entry> entries = new ArrayList<Entry>();
        for(int i=0;i<50;i++)
        {
            entries.add(new Entry(i,i*2));
        }
        LineDataSet dataSet = new LineDataSet(entries,"Prova");
        dataSet.setColor(R.color.gradient_end_color);
        LineData lineData = new LineData(dataSet);
        plot.setData(lineData);
        plot.invalidate();
    }
    Handler handler = new Handler(Looper.getMainLooper());

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
        String where = "NW";

        if(mAzimuth>180)
        {
            mAzimuth-=360;
        }
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity().getApplicationContext());
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
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
    public void onPause() {
        super.onPause();
        stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(accelelistner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        start();
    }

    public void requestPermission() {
        Dexter.withActivity(getActivity()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                getcurrentLocation();
                Timer t = new Timer();
                myTimerTask myTimerTask = new myTimerTask(gps_position,turnGPSOn());
                t.schedule(myTimerTask, 4000L);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
    }

    private Snackbar turnGPSOn() {
        return Snackbar.make(getActivity().findViewById(R.id.drawer_layout), "Please enable GPS, and wait for the position",
                Snackbar.LENGTH_LONG);
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
                LocationServices.getFusedLocationProviderClient(getActivity()).removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int lastestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(lastestLocationIndex).getLatitude();
                    double longitudine = locationResult.getLocations().get(lastestLocationIndex).getLongitude();
                    latlogText.setText(String.format("Lat %s Lng %s", latitude, longitudine));
                    Lat = latitude;
                    Lng = longitudine;
                    gps_position.set(true);
                    myModel.setData(Lat, Lng);
                    myModel.setDataString(String.format("Lat %s Lng %s", latitude, longitudine));
                    //lista_taubd = ottieniTau();
                }
                progressBar.setVisibility(View.GONE);
            }
        };
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());

    }

}

