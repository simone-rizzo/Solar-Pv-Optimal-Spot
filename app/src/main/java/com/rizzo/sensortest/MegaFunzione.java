package com.rizzo.sensortest;

import android.os.Debug;
import android.view.View;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import flanagan.math.Maximisation;

/**
 * Classe che effettua il calcolo nel dato punto con i valori dei sensori.
 * https://en.wikipedia.org/wiki/Nelder%E2%80%93Mead_method
 */
public class MegaFunzione extends Thread {

    private TextView resultText1,resultText2, resultIrradianza;
    private double lat,log;
    private Double[] All_tau_b,All_tau_d;
    private AnyChartView anyChartView;

    public MegaFunzione(double _lat, double _log, TextView res,TextView res2,TextView irr,AnyChartView anychart, Double[] ls1, Double[] lst2)
    {
        lat=_lat;
        log=_log;
        resultText1=res;
        resultText2=res2;
        resultIrradianza=irr;
        All_tau_b=ls1;
        All_tau_d=lst2;
        anyChartView=anychart;
    }
    //leggere i parametri dinamici

    @Override
    public void run() {
        //find_max();
        MaximFunct funct = new MaximFunct(lat,log);
        funct.setTauBD(All_tau_b[5], All_tau_d[5]);
        funct.declinazione_solare();
        double function = funct.function(new double[]{Double.valueOf(34), Double.valueOf(0)});
        DecimalFormat df = new DecimalFormat("#.00");
        resultIrradianza.setText("Irradianza media: " + df.format(function));

    }

    public void find_max()
    {
        //Create instance of Maximisation
        Maximisation max = new Maximisation();

        //come aggiungere un vincolo i=index
        //i1=direzione 1:limitato superiormente 2:limitato inferiormente
        //v=valore
        max.addConstraint(0,1,90D);
        max.addConstraint(0,-1,0D);
        /*max.addConstraint(1,-1,-90D);
        max.addConstraint(1,1,90D);*/

        // Create instace of class holding function to be maximised
        MaximFunct funct = new MaximFunct(lat,log);
        int n_days=21;
        double inclinazione_trovata=0;
        double orientazione_trovata=0;
        double irradianza=0;
        double[] inclinazioni = new double[12];
        for(int i=0;i<12;i++) {

            funct.setTauBD(All_tau_b[i], All_tau_d[i]);
            funct.setNUMERO_DI_GIORNI(n_days);
            n_days+=21;
            // Set values of internal function
            funct.declinazione_solare();

            // initial estimates
            double[] start = {15.0D};

            // initial step sizes
            double[] step = {10D};

            // convergence tolerance
            double ftol = 1e-15;

            // Nelder and Mead maximisation procedure
            max.nelderMead(funct, start, step, ftol);

            // get the maximum value
            double maximum = max.getMaximum();

            // get values of y and z at maximum
            double[] param = max.getParamValues();
            //orientazione_trovata+=param[1];
            inclinazione_trovata+=param[0];
            inclinazioni[i]=param[0];
            irradianza+=maximum;
        }
        //final double orientazione = orientazione_trovata/12;
        final double inclinazione = inclinazione_trovata/12;
        final double irradianzafinale = irradianza/12;

        Cartesian pie = AnyChart.line();
        pie.animation(true);
        pie.title("Inclinazione per mese");
        pie.yAxis(0).title("gradi");
        pie.xAxis(0).title("mesi");

        List<DataEntry> data = new ArrayList<>();
        for(int i=0;i<12;i++)
        {
            data.add(new ValueDataEntry((i+1),inclinazioni[i]));
        }

        pie.data(data);
        DecimalFormat df = new DecimalFormat("#.00");

        resultText1.post(new Runnable() {
            @Override
            public void run() {
                resultText1.setText(df.format(inclinazione) + "°");
                //resultText2.setText(df.format(orientazione)+"°S");
                resultIrradianza.setText("Irradianza media: " + df.format(irradianzafinale));
                anyChartView.setVisibility(View.VISIBLE);
                anyChartView.setChart(pie);
                resultText1.startAnimation(MainActivity.btnAnim);
                resultText2.startAnimation(MainActivity.btnAnim);
                resultIrradianza.startAnimation(MainActivity.btnAnim);
            }
        });
    }
}
