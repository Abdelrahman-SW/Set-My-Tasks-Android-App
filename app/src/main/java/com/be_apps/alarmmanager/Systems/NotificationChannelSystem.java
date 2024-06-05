package com.be_apps.alarmmanager.Systems;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;


public class NotificationChannelSystem {
    public static NotificationChannel  notificationChannel ;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel GetNotificationChannel(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context) ;
        String music = sharedPreferences.getString(Constant.SOUND_URI , String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)) ;
        String Id =  music + sharedPreferences.getBoolean(context.getString(R.string.vibration), true);
                if (notificationChannel==null) {
                notificationChannel = new NotificationChannel(Id, "Task Notifications", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setSound(Uri.parse(music), getAudioAttributes()); // sound of the notification
                if (sharedPreferences.getBoolean(context.getString(R.string.vibration) , true)) {
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern((new long[]{1000, 500, 1000, 500, 1500, 500, 1500}));
                }
                else {
                    notificationChannel.enableVibration(false);
                    notificationChannel.setVibrationPattern((new long[]{0 , 0 , 0 , 0}));
                }
                notificationChannel.setShowBadge(true);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setBypassDnd(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setDescription("Send A notification for your Task");
                Log.e("ab_do" , "Should show lights " + notificationChannel.shouldShowLights());
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
        }
        return notificationChannel ;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static AudioAttributes getAudioAttributes() {
        return new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();
    }
}
