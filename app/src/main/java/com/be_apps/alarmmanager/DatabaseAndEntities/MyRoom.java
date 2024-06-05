package com.be_apps.alarmmanager.DatabaseAndEntities;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.be_apps.alarmmanager.Utilites.ConverterClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {TaskEntity.class , FinishedTasksEntity.class , GeofencesEntity.class} , version = 1)
@TypeConverters(ConverterClass.class)
public abstract class MyRoom extends RoomDatabase {

    private static MyRoom room ; //singleton
    public abstract Dao Dao() ; // required
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

//    private static RoomDatabase.Callback callback = new Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//            Log.e("ab_do" , "onCreateDatabase" );
//        }
//
//        @Override
//        public void onOpen(@NonNull SupportSQLiteDatabase db) {
//            Log.e("ab_do" , "onOpenDatabase" );
//            super.onOpen(db);
//        }
//    };

    public static synchronized MyRoom getInstance (Context context) {

        if (room == null) {
            room = Room.databaseBuilder(context , MyRoom.class , "TaskDatabase")
                    .fallbackToDestructiveMigration() // re-create the database When the database version on the device does not match the latest schema version
                    // and the Migrations that would migrate old database schemas to the latest schema version are not found.
                    //.addCallback(callback)
                    .allowMainThreadQueries()
                    // for testing only
                    // As we should run any actions on the database on background thread to avoid anr
                    .build();
             }
            return room ;
    }

}
