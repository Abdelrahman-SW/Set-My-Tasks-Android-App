package com.be_apps.alarmmanager;

public interface Constant {
    String DELETE_NOTIFICATION = "DeleteNotification" ;
    String PLAY_SOUND = "playSound" ;
    int NO_REPEAT = 0 ;
    int ONCE_PER_DAY = 1 ;
    int ONCE_PER_WEEK = 2 ;
    int ONCE_PER_MONTH = 3 ;
    int ONCE_PER_YEAR = 4 ;
    int OTHER = 5 ;
    String TITLE_SOUND = "title_sound" ;
    String CALENDER = "calender";
    String FIRST_TIME_REQUEST_External_Storage_PERMISSION = "f_request" ;
    String SOUND_URI = "SetSound" ;
    String TEXT_TO_SPEECH = "txttospeech";
    int PICKER_YEAR = 5 ;
    int PICKER_DAY = 2 ;
    int PICKER_MONTH = 4 ;
    int PICKER_WEEK = 3 ;
    int PICKER_HOUR = 1 ;
    int PICKER_MIN = 0 ;
    String CONTENT = "content" ;
    String ACTION_FINISH_KEY = "Action_finish" ;
    String REQUEST_CODE = "REQUEST_CODE" ;
    String IS_GIVE_ME_MIN_ACTION = "GiveMeFiveMin" ;
    String FINISHED_TASK  = "FinishedTask" ;
    String TASK = "task" ;
    String RemindMeVal = "RemindMe" ;
    String IsFirstTimeOpenTheApp = "FinishedIntroScreen";
    String FINE_LOCATION_PERMISSION_First_Time = "FineLocationPermission";
    String BACKGROUND_LOCATION_PERMISSION_First_Time = "BackgroundLocationPermission" ;
    int ENTER_ONLY = 0 ;
    int EXIT_ONLY = 1 ;
    int DWELL_ONLY = 2 ;
    int ENTER_AND_EXIT = 3 ;
    int ENTER_AND_DWELL = 4 ;
    int EXIT_AND_DWELL = 5 ;
    int ENTER_AND_EXIT_AND_DWELL = 6 ;
    String GEOFENCE_TASK = "GeofenceTask" ;
    String SELECTED_TASK = "Selected_Task" ;
    String PENDING_INTENT_REQUEST_CODE = "pendingIntentRequestCode";
    String IS_FROM_WIDGET = "FromWidget" ;
    String GEOFENCE_TASK_ID = "GeoID";
    String PREPARE_NOTIFICATION = "PrepareNotification" ;
    String HINT_LOCATION = "Hint Location" ;
    String REMOVE_ADS_PRODUCT_ID = "remove_ads";
    String AdsRemoved = "RemoveAdsForTheApp";
    String NOTIFICATION_CHANNEL_SYSTEM = "be_apps.alarmManager_Notification_channel_For_system";
    String OPEN_APP_FEW_TIMES = "openAppFewTimes" ;
    String AD_COLONY_APP_ID = "app4795db56569d46f3a4" ;
    String AD_COLONY_INTERSTITIAL_ZONE_EDIT_ACTIVITY = "vz9b1e61e64b2e444c9b" ;
    String AD_COLONY_BANNER_ZONE_MAIN_ACTIVITY = "vz6c3466e6cf44485888" ;
    String APP_ID = "ca-app-pub-3354564452098710~4092415412" ;
    // Open app Ad :
    String APP_OPEN_AD = "ca-app-pub-3354564452098710/7034876161" ; // when the app go again to the foreground
    // Interstitial Ads : 6
    String Interstitial_Ad_for_Edit_Activity_Id =  "ca-app-pub-3354564452098710/3924894459" ; // When add / update task
    String Interstitial_Ad_for_Map_Activity_Id = "ca-app-pub-3354564452098710/1708713082" ; // when add location task // return back
    String Interstitial_Ad_for_Main_Activity_Id = "ca-app-pub-3354564452098710/9879982619" ; // when finish task
    String Interstitial_Ad_for_Settings_Activity_Id = "ca-app-pub-3354564452098710/9252779730" ; // when return back
    String Interstitial_Ad_for_Location_Activity_Id = "ca-app-pub-3354564452098710/2467879531" ;
    String Interstitial_Ad_for_Finished_Task_Activity_Id = "ca-app-pub-3354564452098710/4000453054" ;
    // Native Ads : 8
    String NATIVE_AD_ID_FOR_SETTINGS_ACTIVITY = "ca-app-pub-3354564452098710/3828406126" ;
    String NATIVE_AD_ID_FOR_MAP_ACTIVITY = "ca-app-pub-3354564452098710/8391124205" ;
    String NATIVE_AD_ID_FOR_TOP_EDIT_ACTIVITY = "ca-app-pub-3354564452098710/2366424295" ;
    String NATIVE_AD_ID_FOR_BOTTOM_EDIT_ACTIVITY = "ca-app-pub-3354564452098710/9698119403" ;
    String NATIVE_AD_ID_FOR_DIALOG_EDIT_ACTIVITY = "ca-app-pub-3354564452098710/8385037738" ;
    String NATIVE_AD_ID_FOR_MAIN_ACTIVITY = "ca-app-pub-3354564452098710/8878989613" ; // with recycleView
    String NATIVE_AD_ID_FOR_FINISH_TASK_ACTIVITY = "ca-app-pub-3354564452098710/7649668426" ; // with recycleView
    String NATIVE_AD_ID_FOR_LOCATION_TASK_ACTIVITY = "ca-app-pub-3354564452098710/2313581269" ; // with recycleView
    // Banners : 3
    String BANNER_FINISH_TASK_ACTIVITY = "ca-app-pub-3354564452098710/9541334592" ;
    String BANNER_MAIN_ACTIVITY = "ca-app-pub-3354564452098710/5136520889" ;
    String BANNER_LOCATION_TASK_ACTIVITY = "ca-app-pub-3354564452098710/4574549573" ;

}
