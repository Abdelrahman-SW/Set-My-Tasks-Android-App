package com.be_apps.alarmmanager.WidgetSystem;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.MyViewModel;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;

import java.util.List;

public class TaskWidgetServices extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskWidgetFactory(getApplicationContext());
    }

    static class TaskWidgetFactory implements RemoteViewsFactory {

        Context context ;
        List<TaskEntity> taskEntities ;
        MyViewModel viewModel ;

        public TaskWidgetFactory(Context context) {
            this.context = context ;
        }

        @Override
        public void onCreate() {
            Log.e("ab_do" , "onCreateAdapter") ;
            viewModel = new MyViewModel((Application) context) ;
        }

        @Override
        public void onDataSetChanged() {
            Log.e("ab_do" , "onDataSetChangedWidget") ;
            if (viewModel==null) viewModel = new MyViewModel((Application) context);
            taskEntities = viewModel.GetTasksForBoot() ;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return taskEntities!=null ? taskEntities.size() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Log.e("ab_do" , "getViewAt") ;
            if (taskEntities==null) taskEntities = viewModel.GetTasksForBoot() ;
            TaskEntity taskEntity = taskEntities.get(position) ;
            String time = taskEntity.getTimeString(taskEntity.getDate() , context) ;
            String Date = taskEntity.getDateString(taskEntity.getDate()) ;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName() , R.layout.widget_item_view);
            remoteViews.setTextViewText(R.id.Time_Widget , time);
            remoteViews.setTextViewText(R.id.Date_Widget , Date);
            Intent fillIntent = new Intent() ;
            fillIntent.putExtra(Constant.TASK , taskEntity) ;
            fillIntent.putExtra(Constant.CALENDER , taskEntity.getDate()) ;
            remoteViews.setOnClickFillInIntent(R.id.Widget_item , fillIntent);
            return  remoteViews ;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
