package com.be_apps.alarmmanager.Utilites;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.App;
import com.be_apps.alarmmanager.UI.MainActivity;
import com.be_apps.alarmmanager.UI.splashscreen;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks , LifecycleObserver {
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final App app;
    private Activity CurrentActivity ;
    private static boolean isShowingAd = false;
    private long loadTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    int NumOfFailedRequests = 0 ;
    private final int MAXIMUM_NUMBER_OF_AD_REQUEST = 5 ;

    public AppOpenManager(App app) {
        this.app = app;
        this.app.registerActivityLifecycleCallbacks(this);
        Log.e("ab_do" , "CreateAppOpenManager") ;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void fetchAd() {
        NumOfFailedRequests = 0 ;
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }
        Log.e("ab_do" , "fetchAd");
        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                     //Called when an app open ad has loaded.
                    @Override
                    public void onAppOpenAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = (new Date()).getTime();
                        Log.e("ab_do" , "onAppOpenAdLoaded");
                        NumOfFailedRequests = 0 ;
                    }
                     //Called when an app open ad has failed to load.
                    @Override
                    public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                        Log.e("ab_do" , "onAppOpenAdFailedToLoad  " + loadAdError.getMessage());
                        if (NumOfFailedRequests++ < MAXIMUM_NUMBER_OF_AD_REQUEST) {
                            Log.d("ab_do" , "Try To Load Again") ;
                            AppOpenAd.load(app, Constant.APP_OPEN_AD , getAdRequest(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
                        }
                    }
                };
        AppOpenAd.load(app, Constant.APP_OPEN_AD , getAdRequest(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }


    // Utility method to check if ad was loaded more than n hours ago
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    //Shows the ad if one isn't already showing
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available. (loaded) if is not loaded skip (Go to main Activity (Best practise) ) and load a new one
        // for example in cold start
        if (!isShowingAd && isAdAvailable()) {
            Log.d("ab_do", "Will show ad.");
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // when the user closed the ad :
                            // Set the reference to null so isAdAvailable() returns false. so fetch a new one
                            appOpenAd = null;
                            isShowingAd = false;
                            Log.e("ab_do" , "onAdDismissedFullScreenContent");
                            if (CurrentActivity.getClass().getSimpleName().equals(splashscreen.class.getSimpleName())) {
                                GoToMainActivity(300);
                            }
                            // prepare new ad
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.e("ab_do" , "onAdFailedToShowFullScreenContent  " + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.e("ab_do" , "onAdShowedFullScreenContent");
                            isShowingAd = true;
                        }
                    };
            appOpenAd.show(CurrentActivity, fullScreenContentCallback);
        }
        else {
            // maybe because ad is not prepared yet or there is an and already showing
            if (CurrentActivity.getClass().getSimpleName().equals(splashscreen.class.getSimpleName())) {
                GoToMainActivity(1500);
            }
            Log.d("ab_do", "Can not show ad.");
            fetchAd();
        }
    }

    private void GoToMainActivity(int i) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CurrentActivity.startActivity(new Intent(CurrentActivity.getBaseContext(), MainActivity.class));
                CurrentActivity.overridePendingTransition(0, R.anim.fade_out);
            }
        };
        handler.postDelayed(runnable, i);
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4) ;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.e("ab_do" , "onActivityCreated" + activity.getLocalClassName());
        if (activity.getLocalClassName().equals("com.google.android.gms.ads.AdActivity")) // don`t save a reference to the ad activity
            return;
        CurrentActivity = activity ;
    }
    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.e("ab_do" , "onActivityStarted" + activity.getLocalClassName());
        if (activity.getLocalClassName().equals("com.google.android.gms.ads.AdActivity")) return;
        CurrentActivity = activity ;
    }
    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.e("ab_do" , "onActivityResumed" + activity.getLocalClassName());
        if (activity.getLocalClassName().equals("com.google.android.gms.ads.AdActivity")) return;
        CurrentActivity = activity ;
    }
    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.e("ab_do" , "onActivityPaused" + activity.getLocalClassName());
    }
    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.e("ab_do" , "onActivityStopped" + activity.getLocalClassName());
    }
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.e("ab_do" , "onActivityDestroyed" + activity.getLocalClassName());
    }
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        Log.d("ab_do", "OnLifecycleEventOnStart");
        if (AdsUtilites.IsAdsRemoved(app)) {
            // if the user removed the ads return and remove observer :
            app.unregisterActivityLifecycleCallbacks(this);
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
            /**
            if (CurrentActivity.getClass().getSimpleName().equals(splashscreen.class.getSimpleName())) {
                // the user in the splash screen :
                GoToMainActivity(1500);
            }
             */
            return;
        }
        // best practices : // don`t show ads before user open the app more than two times :
        int numTimesOfOpenApp = PreferenceManager.getDefaultSharedPreferences(app).getInt(Constant.OPEN_APP_FEW_TIMES , 0) ;
        if (numTimesOfOpenApp <= 2) {
            // skip the ads :
            Log.d("ab_do", "SkipAds  " + numTimesOfOpenApp);
            if (CurrentActivity.getClass().getSimpleName().equals(splashscreen.class.getSimpleName())) {
                // the user in the splash screen :
                GoToMainActivity(1500);
            }
            return;
        }
        // the user open the app more than two times so start to show the ad :
        showAdIfAvailable();
    }

}
