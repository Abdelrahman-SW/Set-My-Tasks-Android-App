package com.be_apps.alarmmanager.DatabaseAndEntities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.be_apps.alarmmanager.Utilites.FormatUtilities;
import java.util.Calendar;

@Entity (tableName = "FinishedTasks")
public class FinishedTasksEntity implements Cloneable , Parcelable {
    @PrimaryKey
    private long id;
    private String Title;
    private String description;
    private Calendar Date;
    @Ignore
    private boolean Expand = false ;
    @Ignore
    private boolean SelectedItem = false;

    public FinishedTasksEntity() {

    }

    public boolean IsExpand() {
        return !Expand;
    }

    public void setExpand(boolean expand) {
        Expand = expand;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public FinishedTasksEntity(long id, String title, String description, Calendar date) {
        this.id = id;
        Title = title;
        this.description = description;
        Date = date;
    }

    protected FinishedTasksEntity(Parcel in) {
        id = in.readLong();
        Title = in.readString();
        description = in.readString();
        SelectedItem = in.readByte() != 0;
    }

    public static final Creator<FinishedTasksEntity> CREATOR = new Creator<FinishedTasksEntity>() {
        @Override
        public FinishedTasksEntity createFromParcel(Parcel in) {
            return new FinishedTasksEntity(in);
        }

        @Override
        public FinishedTasksEntity[] newArray(int size) {
            return new FinishedTasksEntity[size];
        }
    };

    public boolean isSelectedItem() {
        return SelectedItem;
    }

    public void setSelectedItem(boolean selectedItem) {
        SelectedItem = selectedItem;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDate() {
        return Date;
    }

    public void setDate(Calendar date) {
        Date = date;
    }

    @Ignore
    public String getTimeString(Calendar calendar , Context context) {
        return FormatUtilities.FormatToTime(calendar , context);
    }

    @Ignore
    public String getDateString(Calendar calendar) {
        return FormatUtilities.FormatToDate(calendar) ;
    }

    public boolean TheSameContentOf (FinishedTasksEntity obj) {
        return this.getTitle().trim().equals(obj.getTitle().trim()) &&
                this.getDescription().trim().equals(obj.getDescription().trim()) &&
                this.getDate().compareTo(obj.getDate())==0 ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(Title);
        dest.writeString(description);
        dest.writeByte((byte) (SelectedItem ? 1 : 0));
    }
}
