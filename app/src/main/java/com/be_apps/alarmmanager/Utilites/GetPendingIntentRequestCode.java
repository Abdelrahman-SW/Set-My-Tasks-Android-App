package com.be_apps.alarmmanager.Utilites;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;

import com.be_apps.alarmmanager.Constant;

public class GetPendingIntentRequestCode {

    public static int GetRequestCode (Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int Request_Code = sharedPreferences.getInt(Constant.PENDING_INTENT_REQUEST_CODE , 0) ;
        sharedPreferences.edit().putInt(Constant.PENDING_INTENT_REQUEST_CODE , (Request_Code+1) ).apply();
        Log.e("ab_do" , "Get Request Code -- > " + Request_Code) ;
        return Request_Code ;
    }
}
