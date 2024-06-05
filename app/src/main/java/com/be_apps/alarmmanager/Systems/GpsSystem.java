package com.be_apps.alarmmanager.Systems;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.be_apps.alarmmanager.Broadcasts.GpsBroadcast;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class GpsSystem {
    private final Context context;
    private final Repository repository;
    private PendingIntent pendingIntent;
    private final FusedLocationProviderClient client;

    public GpsSystem(Context context) {
        this.context = context;
        repository = new Repository((Application) context.getApplicationContext());
        client = LocationServices.getFusedLocationProviderClient(context);
    }
    private PendingIntent GetGpsPendingIntent() {
        if (pendingIntent == null) {
            Intent intent = new Intent(context, GpsBroadcast.class);
            int GPS_REQUEST_CODE = 2500;
            pendingIntent = PendingIntent.getBroadcast(context, GPS_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }
    private boolean IsShouldStartGpsPolling() {
        List<GeofencesEntity> list = repository.GetGeofences();
        if (list != null && list.size() != 0) {
            for (GeofencesEntity entity : list) {
                if (!entity.IsFinished()) // if there is at least one geo not finished so we start gps polling
                    return true;
            }
            return false;
        }
        return false;
    }
    private void StartGpsPolling() {
        Log.e("ab_do" , "StartGpsPolling") ;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(180000); // 3 min
        locationRequest.setFastestInterval(10000); //10 sec
        locationRequest.setMaxWaitTime(600000); //600000 // 10 min
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return;
            }
        }
        client.requestLocationUpdates(locationRequest, GetGpsPendingIntent());
    }
    private void StopGpsPolling() {
        Log.e("ab_do" , "StopGpsPolling") ;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        client.removeLocationUpdates(GetGpsPendingIntent()) ;
    }
    public void SetGpsProcess() {
        if (IsShouldStartGpsPolling())
            StartGpsPolling();
        else
            StopGpsPolling() ;
    }
}
