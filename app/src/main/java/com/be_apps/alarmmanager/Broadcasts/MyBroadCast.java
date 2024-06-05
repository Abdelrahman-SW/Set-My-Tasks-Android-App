package com.be_apps.alarmmanager.Broadcasts;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.AlarmMSystem;
import com.be_apps.alarmmanager.Systems.GpsSystem;
import com.be_apps.alarmmanager.Systems.TTsServices;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.Utilites.GooglePlayServicesUtils;
import com.be_apps.alarmmanager.Utilites.NotificationHelper;
import com.be_apps.alarmmanager.WidgetSystem.TaskWidgetProvider;

import java.util.Calendar;

public class MyBroadCast extends BroadcastReceiver {
    private NotificationHelper notificationHelper ;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ab_do", "onReceive");
        if (intent.getAction()==null) return;
        notificationHelper = new NotificationHelper() ;
        if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            // before Oreo when the user Change The location settings this broadcast will be triggered :
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean IsGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ;
            boolean IsNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ;
            Log.e("ab_do" , "PROVIDERS_CHANGED_ACTION") ;
            if (IsGpsEnabled || IsNetworkProviderEnabled) {
                // Re register All Geofences :
                Repository repository = new Repository((Application) context.getApplicationContext());
                GeofencingHelper geofencingHelper = new GeofencingHelper(context) ;
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

                // Start Polling The Gps :
                if (GooglePlayServicesUtils.IfGooglePlayServicesAvailable(context)) {
                    GpsSystem gpsSystem = new GpsSystem(context);
                    gpsSystem.SetGpsProcess();
                }
            }
            return;
        }
        if (intent.getAction().equals(Constant.DELETE_NOTIFICATION)) {
            // when the notification is deleted
            Log.e("ab_do" , "DELETE_NOTIFICATION") ;
            Intent SpeakIntent = new Intent(context , TTsServices.class) ;
            context.startService(SpeakIntent) ;
                return;
            }
        if (intent.getAction().equals(Constant.PLAY_SOUND)) {
            // when the user press on play action on the notification
            Log.e("ab_do", "PLAY_SOUND ");
            String Content = intent.getStringExtra(Constant.CONTENT);
            Intent SpeakIntent = new Intent(context , TTsServices.class) ;
            SpeakIntent.putExtra(Constant.TEXT_TO_SPEECH , Content) ;
            context.startService(SpeakIntent) ;
            return;
        }
        if (intent.getAction().equals(Constant.ACTION_FINISH_KEY)) {
            // when the user press on finish action on the notification
            Log.e("ab_do", "ACTION_GO_KEY ");
            notificationHelper.ClearNotification(intent.getIntExtra(Constant.ACTION_FINISH_KEY, -1) , context);
            return;
        }
        if (intent.getAction().equals(Constant.IS_GIVE_ME_MIN_ACTION)) {
                // when the user press on Give me min action on the notification
                Log.e("ab_do", "Five min Action");
                Repository repository = new Repository((Application) context.getApplicationContext());
                FinishedTasksEntity FinishedTask = repository.GetFinishedTaskById(intent.getIntExtra(Constant.REQUEST_CODE, -1));
                String RemindMeVal = intent.getStringExtra(Constant.RemindMeVal) ;
                if (RemindMeVal!=null)
                GiveMeFiveMin(context, FinishedTask , RemindMeVal);
                return;
            }
        if (intent.getAction().equals(Constant.PREPARE_NOTIFICATION)) {
                // The Alarm is fired :
                Log.e("ab_do", "PrepareNotification");
                Repository repository = new Repository((Application) context.getApplicationContext());
                TaskEntity task = repository.GetTaskById(intent.getIntExtra(Constant.REQUEST_CODE, -1));
                notificationHelper.PrepareNotification(context, task, false);
            }
        }
    private void GiveMeFiveMin(Context context, FinishedTasksEntity finishedTask , String remindMeVal) {
        int RemindVal = 0 ;
        if (remindMeVal.equals(context.getString(R.string.min1)))
            RemindVal = 1 ;
        if (remindMeVal.equals(context.getString(R.string.min2)))
            RemindVal = 2 ;
        if (remindMeVal.equals(context.getString(R.string.min5)))
            RemindVal = 5 ;
        if (remindMeVal.equals(context.getString(R.string.min10)))
            RemindVal = 10 ;
        notificationHelper.ClearNotification((int) finishedTask.getId() , context);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + RemindVal);
        TaskEntity Task = new TaskEntity(finishedTask.getId() , finishedTask.getTitle() , finishedTask.getDescription() , finishedTask.getDate());
        Task.setDate(calendar);
        Task.setPermanent(false);
        Task.setSelection(Constant.NO_REPEAT);
        Repository repository = new Repository((Application) context.getApplicationContext());
        repository.DeleteFinishedTask(finishedTask);
        repository.Insert(Task);
        TaskWidgetProvider.NotifyWidgetDataChanged(context);
        AlarmMSystem.PrePareAlarm(context, Task);
    }

}
