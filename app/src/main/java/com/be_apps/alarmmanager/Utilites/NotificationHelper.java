package com.be_apps.alarmmanager.Utilites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Broadcasts.MyBroadCast;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.AlarmMSystem;
import com.be_apps.alarmmanager.Systems.NotificationChannelSystem;
import com.be_apps.alarmmanager.Systems.TTsServices;
import com.be_apps.alarmmanager.UI.MainActivity;
import com.be_apps.alarmmanager.WidgetSystem.TaskWidgetProvider;

import java.util.Calendar;

public final class NotificationHelper {
    private final static int REQUEST_CODE_CONTENT_INTENT = 2222  ;
    private final static int DELETE_REQUEST_CODE = 1111 ;
    private int  NotifyId ; // id of the task to be used as notify id for the notification
    private TaskEntity ReceiveTask ;
    private SharedPreferences sharedPreferences ;

    @SuppressLint("ApplySharedPref")
    synchronized public void PrepareNotification(Context context , TaskEntity task , boolean ForBoot) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String Title = "Hello , You have a task to do";
        if (ForBoot) { //prepare a notification due to boot
            Title = "Hello You missed this Task  :(";
        }
        if (task == null) return;
        try {
            ReceiveTask = (TaskEntity) task.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        NotifyId = (int) task.getId();
        NotificationCompat.Builder BuildNotification; // builder to build the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context); // notification Manager to fire the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BuildNotification = new NotificationCompat.Builder(context, NotificationChannelSystem.GetNotificationChannel(context).getId());
        } else {
            // before oreo (api --> 26)
            String soundS = sharedPreferences.getString(Constant.SOUND_URI, String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI));
            BuildNotification = new NotificationCompat.Builder(context, "");
            BuildNotification.setPriority(NotificationCompat.PRIORITY_MAX);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            BuildNotification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            else
            BuildNotification.setSound(Uri.parse(soundS), AudioManager.STREAM_ALARM);
            if (sharedPreferences.getBoolean(context.getString(R.string.vibration) , true))
            BuildNotification.setVibrate((new long[]{1000, 500, 1000, 500, 1500, 500, 1500}));
            BuildNotification.setLights(Color.RED, 2000, 500);
        }
        BuildNotification.setSmallIcon(R.drawable.ic_plan);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.notification);
        BuildNotification.setLargeIcon(largeIcon);
        BuildNotification.setContentTitle(Title);
        BuildNotification.setContentText(task.getDescription());
        BuildNotification.setAutoCancel(true); // when the user click on the notification it will be canceled
        BuildNotification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        BuildNotification.setOnlyAlertOnce(true);
        BuildNotification.setTicker(Title);
        BuildNotification.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
        BuildNotification.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(task.getDescription())
                .setSummaryText("New Task")
                .setBigContentTitle(Title)
        );
        BuildNotification.setCategory(NotificationCompat.CATEGORY_ALARM);
        BuildNotification.addAction(getFinishAction(context));
        if (!task.isPermanent()) {
            Log.e("ab_do" , "YeS");
            BuildNotification.addAction(getGiveMeAction(context));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            BuildNotification.addAction(getPlayAction(context));
        BuildNotification.setContentIntent(CreateContentIntent(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            BuildNotification.setDeleteIntent(PendingIntent.getBroadcast(context, DELETE_REQUEST_CODE, DeleteIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = BuildNotification.build();
        try {
            notificationManager.notify(NotifyId, notification);
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= 26) {
                DeleteCurrentNotificationChannel(sharedPreferences.getString(Constant.SOUND_URI, String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)), context);
                sharedPreferences.edit().putString(Constant.SOUND_URI, String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)).commit();
                sharedPreferences.edit().putString(Constant.TITLE_SOUND, "Default Sound").apply();
                PrepareNotification(context, task, ForBoot);
                return;
            }
        }

        if (task.isPermanent())
            SetNextRepeat(context, task);
        else
            FinishTheNotifiedTask(context, task);
        TaskWidgetProvider.NotifyWidgetDataChanged(context);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void DeleteCurrentNotificationChannel(String Channel_id , Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.deleteNotificationChannel(Channel_id);
        NotificationChannelSystem.notificationChannel = null;

    }
    private  NotificationCompat.Action getFinishAction(Context context) {
        return new NotificationCompat.Action.Builder(0 , "Finish" , PendingIntent.getBroadcast(
                context, GetPendingIntentRequestCode.GetRequestCode(context), CreateIntentForFinishAction(context), 0)).build() ;
    }
    private  NotificationCompat.Action getPlayAction(Context context) {
        return new NotificationCompat.Action.Builder(0 , "Play" , PendingIntent.getBroadcast(context ,
                GetPendingIntentRequestCode.GetRequestCode(context) , PlaySoundIntent(context) , 0)).build();
    }
    private  NotificationCompat.Action getGiveMeAction(Context context ) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String remindMeVal = sharedPreferences.getString(context.getString(R.string.RemindMe) , context.getString(R.string.min5)) ;
        String RemindMe = "" ;
        if (remindMeVal!=null) {
            if (remindMeVal.equals(context.getString(R.string.min1)))
                RemindMe = "Give me 1 min";
            if (remindMeVal.equals(context.getString(R.string.min2)))
                RemindMe = "Give me 2 min";
            if (remindMeVal.equals(context.getString(R.string.min5)))
                RemindMe = "Give me 5 min";
            if (remindMeVal.equals(context.getString(R.string.min10)))
                RemindMe = "Give me 10 min";
        }
        return new NotificationCompat.Action.Builder(0 , RemindMe , PendingIntent.getBroadcast(
                context, GetPendingIntentRequestCode.GetRequestCode(context), GiveMeMinIntent(context , ReceiveTask , remindMeVal), 0)).build() ;
    }
    private  PendingIntent CreateContentIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, REQUEST_CODE_CONTENT_INTENT, intent, 0);
    }
    private  Intent CreateIntentForFinishAction(Context context) {
        Intent intent = new Intent(context, MyBroadCast.class);
        intent.setAction(Constant.ACTION_FINISH_KEY) ;
        intent.putExtra(Constant.ACTION_FINISH_KEY , NotifyId);
        return intent ;
    }
    private  Intent PlaySoundIntent(Context context) {
        Intent intent = new Intent(context , MyBroadCast.class) ;
        intent.setAction(Constant.PLAY_SOUND) ;
        intent.putExtra(Constant.CONTENT , ReceiveTask.getDescription()) ;
        return intent ;
    }
    private  Intent DeleteIntent (Context context) {
        Intent intent = new Intent(context , MyBroadCast.class) ;
        intent.setAction(Constant.DELETE_NOTIFICATION) ;
        return intent ;
    }
    private  Intent GiveMeMinIntent(Context context, TaskEntity task , String RemindMeVal) {
        Intent intent = new Intent(context , MyBroadCast.class) ;
        int RequestCode = (int) task.getId();
        intent.putExtra(Constant.REQUEST_CODE , RequestCode) ;
        intent.setAction(Constant.IS_GIVE_ME_MIN_ACTION);
        intent.putExtra(Constant.RemindMeVal , RemindMeVal) ;
        return intent ;
    }
    public void ClearNotification(int notifyId , Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context) ;
        notificationManager.cancel(notifyId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // stop any playing Sound
            Intent SpeakIntent = new Intent(context, TTsServices.class);
            context.startService(SpeakIntent);
        }
    }
    public void SetNextRepeat(Context context , TaskEntity task) {
        Log.e("ab_do" , "CheckForRepeat") ;
        int Repeat_Selection = task.getSelection() ;
        int PickerTypeVal = task.getPickerTypeValue() ;
        int PickerNumberVal = task.getNumberPickerValue() ;
        // the task is repeat and we should assign the new date to it
        Calendar calendar = task.getDate();
        Calendar Next_Date = (Calendar) calendar.clone();
        int i = 1 ;
        switch (Repeat_Selection) {
            case Constant.ONCE_PER_DAY:
                while (Next_Date.before(Calendar.getInstance())) {
                    Next_Date.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + i);
                    i++;
                }
                task.setDate(Next_Date);
                break;
            case Constant.ONCE_PER_WEEK:
                while (Next_Date.before(Calendar.getInstance())) {
                    Next_Date.set(Calendar.WEEK_OF_MONTH, calendar.get(Calendar.WEEK_OF_MONTH) + i);
                    i++;
                }
                task.setDate(Next_Date);
                break;
            case Constant.ONCE_PER_MONTH:
                while (Next_Date.before(Calendar.getInstance())) {
                    Next_Date.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + i);
                    i++;
                }
                task.setDate(Next_Date);
                break;
            case Constant.ONCE_PER_YEAR:
                while (Next_Date.before(Calendar.getInstance())) {
                    Next_Date.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + i);
                    i++;
                }
                task.setDate(Next_Date);
                break;
            case Constant.OTHER:
                switch (PickerTypeVal) {
                    case Constant.PICKER_MIN:
                        while (Next_Date.before(Calendar.getInstance())) {
                            Next_Date.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                    case Constant.PICKER_HOUR:
                        while (Next_Date.before(Calendar.getInstance())) {
                            Next_Date.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                    case Constant.PICKER_DAY:
                        while (Next_Date.before(Calendar.getInstance()))  {
                            Next_Date.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                    case Constant.PICKER_WEEK:
                        while (Next_Date.before(Calendar.getInstance()))  {
                            Next_Date.set(Calendar.WEEK_OF_MONTH, calendar.get(Calendar.WEEK_OF_MONTH) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                    case Constant.PICKER_MONTH:
                        while (Next_Date.before(Calendar.getInstance()))  {
                            Next_Date.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                    case Constant.PICKER_YEAR:
                        while (Next_Date.before(Calendar.getInstance()))  {
                            Next_Date.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + PickerNumberVal*i);
                            i++ ;
                        }
                        task.setDate(Next_Date);
                        break;
                }
                break;
        }
        UpdateTask(context , task) ;
    }
    private void FinishTheNotifiedTask(Context context, TaskEntity task) {
        Repository repository = new Repository((Application) context.getApplicationContext());
        repository.Delete(task);
        FinishedTasksEntity finishedTasksEntity = new FinishedTasksEntity(task.getId() , task.getTitle() , task.getDescription() , task.getDate());
        repository.InsertFinishedTask(finishedTasksEntity) ;
    }
    private void UpdateTask(Context context , TaskEntity task) {
        Log.e("ab_do" , "UpdateTask") ;
        Repository repository = new Repository((Application) context.getApplicationContext()) ;
        repository.Update(task); // update the task in the database with the new date
        AlarmMSystem.PrePareAlarm(context , task); // prepare the new alarm
    }
}
