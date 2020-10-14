package com.rizzo.sensortest;

import flanagan.math.MaximisationFunction;
/**
 * Classe che estende Maximisation che server per scrivere la propria funzione da massimizzare.
 */
public class MaximFunct implements MaximisationFunction {

    private double a = 0.0D;
    //variabili statiche
    //leggere da file csv tabulati di taub taud mensili
    public float COSTANTE_SOLARE_I0 = (float) 1366.1;
    public float COEFFICENTE_DI_RIFLESSIONE = (float) 0.2;
    public float NUMERO_DI_GIORNI = (float) 173;
    double irradianza_normale=0.0D;
    double altezza_solare=0.0D;
    double irradianza_diffusa=0.0D;
    double azimut=0.0D;
    double longitudine_nostra = 0.0D; //nostra log
    double latitudine_nostra = 0.0D; //nostra lat
    double tau_b,tau_d;

    public MaximFunct(double lat, double log)
    {
        latitudine_nostra=lat;
        longitudine_nostra=log;
    }
    public void setTauBD(double b, double d)
    {
        tau_b=b;
        tau_d=d;
    }

    /**
     * Funzione grande da massimizzare in valore di x
     * @param x un vettore che indica le variabili incognite da trovare
     * @return
     */
    public double function(double[] x){
        double irradianza = (COEFFICENTE_DI_RIFLESSIONE*(irradianza_normale*Math.sin(altezza_solare)+irradianza_diffusa))*
                Math.pow(Math.sin(toRadians(x[0]/2)), 2)+irradianza_diffusa*((1+Math.cos(toRadians(x[0])))/2)+irradianza_normale*Math.cos(Math.acos((Math.cos(altezza_solare)*Math.cos(toRadians(azimut-x[1]))*Math.sin(toRadians(x[0])))+(Math.sin(altezza_solare)*Math.cos(toRadians(x[0])))));
        return irradianza;
    }

    public void declinazione_solare()
    {
        //longitudine_nostra = -1*longitudine_nostra;
        double longitudine_di_riferimento = longitudine_di_riferimento(longitudine_nostra); //modulo 15 più vicina a noi
        double decl_1 = 360*(284+NUMERO_DI_GIORNI)/365;
        double declinazione_solare= (23.45D*Math.sin(toRadians(decl_1)));
        double coefficente_B = 360*(NUMERO_DI_GIORNI-81)/364;
        double equazione_tempo = 9.87D*Math.sin(toRadians(2*coefficente_B))-7.53*Math.cos(toRadians(coefficente_B))-1.5D*Math.sin(toRadians(coefficente_B));
        double equazione_tempo_corretto = equazione_tempo/60;
        double tempo_solare = 12-1+equazione_tempo_corretto+(4*(longitudine_di_riferimento-longitudine_nostra)/60);
        double angolo_solare_orario = toRadians(15*(tempo_solare-12));
        double arg_arcos = Math.sin(toRadians(latitudine_nostra))*Math.sin(toRadians(declinazione_solare))+(Math.cos(toRadians(latitudine_nostra))*Math.cos(toRadians(declinazione_solare))*Math.cos(angolo_solare_orario));
        altezza_solare = Math.asin(arg_arcos);

        double cosDeclinSolare = Math.cos(toRadians(declinazione_solare));
        double azimut2 = Math.asin((cosDeclinSolare*Math.sin(angolo_solare_orario))/Math.cos(altezza_solare));
        double scostamento_orario = toDegrees(Math.acos(cotg(toRadians(latitudine_nostra))*Math.tan(toRadians(declinazione_solare))));
        double angolo_solare_orario_degree = toDegrees(angolo_solare_orario);
        double ew = (scostamento_orario>angolo_solare_orario_degree)?1:-1;
        double ns = 1;
        double w = (angolo_solare_orario_degree>0)?1:-1;
        azimut = ew*ns*toDegrees(azimut2)+((1-ew*ns)/2)*w*180;
        double radiazione_extraterrestre = COSTANTE_SOLARE_I0*(1+0.034D*Math.cos(toRadians(360*NUMERO_DI_GIORNI)/365.35D));
        double coeff_aria = 1/(Math.sin(altezza_solare)+0.50572D*Math.pow((6.07995D+toDegrees(altezza_solare)),-1.634));
        double b = 1.219D-0.043D*tau_b-0.151D*tau_d-0.204D*tau_b*tau_d; //da prendere da excell
        double d = 0.202D+0.852*tau_b-0.007D*tau_d-0.357D*tau_b*tau_d;
        irradianza_normale = radiazione_extraterrestre*Math.pow(Math.E,-tau_b*Math.pow(coeff_aria, b));
        irradianza_diffusa = radiazione_extraterrestre*Math.pow(Math.E,-tau_d*Math.pow(coeff_aria,d));
        int a =0;
    }
    public void setNUMERO_DI_GIORNI(int n)
    {
        NUMERO_DI_GIORNI=n;
    }
    public double toRadians (double angle) {
        return angle * Math.PI / 180;
    }
    public double toDegrees (double angle) {
        return angle * (180 / Math.PI);
    }
    public Double cotg(double angle)
    {
        return Math.cos(angle)/Math.sin(angle);
    }
    public int longitudine_di_riferimento(double longitudine_nostra)
    {
        return (int) (Math.round(longitudine_nostra/15)*15);
    }
}
