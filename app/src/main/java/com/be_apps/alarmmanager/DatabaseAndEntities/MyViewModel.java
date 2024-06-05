package com.be_apps.alarmmanager.DatabaseAndEntities;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
// our activities and fragments are responsible for drawing data to the screen,
// while your ViewModel can take care of holding and processing all the data needed for the UI.
// lets you better follow the single responsibility principle

public class MyViewModel extends AndroidViewModel {

    private final Repository repository ;
    private final LiveData<List<TaskEntity>> TasksDefaultOrder;
    private final LiveData<List<TaskEntity>> TasksOldestToNewest ;
    private final LiveData<List<TaskEntity>> TasksNewestToOldest ;
    private final LiveData<List<TaskEntity>> TasksByAlphabeticallyOrder ;
    private final LiveData<List<FinishedTasksEntity>> FinishedTasks ;
    private final LiveData<List<GeofencesEntity>> GeofencesEntity ;

    public MyViewModel(@NonNull Application application) {
        super(application);
        Log.e("ab_do" , "CreateMyViewModel" );
        repository = new Repository(application) ;
        TasksDefaultOrder = repository.getTasksDefaultOrder() ;
        FinishedTasks = repository.GetFinishedTasks() ;
        TasksByAlphabeticallyOrder = repository.getTasksByAlphabeticallyOrder();
        TasksNewestToOldest = repository.getTasksNewestToOldest();
        TasksOldestToNewest = repository.getTasksOldestToNewest() ;
        GeofencesEntity = repository.GetAllGeofences() ;
    }
    public long Insert(TaskEntity task) {
        return repository.Insert(task);
    }
    public void Delete(TaskEntity task) {
        repository.Delete(task);
    }
    public TaskEntity GetTaskById (long id) {
        return repository.GetTaskById(id) ;
    }
    public List<TaskEntity> GetTasksForBoot () {
        return repository.GetTasksForBoot();
    }
    public void Update(TaskEntity task) {
        repository.Update(task);
    }
    public void DeleteAllTasks() {
       repository.DeleteAllTasks();
    }
    public LiveData<List<TaskEntity>> getTasksDefaultOrder() {
        return TasksDefaultOrder;
    }
    public LiveData<List<FinishedTasksEntity>> GetFinishedTasks() {
        return FinishedTasks ;
    }
    public long InsertFinishedTask (FinishedTasksEntity finishedTasksEntity) {
        return repository.InsertFinishedTask(finishedTasksEntity) ;
    }
    public void DeleteFinishedTask(FinishedTasksEntity finishedTasksEntity) {
        repository.DeleteFinishedTask(finishedTasksEntity);
    }
    public void DeleteAllFinishedTasks(){
        repository.DeleteAllFinishedTasks();
    }
    public void DeleteFinishedTaskById (long id) {
        repository.DeleteFinishedTaskById(id);
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
    public LiveData<List<GeofencesEntity>> GetAllGeofences() {
        return GeofencesEntity ;
    }
    public long InsertGeofence (GeofencesEntity geofenceEntity) {
        return repository.InsertGeofence(geofenceEntity) ;
    }
    public void UpdateGeofence(GeofencesEntity geofenceEntity) {
        repository.UpdateGeofence(geofenceEntity);
    }
    public void DeleteGeofence(GeofencesEntity geofenceEntity) {
        repository.DeleteGeofence(geofenceEntity);
    }
    public void DeleteAllGeofences() {
        repository.DeleteAllGeofences();
    }
    public GeofencesEntity GetGeofenceById (long id) {
        return repository.GetGeofenceById(id) ;
    }
    public void DeleteGeofenceById (long id) {
        repository.DeleteGeofenceById(id);
    }
}
