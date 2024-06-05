package com.be_apps.alarmmanager.Systems;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.be_apps.alarmmanager.Broadcasts.MyBroadCast;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;

public class AlarmMSystem {

    public static void PrePareAlarm(Context context , TaskEntity Task) {
        Intent intent = new Intent(context.getApplicationContext(), MyBroadCast.class) ;
        int RequestCode = (int) Task.getId();
        intent.putExtra(Constant.REQUEST_CODE , RequestCode);
        intent.setAction(Constant.PREPARE_NOTIFICATION);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (alarmManager != null)
            // setAlarmClock ( AlarmClockInfo (TriggerTime , ShowIntent) , PendingIntent )
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(Task.getDate().getTimeInMillis(), null), GetPendingIntentOfAlarm(context , intent , RequestCode));
        }
        else {
            // before api level 21 use setExact with RTC_WAKEUP :
            alarmManager.setExact(AlarmManager.RTC_WAKEUP , Task.getDate().getTimeInMillis() , GetPendingIntentOfAlarm(context , intent , RequestCode));
        }
        Log.e("ab_do", "request code on create alarm : " + Task.getId());

    }
    public static void CancelAlarm (Context context , int RequestCode) {
        Intent intent = new Intent(context, MyBroadCast.class) ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(GetPendingIntentOfAlarm( context , intent  , RequestCode));
        Log.e("ab_do" , "request code on cancel alarm : " + RequestCode);
    }
    private static PendingIntent GetPendingIntentOfAlarm(Context context , Intent intent ,  int RequestCode) {
        return PendingIntent.getBroadcast(context.getApplicationContext(),
                RequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
