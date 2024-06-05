package com.be_apps.alarmmanager.Systems;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;

import java.util.Locale;

public class TTsServices extends Service implements TextToSpeech.OnInitListener {
    private static TextToSpeech textToSpeech ;
    private String Content_to_speech ;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("ab_do" , "onStartCommand") ;
        if (intent.getStringExtra(Constant.TEXT_TO_SPEECH) == null) {
            // this mean we start this services to stop any playing voice ( when the notification is deleted )
            Log.e("ab_do" , "StopSpeakIntent") ;
            stopSelf(); // this will Destroy the service
            return START_STICKY ;
        }
        if (textToSpeech!=null) {
            // stop any playing voice before speak the next
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        textToSpeech = new TextToSpeech(this , this) ;
        Content_to_speech = intent.getStringExtra(Constant.TEXT_TO_SPEECH) ;
        return START_NOT_STICKY ;
    }

    @Override
    public void onDestroy() {
        Log.e("ab_do" , "onDestroyServices") ;
        if (textToSpeech!=null) {
            Log.e("ab_do" , "Shutdown") ;
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null ;
        }
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(this.getApplicationContext(), "Please Wait  . .", Toast.LENGTH_SHORT).show();
            Log.e("ab_do" , "onInit") ;
            SetLanguageToTTS();
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.e("ab_do", "onStart");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.e("ab_do", "onDone");
                    stopSelf();
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e("ab_do", "onError");
                    stopSelf();
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    super.onError(utteranceId, errorCode);
                    stopSelf();
                }
            });
            Log.e("ab_do" , "TextToSpeech  " + Content_to_speech) ;
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
            int Result = textToSpeech.speak(Content_to_speech , TextToSpeech.QUEUE_FLUSH , params , "UNIQUE ID") ;
            if (Result!=TextToSpeech.SUCCESS)
                Toast.makeText(this.getApplicationContext() , "Please Try Again" , Toast.LENGTH_LONG).show();
        }
        else Toast.makeText(this.getApplicationContext() , "Please Try Again" , Toast.LENGTH_LONG).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void SetLanguageToTTS () {
        String language = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.TTS_key) , getString(R.string.UK));
        if (textToSpeech==null)
        textToSpeech = new TextToSpeech(this , this) ;
        int result = TextToSpeech.LANG_NOT_SUPPORTED;
        if (language!=null) {
            if (language.equals(this.getString(R.string.US))) {
                result = textToSpeech.setLanguage(Locale.forLanguageTag("en-US"));
            } else if (language.equals(this.getString(R.string.UK))) {
                result = textToSpeech.setLanguage(Locale.UK);
            } else if (language.equals(this.getString(R.string.AR))) {
                result = textToSpeech.setLanguage(Locale.forLanguageTag("ar"));
            } else if (language.equals(this.getString(R.string.GE))) {
                result = textToSpeech.setLanguage(Locale.forLanguageTag("de"));
            } else if (language.equals(this.getString(R.string.Ch))) {
                result = textToSpeech.setLanguage(Locale.forLanguageTag("zh-CN"));
            } else if (language.equals(this.getString(R.string.IT))) {
                result = textToSpeech.setLanguage(Locale.forLanguageTag("it-IT"));
            }
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Error with language");
            } else {
                Log.e("TTS", "Success with language");
            }
        }
    }
}
