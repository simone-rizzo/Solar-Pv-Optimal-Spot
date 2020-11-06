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

    MutableLiveData<List<DataEntry>> data = new MutableLiveData<List<DataEntry>>();

    public void fetch_data(PVGsAPI p)
    {
        p.set(data);
        Thread a = new Thread(p);
        a.start();
    }
    public void setData()
    {
        List<DataEntry> pisda = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pisda.add(new ValueDataEntry((i + 1), i));
        }
        data.postValue(pisda);
    }

    public LiveData<List<DataEntry>> getData() {
        return data;
    }

}
