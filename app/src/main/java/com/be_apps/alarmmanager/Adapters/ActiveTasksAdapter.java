package com.be_apps.alarmmanager.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.DatabaseAndEntities.TaskEntity;
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.Utilites.FormatUtilities;
import com.be_apps.alarmmanager.Utilites.UtilitiesClass;

import java.util.ArrayList;
import java.util.List;

public class ActiveTasksAdapter extends ListAdapter<TaskEntity ,ActiveTasksAdapter.MyViewHolder> implements Filterable {

    private onItemClickListener listener;
    private onLongItemClickListener listener_long ;
    private final Context context ;
    public boolean Full_data ;
    private final ArrayList<TaskEntity> FullTasks = new ArrayList<>()  ;
    private String SearchTxt = "" ;

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private final Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List <TaskEntity> FilterTasks = new ArrayList<>() ;
            if (constraint == null || constraint.length()==0) {
                // Search Result Not Found // user exit the search view
                SearchTxt = "" ;
                FilterTasks.addAll(FullTasks) ;
            }
            else {
                SearchTxt  = constraint.toString().toLowerCase().trim() ;
                for (TaskEntity Tasks : FullTasks) {
                    if (Tasks.getTitle().toLowerCase().trim().contains(SearchTxt) || Tasks.getDescription().toLowerCase().trim().contains(SearchTxt) || Tasks.getDateString(Tasks.getDate()).toLowerCase().trim().contains(SearchTxt)) {
                        FilterTasks.add(Tasks) ;
                    }
                }
            }
            FilterResults filterResults = new FilterResults() ;
            filterResults.values = FilterTasks ;
            return filterResults ;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Full_data = false ;
            submitList((List<TaskEntity>) results.values);
            notifyDataSetChanged();
        }
    };

    private static final DiffUtil.ItemCallback<TaskEntity> diffUtil = new DiffUtil.ItemCallback<TaskEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return oldItem.getId() == newItem.getId() ;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return newItem.TheSameContentOf(oldItem) ;
        }
    };

    public ActiveTasksAdapter(Context context) {
        super(diffUtil);
        this.context = context;
    }

    @Override
    public void submitList(@Nullable List<TaskEntity> list) {
        if (list!=null) {
        super.submitList(list);
        if (Full_data) {
            FullTasks.clear();
            FullTasks.addAll(list);
        }
    }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview , parent , false) ;
        return new MyViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TaskEntity Task = getItem(position) ;
        if (SearchTxt.trim().length()!=0){
            int StartIndex , EndIndex;
            String title = Task.getTitle().trim().toLowerCase() ;
            String description = Task.getDescription().trim().toLowerCase() ;
            String dateString = Task.getDateString(Task.getDate()).trim().toLowerCase() ;

            SpannableStringBuilder spannableString;
            if (title.contains(SearchTxt)) {
                StartIndex = title.indexOf(SearchTxt) ;  // word -- > world in --> title HelloWorld
                // s = 6 , e = 11 the 11 th character will not be colored
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(Task.getTitle()) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Title.setText(spannableString);
            }

            else holder.Title.setText(Task.getTitle());

            if (description.contains(SearchTxt)) {
                StartIndex = description.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(Task.getDescription()) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Description.setText(spannableString);
            } else holder.Description.setText(Task.getDescription());

            if (dateString.contains(SearchTxt)) {
                StartIndex = dateString.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(Task.getDateString(Task.getDate())) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Date.setText(spannableString);
            } else holder.Date.setText(Task.getDateString(Task.getDate()));
        }
        else {
            holder.Title.setText(Task.getTitle());
            holder.Description.setText(Task.getDescription());
            holder.Date.setText(Task.getDateString(Task.getDate()));
        }
        holder.Time.setText(FormatUtilities.FormatToTime(Task.getDate() , context));

        if (!Task.isSelectedItem()) {
            holder.Background.setBackgroundColor(Color.WHITE);
        }
        else holder.Background.setBackgroundColor(ContextCompat.getColor(context , R.color.Ripple_select));

        if (Task.getSelection()!= Constant.NO_REPEAT) {
            // Repeat Task
            holder.Repeat.setVisibility(View.VISIBLE);
            holder.TxtRepeat.setText(UtilitiesClass.GetRepeatText(Task.getSelection() , Task.getNumberPickerValue() , Task.getPickerTypeValue()));
            holder.Repeat_layout.setVisibility(View.VISIBLE);
        }
        else {
            // Non - Repeat Task
            holder.Repeat.setVisibility(View.GONE);
            holder.Repeat_layout.setVisibility(View.GONE);
        }
        if (Task.IsExpand()) {
            holder.Details.setVisibility(View.GONE);
            holder.Arrow.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.arrow_down));
        }
        else {
            holder.Details.setVisibility(View.VISIBLE);
            holder.Arrow.setImageDrawable(ContextCompat.getDrawable( context , R.drawable.arrow_up));
        }
    }

    @Override
    public Filter getFilter() {
        return myFilter ;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        Typeface typeface = Typeface.createFromAsset(context.getAssets() , "fonts/Calistoga-Regular.ttf");
        TextView Title ;
        TextView Description ;
        TextView Time ;
        TextView Date ;
        TextView title_header, description_header, Date_header, Time_header;
        ImageView Loading_img;
        ImageView Repeat ;
        LinearLayout Repeat_layout ;
        View Background ;
        Drawable drawable;
        ImageView  Arrow ;
        TextView TxtRepeat ;
        View Details ;
        TextView Repeat_header ;
        public MyViewHolder(View itemView) {
            super(itemView);
            Background = itemView.findViewById(R.id.changeBackground);
            title_header = itemView.findViewById(R.id.titleheader) ;
            Time_header = itemView.findViewById(R.id.TimeHeader) ;
            description_header = itemView.findViewById(R.id.descriptionheader);
            Date_header = itemView.findViewById(R.id.DateHeader );
            Repeat_header = itemView.findViewById(R.id.Repeat_header);
            description_header.setTypeface(typeface);
            Time_header.setTypeface(typeface);
            Date_header.setTypeface(typeface);
            title_header.setTypeface(typeface);
            Repeat_header.setTypeface(typeface);
            Title = itemView.findViewById(R.id.title) ;
            Description = itemView.findViewById(R.id.Description) ;
            Time = itemView.findViewById(R.id.Time) ;
            Date = itemView.findViewById(R.id.Date) ;
            Loading_img = itemView.findViewById(R.id.loading) ;
            Repeat = itemView.findViewById(R.id.repeat) ;
            Repeat_layout = itemView.findViewById(R.id.Repeat_layout) ;
            TxtRepeat = itemView.findViewById(R.id.RepeatText);
            drawable = ContextCompat.getDrawable(context , R.drawable.ic_notification);
            Arrow = itemView.findViewById(R.id.MoreDetailsArrow) ;
            Details = itemView.findViewById(R.id.VisibleArea);
            AnimatedVectorDrawableCompat loading_img_animated = AnimatedVectorDrawableCompat.create(context , R.drawable.anim_time) ;
            Loading_img.setImageDrawable(loading_img_animated);
            if (loading_img_animated!=null)
            loading_img_animated.start();
            Arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskEntity taskEntity = getTaskByPos(getAdapterPosition()) ;
                    taskEntity.setExpand(taskEntity.IsExpand());
                    Arrow.animate().rotationBy(360).setDuration(300).start();
                    notifyItemChanged(getAdapterPosition());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition()!=RecyclerView.NO_POSITION)
                    listener.onClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition()!=RecyclerView.NO_POSITION)
                    return listener_long.onLongClick(getAdapterPosition());
                    else return false ;
                }
            });
        }
    }

    public TaskEntity getTaskByPos (int pos) {
        return getItem(pos) ;
    }
    public interface onItemClickListener {
        void onClick (int pos) ;
    }
    public interface onLongItemClickListener {
        boolean onLongClick(int pos) ;
    }
    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener ;
    }
    public void setOnLongItemClickListener(onLongItemClickListener listener) {
        this.listener_long = listener ;
    }
}