package com.be_apps.alarmmanager.Utilites;

import androidx.room.TypeConverter;

import java.util.Calendar;

public class ConverterClass {

    @TypeConverter
    public static long toLong (Calendar calendar){
        return calendar.getTimeInMillis() ;
    }
    @TypeConverter
    public static Calendar toCalender(long TimeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeInMillis);
        return calendar ;
    }
}
