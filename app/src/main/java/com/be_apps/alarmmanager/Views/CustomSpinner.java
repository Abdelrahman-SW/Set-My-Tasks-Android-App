package com.be_apps.alarmmanager.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatSpinner;

import com.be_apps.alarmmanager.Constant;

public class CustomSpinner extends AppCompatSpinner {

    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position) {
        Log.e("ab_do" , "setSelection") ;
        // before calling super.setSelection(pos) getSelectedItemPosition() will get me the previous selection
        boolean sameSelected = (position == getSelectedItemPosition());
        super.setSelection(position);
        // By The default implementation if i selected the same position the
        //listener will not be notified so i override this implementation to be notified :
        if (sameSelected && position == Constant.OTHER) {
            if (getOnItemSelectedListener()!=null)
            getOnItemSelectedListener().onItemSelected(null , null , position , 0);
        }
    }
}
