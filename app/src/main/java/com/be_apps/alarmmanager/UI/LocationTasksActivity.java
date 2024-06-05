package com.be_apps.alarmmanager.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.be_apps.alarmmanager.Adapters.LocationTasksAdapter;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;
import com.be_apps.alarmmanager.databinding.ActivityLocationTasksBinding;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationTasksActivity extends AppCompatActivity {
    ActivityLocationTasksBinding binding ;
    LocationTasksAdapter locationTasksAdapter ;
    MyViewModel viewModel ;
    GeofencingHelper geofencingHelper ;
    SearchView searchView ;
    List<GeofencesEntity> geofencesList ;
    boolean IsActionMode = false ;
    ActionMode actionMode ;
    int SelectedTasksCount  ;
    List<GeofencesEntity> SelectedTasks ;
    long last_time_click ;
    private UnifiedNativeAd NativeAd ;
    private AdLoader adLoader ;
    Activity activity ;
    InterstitialAd mInterstitialAd ;
    AdsUtilites adsUtilites ;
    AdView adView ;
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
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.delete) {
            if (geofencesList.size() == 0)
                return false;
            PrepareDeleteAllDialog();
            return true;
        } else if (itemId == R.id.search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void PrepareDeleteAllDialog() {
        List<GeofencesEntity> geofencesEntities = new ArrayList<>(geofencesList) ;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you Sure you want to delete All tasks you have");
        builder.setTitle("You are going to delete All Tasks");
        builder.setIcon(R.drawable.ic_warning);
        Snackbar delete = UtilitiesClass.GerSnackForUndo("All Tasks have deleted", binding.getRoot(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(GeofencesEntity entity : geofencesEntities) {
                        viewModel.InsertGeofence(entity);
                        if (!entity.IsFinished())
                        geofencingHelper.AddGeofence(entity);
                }
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.DeleteAllGeofences();
                for (GeofencesEntity entity : geofencesEntities) {
                    if (!entity.IsFinished())
                    UnRegisterTheGeofence(entity);
                }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationTasksBinding.inflate(getLayoutInflater()) ;
        View view = binding.getRoot();
        setContentView(view);
        activity = this ;
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            PrepareInterstitialAd(); // will be shown when (None)
            PrepareBannerAd();
        }
        Init();
        PrepareRecycleView();
        PrepareViewModel();
        //PrepareNativeAd();
    }


    private void PrepareBannerAd() {
        if (adsUtilites == null)
        adsUtilites = new AdsUtilites() ;
        adView = adsUtilites.PrepareBannerAd(this , Constant.BANNER_LOCATION_TASK_ACTIVITY , binding.adViewContainer);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            // refresh the ad to take the new size :
            RemoveBannerAd();
            if (adsUtilites == null) adsUtilites = new AdsUtilites();
            adView = adsUtilites.PrepareBannerAd(this, Constant.BANNER_LOCATION_TASK_ACTIVITY, binding.adViewContainer);
        }
    }

    private void RemoveBannerAd() {
        if (adView!=null) {
            binding.adViewContainer.removeView(adView);
            adView.destroy();
        }
    }

    private void PrepareInterstitialAd() {
        adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(this , Constant.Interstitial_Ad_for_Location_Activity_Id);
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
        //viewModel
        ViewModelProvider viewModelProvider= new ViewModelProvider(this , ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())) ;
        viewModel = viewModelProvider.get(MyViewModel.class) ;
        viewModel.GetAllGeofences().observe(this, new Observer<List<GeofencesEntity>>() {
            @Override
            public void onChanged(List<GeofencesEntity> geofencesEntities) {
                locationTasksAdapter.Full_data = true ;
                locationTasksAdapter.submitList(geofencesEntities);
                geofencesList.clear();
                geofencesList.addAll(geofencesEntities) ;
            }
        });
    }

    private void PrepareRecycleView() {
        locationTasksAdapter = new LocationTasksAdapter(this);
        binding.MyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.MyRecyclerView.setAdapter(locationTasksAdapter);
        locationTasksAdapter.SetOnLongClickListener(new LocationTasksAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnLongClick(int pos) {
                if (!IsActionMode)
                    PrepareActionMode();
                 if (locationTasksAdapter.GetGeofenceByPos(pos).isSelected())
                      SetUnSelectedTask(pos);
                 else SetSelectedTask(pos);
                 return true ;
            }
        });
        locationTasksAdapter.SetOnItemClickListener(new LocationTasksAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos) {
                if (!IsActionMode) {
                    if (SystemClock.elapsedRealtime() - last_time_click < 1000) return;
                    last_time_click = SystemClock.elapsedRealtime() ;
                    GeofencesEntity geofencesEntity = locationTasksAdapter.GetGeofenceByPos(pos);
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    intent.putExtra(Constant.GEOFENCE_TASK, geofencesEntity);
                    startActivity(intent);
                    overridePendingTransition(0 , R.anim.fade_out);
                }
                else {
                    if (locationTasksAdapter.GetGeofenceByPos(pos).isSelected())
                         SetUnSelectedTask(pos);
                    else SetSelectedTask(pos);
                }
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0 , ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                GeofencesEntity entity = locationTasksAdapter.GetGeofenceByPos(viewHolder.getAdapterPosition()) ;
                if (!entity.IsFinished())
                PrepareDeleteDialogForSwipe(viewHolder.getAdapterPosition());
                else viewModel.DeleteGeofence(entity);
            }
        }).attachToRecyclerView(binding.MyRecyclerView);
    }

    private void PrepareActionMode() {
        if (actionMode!=null) return;
        IsActionMode = true ;
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.action_mode , menu);
                mode.setTitle("" + SelectedTasksCount);
                menu.findItem(R.id.FinishTask).setVisible(false) ;
                menu.findItem(R.id.Recreate).setVisible(false);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.All) {
                    if (SelectedTasksCount == geofencesList.size())
                        UnSelectAllTasks();
                    else
                        SelectAllTasks();
                    return true;
                } else if (itemId == R.id.delete) {
                    PrepareDeleteSelectedTasksDialog();
                    return true;
                } else if (itemId == R.id.share) {
                    Share_data();
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
        });
    }

    private void Share_data() {
        StringBuilder builder = new StringBuilder();
        for (GeofencesEntity entity : SelectedTasks) {
            builder.append("Description    ").append(entity.getDescription())
                    .append("\n").append("Address :  ").append(GetAddressFromLocation(entity)).append("\n\n") ;
        }
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(builder.toString())
                .startChooser();
    }

    private String GetAddressFromLocation (GeofencesEntity entity) {
        String title = "UnKnown" ;
        if (!Geocoder.isPresent()) return title ;
        Geocoder geocoder = new Geocoder(this) ;
        LatLng latLng = new LatLng(entity.getLatitude() , entity.getLongitude()) ;
        Address address = null ;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude , latLng.longitude , 1);
            if (addresses!=null && addresses.size()!=0) {
                address = addresses.get(0);
            }
            if (address!=null)
                title = address.getAddressLine(0) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return title ;
    }

    private void PrepareDeleteDialogForSwipe(int pos) {
        final boolean [] deleted = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You Will Not Get A Notification For This Task");
        builder.setTitle("You are going to delete this task");
        GeofencesEntity geofenceEntity = locationTasksAdapter.GetGeofenceByPos(pos) ;
        Snackbar delete = UtilitiesClass.GerSnackForUndo("The Task has Successfully Deleted", binding.getRoot(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.InsertGeofence(geofenceEntity);
                geofencingHelper.AddGeofence(geofenceEntity);
                deleted[0] = false ;
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.DeleteGeofence(geofenceEntity);
                deleted[0] = true ;
                delete.show();
                UnRegisterTheGeofence(geofenceEntity);
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
                    locationTasksAdapter.notifyItemChanged(pos);
            }
        });
        d.show();
    }

    private void UnRegisterTheGeofence(GeofencesEntity geofencesEntity) {
        geofencingHelper.SetOnUnRegisterGeofenceListener(new GeofencingHelper.OnUnRegisterGeofenceListener() {
            @Override
            public void onSuccess() {
                Log.e("ab_do" , "Geofence is removed");
            }

            @Override
            public void onFailure() {
                viewModel.InsertGeofence(geofencesEntity);
                Log.e("ab_do" , "Geofence is not removed");
                Toast.makeText(LocationTasksActivity.this, "There is an error", Toast.LENGTH_SHORT).show();
            }
        });
        geofencingHelper.UnRegisterGeofence(String.valueOf(geofencesEntity.getId()));
    }

    private void Init() {
        setSupportActionBar(binding.myToolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.title.setTypeface(Typeface.createFromAsset(this.getAssets() , "fonts/Calistoga-Regular.ttf"));
        if (getIntent().getAction()!=null && getIntent().getAction().equals(Constant.HINT_LOCATION) && PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(Constant.HINT_LOCATION , true)) {
            // show the hint location dialog :
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setView(getLayoutInflater().inflate(R.layout.location_hint , binding.getRoot() , false))
                    .setPositiveButton("Got it and d`not show me again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // save not to show hint location again :
                            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(Constant.HINT_LOCATION , false).apply();
                        }
                    });
            Dialog dialog = builder.create();
            if (dialog.getWindow()!=null)
            dialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation ;
            dialog.show();
        }
        last_time_click = 0 ;
        SelectedTasksCount = 0 ;
        SelectedTasks = new ArrayList<>() ;
        geofencesList = new ArrayList<>() ;
        IsActionMode = false ; actionMode = null ;
        geofencingHelper = new GeofencingHelper(this) ;
        geofencingHelper.SetonAddGeofenceListener(new GeofencingHelper.OnAddGeofenceListener() {
            @Override
            public void onSuccessAddGeofence() {
                Log.e("ab_do" , "onSuccessAddGeofence") ;
            }

            @Override
            public void onFailureAddGeofence(GeofencesEntity geofencesEntity , Exception e) {
                Log.e("ab_do" , "onFailureAddGeofence") ;
                viewModel.DeleteGeofence(geofencesEntity);
                Toast.makeText(getApplicationContext(), geofencingHelper.GetGeofenceErrorMsg(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void finish() {
        //ShowTheInterstitialAd();
        super.finish();
        overridePendingTransition(0 , R.anim.fade_out);
    }
    @Override
    protected void onDestroy() {
        mInterstitialAd = null ;
        if (NativeAd !=null)
        NativeAd.destroy();
        if (actionMode!=null)
           actionMode.finish();
        if (adView!=null)
            adView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void prepareSearchView() {
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("ab_do" , "onQueryTextSubmit " + query);
                locationTasksAdapter.getFilter().filter(query);
                return false ;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("ab_do" , "onQueryTextChange " + newText);
                locationTasksAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void SetSelectedTask(int pos) {
        locationTasksAdapter.GetGeofenceByPos(pos).setSelected(true);
        SelectedTasks.add(locationTasksAdapter.GetGeofenceByPos(pos)) ;
        SelectedTasksCount++ ;
        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            if (SelectedTasksCount==geofencesList.size())
                actionMode.getMenu().findItem(R.id.All).setTitle("UnSelect All") ;
            else
                actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
        }
        locationTasksAdapter.notifyItemChanged(pos);
    }
    private void SetUnSelectedTask(int pos) {
        locationTasksAdapter.GetGeofenceByPos(pos).setSelected(false);
        SelectedTasks.remove(locationTasksAdapter.GetGeofenceByPos(pos)) ;
        SelectedTasksCount-- ;
        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            if (SelectedTasksCount == 0)
            actionMode.finish();
            else actionMode.getMenu().findItem(R.id.All).setTitle("Select All") ;
        }
        locationTasksAdapter.notifyItemChanged(pos);
    }
    private void UnSelectAllTasks() {
        for (GeofencesEntity geofencesEntity : geofencesList) {
            geofencesEntity.setSelected(false);
        }
        SelectedTasksCount = 0 ;
        SelectedTasks.clear();
        locationTasksAdapter.notifyDataSetChanged();

        if (actionMode!=null) {
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("Select All");
            actionMode.finish();
        }
    }
    private void SelectAllTasks() {
        if (actionMode != null) {
            for (GeofencesEntity geofencesEntity : geofencesList) {
                geofencesEntity.setSelected(true);
            }
            SelectedTasksCount = geofencesList.size();
            SelectedTasks.clear();
            SelectedTasks.addAll(geofencesList);
            actionMode.setTitle("" + SelectedTasksCount);
            actionMode.getMenu().findItem(R.id.All).setTitle("UnSelect All");
            locationTasksAdapter.notifyDataSetChanged();
        }
    }
    private void PrepareDeleteSelectedTasksDialog() {
        List<GeofencesEntity> SelectedTasksList = new ArrayList<>(SelectedTasks) ;
        String title , snack_msg  ;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you Sure you want to do this");
        if (SelectedTasksCount>1) {
            title = "You are going to delete  " + SelectedTasksCount + "  Tasks" ;
            snack_msg = "The Tasks have Successfully Deleted" ;
        }
        else {
            title =  "You are going to delete this task" ;
            snack_msg = "The Task has Successfully Deleted" ;
        }
        builder.setTitle(title);
        Snackbar delete = UtilitiesClass.GerSnackForUndo(snack_msg, binding.getRoot(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ab_do" , " Undo delete Selected tasks " + SelectedTasksList.size()) ;
                for (GeofencesEntity entity : SelectedTasksList) {
                    viewModel.InsertGeofence(entity);
                    if (!entity.IsFinished())
                    geofencingHelper.AddGeofence(entity);
                }
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (GeofencesEntity entity : SelectedTasksList) {
                    viewModel.DeleteGeofence(entity);
                    if (!entity.IsFinished())
                    UnRegisterTheGeofence(entity);
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


}