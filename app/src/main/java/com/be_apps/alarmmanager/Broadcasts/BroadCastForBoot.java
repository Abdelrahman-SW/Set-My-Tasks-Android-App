package com.be_apps.alarmmanager.Broadcasts;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.Systems.AlarmMSystem;
import com.be_apps.alarmmanager.Systems.GpsSystem;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.Utilites.GooglePlayServicesUtils;
import com.be_apps.alarmmanager.Utilites.NotificationHelper;
import com.be_apps.alarmmanager.WidgetSystem.TaskWidgetProvider;

import java.util.Calendar;
import java.util.List;

public class BroadCastForBoot extends BroadcastReceiver {
    private Context context;
    private Repository repository ;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Boot", "REBOOT ");
        this.context = context;
        if (intent.getAction() != null) {
                Log.e("Boot", "REBOOT " + intent.getAction());
                PrepareAlarmAfterBoot();
                ReRegisterAllGeofences(context);
                if (GooglePlayServicesUtils.IfGooglePlayServicesAvailable(context))
                CheckGpsSystem(context);
        }
              else Log.e("Boot", "NULL REBOOT");
        }

    private void ReRegisterAllGeofences(Context context) {
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
    }

    private void CheckGpsSystem(Context context) {
        GpsSystem gpsSystem = new GpsSystem(context);
        gpsSystem.SetGpsProcess();
    }

    private void PrepareAlarmAfterBoot() {
        repository = new Repository((Application) context.getApplicationContext());
        NotificationHelper notificationHelper = new NotificationHelper();
        List<TaskEntity> taskEntities = repository.GetTasksForBoot();
        for (TaskEntity Task : taskEntities) {
            if (!Task.isPermanent() && Task.getDate().after(Calendar.getInstance())) {
                Log.e("Boot", Task.getDescription() + " Not Finished So prepare Alarm");
                AlarmMSystem.PrePareAlarm(context , Task);
            }
            else if (!Task.isPermanent() && Task.getDate().before(Calendar.getInstance())) {
                Log.e("Boot", Task.getDescription() + " Not Finished because Boot");
                notificationHelper.PrepareNotification(context , Task , true);
            }
            else if (Task.isPermanent()) {
                Log.e("Boot", Task.getDescription() + "Permanent");
                notificationHelper.SetNextRepeat(context , Task);
            }
            else Log.e("Boot", Task.getDescription() + "Else !");
        }

        TaskWidgetProvider.NotifyWidgetDataChanged(context);
    }

}

