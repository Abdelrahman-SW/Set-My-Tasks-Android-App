package com.be_apps.alarmmanager.DatabaseAndEntities;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class Repository {
    private final Dao dao ;
    private final LiveData<List<TaskEntity>> TasksDefaultOrder; // final mean it still refer to the sameObject
    private final LiveData<List<TaskEntity>> TasksOldestToNewest ;
    private final LiveData<List<TaskEntity>> TasksNewestToOldest ;
    private final LiveData<List<TaskEntity>> TasksByAlphabeticallyOrder ;
    private final LiveData<List<FinishedTasksEntity>> FinishedTasks ;
    private final LiveData<List<GeofencesEntity>> GeofencesEntity ;

    public Repository(Application application) {
        MyRoom room = MyRoom.getInstance(application) ;
        dao = room.Dao();
        // we don`t need to create a thread as
        // When Room queries return LiveData, the queries are automatically run asynchronously on a background thread.
        TasksDefaultOrder = dao.GetTasksByDefaultOrder() ;
        FinishedTasks = dao.GetFinishedTasks() ;
        TasksByAlphabeticallyOrder = dao.GetTasksByAlphabeticallyOrder();
        TasksOldestToNewest = dao.GetTasksOrderFromOldestToNewest() ;
        TasksNewestToOldest = dao.GetTasksOrderFromNewestToOldest() ;
        GeofencesEntity = dao.GetAllGeofences();
    }

    public long Insert(final TaskEntity task) {
//        Callable <Long> callable = new Callable<Long>() {
//            @Override
//            public Long call() throws Exception {
//                return dao.Insert(task);
//            }
//        }; // like runnable but return a value
//        long rowID = -1 ;
//        Future<Long> id = MyRoom.EXECUTOR_SERVICE.submit(callable) ; // placeHolder until get the real value
//        try {
//            rowID = id.get(); //blocking
//        } catch (ExecutionException | InterruptedException e) {
//            return -1 ;
//        }
//        return rowID ;
        return dao.Insert(task);
    }
    public TaskEntity GetTaskById (long id) {
        return dao.GetTaskById(id);
    }
    public List<TaskEntity> GetTasksForBoot () {
         return  dao.GetTasksForBoot();
    }
    public void Delete(final TaskEntity task) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.Delete(task);
            }
        });
    }
    public void Update(final TaskEntity task) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.Update(task);
            }
        });
    }
    public void DeleteAllTasks() {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.DeleteALLTASKS();
            }
        });
    }
    public LiveData<List<TaskEntity>> getTasksDefaultOrder() {
        return TasksDefaultOrder;
    }
    public LiveData<List<TaskEntity>> getTasksOldestToNewest() {
        return TasksOldestToNewest;
    }
    public LiveData<List<TaskEntity>> getTasksNewestToOldest() {
        return TasksNewestToOldest;
    }
    public LiveData<List<TaskEntity>> getTasksByAlphabeticallyOrder() {
        return TasksByAlphabeticallyOrder;
    }
    public LiveData<List<FinishedTasksEntity>> GetFinishedTasks() {
        return FinishedTasks ;
    }
    public long InsertFinishedTask(FinishedTasksEntity finishedTasksEntity) {
        return dao.Insert(finishedTasksEntity) ;
    }
    public void DeleteFinishedTask(FinishedTasksEntity finishedTasksEntity) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.Delete(finishedTasksEntity);
            }
        });
    }
    public void DeleteAllFinishedTasks() {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.DeleteALLFinishedTasks();
            }
        });
    }
    public FinishedTasksEntity GetFinishedTaskById(long id) {
        return dao.GetFinishedTaskById(id) ;
    }
    public void DeleteFinishedTaskById (long id) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.DeleteFinishedTaskById(id);
            }
        });
    }
    public LiveData<List<GeofencesEntity>> GetAllGeofences() {
        return GeofencesEntity ;
    }
    public long InsertGeofence (GeofencesEntity geofenceEntity) {
        return dao.Insert(geofenceEntity) ;
    }
    public void UpdateGeofence(GeofencesEntity geofenceEntity) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.Update(geofenceEntity);
            }
        });
    }
    public void DeleteGeofence(GeofencesEntity geofenceEntity) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.Delete(geofenceEntity);
            }
        });
    }
    public void DeleteAllGeofences() {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.DeleteAllGeofences();
            }
        });
    }
    public GeofencesEntity GetGeofenceById (long id) {
       return dao.GetGeofenceById(id) ;
    }
    public void DeleteGeofenceById (long id) {
        MyRoom.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                dao.DeleteGeofenceById(id);
            }
        });
    }
    public List<GeofencesEntity> GetGeofences() {
        return dao.GetGeofences();
    }
}
