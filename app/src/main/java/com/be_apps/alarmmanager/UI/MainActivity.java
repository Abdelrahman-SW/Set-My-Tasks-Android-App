package com.be_apps.alarmmanager.UI;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.applovin.sdk.AppLovinSdk;
import com.be_apps.alarmmanager.Adapters.ActiveTasksAdapter;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.DatabaseAndEntities.Repository;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.AlarmMSystem;
import com.be_apps.alarmmanager.Systems.BillingSystem;
import com.be_apps.alarmmanager.Systems.GpsSystem;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.GooglePlayServicesUtils;
import com.be_apps.alarmmanager.Utilites.NotificationHelper;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;
import com.be_apps.alarmmanager.WidgetSystem.TaskWidgetProvider;
import com.be_apps.alarmmanager.databinding.ActivityMainBinding;
import com.google.ads.mediation.adcolony.AdColonyMediationAdapter;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import hotchemi.android.rate.AppRate;

//import com.google.android.ads.mediationtestsuite.MediationTestSuite;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener , BillingSystem.BillingProcess {
    ActivityMainBinding binding ;
    MyViewModel viewModel ;
    ActiveTasksAdapter adapter ;
    List<TaskEntity> Tasks = new ArrayList<>();
    SearchView searchView ;
    long last_time_click  ;
    ActionMode actionMode ;
    boolean IsActionMode = false ;
    ArrayList<TaskEntity> SelectedTasks ;
    int SelectedTasksCount ;
    NotificationManagerCompat notificationManager ;
    String OrderValue ;
    BillingSystem billingSystem;
    boolean IsMoreBtnClicked ;
    SharedPreferences sharedPreferences ;
    AdView adView ;
    Activity activity ;
    AdsUtilites adsUtilites ;
    InterstitialAd mInterstitialAd ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activitymainmenu , menu) ;
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        prepareSearchView();
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.delete) {
            if (Tasks.size() == 0)
                return false;
            PrepareDeleteAllDialog();
            return true;
        } else if (itemId == R.id.search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        activity = this ;
        PrepareBillingProcess();
        if (!AdsUtilites.IsAdsRemoved(this)) {
            Log.e("ad_trace" , "yess") ;
            PrepareAdsSdk();
            InitAds();
        }
        init();
        PrepareNavigationDrawer();
        prepareRecycleView() ;
        RateMyApp();
        if (GooglePlayServicesUtils.IfGooglePlayServicesAvailable(this))
        CheckGpsSystem();
        PrepareAlarmAfterBoot();
        UpdateNumTimesOfOpenApp();
        //MediationTestSuite.launch(MainActivity.this);
        // check if the user has removed the ads first :
    }

    private void PrepareAdsSdk() {
        AdColonyAppOptions appOptions = AdColonyMediationAdapter.getAppOptions();
        //appOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, true);
        //appOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, "1");
        appOptions.setKeepScreenOn(true);
        AdColony.configure(this,// activity context
                Constant.AD_COLONY_APP_ID ,
                Constant.AD_COLONY_INTERSTITIAL_ZONE_EDIT_ACTIVITY , Constant.AD_COLONY_BANNER_ZONE_MAIN_ACTIVITY);
        AppLovinSdk.initializeSdk(this);
    }

    private void InitAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("MyApp", "onInitializationComplete");
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    if (status != null) {
                        Log.d("MyApp", String.format(
                                "Adapter name: %s, Description: %s, Latency: %d",
                                adapterClass, status.getDescription(), status.getLatency()));
                    }
                }
                //  It is important to wait for initialization to complete before you load ads
                //  in order to ensure full participation from every ad network on the first ad request.
                PrepareBannerAd();
                PrepareInterstitialAd();
            }
        });
    }

    private void PrepareInterstitialAd() {
        if (adsUtilites==null)
        adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(activity , Constant.Interstitial_Ad_for_Main_Activity_Id);
    }

    private void UpdateNumTimesOfOpenApp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int openAppTimes = sharedPreferences.getInt(Constant.OPEN_APP_FEW_TIMES , 0);
        Log.d("ab_do" , "openAppTimes " + openAppTimes);
        if (openAppTimes>3) return;
        sharedPreferences.edit().putInt(Constant.OPEN_APP_FEW_TIMES , openAppTimes+1).apply();
    }

    private void PrepareBannerAd() {
        adsUtilites = new AdsUtilites() ;
        adView = adsUtilites.PrepareBannerAd(this , Constant.BANNER_MAIN_ACTIVITY , binding.adViewContainer);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            // refresh the ad to take the new size :
            RemoveBannerAd();
            if (adsUtilites == null) adsUtilites = new AdsUtilites();
            adView = adsUtilites.PrepareBannerAd(this, Constant.BANNER_MAIN_ACTIVITY, binding.adViewContainer);
        }
    }
    private void PrepareBillingProcess() {
        billingSystem = new BillingSystem(MainActivity.this  , binding.getRoot()) ;
        billingSystem.AttachListener(MainActivity.this);
        //billingSystem.StartFetchingPurchasesProcess();
    }

    private void CheckGpsSystem() {
        GpsSystem gpsSystem = new GpsSystem(this) ;
        gpsSystem.SetGpsProcess();
    }


    private void init() {
        activity = this ;
        FirebaseAnalytics.getInstance(this);
        setSupportActionBar(binding.myToolbar);
        if (getSupportActionBar()!=null)
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        notificationManager = NotificationManagerCompat.from(getBaseContext()) ;
        SelectedTasks = new ArrayList<>() ;
        SelectedTasksCount = 0 ; last_time_click = 0 ;
        IsActionMode = false ; IsMoreBtnClicked = false ;
        Typeface typeface = Typeface.createFromAsset(this.getAssets() , "fonts/Calistoga-Regular.ttf") ;
        binding.title.setTypeface(typeface);
        binding.clock.setTypeface(typeface);
        binding.NoTaskText.setTypeface(typeface);
        SetListenersToButtons();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        OrderValue = sharedPreferences.getString(getString(R.string.OrderTasks) , getString(R.string.defaultOrder)) ;
        if (OrderValue!=null)
        PrepareViewModel(OrderValue);
    }
    private void SetListenersToButtons() {
        binding.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsMoreBtnClicked) {
                    CollapseMoreBtn();
                }
                else  {
                    ExpandMoreBtn();
                }
            }
        });
        binding.AddTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.AddTaskButton.setEnabled(false);
                startActivity(new Intent(MainActivity.this , EditActivity.class));
                overridePendingTransition(0 , R.anim.fade_out);
                CollapseMoreBtn();
            }
        });
        binding.AddTaskByLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.AddTaskByLoc.setEnabled(false);
                startActivity(new Intent(getBaseContext() , MapsActivity.class));
                overridePendingTransition(0 , R.anim.fade_out);
                CollapseMoreBtn();
            }
        });
    }

    private void ExpandMoreBtn() {
        binding.AddTaskButton.setVisibility(View.VISIBLE);
        binding.AddTaskByLoc.setVisibility(View.VISIBLE);
        binding.more.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.rotate_more_btn_expand));
        binding.AddTaskButton.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.expand_btns));
        binding.AddTaskByLoc.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.expand_btns));
        IsMoreBtnClicked = true ;
    }

    private void CollapseMoreBtn() {
        binding.more.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.rotate_more_btn_collapse));
        binding.AddTaskButton.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.collapse_btns));
        binding.AddTaskByLoc.setAnimation(AnimationUtils.loadAnimation(getBaseContext() , R.anim.collapse_btns));
        binding.AddTaskButton.setVisibility(View.GONE);
        binding.AddTaskByLoc.setVisibility(View.GONE);
        IsMoreBtnClicked = false ;
    }

    public void PrepareViewModel(String OrderValue) {
        viewModel = new ViewModelProvider(this
                , ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(MyViewModel.class);

        if  (OrderValue.equals(getString(R.string.defaultOrder))) {
            viewModel.getTasksDefaultOrder().observe(this, new Observer<List<TaskEntity>>() {
                @Override
                public void onChanged(List<TaskEntity> taskEntities) {
                      Log.e("ab_do"  , "onChanged") ;
                      ApplyDataChanged(taskEntities);
                }
            });
        }
        else if (OrderValue.equals(getString(R.string.AlphabeticallyOrder))) {
            viewModel.getTasksByAlphabeticallyOrder().observe(this, Tasks -> {
                ApplyDataChanged(Tasks);
            });
        }
        else if (OrderValue.equals(getString(R.string.DateOtN))) {
            viewModel.getTasksOldestToNewest().observe(this, Tasks -> {
                ApplyDataChanged(Tasks);
            });
        }
        else if (OrderValue.equals(getString(R.string.DateNtO))) {
            viewModel.getTasksNewestToOldest().observe(this, Tasks -> {
                ApplyDataChanged(Tasks);
            });
        }
    }

    private void ApplyDataChanged(List<TaskEntity> Tasks) {
        adapter.Full_data = true;
        if (Tasks.size() == 0) {
            PrePareAnimationForNoTasksView();
        }
        else {
            binding.NoTasks.setVisibility(View.GONE);
            binding.more.clearAnimation();
        }
        adapter.submitList(Tasks);
        this.Tasks.clear();
        this.Tasks.addAll(Tasks);
        TaskWidgetProvider.NotifyWidgetDataChanged(this);
    }



    private void PrePareAnimationForNoTasksView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            slide.addTarget(binding.NoTasks);
            slide.setDuration(800);
            TransitionManager.beginDelayedTransition(binding.drawerLayout, slide);
        }
        else
        binding.NoTasks.setAnimation(AnimationUtils.loadAnimation(this , R.anim.no_tasks_anim));
        binding.NoTasks.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(binding.NoTaskText, "textColor", new ArgbEvaluator(), Color.BLACK, Color.WHITE)
                .setDuration(1000);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.start();
        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_animation);
        binding.more.setAnimation(animation);
    }


    private void prepareRecycleView() {
        adapter = new ActiveTasksAdapter(this);
        binding.MyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Log.e("ab_do", "ScrollUp");
                    // hide the AddBtn
                    if (IsMoreBtnClicked)
                        CollapseMoreBtn();
                    if (binding.more.getVisibility() == View.VISIBLE) {
                        binding.more.setAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.collapse_btns));
                        binding.more.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("ab_do", "ScrollDown");
                    // if The Btn is Hidden Show it
                    if (binding.more.getVisibility() == View.GONE) {
                        binding.more.setAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.expand_btns));
                        binding.more.setVisibility(View.VISIBLE);
                    }
                }
            }
            });
        binding.MyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.MyRecyclerView.setAdapter(adapter);
        adapter.setOnLongItemClickListener(new ActiveTasksAdapter.onLongItemClickListener() {
            @Override
            public boolean onLongClick(int pos) {
                if (!IsActionMode)
                    PrepareActionMode();
                if (adapter.getTaskByPos(pos).isSelectedItem())
                     SetUnSelectedTask(pos);
                else
                    SetSelectedTask(pos);
                return true ;
            }
        });
        adapter.setOnItemClickListener(new ActiveTasksAdapter.onItemClickListener() {
            @Override
            public void onClick(int pos) {
                if (IsActionMode) {
                    if (adapter.getTaskByPos(pos).isSelectedItem())
                          SetUnSelectedTask(pos);
                    else
                          SetSelectedTask(pos);
                    return;
                }
                if (SystemClock.elapsedRealtime() - last_time_click < 1000) return;
                last_time_click = SystemClock.elapsedRealtime() ;
                Intent intent = new Intent(MainActivity.this , EditActivity.class) ;
                intent.putExtra(Constant.TASK , adapter.getTaskByPos(pos)) ;
                intent.putExtra(Constant.CALENDER , adapter.getTaskByPos(pos).getDate()) ;
                startActivity(intent);
                overridePendingTransition(0 , R.anim.fade_out);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0 , ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                int From_pos = viewHolder.getAdapterPosition() ;
//                int To_pos = target.getAdapterPosition() ;
//                TaskEntity taskEntity = adapter.getTaskByPos(viewHolder.getAdapterPosition()) ;
//                adapter.notifyItemMoved(From_pos , To_pos);
//                Tasks.remove(From_pos) ;
//                Tasks.add(To_pos , taskEntity);
//                adapter.submitList(Tasks);
//                return true ;
                return false ;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
               PrepareDeleteDialogForSwipe(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.MyRecyclerView);
    }
    private void PrepareDeleteSelectedTasksDialog() {
        List<TaskEntity> tasks = new ArrayList<>(SelectedTasks) ;
        String title , snack_msg  ;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you Sure you want to do this");
        if (SelectedTasksCount>1) {
            title = "You are going to delete  " + SelectedTasksCount + "  Tasks" ;
            snack_msg = "The Tasks has Successfully Deleted" ;
        }
        else {
           title =  "You are going to delete this task" ;
            snack_msg = "The Task has Successfully Deleted" ;
        }
        builder.setTitle(title);
        Snackbar delete = UtilitiesClass.GerSnackForUndo(snack_msg, binding.AddTaskButton, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TaskEntity taskEntity : tasks) {
                    viewModel.Insert(taskEntity);
                    AlarmMSystem.PrePareAlarm(getBaseContext(), taskEntity);
                }
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (TaskEntity taskEntity : tasks) {
                    viewModel.Delete(taskEntity);
                    AlarmMSystem.CancelAlarm(getBaseContext(), (int) taskEntity.getId());
                    notificationManager.cancel((int) taskEntity.getId());
                }
                actionMode.finish();
                delete.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog d = builder.create();
        d.show();
    }
    private void PrepareDeleteDialogForSwipe(int pos) {
        final boolean [] deleted = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you Sure you want to do this");
        builder.setTitle("You are going to delete this task");
        TaskEntity taskEntity = adapter.getTaskByPos(pos) ;
        Snackbar delete = UtilitiesClass.GerSnackForUndo("The Task has Successfully Deleted", binding.AddTaskButton, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        viewModel.Insert(taskEntity);
                        AlarmMSystem.PrePareAlarm(getBaseContext(), taskEntity);
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    viewModel.Delete(taskEntity);
                    AlarmMSystem.CancelAlarm(getBaseContext(), (int) taskEntity.getId());
                    deleted[0] = true ;
                    notificationManager.cancel((int) taskEntity.getId());
                    delete.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog d = builder.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //adapter.submitList(Tasks);
                if (!deleted[0])
                adapter.notifyItemChanged(pos);
            }
        });
        d.show();
    }
    private void PrepareDeleteAllDialog() {
        List<TaskEntity> tasks = new ArrayList<>(Tasks) ;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you Sure you want to this ? ");
        builder.setTitle("You are going to delete All Tasks");
        builder.setIcon(R.drawable.ic_warning);
        Snackbar delete = UtilitiesClass.GerSnackForUndo("All Tasks have been deleted", binding.getRoot(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(TaskEntity Task : tasks)  {
                    viewModel.Insert(Task) ;
                    AlarmMSystem.PrePareAlarm(getBaseContext() , Task);
                }
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.DeleteAllTasks();
                for(TaskEntity Task : tasks)  {
                    AlarmMSystem.CancelAlarm(getBaseContext() , (int) Task.getId());
                }
                notificationManager.cancelAll();
                delete.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog d = builder.create();
        d.show();
    }
    private void prepareSearchView() {
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        if (actionMode!=null) actionMode.finish();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        if (adView!=null)
        adView.destroy();
        mInterstitialAd = null ;
        super.onDestroy();
    }



    private void PrepareActionMode() {
        if (actionMode!=null)  return;
        IsActionMode = true ;
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.action_mode, menu);
                    mode.setTitle( "" + SelectedTasksCount);
                    binding.more.setEnabled(false);
                    menu.findItem(R.id.Recreate).setVisible(false) ;
                    menu.findItem(R.id.FinishTask).setVisible(false);
                    return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.share) {
                    Share_data();
                    mode.finish();
                    return true;
                } else if (itemId == R.id.delete) {
                    PrepareDeleteSelectedTasksDialog();
                    return true;
                } else if (itemId == R.id.All) {
                    if (SelectedTasksCount == Tasks.size())
                        UnSelectAllTasks();
                    else
                        SelectAllTasks();
                    return true;
                } else if (itemId == R.id.FinishTask) {
                    FinishTask();
                    return false;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                 Log.e("ab_do" , "onDestroyActionMode") ;
                 UnSelectAllTasks();
                 IsActionMode = false ;
                 actionMode = null ;
                 binding.more.setEnabled(true);
                 mode.getMenu().findItem(R.id.FinishTask).setVisible(false);
                 binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        }) ;
    }

    private void FinishTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (SelectedTasksCount<2)
        builder.setMessage("Do you want to finish this Task ?") ;
        else builder.setMessage("Do you want to finish these Task ?") ;
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (TaskEntity task : SelectedTasks) {
                        viewModel.Delete(task);
                        AlarmMSystem.CancelAlarm(getBaseContext() , (int) task.getId());
                        FinishedTasksEntity finishedTasksEntity = new FinishedTasksEntity(task.getId(), task.getTitle(), task.getDescription(), task.getDate());
                        viewModel.InsertFinishedTask(finishedTasksEntity);
                        String Msg ;
                        if (SelectedTasksCount<2)
                            Msg = "The Task is finished successfully ";
                        else
                            Msg = "The Tasks are finished successfully" ;
                        Snackbar.make(binding.getRoot() , Msg, Snackbar.LENGTH_SHORT ).show();
                    }
                UnSelectAllTasks();
                ShowTheInterstitialAd();
                }
        });
        builder.create().show();
    }

    private void Share_data() {
        String MimeType = "text/plain";
        StringBuilder builder = new StringBuilder();
        for (TaskEntity taskEntity : SelectedTasks) {
            builder.append("Title :  ").append(taskEntity.getTitle()).append("\n").append("Description :\n")
                    .append(taskEntity.getDescription()).append("\nTime :  ").append(taskEntity.getTimeString(taskEntity.getDate() , this))
                    .append("\nDate :   ").append(taskEntity.getDateString(taskEntity.getDate())).append("\n\n\n");
        }
        ShareCompat.IntentBuilder.from(this)
                .setType(MimeType)
                .setText(builder.toString())
                .startChooser();
//                .getIntent() ;
//        if (IntentShare.resolveActivity(getPackageManager())!=null)
//                 startActivity(IntentShare);
    }

    private void SetSelectedTask(int pos) {
        adapter.getTaskByPos(pos).setSelectedItem(true);
        SelectedTasks.add(adapter.getTaskByPos(pos)) ;
        SelectedTasksCount++ ;
        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            if (SelectedTasksCount==Tasks.size())
                actionMode.getMenu().findItem(R.id.All).setTitle("Un Select All") ;
            else
                actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
            actionMode.getMenu().findItem(R.id.FinishTask).setVisible(!ShouldRemoveFinishIcon());
        }
        adapter.notifyItemChanged(pos);
    }
    private void SetUnSelectedTask(int pos) {
        adapter.getTaskByPos(pos).setSelectedItem(false);
        SelectedTasks.remove(adapter.getTaskByPos(pos)) ;
        SelectedTasksCount-- ;
        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
            actionMode.getMenu().findItem(R.id.FinishTask).setVisible(!ShouldRemoveFinishIcon()) ;
            if (SelectedTasksCount==0) actionMode.finish();
        }
        adapter.notifyItemChanged(pos);
    }
    private void UnSelectAllTasks() {
        for (TaskEntity task : Tasks) {
            task.setSelectedItem(false);
        }
        SelectedTasksCount = 0 ;
        SelectedTasks.clear();
        adapter.notifyDataSetChanged();

        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("Select All");
            actionMode.finish();
        }
        // else --> configuration changed
    }
    private void SelectAllTasks() {
        if (actionMode != null) {
            for (TaskEntity task : Tasks) {
                task.setSelectedItem(true);
            }
            actionMode.getMenu().findItem(R.id.FinishTask).setVisible(!ShouldRemoveFinishIcon()) ;
            SelectedTasksCount = Tasks.size();
            SelectedTasks.clear();
            SelectedTasks.addAll(Tasks);
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("UnSelect All");
            adapter.notifyDataSetChanged();
        }
    }
    private boolean  ShouldRemoveFinishIcon () {
        if (SelectedTasks!= null && SelectedTasks.size()!=0) {
            for (TaskEntity taskEntity : SelectedTasks) {
                  if (taskEntity.isPermanent()) return true ;
            }
        }
        return false ;
    }



    private void PrepareNavigationDrawer() {
        binding.navdrawer.setItemIconTintList(null);
        ((TextView)(binding.navdrawer.getHeaderView(0).findViewById(R.id.navtext))).setTypeface(Typeface.createFromAsset(getAssets() , "fonts/Caveat-Bold.ttf"));
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this , binding.drawerLayout , binding.myToolbar , R.string.open_drawer , R.string.close_drawer ) ;
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        binding.navdrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.FinishedTasks) {
                    startActivity(new Intent(MainActivity.this, FinishedTasksActivity.class));
                    overridePendingTransition(0, R.anim.fade_out);
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    overridePendingTransition(0, R.anim.fade_out);
                    return true;
                } else if (itemId == R.id.Share) {
                    ShareMyApp();
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (itemId == R.id.feedback) {
                    ComposeEmail();
                    //billingUtility.PrePareNoAdsDialog();
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (itemId == R.id.Rate) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    return true;
                } else if (itemId == R.id.RemoveAds) {
                    billingSystem.StartBillingProcess();
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (itemId == R.id.LocationTasks) {
                    startActivity(new Intent(getBaseContext(), LocationTasksActivity.class));
                    overridePendingTransition(0, R.anim.fade_out);

                    return true;
                }
                else if (itemId == R.id.insta) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.instagram.com/beapp_s")));
                    return true ;
                }
                else if (itemId == R.id.twitter) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://mobile.twitter.com/Beapps3")));
                    return true ;
                }
                return false ;
            }
        });
    }
    private void ShareMyApp(){
        String text = "Set your tasks and Organize your Time now With easy and free app install it now ! \nhttp://play.google.com/store/apps/details?id=" + getPackageName() ;
        String MimeType = "text/plain" ;
        ShareCompat.IntentBuilder.from(this)
                .setText(text)
                .setType(MimeType)
                .startChooser();
    }
    private void ComposeEmail(){
        Intent intent = new Intent(Intent.ACTION_SENDTO) ;
        intent.setData(Uri.parse("mailto:")) ;
        intent.putExtra(Intent.EXTRA_EMAIL , new String [] {"bedona213@gmail.com"}) ;
        intent.putExtra(Intent.EXTRA_SUBJECT , "My feedback for (Set my tasks app)");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    private void RateMyApp() {
       View view =  getLayoutInflater().inflate(R.layout.fivestar , binding.getRoot() , false);
        AppRate.with(this)
                .setInstallDays(0)
                .setLaunchTimes(3)
                .setRemindInterval(1)
                .setView(view)
                .monitor();
         AppRate.showRateDialogIfMeetsConditions(this) ;
    }
    @Override
    protected void onResume() {
        binding.AddTaskButton.setEnabled(true);
        binding.AddTaskByLoc.setEnabled(true);
        binding.more.setVisibility(View.VISIBLE);
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        if (!IsActionMode)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onResume();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.OrderTasks))) {
           RemoveOldObserve(OrderValue) ;
           String NewOrder = sharedPreferences.getString(getString(R.string.OrderTasks) , getString(R.string.defaultOrder)) ;
           if (NewOrder!=null) {
               PrepareViewModel(NewOrder);
               OrderValue = NewOrder;
           }
        }
    }
    private void RemoveOldObserve(String orderValue) {
        if (orderValue.equals(getString(R.string.defaultOrder))) {
            viewModel.getTasksDefaultOrder().removeObservers(this);
        }
        else if (orderValue.equals(getString(R.string.AlphabeticallyOrder))) {
            viewModel.getTasksByAlphabeticallyOrder().removeObservers(this);
        }
        else if (orderValue.equals(getString(R.string.DateNtO))) {
            viewModel.getTasksNewestToOldest().removeObservers(this);
        }
        else if (orderValue.equals(getString(R.string.DateOtN))) {
            viewModel.getTasksOldestToNewest().removeObservers(this);
        }
    }
    private void PrepareAlarmAfterBoot() {
        NotificationHelper notificationHelper = new NotificationHelper();
        Repository repository = new Repository((Application) getApplicationContext());
        List<TaskEntity> taskEntities = repository.GetTasksForBoot();
        if (taskEntities==null ) return;
        if (taskEntities.size()==0)  return;
        for (TaskEntity Task : taskEntities) {
            if (!Task.isPermanent() && Task.getDate().after(Calendar.getInstance())) {
                Log.e("Boot", Task.getDescription() + " Not Finished So prepare Alarm");
                AlarmMSystem.PrePareAlarm(this , Task);
            }
            else if (!Task.isPermanent() && Task.getDate().before(Calendar.getInstance())) {
                Log.e("Boot", Task.getDescription() + " Not Finished because Boot");
                notificationHelper.PrepareNotification(this , Task , true);
            }
            else if (Task.isPermanent()) {
                Log.e("Boot", Task.getDescription() + "Permanent");
                notificationHelper.SetNextRepeat(this , Task);
            }
            else Log.e("Boot", Task.getDescription() + "Else !");
        }
    }

    @Override
    public void OnSuccessOperation() {
        SetCelebration();
        RemoveBannerAd();
    }

    private void RemoveBannerAd() {
        if (adView!=null) {
            binding.adViewContainer.removeView(adView);
            adView.destroy();
        }
    }

    private void SetCelebration() {
        Animation animation = AnimationUtils.loadAnimation(getBaseContext() , R.anim.celebration_anim) ;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                   RemoveCelebration();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.celebrate1.setVisibility(View.VISIBLE);
        binding.celebrate2.setVisibility(View.VISIBLE);
        binding.celebrate3.setVisibility(View.VISIBLE);
        binding.celebrate1.setAnimation(animation);
        binding.celebrate2.setAnimation(animation);
        binding.celebrate3.setAnimation(animation);
        PlayClappingSound();
    }

    private void PlayClappingSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext() , R.raw.remove_ads_effect);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("ab_do" , "onCompletion");
                mp.release();

            }
        });
        mediaPlayer.start();
    }

    private void RemoveCelebration() {
        binding.celebrate1.setVisibility(View.GONE);
        binding.celebrate2.setVisibility(View.GONE);
        binding.celebrate3.setVisibility(View.GONE);
    }

    private void ShowTheInterstitialAd() {
        if (mInterstitialAd==null) {
            PrepareInterstitialAd();
            return;
        }
        if (mInterstitialAd.isLoaded()) {
            Log.d("TAG", "The interstitial wasn't loaded .");
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    //!Is Finished
}