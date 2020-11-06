package com.rizzo.sensortest;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.anychart.AnyChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

public class PVGsAPI implements Runnable {

    @Override
    public void run() {
        try {
            getHTML(urlToread);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private String urlToread;
    private  TextView Lat,Lng;
    private  ProgressBar progressBar;
    private  LineChart anyChartView;
    //private  Handler handler;
    private  boolean pv;
    private MutableLiveData<List<DataEntry>> mutable;

    public PVGsAPI(String urlToread, TextView lat, TextView lng, boolean pv, ProgressBar progressBar, LineChart anyChartView) {
        this.urlToread = urlToread;
        this.Lat=lat;
        this.Lng=lng;
        this.pv=pv;
        this.progressBar=progressBar;
        this.anyChartView=anyChartView;
    }

    public void getHTML(String urlToRead) throws Exception {
        URL url = new URL(urlToRead);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String json = rd.readLine();
            ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            final ObjectNode node = mapper.readValue(json, ObjectNode.class);
            int[] value = new int[2];
            value[0] = node.get("inputs").get("mounting_system").get("fixed").get("slope").get("value").asInt();
            value[1] = node.get("inputs").get("mounting_system").get("fixed").get("azimuth").get("value").asInt();
            ArrayNode listaIrradianza = (ArrayNode) node.get("outputs").get("hourly");

            ArrayList<Double> dati = new ArrayList<Double>();
            int numero_days = 1;
            double media = 0.0f;
            for (int i = 0; i < listaIrradianza.size() && numero_days <= 365; i++) {
                if ((i + 1) % 24 == 0) {
                    media = media / 24;
                    dati.add(media);
                    //data.add(new ValueDataEntry((numero_days),media));
                    numero_days++;
                    media = 0;
                } else {
                    ObjectNode n = (ObjectNode) listaIrradianza.get(i);
                    double irradianza = n.get((!pv) ? "G(i)" : "P").asDouble();
                    if (irradianza > 0) {
                        media = (float) (media + (double) irradianza);
                    }
                }
            }
            List<Entry> data = new ArrayList<Entry>();
            for (int i = 0; i < dati.size(); i++) {
                double d = dati.get(i);
                data.add(new Entry((i + 1), (float)(d)));
            }
            LineDataSet dataSet = new LineDataSet(data,(!pv)?"Irradianza":"Potenza");
            dataSet.setColor(R.color.gradient_end_color);
            LineData lineData = new LineData(dataSet);

            //int altezza = node.get("inputs").get("location").get("elevation").asInt();
            Lat.post(new Runnable() {
                @Override
                public void run() {
                    Lat.setText(value[0] + "°");
                    Lng.setText(value[1] + "° S");
                    Lat.setAnimation(MainActivity.btnAnim);
                    Lng.setAnimation(MainActivity.btnAnim);
                    progressBar.setVisibility(View.GONE);
                    //OpenGLRenderer.Instance.SetOptimalValues(new Float(value[0]),new Float(value[1]));
                }
            });
            anyChartView.setData(lineData);
            Description d = new Description();
            d.setText((!pv)?"Irradianza per giorno":"Potenza per giorno");
            anyChartView.setDescription(new Description());
            anyChartView.animate();
            anyChartView.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }
    }
    public void set(MutableLiveData<List<DataEntry>> data)
    {
        this.mutable=data;
    }

}
