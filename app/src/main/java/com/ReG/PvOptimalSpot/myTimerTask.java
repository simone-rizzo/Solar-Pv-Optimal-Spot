package com.ReG.PvOptimalSpot;

import com.google.android.material.snackbar.Snackbar;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class myTimerTask extends TimerTask {

    private AtomicBoolean gps_position;
    private Snackbar snackbar;

    public myTimerTask(AtomicBoolean gps_position, Snackbar snack) {
        this.gps_position = gps_position;
        snackbar=snack;
    }

    @Override
    public void run() {
        if(!gps_position.get())
        {
            snackbar.show();
        }
    }
}
