package com.be_apps.alarmmanager.Broadcasts;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.be_apps.alarmmanager.Systems.GpsSystem;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.Utilites.GooglePlayServicesUtils;

public class GooglePlayServicesDataClearedReceiver extends BroadcastReceiver {
    private Repository repository ;

    @Override
    public void onReceive(Context context, Intent intent) {
        // will be triggered when the google play service data Cleared
        // we want to Re register All Geofences
        String action = intent.getAction();
        if (TextUtils.equals(Intent.ACTION_PACKAGE_DATA_CLEARED, action)) {
            Uri uri = intent.getData();
            if (uri==null) return;
            if (uri.toString().equals("package:com.google.android.gms")) {
                // Code here to handle Google Play services data cleared
                repository = new Repository((Application) context.getApplicationContext()) ;
                GeofencingHelper geofencingHelper = new GeofencingHelper(context);
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
                geofencingHelper.ReRegisterAllGeofences();
                if (GooglePlayServicesUtils.IfGooglePlayServicesAvailable(context)) {
                    GpsSystem gpsSystem = new GpsSystem(context);
                    gpsSystem.SetGpsProcess();
                }
            }
        }
    }
}
