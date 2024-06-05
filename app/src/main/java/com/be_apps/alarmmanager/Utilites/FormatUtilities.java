package com.be_apps.alarmmanager.Utilites;

import android.content.Context;
import android.text.format.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FormatUtilities {
    public static String FormatToDate(Calendar calendar) {
       return java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(calendar.getTime());
    }
    public static String FormatToTime(Calendar calendar , Context context) {
        SimpleDateFormat format ;
        if (!DateFormat.is24HourFormat(context))
        format = new SimpleDateFormat("h:mm  a");
        else
        format = new SimpleDateFormat("HH:mm  ");
        return format.format(calendar.getTime()) ;
    }
}
