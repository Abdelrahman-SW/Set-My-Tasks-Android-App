package com.be_apps.alarmmanager.UI;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.AlarmMSystem;
import com.be_apps.alarmmanager.Systems.VibrationSystem;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.Utilites.FormatUtilities;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;
import com.be_apps.alarmmanager.Views.DatePickerFragment;
import com.be_apps.alarmmanager.Views.RepeatChooserDialog;
import com.be_apps.alarmmanager.Views.TimePickerFragment;
import com.be_apps.alarmmanager.databinding.ActivityEditBinding;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class EditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener , TimePickerDialog.OnTimeSetListener, RepeatChooserDialog.onDialogClickListener {
    public static final String PICKER_VAL = "PickerVal";
    public static final String PICKER_TYPE = "PickerType";
    public static final String IS_OTHER_SET = "IsOtherSet";
    public static final String IS_TIME_SET = "IsTimeSet";
    public static final String IS_DATE_SET = "IsDateSet";
    ActivityEditBinding binding;
    Typeface typeface;
    Typeface typeface2;
    boolean TimeSet = false;
    boolean DateSet = false;
    Calendar calendar;
    Intent ReceiveIntent;
    boolean EditActivity = false;
    boolean ReCreateActivity = false ;
    int Repeat_value = -1;
    int Repeat_value_type = -1;
    boolean OtherSet = false;
    String title , SelectedSpinnerText ;
    TaskEntity ReceiveTask;
    TaskEntity UpdatedTask;
    private final int  RESULT_CODE_FOR_TITLE = 100;
    private final int RESULT_CODE_FOR_Description = 101;
    SharedPreferences sharedPreferences ;
    RepeatChooserDialog dialogRepeat ;
    MyViewModel viewModel ;
    InterstitialAd mInterstitialAd ;
    Activity activity ;
    ViewGroup AdViewInDialog ;
    AlertDialog ConfirmAddTaskDialog ;
    AdsUtilites adsUtilites , adsUtilites2 , adsUtilites3 ;
    private ViewGroup DialogAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        activity = this ;
        Init();
        PrepareActivity();
        createConfirmAddTaskDialog();
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            PrepareInterstitialAd(); // will be shown when the user add/update task
            PrepareNativeAdInConfirmDialog() ;
            PrepareTopNativeAd();
            PrepareBottomNativeAd();
        }
    }

    private void PrepareNativeAdInConfirmDialog() {
        if (AdViewInDialog==null)
        AdViewInDialog = (ViewGroup) getLayoutInflater().inflate(R.layout.ad_in_dialog , binding.getRoot() , false);
        TemplateView templateView = AdViewInDialog.findViewById(R.id.my_template);
        adsUtilites3 = new AdsUtilites();
        adsUtilites3.PrepareNativeAd(activity , Constant.NATIVE_AD_ID_FOR_DIALOG_EDIT_ACTIVITY , AdViewInDialog , templateView , DialogAd) ;
    }

    private void PrepareBottomNativeAd() {
        adsUtilites2 = new AdsUtilites() ;
        adsUtilites2.PrepareNativeAd(activity , Constant.NATIVE_AD_ID_FOR_BOTTOM_EDIT_ACTIVITY , binding.myTemplateContainer2
                , binding.myTemplate2 , null) ;
    }

    private void PrepareTopNativeAd() {
        adsUtilites = new AdsUtilites() ;
        adsUtilites.PrepareNativeAd(activity , Constant.NATIVE_AD_ID_FOR_TOP_EDIT_ACTIVITY , binding.myTemplateContainer
                , binding.myTemplate , null) ;
    }


    private void PrepareInterstitialAd() {
        AdsUtilites adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(this , Constant.Interstitial_Ad_for_Edit_Activity_Id);
    }

    private void PrepareActivity() {
        if (ReceiveIntent.hasExtra(Constant.FINISHED_TASK)) {
            // ReCreate Finished Task Activity :
            PrepareReCreateFinishedTaskActivity();
        }
        else if (ReceiveIntent.hasExtra(Constant.TASK)) {
            //Edit Task Activity :
            PrepareEditTaskActivity();
        }
        else {
            // Add Task Activity :
            PrepareAddTaskActivity();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.e("ab_do" , "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }



    private void PrepareReCreateFinishedTaskActivity() {
                        binding.title.setText("ReCreate Task");
                        binding.SetTask.setText("ReCreate Task");
                        ReCreateActivity = true ;
                        ReceiveTask = (ReceiveIntent.getParcelableExtra(Constant.FINISHED_TASK));
                        try {
                            if (ReceiveTask!=null)
                                UpdatedTask = (TaskEntity) ReceiveTask.clone();
                        } catch (CloneNotSupportedException e) {
                            Log.e("ab_do", "Exception");
                        }
                        calendar = Calendar.getInstance() ;
                        populateView();
                    }

                    private void PrepareAddTaskActivity() {
                        binding.title.setText("Add Task");
                        calendar = Calendar.getInstance();
                    }

                    private void PrepareEditTaskActivity() {
                        EditActivity = true;
                        binding.title.setText("Edit Task");
                        binding.SetTask.setText("Update Task");
                        ReceiveTask = ReceiveIntent.getParcelableExtra(Constant.TASK);
                        try {
                            if (ReceiveTask!=null)
                                UpdatedTask = (TaskEntity) ReceiveTask.clone();
                        } catch (CloneNotSupportedException e) {
                            Log.e("ab_do", "Exception");
                        }
                        populateView();
                    }

                    private void Init() {
                        setSupportActionBar(binding.myToolbarEdit);
                        if (getSupportActionBar()!=null) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                        }
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext()) ;
                        if (getIntent()!=null)
                            ReceiveIntent = getIntent() ;
                        setupSpinner();
                        setListenersToButtons();
                        setTypeface();
                    }

                    private void setListenersToButtons() {
                        binding.TitleMic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String language = sharedPreferences.getString(getString(R.string.STT_key) , getString(R.string.US)) ;
                                PrepareSpeechApI(RESULT_CODE_FOR_TITLE , language);
                            }
                        });
                        binding.DescriptionMic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String language = sharedPreferences.getString(getString(R.string.STT_key) , getString(R.string.US)) ;
                                PrepareSpeechApI(RESULT_CODE_FOR_Description , language);
                            }
                        });
                        binding.btnTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //polymorphism :
                                DialogFragment timePickerFragment = TimePickerFragment.getInstance(calendar);
                                timePickerFragment.show(getSupportFragmentManager(), "Time");
                            }
                        });
                        binding.btnDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogFragment datePickerFragment = DatePickerFragment.getInstance(calendar);
                                datePickerFragment.show(getSupportFragmentManager(), "Date");
                            }
                        });
                        binding.SetTask.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (EditActivity && !CheckIfTaskDataChanged()) {
                                    if (IsValidDate()) finish();
                                    else
                                        UtilitiesClass.GetSnackBarForMsg("The date is over please Enter Coming Date !", binding.SetTask).show();
                                    return;
                                }
                                if (TimeSet && DateSet) {
                                    if (IsValidDate()) {
                                        if (binding.Description.getText().toString().trim().length() == 0) {
                                            UtilitiesClass.GetSnackBarForMsg("You should enter a description For your Task !", binding.getRoot()).show();
                                            VibrationSystem.SetVibrateAction(getBaseContext());
                                            return;
                                        }
                                        if (binding.Title.getText().toString().trim().length() > 15) {
                                            UtilitiesClass.GetSnackBarForMsg("You title should be not more than 15 characters !", binding.getRoot()).show();
                                            VibrationSystem.SetVibrateAction(getBaseContext());
                                            return;
                                        }
                                        ShowConfirmAddTaskDialog();
                                    }
                                    else {
                                        UtilitiesClass.GetSnackBarForMsg("The date is over please Enter Coming Date !", binding.getRoot()).show();
                                        VibrationSystem.SetVibrateAction(getBaseContext());
                                    }
                                } else {
                                    UtilitiesClass.GetSnackBarForMsg("Please Select The Time/Date First !", binding.getRoot()).show();
                                    VibrationSystem.SetVibrateAction(getBaseContext());
                }
            }
        });
        binding.Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.Time.setText("None");
                binding.Description.setText("");
                binding.Date.setText("None");
                binding.Title.setText("");
                TimeSet = DateSet = OtherSet =  false;
                binding.Myspinner.setSelection(Constant.NO_REPEAT);
                calendar = Calendar.getInstance();
            }
        });
    }


    private void setTypeface() {
        typeface = Typeface.createFromAsset(this.getAssets(), "fonts/Caveat-Bold.ttf");
        typeface2 = Typeface.createFromAsset(getAssets() ,"fonts/Calistoga-Regular.ttf");
        binding.Reset.setTypeface(typeface2, Typeface.BOLD);
        binding.SetTask.setTypeface(typeface2, Typeface.BOLD);
        binding.AboutHeader.setTypeface(typeface2);
        binding.moreOptionsHeader.setTypeface(typeface2);
        binding.TimingHeader.setTypeface(typeface2);
        binding.titleHeader.setTypeface(typeface2);
        binding.DescriptionHeader.setTypeface(typeface2);
        binding.repeatHeader.setTypeface(typeface2);
        binding.timeHeader.setTypeface(typeface2);
        binding.dateHeader.setTypeface(typeface2);
        binding.title.setTypeface(typeface2);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ActionForBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        DateSet = true;
        binding.Date.setText(FormatUtilities.FormatToDate(calendar));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        TimeSet = true;
        binding.Time.setText(FormatUtilities.FormatToTime(calendar , this));
    }

    public void createConfirmAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String Date = FormatUtilities.FormatToDate(calendar);
        String time = FormatUtilities.FormatToTime(calendar , this);
        //builder.setMessage("With Date : " + Date + "\n" + "Time :  " + time );
        View dialog_view =  LayoutInflater.from(getBaseContext()).inflate(R.layout.addtaskdialog , null);
        ((TextView)dialog_view.findViewById(R.id.with_date)).setTypeface(typeface2);
        ((TextView)dialog_view.findViewById(R.id.with_time)).setTypeface(typeface2);
        ((TextView)dialog_view.findViewById(R.id.date_txt)).setTypeface(typeface2);
        ((TextView)dialog_view.findViewById(R.id.time_txt)).setTypeface(typeface2);
        ((TextView)dialog_view.findViewById(R.id.date_txt)).setText(Date);
        ((TextView)dialog_view.findViewById(R.id.time_txt)).setText(time);
        builder.setView(dialog_view);
        if (EditActivity) builder.setTitle("You are going to Update The Task");
        else
        builder.setTitle("You are going to add a new Task");
        builder.setIcon(R.drawable.ic_baseline_info_24);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HandleConfirmClick();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        DialogAd =  dialog_view.findViewById(R.id.ad_conatiner);
        if(AdsUtilites.IsAdsRemoved(getBaseContext())) DialogAd.setVisibility(View.GONE);
        ConfirmAddTaskDialog = builder.create();
    }

    public void ShowConfirmAddTaskDialog() {
        if (ConfirmAddTaskDialog==null) createConfirmAddTaskDialog();
        ConfirmAddTaskDialog.show();
    }

    private void HandleConfirmClick() {
        if (EditActivity) {
            UpdateTask();
        }
        else // Add activity or Recreate :
        CreateNewTask();
        // if From widget : you should go to the mainActivity not to the launcher
        // else will return you to the mainActivity By Default
        if (ReceiveIntent.getAction()!=null && ReceiveIntent.getAction().equals(Constant.IS_FROM_WIDGET)) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            overridePendingTransition(0, R.anim.fade_out);
        }
        finish();
        if (!AdsUtilites.IsAdsRemoved(this)) // check if the user has removed the ads first :
        ShowTheInterstitialAd();

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

    private void CreateNewTask() {
        TaskEntity task = new TaskEntity() ;
        title = GetTitle(binding.Title.getText().toString().trim());
        task.setTitle(title);
        task.setDescription(binding.Description.getText().toString().trim());
        task.setSelection(binding.Myspinner.getSelectedItemPosition());
        if (OtherSet) {
            task.setNumberPickerValue(Repeat_value);
            task.setPickerTypeValue(Repeat_value_type);
        }
        task.setPermanent(binding.Myspinner.getSelectedItemPosition()!= Constant.NO_REPEAT);
        task.setDate(calendar);
        task.setSelectedItem(false);
        viewModel = new MyViewModel(getApplication()) ;
        long id = viewModel.Insert(task);
        Log.e("ab_do" , "Id " + id) ;
        task.setId(id);
        AlarmMSystem.PrePareAlarm(getBaseContext() , task);
        if (ReCreateActivity)
            ReCreateTaskAction();
    }

    private void ReCreateTaskAction() {
        FinishedTasksEntity finishedTasksEntity = ReceiveIntent.getParcelableExtra(Constant.SELECTED_TASK);
        viewModel.DeleteFinishedTask(finishedTasksEntity);
    }

    private boolean IsValidDate() {
        return (!calendar.before(Calendar.getInstance()));
    }

    private void UpdateTask() {
        title = GetTitle(binding.Title.getText().toString().trim());
        UpdatedTask.setTitle(title);
        UpdatedTask.setDescription(binding.Description.getText().toString().trim());
        UpdatedTask.setSelection(binding.Myspinner.getSelectedItemPosition());
        if (OtherSet) {
            UpdatedTask.setNumberPickerValue(Repeat_value);
            UpdatedTask.setPickerTypeValue(Repeat_value_type);
        }
        UpdatedTask.setDate(calendar);
        UpdatedTask.setSelectedItem(false);
        UpdatedTask.setPermanent(binding.Myspinner.getSelectedItemPosition()!=Constant.NO_REPEAT);
        viewModel = new MyViewModel((Application) getApplicationContext()) ;
        viewModel.Update(UpdatedTask);
        AlarmMSystem.PrePareAlarm(getBaseContext() , UpdatedTask);
    }

    private void populateView() {
        if (ReceiveTask!=null) {
            if (!ReceiveTask.getTitle().equals("None"))
                title = ReceiveTask.getTitle();

            else title = "";
            binding.Title.setText(title);
            binding.Description.setText(ReceiveTask.getDescription());
        }
        if (EditActivity) {
            PopulateViewForEditActivity();
        }
    }


    private void PopulateViewForEditActivity() {
        calendar = (Calendar) ((Calendar) Objects.requireNonNull(ReceiveIntent.getSerializableExtra(Constant.CALENDER))).clone();
        binding.Myspinner.setSelection(ReceiveTask.getSelection());
        binding.Time.setText(ReceiveTask.getTimeString(calendar , this));
        binding.Date.setText(ReceiveTask.getDateString(calendar));
        TimeSet = DateSet = true;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.EntriesForRepeat, R.layout.spinnerlayout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.Myspinner.setAdapter(adapter);
        // to prevent calling onItemSelected twice and only call one time from the system :
        binding.Myspinner.setSelection(0 , false);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() ;
        binding.Myspinner.setOnTouchListener(spinnerInteractionListener);
        binding.Myspinner.setOnItemSelectedListener(spinnerInteractionListener);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("ab_do" , "onSaveInstanceState") ;
        super.onSaveInstanceState(outState);
        outState.putInt(PICKER_VAL, Repeat_value);
        outState.putInt(PICKER_TYPE, Repeat_value_type);
        outState.putSerializable(Constant.CALENDER , calendar);
        outState.putBoolean(IS_OTHER_SET, OtherSet);
        outState.putBoolean(IS_TIME_SET, TimeSet);
        outState.putBoolean(IS_DATE_SET, DateSet);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e("ab_do" , "onRestoreInstanceState") ;
        Repeat_value = savedInstanceState.getInt(PICKER_VAL  , -1) ;
        Repeat_value_type = savedInstanceState.getInt(PICKER_TYPE  , -1) ;
        calendar = (Calendar) savedInstanceState.getSerializable(Constant.CALENDER);
        OtherSet = savedInstanceState.getBoolean(IS_OTHER_SET) ;
        TimeSet = savedInstanceState.getBoolean(IS_TIME_SET) ;
        DateSet = savedInstanceState.getBoolean(IS_DATE_SET) ;
        if (TimeSet)
        binding.Time.setText(FormatUtilities.FormatToTime(calendar , this));
        if (DateSet)
        binding.Date.setText(FormatUtilities.FormatToDate(calendar));
    }

    private void createRepeatDialog() {
        int pickerVal, PickerValType;
        if (EditActivity&&!OtherSet && ReceiveTask.getSelection()==Constant.OTHER) {
            pickerVal = ReceiveTask.getNumberPickerValue();
            PickerValType = ReceiveTask.getPickerTypeValue();
        }
        else {
            pickerVal = Repeat_value;
            PickerValType = Repeat_value_type;
        }
        dialogRepeat = RepeatChooserDialog.getInstance(pickerVal , PickerValType);

        // Add the same dialog (for example while configuration) will cause an error as you want to add the same fragment
        // on the screen ( the fragment has already added ) so we add this check :
        if (!dialogRepeat.isAdded())
        dialogRepeat.show(getSupportFragmentManager(), "Repeat");
    }

    @Override
    public void SetDialogClick(int number, int type) {
        Repeat_value = number;
        Repeat_value_type = type;
        OtherSet = true;
        SelectedSpinnerText = UtilitiesClass.GetRepeatText(Constant.OTHER, number, type) ;
        ((TextView) binding.Myspinner.getSelectedView()).setText(SelectedSpinnerText) ;
    }

    @Override
    public void CancelDialogClick() {
        Log.e("ab_do" , "CancelDialogClick") ;
        if (OtherSet) {
            ((TextView) binding.Myspinner.getSelectedView()).setText(UtilitiesClass.GetRepeatText(Constant.OTHER, Repeat_value, Repeat_value_type));
            return;
        }
        else if (EditActivity && (ReceiveTask.getSelection() == Constant.OTHER)) {
            ((TextView) binding.Myspinner.getSelectedView()).setText(UtilitiesClass.GetRepeatText(Constant.OTHER, ReceiveTask.getNumberPickerValue(), ReceiveTask.getPickerTypeValue()));
            return;
        }
        if (EditActivity)
             binding.Myspinner.setSelection(ReceiveTask.getSelection());
        else binding.Myspinner.setSelection(Constant.NO_REPEAT);

    }

    private boolean CheckIfTaskDataChanged() {
        Calendar TimeAfterUpdate = (Calendar) calendar.clone();
        Calendar TimeBeforeUpdate = (Calendar) (((Calendar) Objects.requireNonNull(ReceiveIntent.getSerializableExtra(Constant.CALENDER)))).clone();
        String title = GetTitle(binding.Title.getText().toString().trim());
        return (!title.equals(ReceiveTask.getTitle()) ||
                !binding.Description.getText().toString().trim().equals(ReceiveTask.getDescription()) ||
                TimeAfterUpdate.compareTo(TimeBeforeUpdate) != 0 ||
                binding.Myspinner.getSelectedItemPosition() != ReceiveTask.getSelection() || OtherSet);
    }

    private String GetTitle(String title_field) {
        if (title_field.length() == 0)
            return "None";
        else
            return title_field;
    }

    private void ShowQuitWithoutSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Any information you have been entered will be lost");
        builder.setTitle("Are you Sure ?");
        builder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    //if (!AdsUtilites.IsAdsRemoved(activity)) // check if the user has removed the ads first :
                    //ShowTheInterstitialAd();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        ActionForBackPressed();
    }

    private void ActionForBackPressed() {
        if ((EditActivity && CheckIfTaskDataChanged()))
            ShowQuitWithoutSavingDialog();
        else if (!EditActivity && IsAnyDataEntered())
            ShowQuitWithoutSavingDialog();
        else {
               //if (!AdsUtilites.IsAdsRemoved(activity)) // check if the user has removed the ads first :
               // ShowTheInterstitialAd();
            finish();
            super.onBackPressed();
        }
    }

    private boolean IsAnyDataEntered() {
        return (binding.Title.getText().toString().trim().length() != 0 || binding.Description.getText().toString().trim().length()
                != 0 || TimeSet || DateSet || OtherSet);
    }

    private void PrepareSpeechApI(int RequestCode , String language) {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() != 0) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT , "Speak . . . ") ;
            //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            if (language.equals(getString(R.string.US))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE , "en-US") ;
            }
            else if (language.equals(getString(R.string.UK))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE , "en-UK") ;
            }
            else if (language.equals(getString(R.string.AR))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar");
            }
            else if (language.equals(getString(R.string.GE))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE , "de") ;
            }
            else if (language.equals(getString(R.string.Ch))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE , "zh-CN") ;
            }
            else if (language.equals(getString(R.string.IT))) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE , "it-IT") ;
            }
            startActivityForResult(intent, RequestCode);
        }
        else {
            Snackbar.make(binding.getRoot(), "Sorry , This Feature is not allowed in your device", Snackbar.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> ResultTextArray;
        if (resultCode == RESULT_OK) {
            ResultTextArray = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
            if (requestCode == RESULT_CODE_FOR_TITLE) {
                binding.Title.setText(ResultTextArray != null ? ResultTextArray.get(0) : "");
            }
            else if (requestCode == RESULT_CODE_FOR_Description) {
                binding.Description.setText(ResultTextArray != null ? ResultTextArray.get(0) : "");
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0 , R.anim.fade_out);
    }

    public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        boolean userSelect = false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.e("ab_do" ,  "onTouch") ;
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Your selection handling code here
                Log.e("ab_do", "onItemSelected " + pos);
            switch (pos) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    OtherSet = false;
                    break;
                case 5:
                    if (EditActivity) {
                        // Edit Activity
                        if (!userSelect) {
                            // Configuration Changes
                            ((TextView) binding.Myspinner.getSelectedView()).setText(UtilitiesClass.GetRepeatText(Constant.OTHER,
                                    ReceiveTask.getNumberPickerValue()
                                    , ReceiveTask.getPickerTypeValue()));
                            return;
                        }
                        else
                            createRepeatDialog();
                    }
                    else
                        if (userSelect)
                        createRepeatDialog();
                    else {
                        // Configuration Changes
                        ((TextView) binding.Myspinner.getSelectedView()).setText(UtilitiesClass.GetRepeatText(Constant.OTHER, Repeat_value
                                , Repeat_value_type));
                    }
                    break;
            }
            userSelect = false;
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    protected void onDestroy() {
        Log.d("MyApp" ,"onDestroyEditActivity" );
        mInterstitialAd = null ;
        if (adsUtilites!=null)
        adsUtilites.DestroyTheCurrentNativeAd();
        if (adsUtilites2!=null)
        adsUtilites2.DestroyTheCurrentNativeAd();
        if (adsUtilites3!=null)
        adsUtilites3.DestroyTheCurrentNativeAd();
        super.onDestroy();
    }
}
