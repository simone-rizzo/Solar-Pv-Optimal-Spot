package com.rizzo.sensortest.myfragments;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.rizzo.sensortest.PVGsAPI;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class solar_viewModel extends ViewModel {

    MutableLiveData<Double> lat = new MutableLiveData<Double>();
    MutableLiveData<Double> lng = new MutableLiveData<Double>();
    MutableLiveData<String> testo = new MutableLiveData<String>();
    MutableLiveData<Double> peak_max = new MutableLiveData<Double>();
    MutableLiveData<Double> peak_mean = new MutableLiveData<Double>();

    public void setPeak(double max, double mean)
    {
        this.peak_max.postValue(max);
        this.peak_mean.postValue(mean);
    }

    public MutableLiveData<Double> getPeak_max() {
        return peak_max;
    }

    public MutableLiveData<Double> getPeak_mean() {
        return peak_mean;
    }

    public void setData(double lat, double lng)
    {
        this.lat.postValue(lat);
        this.lng.postValue(lng);
    }
    public void setDataString(String text)
    {
        this.testo.postValue(text);
    }
    public LiveData<String> getText()
    {
        return testo;
    }

    public LiveData<Double> getLat() {
        return lat;
    }
    public LiveData<Double> getLng() {
        return lng;
    }

}
