package com.be_apps.alarmmanager.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.VibrationSystem;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.GeofencingHelper;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;
import com.be_apps.alarmmanager.Views.DwellGeofenceDialog;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, DwellGeofenceDialog.OnDwellGeofenceDialogClickListener {

    private GoogleMap mMap;
    private WifiManager wifiManager;
    LocationRequest locationRequest;
    int ResolvableApiExceptionConstant = 104;
    Geocoder geocoder;
    LocationCallback locationCallback;
    FusedLocationProviderClient locationProviderClient;
    GoogleApiAvailability googleApiAvailability;
    int ApiAvailability;
    Marker MyLocationMarker;
    Marker SearchMarker;
    int AccessFineLocationPermission = 102;
    TextView textView;
    Activity activity;
    Marker GeofenceMarker;
    Circle GeofenceCircle;
    int AccessBackgroundLocationPermission = 103;
    GeofencingHelper geofencingHelper;
    int RequestCodeForSpeechApi = 101;
    TextView Description;
    BottomSheetDialog bottomSheetDialog;
    SeekBar seekBar;
    CheckBox EnterCheckBox;
    CheckBox ExitCheckBox;
    CheckBox DwellCheckbox;
    TextView DwellTxt;
    int DwellTimeInMilliSecSec;
    int PickerType;
    MyViewModel viewModel;
    String DwellTimeStr;
    boolean IsEditGeofenceTask; // Recreate or update
    GeofencesEntity ReceiveGeofence ;
    Intent ReceiveIntent;
    BottomSheetDialog bottomSheetDialogGeofenceTask;
    Button AddTaskBtn ;
    AppCompatImageView DescriptionMic ;
    LinearLayout InvalidGooglePlayServicesLayout ;
    int PickerVal ;
    String DescriptionTxt ;
    DwellGeofenceDialog dwellGeofenceDialog ;
    boolean IsGeofenceTaskUpdated ;
    boolean IsBackgroundPermission = false ;
    View MapView ;
    private InterstitialAd mInterstitialAd;
    AdsUtilites adsUtilites ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        InitSetup();
        CheckAvailabilityOfGooglePlayServices();
    }

    private void InitSetup() {
        InvalidGooglePlayServicesLayout = findViewById(R.id.InvalidGooglePlayServices);
        activity = this ; setSupportActionBar(findViewById(R.id.Toolbar));
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    private void CheckAvailabilityOfGooglePlayServices() {
        googleApiAvailability = GoogleApiAvailability.getInstance();
        ApiAvailability = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ApiAvailability == ConnectionResult.SUCCESS) {
            // this mean that google play services is available in your device
            if (!AdsUtilites.IsAdsRemoved(this)) // check if the user has removed the ads first :
                PrepareInterstitialAd(); // will be shown when the user add location task or return back
            inti();
            init_Map();
            inti_Places();
            CheckForWifi();
            CreateLocationRequest();
            SetUpSettingsRequest();
        }
        else {
            // this mean that google play services is not available in your device
            SetGooglePlayNotAvailable();
        }
    }

    private void PrepareInterstitialAd() {
        // load the ad so it be ready to shown :
        adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(this , Constant.Interstitial_Ad_for_Map_Activity_Id);
    }


    private void SetGooglePlayNotAvailable() {
        Log.e("ab_do" , "ApiAvailability " + ApiAvailability) ;
        AutocompleteSupportFragment fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (fragment!=null && fragment.getView()!=null)
            fragment.getView().setVisibility(View.GONE);
        InvalidGooglePlayServicesLayout.setVisibility(View.VISIBLE);
        Dialog dialog =  googleApiAvailability.getErrorDialog(this, ApiAvailability, 1, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.e("ab_do" , "onCancel") ;
                finish();
            }
        });
        dialog.show();
    }

    private void PrepareGeofenceTaskInMap() {
        // may be because press on any location task or press on the notification :
        GeofencesEntity geofencesEntity ;
        if (ReceiveIntent.hasExtra(Constant.GEOFENCE_TASK))
            // press on any location task
            geofencesEntity = ReceiveIntent.getParcelableExtra(Constant.GEOFENCE_TASK);
        else
            geofencesEntity = viewModel.GetGeofenceById(ReceiveIntent.getLongExtra(Constant.GEOFENCE_TASK_ID , -1));
        if (geofencesEntity==null) return;
        MoveCameraToTheGeofenceTask(geofencesEntity);
        PrepareSheetDialogForEditGeofenceTask(geofencesEntity);
    }

    private void PrepareSheetDialogForEditGeofenceTask(GeofencesEntity geofencesEntity) {
        if (bottomSheetDialog==null) PrepareAddTaskByLocBottomSheetDialog() ;
        bottomSheetDialogGeofenceTask = bottomSheetDialog ; // prepare the normal bottom sheet dialog
        // bottomSheetDialogGeofenceTask = PrepareAddTaskByLocBottomSheetDialog();
        // then start to edit a few things :
        Description.setText(geofencesEntity.getDescription());
        DwellTimeStr = geofencesEntity.getDwellTimeStr();
        DwellTxt.setText("You will get a notification for your task when you stay in the specified position for " + DwellTimeStr);
        ApplyChangesToCheckBoxes(geofencesEntity);
        Log.e("ab_do" , "Finished task ? " + geofencesEntity.IsFinished());
        if (!geofencesEntity.IsFinished())
            AddTaskBtn.setText(R.string.update_task);
        else AddTaskBtn.setText(R.string.recreate_task);
        AddTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InvalidData()) return;
                AddTaskBtn.setEnabled(false);
                UpdateGeofence(geofencesEntity.getId());
            }
        });
        bottomSheetDialogGeofenceTask.show();
        DwellTimeInMilliSecSec = geofencesEntity.getDwellTime();
    }

    private void ApplyChangesToCheckBoxes(GeofencesEntity geofencesEntity) {
        switch (geofencesEntity.getTransitionType()) {
            case Constant.ENTER_ONLY:
                EnterCheckBox.setChecked(true);
                break;
            case Constant.ENTER_AND_EXIT:
                EnterCheckBox.setChecked(true);
                ExitCheckBox.setChecked(true);
                break;
            case Constant.ENTER_AND_EXIT_AND_DWELL:
                EnterCheckBox.setChecked(true);
                ExitCheckBox.setChecked(true);
                DwellCheckbox.setChecked(true);
                DwellTxt.setVisibility(View.VISIBLE);
                break;
            case Constant.EXIT_ONLY:
                ExitCheckBox.setChecked(true);
                break;
            case Constant.DWELL_ONLY:
                DwellCheckbox.setChecked(true);
                DwellTxt.setVisibility(View.VISIBLE);
                break;
            case Constant.EXIT_AND_DWELL:
                ExitCheckBox.setChecked(true);
                DwellCheckbox.setChecked(true);
                DwellTxt.setVisibility(View.VISIBLE);
                break;
            case Constant.ENTER_AND_DWELL:
                EnterCheckBox.setChecked(true);
                DwellCheckbox.setChecked(true);
                DwellTxt.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void UpdateGeofence(Long id) {
        GeofencesEntity UpdatedGeofence = new GeofencesEntity(GeofenceMarker.getPosition().longitude, GeofenceMarker.getPosition().latitude, Description.getText().toString(), GetTransitionTypeOfGeofence(), GeofenceCircle.getRadius(), DwellTimeInMilliSecSec, DwellTimeStr);
        UpdatedGeofence.setId(id);
        UpdatedGeofence.setIsFinished(false); // important !!!!!
        HandleGeofenceTransition(UpdatedGeofence);
        viewModel.UpdateGeofence(UpdatedGeofence);
        // When two geofences with the same requestId are monitored, the new one will replace the old one
        geofencingHelper.AddGeofence(UpdatedGeofence);
        CheckIfGeofenceTaskUpdated(UpdatedGeofence);

    }

    private void CheckIfGeofenceTaskUpdated(GeofencesEntity UpdatedGeofence) {
        if (ReceiveGeofence==null)  return ;
        IsGeofenceTaskUpdated = !ReceiveGeofence.getDescription().trim().equals(UpdatedGeofence.getDescription().trim())
                || ReceiveGeofence.getDwellTime() != UpdatedGeofence.getDwellTime()
                || ReceiveGeofence.getTransitionType() != UpdatedGeofence.getTransitionType() || ReceiveGeofence.getRadius() != UpdatedGeofence.getRadius();
    }

    public void MoveCameraToTheGeofenceTask(GeofencesEntity geofencesEntity) {
        Log.e("ab_do" , "MoveCameraToTheGeofenceTask") ;
        LatLng latLng = new LatLng(geofencesEntity.getLatitude(), geofencesEntity.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(geofencesEntity.getRadius());
        circleOptions.strokeColor(ContextCompat.getColor(getBaseContext() , R.color.StrokeGeofence));
        circleOptions.fillColor(ContextCompat.getColor(getBaseContext() , R.color.FillColorGeofence));
        circleOptions.strokeWidth(2);
        GeofenceCircle = mMap.addCircle(circleOptions);
        String title = getAddress(latLng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.notification));
        markerOptions.draggable(true);
        GeofenceMarker = mMap.addMarker(markerOptions);
        seekBar.setVisibility(View.VISIBLE);
        int progress = (int) ((geofencesEntity.getRadius()-100)/10);
        seekBar.setProgress(progress);
    }

    private String getAddress(LatLng latLng) {
        Address address = null;
        String title = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() != 0) {
                address = addresses.get(0);
            }
            if (address != null)
                title = address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Done_Map) {
            Log.e("ab_do" , "hhhhh");
            HandleAddLocationTask();
            return true ;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true ;
        }
        if (item.getItemId() == R.id.help) {
            HelpDialog();
            return true ;
        }
        return super.onOptionsItemSelected(item);
    }

    private void HandleAddLocationTask() {
        if (GeofenceMarker == null || GeofenceCircle == null) {
            ShowShouldMarkLocationFirstDialog();
        }

        else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                RequestFineLocationPermission();
                return;
            }
            if (Build.VERSION.SDK_INT >= 29) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    RequestBackgroundLocationPermission();
                    return;
                }
            }

            if (IsEditGeofenceTask || getIntent().hasExtra(Constant.GEOFENCE_TASK_ID)) { // update or recreate
                bottomSheetDialogGeofenceTask.show();
                Log.e("ab_do" , "Update" ) ;

            }
            else {
                // Add Task :
                if (bottomSheetDialog==null) {
                    PrepareAddTaskByLocBottomSheetDialog().show();
                }
                else bottomSheetDialog.show(); // show the same instance of the dialog
            }
        }
    }

    private void ShowShouldMarkLocationFirstDialog() {
        Snackbar.make(findViewById(R.id.root_map), "You should Mark the location of your task first !", Snackbar.LENGTH_LONG)
                .setAction("Need Help", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HelpDialog();
                    }
                }).show();
    }

    private void HelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(LayoutInflater.from(this).inflate(R.layout.help_dialog , null))
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private BottomSheetDialog PrepareAddTaskByLocBottomSheetDialog() {
        Log.e("ab_do" , "PrepareAddTaskByLocBottomSheetDialog") ;
        bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomSheetDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.add_task_by_location_dialog, findViewById(R.id.root_map) , false);
        bottomSheetDialog.setContentView(view);
        DescriptionMic = view.findViewById(R.id.DescriptionMic);
        AddTaskBtn = view.findViewById(R.id.SetTask);
        Description = view.findViewById(R.id.Description);
        EnterCheckBox = view.findViewById(R.id.EnterCheckBox);
        ExitCheckBox = view.findViewById(R.id.ExitCheckBox);
        DwellCheckbox = view.findViewById(R.id.DwellCheckBox);
        DwellTxt = view.findViewById(R.id.DwellTxt);
        ViewGroup TemplateContainer = view.findViewById(R.id.my_template_container);
        TemplateView myTemplate = view.findViewById(R.id.my_template);
        if (!AdsUtilites.IsAdsRemoved(this)) {
            if (adsUtilites == null) adsUtilites = new AdsUtilites();
            adsUtilites.PrepareNativeAd(activity, Constant.NATIVE_AD_ID_FOR_MAP_ACTIVITY, TemplateContainer
                    , myTemplate , null);
        }
        DwellCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DwellCheckbox.isChecked()) {
                    // so show the dialog
                    Log.e("ab_do" , "ff " + DwellTimeInMilliSecSec + " " + PickerType) ;
                    dwellGeofenceDialog  = DwellGeofenceDialog.getInstance(PickerVal, PickerType);
                    dwellGeofenceDialog.show(getSupportFragmentManager(), "dwellGeofenceDialog");
                }
                else {
                    DwellTxt.setVisibility(View.GONE);
                }
            }
        });
        DescriptionMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String language = sharedPreferences.getString(getString(R.string.STT_key), getString(R.string.US));
                PrepareSpeechApI(language);
            }
        });
        AddTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InvalidData()) return; // this mean there is missing data so return from the fun
                AddTaskBtn.setEnabled(false);
                AddGeofence();
            }
        });
        return bottomSheetDialog;
    }

    private boolean InvalidData() {
        if (Description.getText().toString().trim().length() == 0) {
            UtilitiesClass.GetSnackBarForMsg("You should enter a description for your task" , Description)
                    .setActionTextColor(ContextCompat.getColor(getBaseContext() , R.color.PinkColor))
                    .show();
            VibrationSystem.SetVibrateAction(getBaseContext());
            return true;
        }
        if (!EnterCheckBox.isChecked() && !ExitCheckBox.isChecked() && !DwellCheckbox.isChecked()) {
            // the user did not select any option :
            UtilitiesClass.GetSnackBarForMsg("You should Select at least one option for your task" , Description)
                    .setActionTextColor(ContextCompat.getColor(getBaseContext() , R.color.PinkColor))
                    .show();
            VibrationSystem.SetVibrateAction(getBaseContext());
            return true;
        }
        return false;
    }

    private void PrepareSpeechApI(String language) {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() != 0) {
            // this intent can be handled
            Log.e("ab_do", "PrepareSpeechApI");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak .. ");
            if (language.equals(getString(R.string.US))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            } else if (language.equals(getString(R.string.UK))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-UK");
            } else if (language.equals(getString(R.string.AR))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar");
            } else if (language.equals(getString(R.string.GE))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de");
            } else if (language.equals(getString(R.string.Ch))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
            } else if (language.equals(getString(R.string.IT))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT");
            }
            startActivityForResult(intent, RequestCodeForSpeechApi);
        } else {
            // this intent cannot be handled
            UtilitiesClass.GetSnackBarForMsg("This Features is not allowed in your device" , Description)
                    .setActionTextColor(ContextCompat.getColor(getBaseContext() , R.color.PinkColor))
                    .show();
        }
    }

    private int GetTransitionTypeOfGeofence() {
        if (EnterCheckBox.isChecked() && ExitCheckBox.isChecked() && DwellCheckbox.isChecked())
            return Constant.ENTER_AND_EXIT_AND_DWELL;
        else if (EnterCheckBox.isChecked() && !ExitCheckBox.isChecked() && !DwellCheckbox.isChecked())
            return Constant.ENTER_ONLY;
        else if (EnterCheckBox.isChecked() && !ExitCheckBox.isChecked() && DwellCheckbox.isChecked())
            return Constant.ENTER_AND_DWELL;
        else if (EnterCheckBox.isChecked() && ExitCheckBox.isChecked() && !DwellCheckbox.isChecked())
            return Constant.ENTER_AND_EXIT;
        else if (!EnterCheckBox.isChecked() && ExitCheckBox.isChecked() && DwellCheckbox.isChecked())
            return Constant.EXIT_AND_DWELL;
        else if (!EnterCheckBox.isChecked() && ExitCheckBox.isChecked() && !DwellCheckbox.isChecked())
            return Constant.EXIT_ONLY;
        else if (!EnterCheckBox.isChecked() && !ExitCheckBox.isChecked() && DwellCheckbox.isChecked())
            return Constant.DWELL_ONLY ;
        return Constant.ENTER_ONLY ;
    }

    private void AddGeofence() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && (Build.VERSION.SDK_INT > 29 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return ;
        }
        GeofencesEntity geofencesEntity = new GeofencesEntity(GeofenceMarker.getPosition().longitude, GeofenceMarker.getPosition().latitude, Description.getText().toString(), GetTransitionTypeOfGeofence(), GeofenceCircle.getRadius(), DwellTimeInMilliSecSec
                , DwellTimeStr) ;
        geofencesEntity.setIsFinished(false);
        HandleGeofenceTransition(geofencesEntity);
        long id = viewModel.InsertGeofence(geofencesEntity);
        geofencesEntity.setId(id);
        geofencingHelper.AddGeofence(geofencesEntity);
    }

    private void HandleGeofenceTransition(GeofencesEntity geofencesEntity) {
        switch (geofencesEntity.getTransitionType()) {
            case Constant.ENTER_ONLY :
                geofencesEntity.setShowEnterTransition(true);
                break;
            case Constant.ENTER_AND_EXIT :
                geofencesEntity.setShowEnterTransition(true);
                geofencesEntity.setShowExitTransition(true);
                break;
            case Constant.ENTER_AND_EXIT_AND_DWELL :
                geofencesEntity.setShowEnterTransition(true);
                geofencesEntity.setShowDwellTransition(true);
                geofencesEntity.setShowExitTransition(true);
                break;
            case Constant.EXIT_ONLY :
                geofencesEntity.setShowExitTransition(true);
                break;
            case Constant.DWELL_ONLY :
                geofencesEntity.setShowDwellTransition(true);
                break;
            case Constant.EXIT_AND_DWELL :
                geofencesEntity.setShowDwellTransition(true);
                geofencesEntity.setShowExitTransition(true);
                break;
            case Constant.ENTER_AND_DWELL:
                geofencesEntity.setShowEnterTransition(true);
                geofencesEntity.setShowDwellTransition(true);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void RequestBackgroundLocationPermission() {
        Log.e("ab_do" , "RequestBackgroundLocationPermission") ;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean FirstTime = sharedPreferences.getBoolean(Constant.BACKGROUND_LOCATION_PERMISSION_First_Time, true);
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) && !FirstTime) {
            IsBackgroundPermission = true ;
            ShowGoToSettingsDialog();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, AccessBackgroundLocationPermission);
        }
        editor.putBoolean(Constant.BACKGROUND_LOCATION_PERMISSION_First_Time, false);
        editor.apply();
    }

    private void RequestFineLocationPermission() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean FirstTime = sharedPreferences.getBoolean(Constant.FINE_LOCATION_PERMISSION_First_Time, true);
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && !FirstTime) {
            // this mean the user click don`t remind me again
            IsBackgroundPermission = false ;
            ShowGoToSettingsDialog();
        }
        else
            ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.ACCESS_FINE_LOCATION} , AccessFineLocationPermission);
        editor.putBoolean(Constant.FINE_LOCATION_PERMISSION_First_Time, false);
        editor.apply();
    }

    private void ShowGoToSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("The Permission is needed First")
                .setMessage(!IsBackgroundPermission ? "We need The permission to Access your Location on the map " : " We need The permission fisrt to add location tasks" )
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS , Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void init_Map() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            MapView = mapFragment.getView();
        }
    }
    private void StartRequestUpdate() {
        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    private void inti() {
        DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("dwellGeofenceDialog");
        if (dialogFragment!=null && dialogFragment.isAdded())
            dialogFragment.dismiss();
        InvalidGooglePlayServicesLayout.setVisibility(View.GONE);
        viewModel = new MyViewModel(getApplication());
        ReceiveIntent = getIntent();
        IsEditGeofenceTask = ReceiveIntent.hasExtra(Constant.GEOFENCE_TASK); // this mean we are in the update activity
        if (IsEditGeofenceTask)
            ReceiveGeofence = ReceiveIntent.getParcelableExtra(Constant.GEOFENCE_TASK) ;
        if (ReceiveIntent.hasExtra(Constant.GEOFENCE_TASK_ID))
            ReceiveGeofence = viewModel.GetGeofenceById(ReceiveIntent.getLongExtra(Constant.GEOFENCE_TASK_ID , -1));
        geocoder = new Geocoder(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        PickerVal = -1 ;
        PickerType = -1 ;
        DwellTimeInMilliSecSec = -1 ;
        DwellTimeStr = "" ;
        DescriptionTxt = "" ;
        textView = findViewById(R.id.title);
        textView.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Calistoga-Regular.ttf"));
        seekBar = findViewById(R.id.seekbar) ;
        viewModel = new MyViewModel(getApplication());
        geofencingHelper = new GeofencingHelper(this) ;
        geofencingHelper.SetonAddGeofenceListener(new GeofencingHelper.OnAddGeofenceListener() {
            @Override
            public void onSuccessAddGeofence() {
                // the geofence has been registered in the system
                if (IsEditGeofenceTask || getIntent().hasExtra(Constant.GEOFENCE_TASK_ID)) {
                    // may be update or recreate by click on the task (IsEditGeofenceTask)
                    // or click on the notification (hasExtra)
                    if (!ReceiveGeofence.IsFinished()) {
                        // Update :
                        if (IsGeofenceTaskUpdated)
                            Toast.makeText(getApplicationContext(), "The Task has successfully Updated", Toast.LENGTH_LONG).show();
                    }
                    else // Recreate
                        Toast.makeText(getApplicationContext(), "The Task has successfully Re Created", Toast.LENGTH_LONG).show();
                    if (IsEditGeofenceTask) { // not from notification :
                        if (!AdsUtilites.IsAdsRemoved(getBaseContext())) // check if the user has removed the ads first :
                            ShowTheInterstitialAd();
                    }
                }
                else { // Add
                    Toast.makeText(getApplicationContext(), "The Task has successfully added", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getBaseContext(), LocationTasksActivity.class).setAction(Constant.HINT_LOCATION));
                    overridePendingTransition(0 , R.anim.fade_out);
                    if (!AdsUtilites.IsAdsRemoved(getBaseContext())) // check if the user has removed the ads first :
                        ShowTheInterstitialAd();
                }
                finish();
                Log.e("ab_do" , "onSuccessAddGeofence") ;
            }

            @Override
            public void onFailureAddGeofence(GeofencesEntity geofencesEntity , Exception e) {
                Log.e("ab_do" , "onFailureAddGeofence") ;
                viewModel.DeleteGeofence(geofencesEntity);
                UtilitiesClass.GetSnackBarForMsg(geofencingHelper.GetGeofenceErrorMsg(e) , Description)
                        .setActionTextColor(ContextCompat.getColor(getBaseContext() , R.color.PinkColor))
                        .show();
                AddTaskBtn.setEnabled(true);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double radius = 100 + (progress*10) ;
                if (GeofenceCircle!=null)
                    GeofenceCircle.setRadius(radius);
                Log.e("ab_do" , "Radius : " + radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("ab_do" , "locationResult == null" ) ;
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location!=null) {
                    SetUserLocation(location);
                }
            }
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (!locationAvailability.isLocationAvailable()) {
                    Log.e("ab_do" , "onNotLocationAvailability") ;
                    Snackbar.make(findViewById(R.id.root_map) , "There is an error please check your location Settings", Snackbar.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void ShowTheInterstitialAd() {
        if (mInterstitialAd==null) return;
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("MyApp", "The interstitial wasn't loaded yet.");
        }
    }


    private void CheckForWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            RequestToOpenTheWifi();
        }
    }
    private void RequestToOpenTheWifi() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("For best Accuracy Please turn on your wifi") ;
        alertDialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT < 29) {
                    boolean x = wifiManager.setWifiEnabled(true);
                    Log.e("ab_do" , "The system turn on wifi " + x) ;
                }
                else {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    startActivityForResult(panelIntent , 100);
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.create().show();
    }
    private void inti_Places() {
        Places.initialize(this , getString(R.string.maps_api_key));
        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteSupportFragment==null) return;
        autocompleteSupportFragment.setTypeFilter(TypeFilter.ADDRESS);
        List<Place.Field> fields = new ArrayList<>() ;
        fields.add(Place.Field.LAT_LNG);
        autocompleteSupportFragment.setPlaceFields(fields) ;
        autocompleteSupportFragment.setHint("Search");
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.e("ab_do" , "onPlaceSelected") ;
                MoveTheMapToSpecificPlace(place.getLatLng());
            }

            @Override
            public void onError(@NonNull Status status) {
                if (!status.getStatus().equals(Status.RESULT_CANCELED))
                    Snackbar.make(findViewById(R.id.root_map) ,status.getStatusMessage()!=null ? status.getStatusMessage() : "There is an error" , Snackbar.LENGTH_LONG).show();
            }
        });
        
    }
    private void CreateLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setMaxWaitTime(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void SetUpSettingsRequest() {
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
        List<LocationRequest> locationRequests = new ArrayList<>();
        locationRequests.add(locationRequest);
        locationRequests.add(new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY));
        locationSettingsRequestBuilder.addAllLocationRequests(locationRequests);
        locationSettingsRequestBuilder.setAlwaysShow(true);
        locationSettingsRequestBuilder.setNeedBle(true) ;
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> settingsResponseTask = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        settingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                LocationSettingsResponse response = null;
                try {
                    response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    LocationSettingsStates locationSettingsStates = response.getLocationSettingsStates();
                    Log.e("ab_do", "isNetworkLocationPresent " + locationSettingsStates.isNetworkLocationPresent());
                    Log.e("ab_do", "isNetworkLocationUsable " + locationSettingsStates.isNetworkLocationUsable());
                    Log.e("ab_do", "isGpsPresent " + locationSettingsStates.isGpsPresent());
                    Log.e("ab_do", "isGpsUsable " + locationSettingsStates.isGpsUsable());
                    Log.e("ab_do", "isLocationPresent " + locationSettingsStates.isLocationPresent());
                    Log.e("ab_do", "isLocationUsable " + locationSettingsStates.isLocationUsable());
                    Log.e("ab_do", "isBlePresent " + locationSettingsStates.isBlePresent());
                    Log.e("ab_do", "isBleUsable " + locationSettingsStates.isBleUsable());
                }
            }
        });
        settingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("ab_do", "onSuccess");
                // start request update ..
                StartRequestUpdate();
            }
        });
        settingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (((ApiException) e).getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Snackbar.make(findViewById(R.id.root_map) , "There is an error please check your location Settings", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (e instanceof ResolvableApiException) {
                    Log.e("ab_do", "OnFailureListener " + ((ResolvableApiException) e).getStatusCode());
                    try { // show the user dialog to enable location :
                        ((ResolvableApiException) e).startResolutionForResult(MapsActivity.this, ResolvableApiExceptionConstant);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.e("ab_do", ex.toString());
                        Snackbar.make(findViewById(R.id.root_map) , "There is an error please check your location Settings", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    private void StopRequestUpdate() {
        if (locationProviderClient!=null)
            locationProviderClient.removeLocationUpdates(locationCallback) ;
    }
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    private void SetUserLocation(Location location)  {
        //Log.e("ab_do" , "SetUserLocation\n" + location.toString()) ;
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude()) ;
        String title = getAddress(latLng);
        if (MyLocationMarker == null) {
            // zoom to user location did`nt be called
            MarkerOptions markerOptions = new MarkerOptions() ;
            markerOptions.position(latLng);
            markerOptions.title(title);
            if (!IsEditGeofenceTask && !getIntent().hasExtra(Constant.GEOFENCE_TASK_ID) && SearchMarker == null) // Add Task and the user didn`t search in specific place
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            MyLocationMarker = mMap.addMarker(markerOptions) ;
        }
        else {
            MyLocationMarker.setPosition(latLng);
            MyLocationMarker.setTitle(title);
        }
    }
    private void ZoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = locationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) return;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(getAddress(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                MyLocationMarker = mMap.addMarker(markerOptions);
            }
        }) ;
    }
    private void MoveTheMapToSpecificPlace(LatLng latLng) {
        if (SearchMarker==null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.search)) ;
            SearchMarker = mMap.addMarker(markerOptions) ;
        }
        else
            SearchMarker.setPosition(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , 17));
    }
    @Override
    public void onBackPressed() {
        if (!AdsUtilites.IsAdsRemoved(getBaseContext())) // check if the user has removed the ads first :
            ShowTheInterstitialAd();
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onDestroy() {
        StopRequestUpdate();
        mInterstitialAd = null ;
        if (adsUtilites!=null)
            adsUtilites.DestroyTheCurrentNativeAd();
        super.onDestroy();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("ab_do" , "onMapReady ") ;
        mMap = googleMap;
        mMap.setPadding(0 , 16 , 0 , 0);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            AccessAfterGranted();
        }
        else {
            AlertDialog dialog =  new AlertDialog.Builder(this).setView(LayoutInflater.from(this).inflate(R.layout.location_note, findViewById(R.id.root_map), false)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RequestFineLocationPermission();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
    }
    private void AccessAfterGranted() {
        // When you press on the notification
        if (bottomSheetDialog==null) {
            PrepareAddTaskByLocBottomSheetDialog();
        }
        if (IsEditGeofenceTask || ReceiveIntent.hasExtra(Constant.GEOFENCE_TASK_ID)) {
            enableUserLocation();
            PrepareGeofenceTaskInMap() ;
            return ;
        }
        enableUserLocation();
        ZoomToUserLocation();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ab_do" , "onActivityResult") ;
        if (requestCode == ResolvableApiExceptionConstant) {
            if (resultCode==RESULT_OK) {
                Log.e("ab_do" , "i accept");
                StartRequestUpdate();
            }
            else {
                SetUpSettingsRequest();
            }
        }
        if (requestCode == RequestCodeForSpeechApi) {
            Log.e("ab_do" , "RequestCodeForSpeechApi") ;
            if (resultCode == RESULT_OK) {
                Log.e("ab_do", "Result Ok");
                ArrayList<String> ResultTextArray;
                ResultTextArray = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
                if (Description!=null)
                    Description.setText(ResultTextArray!=null ? ResultTextArray.get(0) : "");
            }
            else
                Toast.makeText(activity, "Please Try again", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==AccessFineLocationPermission) {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
                AccessAfterGranted();
            else {
                new AlertDialog.Builder(this).setMessage("We need the Permission to display your location On The Map")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestFineLocationPermission();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        }
        if (requestCode==AccessBackgroundLocationPermission) {
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                new AlertDialog.Builder(this).setMessage("We need the Permission (Allow All Time) first to start Adding location tasks so that the app can detect your Location Tasks and send a notification" +
                        " of your tasks even if the app was closed or not in use")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    RequestBackgroundLocationPermission();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        }
    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        HandleGeofenceMarker(latLng);
        HandleGeofenceCircle(latLng);
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
        VibrationSystem.SetVibrateAction(getBaseContext());
    }
    private void HandleGeofenceCircle(LatLng latLng) {
        if (GeofenceCircle==null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.radius(100) ;
            circleOptions.strokeColor(ContextCompat.getColor(getBaseContext() , R.color.StrokeGeofence));
            circleOptions.fillColor(ContextCompat.getColor(getBaseContext() , R.color.FillColorGeofence));
            circleOptions.strokeWidth(2) ;
            GeofenceCircle = mMap.addCircle(circleOptions);
        }
        else
            GeofenceCircle.setCenter(latLng);
    }
    private void HandleGeofenceMarker(LatLng latLng) {
        String title = getAddress(latLng);
        if (GeofenceMarker==null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(title);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.notification)) ;
            markerOptions.draggable(true);
            GeofenceMarker = mMap.addMarker(markerOptions);
        }
        else {
            GeofenceMarker.setPosition(latLng);
            GeofenceMarker.setTitle(title);
        }
    }
    @Override
    public void onMarkerDragStart(Marker marker) {

    }
    @Override
    public void onMarkerDrag(Marker marker) {
        GeofenceCircle.setCenter(marker.getPosition());
    }
    @Override
    public void onMarkerDragEnd(Marker marker) {}
    @Override
    public void SetDialogClick(int number, int type) {
        PickerType = type ;
        PickerVal = number ;
        DwellTimeInMilliSecSec = number ;
        DwellTxt.setVisibility(View.VISIBLE);
        String Type = getDwellTxtStr(type , number);
        DwellTxt.setText(Type);
        SetDwellTimeInMilliSec(number , type);
    }

    private String getDwellTxtStr(int type , int number) {
        String Type = "" ;
        switch (type) {
            case Constant.PICKER_MIN :
                Type = "Minutes" ;
                break;
            case Constant.PICKER_HOUR :
                Type = "Hours" ;
                break;
            case Constant.PICKER_DAY :
                Type = "Days" ;
                break;
        }
        DwellTimeStr = number + " " + Type ;
        return "You will get a notification for your task when you stay in the specified Location for " + number + " " + Type ;
    }

    @Override
    public void CancelDialog() {
        DwellCheckbox.setChecked(false);
        DwellTxt.setVisibility(View.GONE);
    }


    private void SetDwellTimeInMilliSec(int num , int type) {
        switch (type) {
            case Constant.PICKER_MIN :
                DwellTimeInMilliSecSec = 60000*num ;
                break;
            case Constant.PICKER_HOUR :
                DwellTimeInMilliSecSec = 3600000*num ;
                break;
            case Constant.PICKER_DAY :
                DwellTimeInMilliSecSec = 86400000*num ;
                break;
        }
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0 , R.anim.fade_out);
    }
}