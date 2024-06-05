package com.be_apps.alarmmanager.Systems;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.multidex.MultiDexApplication;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.UI.MainActivity;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.AppOpenManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.startapp.sdk.adsbase.StartAppAd;

import java.util.Collections;
import java.util.List;

public class App extends MultiDexApplication {
    BillingSystem billingSystem ;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ad_trace", "OnCreateApp");
        PrepareBillingProcess();
        if (!AdsUtilites.IsAdsRemoved(this)) {
            InitAds();
            StartAppAd.disableSplash();
        }
        new AppOpenManager(this);
        CreateNotificationChannelForSystemNotification();
    }

    private void InitAds() {
        List<String> testDeviceIds = Collections.singletonList("871F8FEFCB335C19275871536D6AFB1A");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
    }

    private void CreateNotificationChannelForSystemNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constant.NOTIFICATION_CHANNEL_SYSTEM, "Notifications from the Server", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, getAudioAttributes()); // sound of the notification
            notificationChannel.setShowBadge(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setBypassDnd(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setDescription("Notifications from server");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            super.onCreate();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static AudioAttributes getAudioAttributes() {
        return new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
    }

    private void PrepareBillingProcess() {
        billingSystem = new BillingSystem(this , null) ;
        billingSystem.StartFetchingPurchasesProcess();
    }

}
