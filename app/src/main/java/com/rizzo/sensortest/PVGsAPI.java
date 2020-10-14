package com.rizzo.sensortest;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rizzo.sensortest.opengl.OpenGLRenderer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


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
    private static TextView Lat,Lng, Altezza;
    private static ProgressBar progressBar;

    public PVGsAPI(String urlToread, TextView lat, TextView lng, TextView altezza, ProgressBar progressBar) {
        this.urlToread = urlToread;
        this.Lat=lat;
        this.Lng=lng;
        this.Altezza=altezza;
        this.progressBar=progressBar;
    }

    public static void getHTML(String urlToRead) throws Exception {
        URL url = new URL(urlToRead);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String json = rd.readLine();
            ObjectMapper mapper =new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            final ObjectNode node = mapper.readValue(json, ObjectNode.class);
            int[] value = new int[2];
            value[0]=node.get("inputs").get("mounting_system").get("fixed").get("slope").get("value").asInt();
            value[1]=node.get("inputs").get("mounting_system").get("fixed").get("azimuth").get("value").asInt();
            int altezza = node.get("inputs").get("location").get("elevation").asInt();
            Lat.post(new Runnable() {
                @Override
                public void run() {
                    Lat.setText(value[0]+"°");
                    Lng.setText(value[1]+"° S");
                    //Altezza.setText("Elevation: "+altezza);
                    Lat.setAnimation(MainActivity.btnAnim);
                    Lng.setAnimation(MainActivity.btnAnim);
                    //Altezza.setAnimation(MainActivity.btnAnim);
                    progressBar.setVisibility(View.GONE);
                    OpenGLRenderer.Instance.SetOptimalValues(new Float(value[0]),new Float(value[1]));
                }
            });

        } finally {
            urlConnection.disconnect();
        }
    }
}
