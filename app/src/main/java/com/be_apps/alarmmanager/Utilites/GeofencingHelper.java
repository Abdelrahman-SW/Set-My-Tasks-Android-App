package com.be_apps.alarmmanager.Utilites;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.be_apps.alarmmanager.Broadcasts.GeofencingBroadcast;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.NotificationChannelSystem;
import com.be_apps.alarmmanager.UI.MapsActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

public class GeofencingHelper {
    private final Context context;
    private PendingIntent pendingIntentGeofencing ;
    private GeofencingClient geofencingClient ;
    private OnAddGeofenceListener addGeofenceListener;
    private OnUnRegisterGeofenceListener onUnRegisterGeofenceListener ;
    private final Repository repository ;

    public GeofencingHelper(Context context) {
        this.context = context ;
        repository = new Repository((Application) context.getApplicationContext());
    }
    private Geofence CreateGeofence (GeofencesEntity geofenceEntity) {
        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(geofenceEntity.getLatitude() , geofenceEntity.getLongitude() , (float) geofenceEntity.getRadius());
        builder.setTransitionTypes(GetTransitionType(geofenceEntity.getTransitionType()));
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE); //  we will remove it when it done
        builder.setRequestId(String.valueOf(geofenceEntity.getId()));
        builder.setNotificationResponsiveness(1000);
        if (geofenceEntity.getDwellTime()!=-1) {
            // this value will be ignored if the transition type did not include the dwell trans
            builder.setLoiteringDelay(geofenceEntity.getDwellTime()) ;
            Log.e("ab_do" , "DwellTime " + geofenceEntity.getDwellTime()) ;
        }
        return builder.build() ;
    }
    private int GetTransitionType(int transitionType) {
        switch (transitionType) {
            case Constant.ENTER_AND_EXIT :
                return Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT ;
            case Constant.ENTER_AND_EXIT_AND_DWELL :
                return Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL ;
            case Constant.EXIT_ONLY :
                return Geofence.GEOFENCE_TRANSITION_EXIT ;
            case Constant.DWELL_ONLY :
                return Geofence.GEOFENCE_TRANSITION_DWELL ;
            case Constant.EXIT_AND_DWELL :
                return Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL ;
            case Constant.ENTER_AND_DWELL:
                return Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL ;
            default: return Geofence.GEOFENCE_TRANSITION_ENTER ;
        }
    }
    private GeofencingRequest CreateGeofenceRequest(Geofence geofence , GeofencesEntity entity) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(SetInitialTriggerOfGeofence(entity))
                .addGeofence(geofence)
                .build();
    }
    private int SetInitialTriggerOfGeofence(@NonNull GeofencesEntity entity) {
        switch (entity.getTransitionType()) {
            case Constant.ENTER_ONLY :
                return GeofencingRequest.INITIAL_TRIGGER_ENTER ;
            case Constant.ENTER_AND_EXIT :
                return GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT ;
            case Constant.ENTER_AND_EXIT_AND_DWELL :
                return GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL;
            case Constant.EXIT_ONLY :
                return GeofencingRequest.INITIAL_TRIGGER_EXIT;
            case Constant.DWELL_ONLY :
                return GeofencingRequest.INITIAL_TRIGGER_DWELL ;
            case Constant.EXIT_AND_DWELL :
                return GeofencingRequest.INITIAL_TRIGGER_EXIT | GeofencingRequest.INITIAL_TRIGGER_DWELL ;
            case Constant.ENTER_AND_DWELL:
                return (GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL) ;
            default: return 0 ;
        }
    }
    public PendingIntent GetPendingIntentForGeofencing (){
        if (pendingIntentGeofencing==null) {
            Intent intent = new Intent(context , GeofencingBroadcast.class);
            int pendingIntentGeofencingRequestCode = 10001;
            pendingIntentGeofencing = PendingIntent.getBroadcast(context, pendingIntentGeofencingRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntentGeofencing ;
    }
    public void UnRegisterGeofence (String id) {
        geofencingClient = LocationServices.getGeofencingClient(context);
        List <String> ids = new ArrayList<>();
        ids.add(id) ;
        geofencingClient.removeGeofences(ids).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onUnRegisterGeofenceListener.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               onUnRegisterGeofenceListener.onFailure();
            }
        });
    }
    public String GetGeofenceErrorMsg(Exception e) {
        String msg = "There is an error please try again" ;
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    msg = "There is an error Please check your location Settings";
                break;
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    msg = "You have exceed the available number of Location Tasks";
                break;
                default: msg = "There is an error please try again" ;
            }
        }
        return msg ;
    }
    public void AddGeofence(GeofencesEntity geofencesEntity) {
        geofencingClient = LocationServices.getGeofencingClient(context) ;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        Geofence geofence = CreateGeofence(geofencesEntity);
        GeofencingRequest geofencingRequest = CreateGeofenceRequest(geofence , geofencesEntity);
        geofencingClient.addGeofences(geofencingRequest, GetPendingIntentForGeofencing())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addGeofenceListener.onSuccessAddGeofence();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addGeofenceListener.onFailureAddGeofence(geofencesEntity , e);
                    }
                });
    }
    public void ReRegisterAllGeofences() {
        List<GeofencesEntity> geofencesEntities = repository.GetGeofences();
        if (geofencesEntities!=null && geofencesEntities.size()!=0) {
            for (GeofencesEntity entity : geofencesEntities) {
                if (!entity.IsFinished())
                AddGeofence(entity);
            }
        }
    }
    public interface OnAddGeofenceListener {
        void onSuccessAddGeofence() ;
        void onFailureAddGeofence(GeofencesEntity geofencesEntity , Exception e) ;
    }
    public interface OnUnRegisterGeofenceListener {
        void onSuccess() ;
        void onFailure() ;
    }
    public void SetOnUnRegisterGeofenceListener (OnUnRegisterGeofenceListener listener) {
        this.onUnRegisterGeofenceListener = listener  ;
    }
    public void SetonAddGeofenceListener(OnAddGeofenceListener  listener) {
        this.addGeofenceListener = listener ;
    }
    public void HandleGeofenceNotification(Geofence geofence, int TransitionType) {
        // show the notification :
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context) ;
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.location);
        GeofencesEntity entity = GetGeofenceTaskFromGeo(geofence) ;
        NotificationCompat.Builder buildNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            buildNotification = new NotificationCompat.Builder(context , NotificationChannelSystem.GetNotificationChannel(context).getId());
        else {
            String soundS = PreferenceManager.getDefaultSharedPreferences(context).getString(Constant.SOUND_URI , String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)) ;
            buildNotification = new NotificationCompat.Builder(context , "") ;
            buildNotification.setPriority(NotificationCompat.PRIORITY_MAX);
            buildNotification.setSound(Uri.parse(soundS));
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.vibration) , true))
            buildNotification.setVibrate((new long[]{1000, 500, 1000, 500, 1500, 500, 1500}));
            buildNotification.setLights(Color.RED, 2000, 500);
        }
        buildNotification.setSmallIcon(R.drawable.ic_plan);
        buildNotification.setLargeIcon(largeIcon);
        buildNotification.setContentText(entity.getDescription());
        buildNotification.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(entity.getDescription())
        );
        buildNotification.setCategory(NotificationCompat.CATEGORY_ALARM);
        buildNotification.setAutoCancel(true);
        String Title = "" ;
        if (TransitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
            Title = "You Enter the Location of your Task" ;
        else if (TransitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
            Title = "You EXIT the Location of your Task" ;
        else if (TransitionType == Geofence.GEOFENCE_TRANSITION_DWELL){
           Title = "You Stay in the Location for " + entity.getDwellTimeStr() ;
        }
        buildNotification.setContentTitle(Title);
        buildNotification.setTicker(Title);
        buildNotification.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
        buildNotification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        HandleNotifiedGeofence(entity , TransitionType);
        buildNotification.setContentIntent(GetContentIntentForGeoNotification(context, entity)) ;
        Notification notification = buildNotification.build();
        notificationManager.notify((int) (entity.getId()) , notification);
    }
    private GeofencesEntity GetGeofenceTaskFromGeo(Geofence geofence) {
        long id = Long.parseLong(geofence.getRequestId()) ;
        return repository.GetGeofenceById(id) ;
    }
    private PendingIntent GetContentIntentForGeoNotification (Context context , GeofencesEntity entity) {
        Intent intent = new Intent(context , MapsActivity.class) ;
        intent.putExtra(Constant.GEOFENCE_TASK_ID , entity.getId());
        return PendingIntent.getActivity(context , (int) entity.getId(), intent , PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void HandleNotifiedGeofence(GeofencesEntity entity , int TransitionType) {
        if (TransitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            switch (entity.getTransitionType()) {
                case Constant.ENTER_ONLY : // the task is finished
                    entity.setIsFinished(true);
                    UnRegisterNotifiedGeofence(entity); // remove the geofence
                    break;
                case Constant.ENTER_AND_EXIT :
                    entity.setTransitionType(Constant.EXIT_ONLY);
                    break;
                case Constant.ENTER_AND_DWELL :
                    entity.setTransitionType(Constant.DWELL_ONLY);
                    break;
                case Constant.ENTER_AND_EXIT_AND_DWELL :
                    entity.setTransitionType(Constant.EXIT_AND_DWELL);
                    break;
            }
            entity.setIsEnterTransitionNotified(true);
        }
        else if (TransitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            switch (entity.getTransitionType()) {
                case Constant.EXIT_ONLY :
                    entity.setIsFinished(true);
                    UnRegisterNotifiedGeofence(entity);
                    break;
                case Constant.ENTER_AND_EXIT :
                    entity.setTransitionType(Constant.ENTER_ONLY);
                    break;
                case Constant.EXIT_AND_DWELL :
                    entity.setTransitionType(Constant.DWELL_ONLY);
                    break;
                case Constant.ENTER_AND_EXIT_AND_DWELL :
                    entity.setTransitionType(Constant.ENTER_AND_DWELL);
                    break;
            }
            entity.setIsExitTransitionNotified(true);
        }
        else if (TransitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
            switch (entity.getTransitionType()) {
                case Constant.DWELL_ONLY :
                    entity.setIsFinished(true);
                    UnRegisterNotifiedGeofence(entity);
                    break;
                case Constant.ENTER_AND_DWELL :
                    entity.setTransitionType(Constant.ENTER_ONLY);
                    break;
                case Constant.EXIT_AND_DWELL :
                    entity.setTransitionType(Constant.EXIT_ONLY);
                    break;
                case Constant.ENTER_AND_EXIT_AND_DWELL :
                    entity.setTransitionType(Constant.ENTER_AND_EXIT);
                    break;
            }
            entity.setIsDwellTransitionNotified(true);
        }
        repository.UpdateGeofence(entity);
    }
    private void UnRegisterNotifiedGeofence(GeofencesEntity entity) {
        String id = String.valueOf(entity.getId());
        geofencingClient = LocationServices.getGeofencingClient(context);
        List <String> ids = new ArrayList<>();
        ids.add(id) ;
        geofencingClient.removeGeofences(ids).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("ab_do" , "OnSuccessRemoveNotifiedGeofence") ;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ab_do" , "onFailureRemoveNotifiedGeofence") ;
                UnRegisterNotifiedGeofence(entity);
            }
        });
    }

    }