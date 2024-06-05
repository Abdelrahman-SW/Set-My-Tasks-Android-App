package com.be_apps.alarmmanager.Systems;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationSystem {

    public static void SetVibrateAction(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(100 , VibrationEffect.DEFAULT_AMPLITUDE));
        else vibrator.vibrate(100);
    }
}
