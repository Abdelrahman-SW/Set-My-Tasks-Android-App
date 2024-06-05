package com.be_apps.alarmmanager.DatabaseAndEntities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity (tableName = "Geofences")
public class GeofencesEntity implements Cloneable, Parcelable {
    @PrimaryKey (autoGenerate = true)
    private long id ;
    private double longitude ;
    private double latitude ;
    private String Description ;
    private int TransitionType;
    private double radius ;
    private int DwellTime ;
    private String DwellTimeStr ;
    private boolean ShowEnterTransition, ShowExitTransition , ShowDwellTransition  ;
    private boolean IsEnterTransitionNotified , IsExitTransitionNotified , IsDwellTransitionNotified ;
    private boolean IsFinished ;
    @Ignore
    private boolean IsSelected;

    public GeofencesEntity() {
    }

    @Ignore
    public GeofencesEntity(double longitude, double latitude, String Description, int TransitionType, double radius , int DwellTime , String DwellTimeStr) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.Description = Description;
        this.TransitionType = TransitionType;
        this.radius = radius;
        this.DwellTime = DwellTime ;
        this.DwellTimeStr = DwellTimeStr ;
        IsSelected = false ;
        IsFinished = false ;
        IsEnterTransitionNotified = IsExitTransitionNotified = IsDwellTransitionNotified = false ;
        ShowEnterTransition = ShowExitTransition = ShowDwellTransition = false ;
    }


    protected GeofencesEntity(Parcel in) {
        id = in.readLong();
        longitude = in.readDouble();
        latitude = in.readDouble();
        Description = in.readString();
        TransitionType = in.readInt();
        radius = in.readDouble();
        DwellTime = in.readInt();
        DwellTimeStr = in.readString();
        ShowEnterTransition = in.readByte() != 0;
        ShowExitTransition = in.readByte() != 0;
        ShowDwellTransition = in.readByte() != 0;
        IsEnterTransitionNotified = in.readByte() != 0;
        IsExitTransitionNotified = in.readByte() != 0;
        IsDwellTransitionNotified = in.readByte() != 0;
        IsFinished = in.readByte() != 0;
        IsSelected = in.readByte() != 0;
    }

    public static final Creator<GeofencesEntity> CREATOR = new Creator<GeofencesEntity>() {
        @Override
        public GeofencesEntity createFromParcel(Parcel in) {
            return new GeofencesEntity(in);
        }

        @Override
        public GeofencesEntity[] newArray(int size) {
            return new GeofencesEntity[size];
        }
    };

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getTransitionType() {
        return TransitionType;
    }

    public void setTransitionType(int transitionType) {
        TransitionType = transitionType;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getDwellTime() {
        return DwellTime;
    }

    public void setDwellTime(int dwellTime) {
        DwellTime = dwellTime;
    }

    public String getDwellTimeStr() {
        return DwellTimeStr;
    }

    public void setDwellTimeStr(String dwellTimeStr) {
        DwellTimeStr = dwellTimeStr;
    }

    public boolean IsTheSameContentOf (GeofencesEntity geofencesEntity) {
        return (this.Description.trim().equals(geofencesEntity.getDescription().trim()) &&
                this.getDwellTime() == geofencesEntity.getDwellTime() && this.getLatitude() == geofencesEntity.getLatitude()
                && this.getLongitude() == geofencesEntity.getLongitude() && this.getRadius() == geofencesEntity.getRadius()
                && this.getTransitionType() == geofencesEntity.getTransitionType() && this.isShowEnterTransition() == geofencesEntity.isShowEnterTransition()
        && this.isShowExitTransition() == geofencesEntity.isShowExitTransition() && this.isShowDwellTransition() == geofencesEntity.isShowDwellTransition())
                && this.IsFinished() == geofencesEntity.IsFinished() && this.IsEnterTransitionNotified() == geofencesEntity.IsEnterTransitionNotified()
                && this.IsExitTransitionNotified() == geofencesEntity.IsExitTransitionNotified() && this.IsDwellTransitionNotified() == geofencesEntity.IsDwellTransitionNotified();
    }

    public boolean isSelected() {
        return IsSelected;
    }

    public void setSelected(boolean selected) {
        IsSelected = selected;
    }

    public boolean isShowEnterTransition() {
        return ShowEnterTransition;
    }

    public void setShowEnterTransition(boolean showEnterTransition) {
        ShowEnterTransition = showEnterTransition;
    }

    public boolean isShowExitTransition() {
        return ShowExitTransition;
    }

    public void setShowExitTransition(boolean showExitTransition) {
        ShowExitTransition = showExitTransition;
    }

    public boolean isShowDwellTransition() {
        return ShowDwellTransition;
    }

    public void setShowDwellTransition(boolean showDwellTransition) {
        ShowDwellTransition = showDwellTransition;
    }

    public boolean IsEnterTransitionNotified() {
        return IsEnterTransitionNotified;
    }

    public void setIsEnterTransitionNotified(boolean enterTransitionNotified) {
        IsEnterTransitionNotified = enterTransitionNotified;
    }

    public boolean IsExitTransitionNotified() {
        return IsExitTransitionNotified;
    }

    public void setIsExitTransitionNotified(boolean exitTransitionNotified) {
        IsExitTransitionNotified = exitTransitionNotified;
    }

    public boolean IsDwellTransitionNotified() {
        return IsDwellTransitionNotified;
    }

    public void setIsDwellTransitionNotified(boolean dwellTransitionNotified) {
        IsDwellTransitionNotified = dwellTransitionNotified;
    }

    public boolean IsFinished() {
        return IsFinished;
    }

    public void setIsFinished(boolean finished) {
        IsFinished = finished;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(Description);
        dest.writeInt(TransitionType);
        dest.writeDouble(radius);
        dest.writeInt(DwellTime);
        dest.writeString(DwellTimeStr);
        dest.writeByte((byte) (ShowEnterTransition ? 1 : 0));
        dest.writeByte((byte) (ShowExitTransition ? 1 : 0));
        dest.writeByte((byte) (ShowDwellTransition ? 1 : 0));
        dest.writeByte((byte) (IsEnterTransitionNotified ? 1 : 0));
        dest.writeByte((byte) (IsExitTransitionNotified ? 1 : 0));
        dest.writeByte((byte) (IsDwellTransitionNotified ? 1 : 0));
        dest.writeByte((byte) (IsFinished ? 1 : 0));
        dest.writeByte((byte) (IsSelected ? 1 : 0));
    }
}
