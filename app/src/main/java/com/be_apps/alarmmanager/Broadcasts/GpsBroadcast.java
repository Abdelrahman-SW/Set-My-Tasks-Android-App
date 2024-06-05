package com.be_apps.alarmmanager.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class GpsBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationResult locationResult = LocationResult.extractResult(intent) ;
        if (locationResult!=null && locationResult.getLastLocation()!=null) {
            Log.e("ab_do" , "Location Gps " + locationResult.getLastLocation()) ;
        }
    }
}
