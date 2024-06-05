package com.be_apps.alarmmanager.Broadcasts;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.be_apps.alarmmanager.Systems.GpsSystem;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.Utilites.GooglePlayServicesUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


import java.util.List;

public class GeofencingBroadcast extends BroadcastReceiver {
    private Repository repository ;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ab_do" , "onReceiveGeofence") ;
        GeofencingHelper geofencingHelper = new GeofencingHelper(context);
        repository = new Repository((Application) context.getApplicationContext());
        geofencingHelper.SetonAddGeofenceListener(new GeofencingHelper.OnAddGeofenceListener() {
            @Override
            public void onSuccessAddGeofence() {
                Log.e("ab_do" , "onSuccessAddGeofence") ;
            }

            @Override
            public void onFailureAddGeofence(GeofencesEntity geofencesEntity , Exception e) {
                Log.e("ab_do" , "onFailureAddGeofence") ;
                repository.DeleteGeofence(geofencesEntity);
                Toast.makeText(context.getApplicationContext(), geofencingHelper.GetGeofenceErrorMsg(e), Toast.LENGTH_LONG).show();
            }
        });

        // Start Process :
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
           Log.e("ab_do" , "Error Geofence" + geofencingEvent.getErrorCode()) ;
           // try to register All Geofences :
           geofencingHelper.ReRegisterAllGeofences();
           return;
        }
        int type = geofencingEvent.getGeofenceTransition() ;
        List<Geofence> geofences = geofencingEvent.getTriggeringGeofences() ;
        Log.e("ab_do" , "Size " + geofences.size()) ;
        if (geofences.size() != 0) {
            for (Geofence geofence : geofences) {
                Log.e("ab_do" , "Type " + type) ;
                geofencingHelper.HandleGeofenceNotification(geofence , type);
            }
        }

        CheckGpsSystem(context);
    }

    private void CheckGpsSystem(Context context) {
        if (GooglePlayServicesUtils.IfGooglePlayServicesAvailable(context)) {
            GpsSystem gpsSystem = new GpsSystem(context);
            gpsSystem.SetGpsProcess();
        }
    }
}
