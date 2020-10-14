package com.rizzo.sensortest;

public class Coordinate {
    public double lat, lng;
    public double taub, taud;
    public Double[] all_tauB,all_tauD;

    public String city;

    public Coordinate(String nome, double _lat, double _lng, double _taub, double _taud, Double[] all1,Double[] all2) {
        city=nome;
        lat = _lat;
        lng = _lng;
        taub = _taub;
        taud = _taud;
        all_tauB=all1;
        all_tauD=all2;
    }

    public double distanza_euclidea(double latitude, double longitude) {
        return Math.sqrt(Math.pow(latitude - lat, 2) + Math.pow(longitude - lng, 2));
    }

    public double getTaud() {
        return taud;
    }

    public double getTaub() {
        return taub;
    }
    public Tau getTau()
    {
        return new Tau(taub,taud);
    }

}
