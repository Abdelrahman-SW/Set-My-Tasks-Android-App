package com.be_apps.alarmmanager.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.be_apps.alarmmanager.DatabaseAndEntities.FinishedTasksEntity;
import com.be_apps.alarmmanager.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterForFinishedTasks extends ListAdapter<FinishedTasksEntity, AdapterForFinishedTasks.MyViewHolder> implements Filterable {

    private final Context context ;
    private final ArrayList<FinishedTasksEntity> FullTasks = new ArrayList<>()  ;
    private String SearchTxt = "" ;
    public boolean Full_data ;
    private onItemClickListener listener;
    private onLongItemClickListener listener_long ;

    public AdapterForFinishedTasks(Context context) {
        super(diffCallback);
        this.context = context ;
    }

    private final Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<FinishedTasksEntity> FilterTasks = new ArrayList<>() ;
            if (constraint == null || constraint.length()==0) {
                SearchTxt = "" ;
                FilterTasks.addAll(FullTasks) ;
            }
            else {
                SearchTxt  = constraint.toString().toLowerCase().trim() ;
                for (FinishedTasksEntity Tasks : FullTasks) {
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
            submitList((List<FinishedTasksEntity>) results.values);
            notifyDataSetChanged();
        }
    };
    private static final DiffUtil.ItemCallback<FinishedTasksEntity> diffCallback = new DiffUtil.ItemCallback<FinishedTasksEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull FinishedTasksEntity oldItem, @NonNull FinishedTasksEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FinishedTasksEntity oldItem, @NonNull FinishedTasksEntity newItem) {
            return oldItem.TheSameContentOf(newItem) ;
        }
    };
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemview , parent , false) ;
        return new MyViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         FinishedTasksEntity finishedTasksEntity = getItem(position);
        if (SearchTxt.trim().length()!=0){
            int StartIndex , EndIndex;
            String title = finishedTasksEntity.getTitle().trim().toLowerCase() ;
            String description = finishedTasksEntity.getDescription().trim().toLowerCase() ;
            String dateString = finishedTasksEntity.getDateString(finishedTasksEntity.getDate()).trim().toLowerCase() ;

            SpannableStringBuilder spannableString;
            if (title.contains(SearchTxt)) {
                StartIndex = title.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(finishedTasksEntity.getTitle()) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Title.setText(spannableString);
            } else holder.Title.setText(finishedTasksEntity.getTitle());

            if (description.contains(SearchTxt)) {
                StartIndex = description.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(finishedTasksEntity.getDescription()) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Description.setText(spannableString);
            } else holder.Description.setText(finishedTasksEntity.getDescription());

            if (dateString.contains(SearchTxt)) {
                StartIndex = dateString.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                spannableString = new SpannableStringBuilder(finishedTasksEntity.getDateString(finishedTasksEntity.getDate())) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
                holder.Date.setText(spannableString);
            } else holder.Date.setText(finishedTasksEntity.getDateString(finishedTasksEntity.getDate()));
        }
        else {
            holder.Title.setText(finishedTasksEntity.getTitle());
            holder.Description.setText(finishedTasksEntity.getDescription());
            holder.Date.setText(finishedTasksEntity.getDateString(finishedTasksEntity.getDate()));
        }
        holder.Time.setText(finishedTasksEntity.getTimeString(finishedTasksEntity.getDate() , context));
        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(context.getResources() , R.drawable.tick_blue , context.getTheme()) ;
        holder.done.setImageDrawable(vectorDrawableCompat);
        if (!finishedTasksEntity.isSelectedItem()) {
            holder.Background.setBackgroundColor(Color.WHITE);
        }
        else holder.Background.setBackgroundColor(ContextCompat.getColor(context , R.color.Ripple_select));
        if (finishedTasksEntity.IsExpand()) {
            holder.Details.setVisibility(View.GONE);
            holder.Arrow.setImageDrawable(ContextCompat.getDrawable( context , R.drawable.arrow_down));
        }
        else {
            holder.Details.setVisibility(View.VISIBLE);
            holder.Arrow.setImageDrawable(ContextCompat.getDrawable( context , R.drawable.arrow_up));
        }
    }

       class MyViewHolder extends RecyclerView.ViewHolder{
           Typeface typeface = Typeface.createFromAsset(context.getAssets() , "fonts/Calistoga-Regular.ttf");
           TextView Title ;
           TextView Description ;
           TextView Time ;
           TextView Date ;
           TextView title_header, description_header, Date_header, Time_header;
           ImageView done ;
           View Background ;
           ImageView Arrow ;
           View Details ;
           ViewGroup Repeat_layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Background = itemView.findViewById(R.id.changeBackground);
            title_header = itemView.findViewById(R.id.titleheader) ;
            Time_header = itemView.findViewById(R.id.TimeHeader) ;
            description_header = itemView.findViewById(R.id.descriptionheader);
            Date_header = itemView.findViewById(R.id.DateHeader );
            Title = itemView.findViewById(R.id.title) ;
            Description = itemView.findViewById(R.id.Description) ;
            Time = itemView.findViewById(R.id.Time) ;
            Date = itemView.findViewById(R.id.Date) ;
            done = itemView.findViewById(R.id.loading) ;
            description_header.setTypeface(typeface);
            Time_header.setTypeface(typeface);
            Date_header.setTypeface(typeface);
            title_header.setTypeface(typeface);
            Repeat_layout = itemView.findViewById(R.id.Repeat_layout) ;
            Repeat_layout.setVisibility(View.GONE); // any finished task will not be a repeat task
            Arrow = itemView.findViewById(R.id.MoreDetailsArrow) ;
            Details = itemView.findViewById(R.id.VisibleArea);
            Arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FinishedTasksEntity taskEntity = getTaskByPos(getAdapterPosition()) ;
                    taskEntity.setExpand(taskEntity.IsExpand());
                    Arrow.animate().rotationBy(360).setDuration(300).start();
                    notifyItemChanged(getAdapterPosition());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition()!= RecyclerView.NO_POSITION)
                    listener.onClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition()!=RecyclerView.NO_POSITION)
                    return listener_long.onLongClick(getAdapterPosition());
                    else
                    return false ;
                }
            });
        }
    }
    @Override
    public Filter getFilter() {
        return myFilter ;
    }
    @Override
    public void submitList(@Nullable List<FinishedTasksEntity> list) {
        if (list!=null) {
            super.submitList(list);
            if (Full_data) {
                FullTasks.clear();
                FullTasks.addAll(list);
            }
        }
    }
    public FinishedTasksEntity getTaskByPos (int pos) {
        return getItem(pos);
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
