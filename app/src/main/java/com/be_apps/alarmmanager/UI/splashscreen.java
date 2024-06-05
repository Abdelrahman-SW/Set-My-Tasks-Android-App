package com.be_apps.alarmmanager.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.App;
import com.be_apps.alarmmanager.Systems.BillingSystem;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.AppOpenManager;
import com.startapp.sdk.adsbase.StartAppAd;

public class splashscreen extends AppCompatActivity {
    //CharSequence charSequence;
    //int index;
    //long delay = 200;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    BillingSystem billingSystem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        PrepareBillingProcess();
        StartAppAd.disableSplash();
        if (AdsUtilites.IsAdsRemoved(this)) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getBaseContext() , MainActivity.class));
                }
            };
            handler.postDelayed(runnable , 1500);
        }

        //new AppOpenManager((App) getApplication());
        // SplashTxt = findViewById(R.id.Txt_splash) ;
        //PrepareOpenMainActivityAction();

//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                  SplashTxt.setText(charSequence.subSequence(0 , index++));
//                  if (index <= charSequence.length()) {
//                      handler.postDelayed(runnable, delay);
//                  }
//            }
//        };
//        AnimateText("Add tasks\nManage your time");
//    }
//    private void AnimateText(CharSequence cs) {
//       charSequence = cs ;
//       index = 0 ;
//       SplashTxt.setText("");
//       //handler.removeCallbacks(runnable);
//       handler.postDelayed(runnable , delay) ;
//    }
    }
    private void PrepareBillingProcess() {
        billingSystem = new BillingSystem(this , null) ;
        billingSystem.StartFetchingPurchasesProcess();
    }
   /** private void PrepareOpenMainActivityAction() {
        runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, R.anim.fade_out);
            }
        };
        handler.postDelayed(runnable , 3000) ;}**/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}