package com.be_apps.alarmmanager.DatabaseAndEntities;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@androidx.room.Dao
public interface Dao {

    // Tasks :
    @Insert
    long Insert (TaskEntity task) ;
    @Update
    void Update (TaskEntity task) ;
    @Delete
    void Delete (TaskEntity task) ;
    @Query("DELETE FROM Tasks")
    void DeleteALLTASKS() ;
    @Query("SELECT * FROM Tasks WHERE id = :id")
    TaskEntity GetTaskById(long id) ;
    @Query("SELECT * FROM Tasks ORDER BY Date ASC ")
    LiveData<List<TaskEntity>> GetTasksOrderFromOldestToNewest() ;
    @Query("SELECT * FROM Tasks ORDER BY Date DESC ")
    LiveData<List<TaskEntity>> GetTasksOrderFromNewestToOldest() ;
    @Query("SELECT * FROM Tasks")
    LiveData<List<TaskEntity>> GetTasksByDefaultOrder() ;
    @Query("SELECT * FROM Tasks ORDER BY description")
    LiveData<List<TaskEntity>> GetTasksByAlphabeticallyOrder() ;
    @Query("SELECT * FROM Tasks")
    List<TaskEntity> GetTasksForBoot() ;

    //////////////////////////////////////////////////////

    // FinishedTasks :

    @Query("SELECT * FROM FinishedTasks")
    LiveData<List<FinishedTasksEntity>> GetFinishedTasks() ;
    @Insert
    long Insert (FinishedTasksEntity task) ;
    @Delete
    void Delete (FinishedTasksEntity task) ;
    @Query("DELETE FROM FinishedTasks")
    void DeleteALLFinishedTasks() ;
    @Query("SELECT * FROM FinishedTasks WHERE id = :id")
    FinishedTasksEntity GetFinishedTaskById (long id) ;
    @Query("DELETE FROM FinishedTasks WHERE id = :id")
    void DeleteFinishedTaskById(long id) ;

//    @Query("SELECT * FROM Tasks WHERE Title LIKE '%' || :filter || '%' OR description LIKE '%' || :filter ||'%' OR Date LIKE '%' || :filter ||'%'")
//     List<TaskEntity> GetTaskFilter(String filter) ;

    //////////////////////////////////////////////

    // Location Tasks :

    @Insert
    long Insert (GeofencesEntity geofenceEntity) ;
    @Update
    void Update (GeofencesEntity geofenceEntity) ;
    @Delete
    void Delete (GeofencesEntity geofenceEntity) ;
    @Query("DELETE FROM Geofences")
    void DeleteAllGeofences() ;
    @Query("SELECT * FROM Geofences")
    LiveData<List<GeofencesEntity>> GetAllGeofences();
    @Query("SELECT * FROM Geofences WHERE id = :id")
    GeofencesEntity GetGeofenceById (long id) ;
    @Query("DELETE FROM Geofences WHERE id = :id")
    void DeleteGeofenceById(long id) ;
    @Query("SELECT * FROM Geofences")
    List<GeofencesEntity> GetGeofences() ;
}
