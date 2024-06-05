package com.be_apps.alarmmanager.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.be_apps.alarmmanager.Adapters.IntroScreenPagerAdapter;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Views.ScreenIntroItem;
import com.be_apps.alarmmanager.databinding.ActivityIntroBinding;
import com.startapp.sdk.adsbase.StartAppAd;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {
    ActivityIntroBinding activityIntroBinding ;
    SharedPreferences sharedPreferences ;
    List<ScreenIntroItem> screenIntroItems ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
        CheckIfShouldIgnoreThisScreen();
        StartAppAd.disableSplash();
        Init();
        SetUpViewPager();
        SetListeners();
    }

    private void Init() {
        activityIntroBinding = ActivityIntroBinding.inflate(getLayoutInflater());
        View view = activityIntroBinding.getRoot();
        setContentView(view);
        SetFullScreen();
    }

    private void SetFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        }
        else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    private void CheckIfShouldIgnoreThisScreen() {
        if (!IsFirstTimeOpenTheApp()) {
            startActivity(new Intent(IntroActivity.this , splashscreen.class));
            finish();
            overridePendingTransition(0 , R.anim.fade_out);
        }
    }
    private boolean IsFirstTimeOpenTheApp() {
        return sharedPreferences.getBoolean(Constant.IsFirstTimeOpenTheApp, true) ;
    }
    private void SetUpViewPager() {
        screenIntroItems = getScreenIntroItems();
        IntroScreenPagerAdapter pagerAdapter = new IntroScreenPagerAdapter(this, screenIntroItems , activityIntroBinding.getRoot());
        activityIntroBinding.viewPager.setAdapter(pagerAdapter);
        activityIntroBinding.tabLayout.setupWithViewPager(activityIntroBinding.viewPager);
    }
    private List<ScreenIntroItem> getScreenIntroItems() {
        List<ScreenIntroItem> screenIntroItems = new ArrayList<>();
        screenIntroItems.add(new ScreenIntroItem("Set Your Tasks and Organize your Time", R.drawable.newimg2));
        screenIntroItems.add(new ScreenIntroItem("Add one Time Task Or a Repeat Task", R.drawable.ic_stopwatch));
        screenIntroItems.add(new ScreenIntroItem("Get a notification for your Task on Time", R.drawable.ic_notification__intro));
        screenIntroItems.add(new ScreenIntroItem("Recreate Finished Tasks", R.drawable.ic_test));
        screenIntroItems.add(new ScreenIntroItem("Add Tasks By Location", R.drawable.ic_map));
        screenIntroItems.add(new ScreenIntroItem("Get a Notification for your task\n when (Enter - Exit - Stay for specified Duration)", R.drawable.loc_mark_intro));
        screenIntroItems.add(new ScreenIntroItem("Easy to Add tasks with your voice", R.drawable.ic_microphone));
        screenIntroItems.add(new ScreenIntroItem("Listen to The Description of your Task", R.drawable.ic_listening));
        return screenIntroItems;
    }
    private void SetListeners() {
        activityIntroBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                  //  Log.e("ab_do" , "onPageScrolled") ;
            }

            @Override
            public void onPageSelected(int position) {
                Log.e("ab_do", "onPageSelected " + position);
                if (position == screenIntroItems.size() - 1) {
                    // we reach to the last screen :
                    loadLastScreen();
                }
                else {
                    loadNormalScreen();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        activityIntroBinding.NextButton.setOnClickListener(v -> {
            int pos = activityIntroBinding.viewPager.getCurrentItem(); // current page
            pos++; // Go To next Page
            activityIntroBinding.viewPager.setCurrentItem(pos);
        });
        activityIntroBinding.tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetStarted();
            }
        });
        activityIntroBinding.btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetStarted();
            }
        });
    }
    private void GetStarted() {
        SaveIntroFinishedPrefs();
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
    }
    private void loadNormalScreen() {
        activityIntroBinding.NextButton.setVisibility(View.VISIBLE);
        activityIntroBinding.tvSkip.setVisibility(View.VISIBLE);
        activityIntroBinding.btnGetStarted.setVisibility(View.GONE);
    }
    private void loadLastScreen() {
        activityIntroBinding.NextButton.setVisibility(View.INVISIBLE);
        activityIntroBinding.tvSkip.setVisibility(View.GONE);
        activityIntroBinding.btnGetStarted.setVisibility(View.VISIBLE);
        activityIntroBinding.btnGetStarted.setAnimation(AnimationUtils.loadAnimation(getApplicationContext() , R.anim.started_btn_anim));
    }
    private void SaveIntroFinishedPrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit() ;
        editor.putBoolean(Constant.IsFirstTimeOpenTheApp, false) ;
        editor.apply();
    }
    @Override
    public void onBackPressed() {
        int pos = activityIntroBinding.viewPager.getCurrentItem() ;
        if (pos==0)
        super.onBackPressed();
        else {
            pos -- ;
            activityIntroBinding.viewPager.setCurrentItem(pos);
        }
    }
}