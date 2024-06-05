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

@Entity(tableName = "Tasks")
public class TaskEntity implements Cloneable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String Title;
    private String description;
    private Calendar Date;
    private int Selection; // selected option of the spinner
    // For Other Option :
    private int NumberPickerValue;
    private int PickerTypeValue;
    private boolean Permanent;
    @Ignore
    private boolean SelectedItem = false;
    @Ignore
    private boolean Expand = false ;

    protected TaskEntity(Parcel in) {
        id = in.readLong();
        Title = in.readString();
        description = in.readString();
        Selection = in.readInt();
        NumberPickerValue = in.readInt();
        PickerTypeValue = in.readInt();
        Permanent = in.readByte() != 0;
        SelectedItem = in.readByte() != 0;
        Expand = in.readByte() != 0;
    }

     public static final Creator<TaskEntity> CREATOR = new Creator<TaskEntity>() {
        @Override
        public TaskEntity createFromParcel(Parcel in) {
            return new TaskEntity(in);
        }

        @Override
        public TaskEntity[] newArray(int size) {
            return new TaskEntity[size];
        }
    };

    public boolean IsExpand() {
        return !Expand;
    }

    public void setExpand(boolean expand) {
        Expand = expand;
    }

    @Ignore
    public TaskEntity( long id , String title, String description, Calendar date) {
        Title = title;
        this.description = description;
        Date = date;
        this.id = id ;
    }

    @Ignore
    public TaskEntity() {
    }

    // this constructor is necessary for database
    public TaskEntity(long id, String Title, String description, Calendar Date, int Selection, int NumberPickerValue, int PickerTypeValue, boolean Permanent) {
        this.id = id;
        this.Title = Title;
        this.description = description;
        this.Date = Date;
        this.Selection = Selection;
        this.NumberPickerValue = NumberPickerValue;
        this.PickerTypeValue = PickerTypeValue;
        this.Permanent = Permanent;
    }

    public boolean isPermanent() {
        return Permanent;
    }

    public void setPermanent(boolean permanent) {
        Permanent = permanent;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelectedItem() {
        return SelectedItem;
    }

    public void setSelectedItem(boolean selectedItem) {
        SelectedItem = selectedItem;
    }


    public int getSelection() {
        return Selection;
    }

    public void setSelection(int selection) {
        Selection = selection;
    }

    public int getNumberPickerValue() {
        return NumberPickerValue;
    }

    public void setNumberPickerValue(int numberPickerValue) {
        NumberPickerValue = numberPickerValue;
    }

    public int getPickerTypeValue() {
        return PickerTypeValue;
    }

    public void setPickerTypeValue(int pickerTypeValue) {
        PickerTypeValue = pickerTypeValue;
    }

    public void setDate(Calendar date) {
        Date = date;
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

    public String getDescription() {
        return description;
    }

    public Calendar getDate() {
        return Date;
    }

    @Ignore
    public String getTimeString(Calendar calendar , Context context) {
        return FormatUtilities.FormatToTime(calendar , context) ;
    }

    @Ignore
    public String getDateString(Calendar calendar) {
        return FormatUtilities.FormatToDate(calendar);
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean TheSameContentOf (TaskEntity obj) {
        return this.getTitle().trim().equals(obj.getTitle().trim()) &&
                this.getDescription().trim().equals(obj.getDescription().trim()) &&
                this.getDate().compareTo(obj.getDate()) == 0 &&
                this.getSelection() == obj.getSelection() && this.getPickerTypeValue() == obj.getPickerTypeValue()
                && this.getNumberPickerValue() == obj.getNumberPickerValue() && this.isSelectedItem()==obj.isSelectedItem();
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
        dest.writeInt(Selection);
        dest.writeInt(NumberPickerValue);
        dest.writeInt(PickerTypeValue);
        dest.writeByte((byte) (Permanent ? 1 : 0));
        dest.writeByte((byte) (SelectedItem ? 1 : 0));
        dest.writeByte((byte) (Expand ? 1 : 0));
    }
}
