package com.be_apps.alarmmanager.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Systems.BillingSystem;
import com.be_apps.alarmmanager.Systems.NotificationChannelSystem;
import com.be_apps.alarmmanager.Utilites.AdsUtilites;
import com.be_apps.alarmmanager.databinding.ActivitySettingsBinding;
import com.google.android.gms.ads.InterstitialAd;

public class SettingsActivity extends AppCompatActivity implements BillingSystem.BillingProcess {
    public static final int REQUEST_CODE_PERMISSION = 100;
    public static final int REQUEST_CODE_PICKER_RINGTONE = 200;
    ActivitySettingsBinding binding ;
    static SharedPreferences sharedPreferences ;
    static SharedPreferences.Editor editor ;
    static SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener ;
    InterstitialAd mInterstitialAd ;
    Activity activity ;
    AdsUtilites adsUtilites ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater()) ;
        View view = binding.getRoot() ;
        setContentView(view);
        activity = this ;
        if (!AdsUtilites.IsAdsRemoved(this)) {
            // check if the user has removed the ads first :
            PrepareInterstitialAd(); // will be shown when the user add/update task
            PrepareTopNativeAd();
        }
        Init();
    }


    private void PrepareTopNativeAd() {
          if (adsUtilites==null) adsUtilites = new AdsUtilites();
          adsUtilites.PrepareNativeAd(activity , Constant.NATIVE_AD_ID_FOR_SETTINGS_ACTIVITY , binding.myTemplateContainer
        , binding.myTemplate , null) ;
    }

    private void PrepareInterstitialAd() {
        AdsUtilites adsUtilites = new AdsUtilites();
        mInterstitialAd = adsUtilites.PrepareInterstitialAd(this , Constant.Interstitial_Ad_for_Settings_Activity_Id);
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

    private void Init() {
        setSupportActionBar(binding.myToolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Calistoga-Regular.ttf"));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext()) ;
        editor = sharedPreferences.edit() ;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true ;
        }
        return false ;
    }

    @Override
    public void OnSuccessOperation() {
        SetCelebration();
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

    private void RemoveCelebration() {
        binding.celebrate1.setVisibility(View.GONE);
        binding.celebrate2.setVisibility(View.GONE);
        binding.celebrate3.setVisibility(View.GONE);
    }

    private void PlayClappingSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext() , R.raw.remove_ads_effect);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_screen, rootKey);
            // we must update the summary for each preference form the default shared Preference value
            // as if we did not do that i will take the value that saved in the xml file...
            UpdateSummaryAndSetChangeListener();
            PrepareSharedPreferenceChangeListener();
        }

        private void PrepareSharedPreferenceChangeListener() {
            onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(Constant.TITLE_SOUND)) {
                        Preference Sound = findPreference(getString(R.string.SetSoundKey)) ;
                        if (Sound!=null)
                        Sound.setSummary(sharedPreferences.getString(Constant.TITLE_SOUND , "Default Sound"));
                    }

                }
            } ;
            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        private void UpdateSummaryAndSetChangeListener() {
            Preference SoundPreference = findPreference(getString(R.string.SetSoundKey));
            Preference TTsPreference = findPreference(getString(R.string.TTS_key));
            Preference SSTPreference = findPreference(getString(R.string.STT_key));
            Preference RemindMePreference = findPreference(getString(R.string.RemindMe));
            Preference OrderBy = findPreference(getString(R.string.OrderTasks));
            if (getContext() != null && sharedPreferences==null)
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (SoundPreference != null) {
                SoundPreference.setSummary(sharedPreferences.getString(Constant.TITLE_SOUND, "Default Sound"));
                SoundPreference.setOnPreferenceChangeListener(this);
             }
            if(TTsPreference != null) {
                TTsPreference.setSummary(sharedPreferences.getString(getString(R.string.TTS_key), "Default (English_US)"));
                TTsPreference.setOnPreferenceChangeListener(this);
            }
            if(SSTPreference != null) {
                SSTPreference.setSummary(sharedPreferences.getString(getString(R.string.STT_key), "Default (English_US)"));
                SSTPreference.setOnPreferenceChangeListener(this);
            }
            if (OrderBy != null) {
                OrderBy.setSummary(sharedPreferences.getString(getString(R.string.OrderTasks), "Default"));
                OrderBy.setOnPreferenceChangeListener(this);
            }
            if (RemindMePreference != null) {
                RemindMePreference.setSummary("When You click Remind Me You will Get a notification after " + sharedPreferences.getString(getString(R.string.RemindMe), getString(R.string.min5)) + " min");
                RemindMePreference.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.SetSoundKey))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // We must First Check For Permission :
                    if (getContext()!=null)
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        RequestReadExternalStoragePermission();
                    }
                    else startActivityForResult(GetPickerRingtoneIntent() , REQUEST_CODE_PICKER_RINGTONE);
                }
                else
                    startActivityForResult(GetPickerRingtoneIntent() , REQUEST_CODE_PICKER_RINGTONE);
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.feedback))) {
                 return ComposeEmail();
            }
            else if (preference.getKey().equals(getString(R.string.MoreApps))) {
                MoreAppAction();
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.RemoveAdKey))) {
                RemoveAdsAction();
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.facebook))) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/be.apps.77")));
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.Twitter))) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://mobile.twitter.com/Beapps3")));
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.Instagram))) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/beapp_s")));
                return true;
            }
            else if (preference.getKey().equals(getString(R.string.Youtube))) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://youtube.com/channel/UCjCBHgwNrPReKBhBnQezrTw")));
                return true;
            }
            if(preference.getKey().equals(getString(R.string.vibration))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    DeleteCurrentNotificationChannel(NotificationChannelSystem.GetNotificationChannel(getContext()).getId(),getContext());
                return true ;
            }
            return false;
        }

        private void RemoveAdsAction() {
            BillingSystem billingUtility = new BillingSystem(getActivity() , (ViewGroup) requireView().getRootView());
            billingUtility.AttachListener(getActivity());
            billingUtility.StartBillingProcess();
        }

        private void RequestReadExternalStoragePermission() {
            boolean firstTime = sharedPreferences.getBoolean(Constant.FIRST_TIME_REQUEST_External_Storage_PERMISSION, true);
            if (getActivity()!=null)
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) && !firstTime) {
                GoToSettingsDialog();
            }
            else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }

        private void GoToSettingsDialog() {
            if (getContext()!=null)
            new AlertDialog.Builder(getContext())
                    .setTitle("The Permission is needed First")
                    .setMessage("We need The permission to choose your Ringtone")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            if (getActivity()!=null)
                            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
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

        private void MoreAppAction() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=B_e_d_O"));
            startActivity(intent);
        }

        private boolean ComposeEmail() {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bedona213@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "My feedback for (Set my tasks app)");
            if (getActivity()!=null) {
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.e("ab_do" , "onPreferenceChange " + preference.getKey() + "   " + newValue.toString()) ;
            if (preference.getKey().equals(getString(R.string.TTS_key))) {
                preference.setSummary(newValue.toString());
            }
            if (preference.getKey().equals(getString(R.string.STT_key)))
                preference.setSummary(newValue.toString());

            if (preference.getKey().equals(getString(R.string.RemindMe))) {
                preference.setSummary("When You click Remind Me You will take a notification after " + newValue.toString() + " min");
            }
            if (preference.getKey().equals(getString(R.string.OrderTasks))) {
                preference.setSummary(newValue.toString());
            }
            return true ;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            String oldSound = sharedPreferences.getString(Constant.SOUND_URI , String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)) ;
            if (data!=null) {
                Uri result = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (result!=null) {
                    editor.putString(Constant.TITLE_SOUND, RingtoneManager.getRingtone(getBaseContext(), result).getTitle(this)).commit();
                    editor.putString(Constant.SOUND_URI, result.toString()).commit();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !result.toString().equals(oldSound)) {
                        // The User Select New Sound so we will Remove The channel and Create a new one to Change The Sound :
                        Log.e("ab_do" , "Old " + oldSound);
                        Log.e("ab_do" , "New " + result.toString());
                        DeleteCurrentNotificationChannel(oldSound , getBaseContext());
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void DeleteCurrentNotificationChannel(String Channel_id , Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.deleteNotificationChannel(Channel_id);
        NotificationChannelSystem.notificationChannel = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("ab_do" , "onRequestPermissionsResult") ;
        if (requestCode== REQUEST_CODE_PERMISSION && grantResults.length>0) {
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The user Accept The permission :
                startActivityForResult(GetPickerRingtoneIntent() , REQUEST_CODE_PICKER_RINGTONE);
            }
            else {
                // the user rejected the permission :
                if (sharedPreferences.getBoolean(Constant.FIRST_TIME_REQUEST_External_Storage_PERMISSION, true)) {
                    ShowNeedThePermissionDialog();
                }
            }
            editor.putBoolean(Constant.FIRST_TIME_REQUEST_External_Storage_PERMISSION, false) ;
            editor.commit();
        }
    }

    private void ShowNeedThePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("The Permission is needed First")
                .setMessage("We need The permission to choose your Ringtone")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    protected void onDestroy() {
        Log.e("ab_do" , "onDestroy") ;
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        if (adsUtilites!=null)
        adsUtilites.DestroyTheCurrentNativeAd();
        mInterstitialAd = null ;
        super.onDestroy();
    }

    @Override
    public void finish() {
        ShowTheInterstitialAd();
        super.finish();
        overridePendingTransition(0 , R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public static Intent GetPickerRingtoneIntent() {
        String sound = sharedPreferences.getString(Constant.SOUND_URI , String.valueOf(Settings.System.DEFAULT_NOTIFICATION_URI)) ;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER) ;
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT , true) ;
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT , false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE , RingtoneManager.TYPE_NOTIFICATION) ;
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI , Settings.System.DEFAULT_NOTIFICATION_URI) ;
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI , Uri.parse(sound));
        return intent ;
    }
}