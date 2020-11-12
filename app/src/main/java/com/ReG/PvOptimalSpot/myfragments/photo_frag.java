package com.ReG.PvOptimalSpot.myfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.snackbar.Snackbar;
import com.ReG.PvOptimalSpot.PVGsAPI;
import com.ReG.PvOptimalSpot.R;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static android.content.Context.SENSOR_SERVICE;

public class photo_frag extends Fragment implements SensorEventListener, AdapterView.OnItemSelectedListener {

    ImageView compass_img;
    private TextView txt_compass;
    private TextView txt_inclinazione, inclinazioneOttima, orientamentoOttimo, peak, mean;
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

    private LineChart anyChartView;

    public static Animation btnAnim;

    private String pv_choise = "crystSi";
    private solar_viewModel model;
    public AtomicBoolean thread_started = new AtomicBoolean(false);
    private EditText editTextNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment2, container, false);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.pv_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        editTextNumber = (EditText) view.findViewById(R.id.editTextNumber);
        compass_img = (ImageView) view.findViewById(R.id.imageView);
        txt_compass = (TextView) view.findViewById(R.id.graditext);
        txt_inclinazione = (TextView) view.findViewById(R.id.ydegree);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        latlogText = (TextView) view.findViewById(R.id.latlog);
        inclinazioneOttima = (TextView) view.findViewById(R.id.yOptimaldegree);
        orientamentoOttimo = (TextView) view.findViewById(R.id.gradiOptimaltext);
        anyChartView = (LineChart) view.findViewById(R.id.plot);
        btnAnim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.button_animation);
        peak = (TextView) view.findViewById(R.id.peak);
        mean = (TextView) view.findViewById(R.id.mean);
        model =  new ViewModelProvider(requireActivity()).get(solar_viewModel.class);
        model.getLat().observe(getViewLifecycleOwner(), item -> {
            Lat=item;
        });
        model.getLng().observe(getViewLifecycleOwner(), item -> {
            Lng=item;
        });
        model.getText().observe(getViewLifecycleOwner(), item ->{
            latlogText.setText(item);
        });
        model.getPeak_max().observe(getViewLifecycleOwner(), item ->{
            String text = editTextNumber.getText().toString();
            if(!text.equals("")) {
                double m2 = Double.parseDouble(editTextNumber.getText().toString());
                peak.setText("Peak power: " + valoreMoltiplicato(m2, item)+"W");
            }
        });
        model.getPeak_mean().observe(getViewLifecycleOwner(), item ->{
            String text = editTextNumber.getText().toString();
            if(!text.equals("")) {
                double m2 = Double.parseDouble(editTextNumber.getText().toString());
                mean.setText("Mean power: " + valoreMoltiplicato(m2, item)+"W");
            }
        });
        Button button = (Button) view.findViewById(R.id.button);
        button.setAnimation(btnAnim);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Lat != null && Lng != null) {
                    if(!thread_started.get()) {
                        String id = "";
                        thread_started.set(true);
                        PVGsAPI task = new PVGsAPI(thread_started, "https://re.jrc.ec.europa.eu/api/seriescalc?lat=" + Lat.toString() + "&lon=" + Lng.toString() + "&optimalangles=1&outputformat=json&startyear=2013&endyear=2016&pvtechchoice=" + pv_choise + "&pvcalculation=1&peakpower=1&loss=1", inclinazioneOttima, orientamentoOttimo, true, progressBar, anyChartView, model);
                        Thread a = new Thread(task);
                        a.start();
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    turnGPSOn();
                }
            }
        });
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(accelelistner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        start();
        return view;
    }
    public static String formatToSignificant(double value,
                                             int significant)
    {
        MathContext mathContext = new MathContext(significant,
                RoundingMode.DOWN);
        BigDecimal bigDecimal = new BigDecimal(value,
                mathContext);
        return bigDecimal.toPlainString();
    }
    public String valoreMoltiplicato(double m2, double p)
    {
        double n_pannelli = (int)(m2/1.7);
        return formatToSignificant(n_pannelli*p,2);
    }

    private void turnGPSOn(){
        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), "Please enable GPS, and wait for the position",
                Snackbar.LENGTH_LONG)
                .show();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
        }
    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String s = (String) parent.getItemAtPosition(pos);
        pv_choise=s;
        Log.d("cazzo",s);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

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
            if(gradi>90)
            {
                gradi=180-gradi;
            }
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
        /*OpenGLRenderer.Instance.setZ(mAzimuth);
        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);*/

        String where = "NW";

        if(mAzimuth>180)
        {
            mAzimuth-=360;
        }
        mAzimuth=mAzimuth*-1;
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
}

