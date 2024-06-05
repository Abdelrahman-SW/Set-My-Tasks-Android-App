package com.be_apps.alarmmanager.Utilites;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayServicesUtils {

    public static boolean IfGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int apiAvailability = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return apiAvailability == ConnectionResult.SUCCESS;
    }
}
