package com.ReG.PvOptimalSpot;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anychart.chart.common.dataentry.DataEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.ReG.PvOptimalSpot.myfragments.solar_viewModel;

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
    private AtomicBoolean thread_started = new AtomicBoolean();
    private solar_viewModel model;


    public PVGsAPI(AtomicBoolean thread_started,String urlToread, TextView lat, TextView lng, boolean pv, ProgressBar progressBar, LineChart anyChartView, solar_viewModel model) {
        this.thread_started=thread_started;
        this.urlToread = urlToread;
        this.Lat=lat;
        this.Lng=lng;
        this.pv=pv;
        this.progressBar=progressBar;
        this.anyChartView=anyChartView;
        this.model=model;
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
            double MediaTot = 0.0f;
            double max = 0.0f;
            for (int i = 0; i < listaIrradianza.size() && numero_days <= 365; i++) {
                if ((i + 1) % 24 == 0) {
                    media = media / 24;
                    MediaTot+=media;
                    if(media>max)
                        max=media;
                    dati.add(media);
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
            MediaTot=MediaTot/365;
            if(pv)
            {
                model.setPeak(max,MediaTot);
            }
            List<Entry> data = new ArrayList<Entry>();
            for (int i = 0; i < dati.size(); i++) {
                double d = dati.get(i);
                data.add(new Entry((i + 1), (float)(d)));
            }
            LineDataSet dataSet = new LineDataSet(data,(!pv)?"Daily mean irradiance on optimal spot each year":"Daliy mean power produced by one PV panel each year");
            dataSet.setColor(R.color.gradient_end_color);
            LineData lineData = new LineData(dataSet);

            Lat.post(new Runnable() {
                @Override
                public void run() {
                    Lat.setText(value[0] + "°");
                    Lng.setText(value[1] + "° S");
                    Lat.setAnimation(TabbedActivity.btnAnim);
                    Lng.setAnimation(TabbedActivity.btnAnim);
                    progressBar.setVisibility(View.GONE);
                    anyChartView.setVisibility(View.VISIBLE);
                    //OpenGLRenderer.Instance.SetOptimalValues(new Float(value[0]),new Float(value[1]));
                }
            });
            anyChartView.setData(lineData);
            anyChartView.getDescription().setEnabled(false);
            MyXAxisFormatter formatterX = new MyXAxisFormatter();
            MyYAxisFormatter formatterY = new MyYAxisFormatter();
            anyChartView.getAxisLeft().setValueFormatter((!pv)?formatterY:new MyY2AxisFormatter());
            anyChartView.getXAxis().setValueFormatter(formatterX);
            anyChartView.invalidate();
            thread_started.set(false);
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

    class MyYAxisFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return value+"W/m2";
        }
    }
    class MyY2AxisFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return value+"W";
        }
    }
    class MyXAxisFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return ((int)value)+" d";
        }
    }

}
