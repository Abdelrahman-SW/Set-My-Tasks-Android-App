package com.be_apps.alarmmanager.Utilites;

import android.view.View;

import androidx.room.Ignore;

import com.be_apps.alarmmanager.Constant;
import com.google.android.material.snackbar.Snackbar;

public class UtilitiesClass {

    public  static String GetRepeatText(int selection , int PickerValue , int PickerTypeValue) {
        if (selection== Constant.OTHER) {
            return "every " + PickerValue + " " + GetPickerDisplayName(PickerTypeValue) ;
        }
        else {
            switch (selection) {
                case Constant.ONCE_PER_DAY:
                    return "Once Per Day";
                case Constant.ONCE_PER_WEEK:
                    return "Once Per Week";
                case Constant.ONCE_PER_MONTH:
                    return "Once Per Month";
                case Constant.ONCE_PER_YEAR:
                    return "Once Per Year";
                default:
                    return "";
            }
        }
    }
    @Ignore
    private static String GetPickerDisplayName (int PickerTypeValue) {
        switch (PickerTypeValue) {
            case Constant.PICKER_MIN :
                return "Min" ;
            case Constant.PICKER_DAY:
                return "Day" ;
            case Constant.PICKER_HOUR :
                return "Hour" ;
            case Constant.PICKER_WEEK :
                return "Week" ;
            case Constant.PICKER_MONTH :
                return "Month" ;
            case Constant.PICKER_YEAR :
                return "Year" ;
            default: return "" ;
        }
    }
    public static Snackbar GerSnackForUndo(String txt , View view , View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(view , txt , Snackbar.LENGTH_LONG) ;
        snackbar.setAction("Undo" , listener) ;
        return snackbar ;
    }
    public static Snackbar GetSnackBarForMsg (String txt , View view) {
        Snackbar snackbar = Snackbar.make(view , txt , Snackbar.LENGTH_LONG );
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        return snackbar ;
    }
}
