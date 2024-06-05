package com.be_apps.alarmmanager.WidgetSystem;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.UI.EditActivity;
import com.be_apps.alarmmanager.UI.MainActivity;

public class TaskWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.e("ab_do" , "OnUpdateWidget") ;
        for (int appWidgetId : appWidgetIds) {
             Log.e("ab_do" ,  "appwidgetID " + appWidgetId) ;
             SetUpAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void SetUpAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Intent OpenApp = new Intent(context , MainActivity.class) ;
        OpenApp.setAction(Constant.IS_FROM_WIDGET) ;
        Intent OpenAddActivity = new Intent(context , EditActivity.class) ;
        OpenAddActivity.setAction(Constant.IS_FROM_WIDGET) ;
        Intent OpenEditActivityTemplate = new Intent(context , EditActivity.class) ;
        OpenEditActivityTemplate.setAction(Constant.IS_FROM_WIDGET) ;
        PendingIntent OpenAppPendingIntent = PendingIntent.getActivity(context , appWidgetId , OpenApp , 0) ;
        PendingIntent AddActivityPendingIntent = PendingIntent.getActivity(context , appWidgetId , OpenAddActivity  , 0 ) ;
        PendingIntent TemplateIntent = PendingIntent.getActivity(context , appWidgetId+1001 , OpenEditActivityTemplate , 0) ;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName() , R.layout.task_widget) ;
        remoteViews.setOnClickPendingIntent(R.id.Widget_toolbar , OpenAppPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.Widget_Add_img , AddActivityPendingIntent);
        Intent serviceIntent = new Intent(context , TaskWidgetServices.class) ;
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.Widget_list_view , serviceIntent);
        remoteViews.setEmptyView(R.id.Widget_list_view , R.id.Widget_No_Tasks);
        remoteViews.setPendingIntentTemplate(R.id.Widget_list_view , TemplateIntent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.Widget_list_view);
        appWidgetManager.updateAppWidget(appWidgetId , remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    public static void NotifyWidgetDataChanged(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext()) ;
        int [] arr = appWidgetManager.getAppWidgetIds(new ComponentName( context , TaskWidgetProvider.class)) ;
        for (int appWidgetId : arr) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.Widget_list_view);
        }
    }
}

