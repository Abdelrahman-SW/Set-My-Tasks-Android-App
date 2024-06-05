package com.be_apps.alarmmanager.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.be_apps.alarmmanager.Adapters.AdapterForFinishedTasks;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;
import com.be_apps.alarmmanager.databinding.ActivityFinishedTasksBinding;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FinishedTasksActivity extends AppCompatActivity {

     ActivityFinishedTasksBinding binding ;
     AdapterForFinishedTasks adapter ;
     SearchView searchView ;
     MyViewModel viewModel ;
     List<FinishedTasksEntity> finishedTaskEntities = new ArrayList<>() ;
     ActionMode actionMode ;
     boolean IsActionMode = false ;
     ArrayList<FinishedTasksEntity> SelectedTasks = new ArrayList<>() ;
     Activity activity ;
     InterstitialAd mInterstitialAd ;
     int SelectedTasksCount = 0 ;
     AdsUtilites adsUtilites ;
     AdView adView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinishedTasksBinding.inflate(getLayoutInflater()) ;
        View view  = binding.getRoot() ;
        setContentView(view);
        activity = this ;
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            PrepareInterstitialAd(); // will be shown when (None)
            PrepareBannerAd();
        }
        Init();
        PrepareRecycleView() ;
        PrepareViewModel();
    }

    private void PrepareInterstitialAd() {
        AdsUtilites adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(this , Constant.Interstitial_Ad_for_Finished_Task_Activity_Id);
    }

    private void ShowTheInterstitialAd() {
        if (mInterstitialAd==null) return;
        if (mInterstitialAd.isLoaded()) {
            Log.d("TAG", "The interstitial wasn't loaded .");
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }



    private void PrepareViewModel() {
        viewModel = new ViewModelProvider(this , ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(MyViewModel.class) ;
        viewModel.GetFinishedTasks().observe(this , FinishedTasks -> {
            adapter.Full_data = true ;
            adapter.submitList(FinishedTasks);
            finishedTaskEntities = FinishedTasks ;
        });
    }

    private void Init() {
        setSupportActionBar(binding.myToolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.title.setTypeface(Typeface.createFromAsset(this.getAssets() , "fonts/Calistoga-Regular.ttf"));
    }

    private void PrepareRecycleView() {
        adapter = new AdapterForFinishedTasks(this) ;
        binding.MyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.MyRecyclerView.setAdapter(adapter);
        adapter.setOnLongItemClickListener(new AdapterForFinishedTasks.onLongItemClickListener() {
            @Override
            public boolean onLongClick(int pos) {
                if (!IsActionMode)
                    PrepareActionMode();
                if (adapter.getTaskByPos(pos).isSelectedItem())
                     SetUnSelectedTask(pos);
                else SetSelectedTask(pos);
                return true ;
            }
        });
        adapter.setOnItemClickListener(new AdapterForFinishedTasks.onItemClickListener() {
            @Override
            public void onClick(int pos) {
                if (IsActionMode) {
                    if (adapter.getTaskByPos(pos).isSelectedItem())
                        SetUnSelectedTask(pos);
                    else
                        SetSelectedTask(pos);
                }
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0 , ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false ;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                 viewModel.DeleteFinishedTask(adapter.getTaskByPos(viewHolder.getAdapterPosition()));
            }
        }).attachToRecyclerView(binding.MyRecyclerView);
    }

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
            if (finishedTaskEntities.size() == 0) return false;
            PrepareDeleteAllDialog();
            return true;
        }
        else if (itemId == android.R.id.home) {
            finish();
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    private void PrepareDeleteAllDialog() {
        List<FinishedTasksEntity> tasks = new ArrayList<>(finishedTaskEntities) ;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete All Finished Tasks ?");
        Snackbar delete = UtilitiesClass.GerSnackForUndo("All Tasks have deleted", binding.getRoot(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(FinishedTasksEntity Task : tasks)  {
                    viewModel.InsertFinishedTask(Task) ;
                }
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.DeleteAllFinishedTasks();
                ShowTheInterstitialAd();
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
                Log.e("ab_do" , "Change filter " + newText);
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
    private void PrepareActionMode() {
        if (actionMode!=null)  return;
        IsActionMode = true ;
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                Log.e("ab_do", "onCreateActionMode");
                mode.getMenuInflater().inflate(R.menu.action_mode, menu);
                menu.findItem(R.id.Recreate).setVisible(SelectedTasksCount<2) ;
                mode.setTitle( "" + SelectedTasksCount);
                menu.findItem(R.id.FinishTask).setVisible(false) ;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Log.e("ab_do" , "onPrepareActionMode");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Log.e("ab_do" , "onActionItemClicked");
                int itemId = item.getItemId();
                if (itemId == R.id.share) {
                    Share_data();
                    mode.finish();
                    return true;
                } else if (itemId == R.id.delete) {
                    for (FinishedTasksEntity taskEntity : SelectedTasks) {
                        viewModel.DeleteFinishedTask(taskEntity);
                    }
                    mode.finish();
                    return true;
                } else if (itemId == R.id.All) {
                    if (SelectedTasksCount == finishedTaskEntities.size())
                        UnSelectAllTasks();
                    else
                        SelectAllTasks();
                    return true;
                } else if (itemId == R.id.Recreate) {
                    ReCreateTask();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.e("ab_do" , "onDestroyActionMode");
                UnSelectAllTasks();
                IsActionMode = false ;
                actionMode = null ;
            }
        }) ;
    }
    private void SetSelectedTask(int pos) {
        adapter.getTaskByPos(pos).setSelectedItem(true);
        SelectedTasks.add(adapter.getTaskByPos(pos)) ;
        SelectedTasksCount++ ;
        if (actionMode!=null) {
            actionMode.getMenu().findItem(R.id.Recreate).setVisible(SelectedTasksCount < 2);
            actionMode.setTitle("" + SelectedTasksCount);
            if (SelectedTasksCount==finishedTaskEntities.size())
                actionMode.getMenu().findItem(R.id.All).setTitle("Un Select All") ;
            else
                actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
        }
        adapter.notifyItemChanged(pos);
    }
    private void SetUnSelectedTask(int pos) {
        adapter.getTaskByPos(pos).setSelectedItem(false);
        SelectedTasks.remove(adapter.getTaskByPos(pos)) ;
        SelectedTasksCount-- ;
        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.Recreate).setVisible(SelectedTasksCount<2) ;
            actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
            if (SelectedTasksCount==0) actionMode.finish();
        }

        adapter.notifyItemChanged(pos);
    }
    private void UnSelectAllTasks() {
        for (FinishedTasksEntity task : finishedTaskEntities) {
            task.setSelectedItem(false);
        }
        SelectedTasksCount = 0 ;
        SelectedTasks.clear();
        adapter.notifyDataSetChanged();

        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().getItem(0).setTitle("Select All");
            actionMode.finish();
        }
        // else --> configuration changed
    }
    private void SelectAllTasks() {
        if (actionMode != null) {
            actionMode.getMenu().findItem(R.id.Recreate).setVisible(false) ;
            for (FinishedTasksEntity task : finishedTaskEntities) {
                task.setSelectedItem(true);
            }
            SelectedTasksCount = finishedTaskEntities.size();
            SelectedTasks.clear();
            SelectedTasks.addAll(finishedTaskEntities);
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("UnSelect All");
            adapter.notifyDataSetChanged();
        }
    }
    private void Share_data() {
        String MimeType = "text/plain";
        StringBuilder builder = new StringBuilder();
        for (FinishedTasksEntity taskEntity : SelectedTasks) {
            builder.append("Title :  ").append(taskEntity.getTitle()).append("\n").append("Description :\n")
                    .append(taskEntity.getDescription()).append("\nTime :  ").append(taskEntity.getTimeString(taskEntity.getDate() , this))
                    .append("\nDate :   ").append(taskEntity.getDateString(taskEntity.getDate())).append("\n\n\n");
        }
//        Intent IntentShare =
        ShareCompat.IntentBuilder.from(this)
                .setType(MimeType)
                .setText(builder.toString())
                .startChooser();

//                .getIntent() ;
//        if (IntentShare.resolveActivity(getPackageManager())!=null)
//                 startActivity(IntentShare);
    }
    private void ReCreateTask() {
        FinishedTasksEntity Selected_task = SelectedTasks.get(0) ;
        TaskEntity taskEntity = new TaskEntity() ;
        taskEntity.setTitle(Selected_task.getTitle());
        taskEntity.setDescription(Selected_task.getDescription());
        Intent intent = new Intent(FinishedTasksActivity.this , EditActivity.class) ;
        intent.putExtra(Constant.FINISHED_TASK , taskEntity) ;
        intent.putExtra(Constant.SELECTED_TASK , Selected_task) ;
        startActivity(intent);
        overridePendingTransition(0 , R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mInterstitialAd = null ;
        if (adView!=null)
            adView.destroy();
        super.onDestroy();
    }

    private void PrepareBannerAd() {
        if (adsUtilites == null)
            adsUtilites = new AdsUtilites() ;
        adView = adsUtilites.PrepareBannerAd(this , Constant.BANNER_FINISH_TASK_ACTIVITY , binding.adViewContainer);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            // refresh the ad to take the new size :
            RemoveBannerAd();
            if (adsUtilites == null) adsUtilites = new AdsUtilites();
            adView = adsUtilites.PrepareBannerAd(this, Constant.BANNER_FINISH_TASK_ACTIVITY, binding.adViewContainer);
        }
    }

    private void RemoveBannerAd() {
        if (adView!=null) {
            binding.adViewContainer.removeView(adView);
            adView.destroy();
        }
    }

    @Override
    public void finish() {
        //ShowTheInterstitialAd();
        super.finish();
        overridePendingTransition(0 , R.anim.fade_out);
    }
}