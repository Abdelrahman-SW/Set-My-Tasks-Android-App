package com.be_apps.alarmmanager.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import com.be_apps.alarmmanager.R;
import com.be_apps.alarmmanager.DatabaseAndEntities.GeofencesEntity;

import java.util.ArrayList;
import java.util.List;

public class LocationTasksAdapter extends ListAdapter <GeofencesEntity , LocationTasksAdapter.MyViewHolder> implements Filterable {
    private final Context context ;
    private static final DiffUtil.ItemCallback <GeofencesEntity> diffCallback = new DiffUtil.ItemCallback<GeofencesEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull GeofencesEntity oldItem, @NonNull GeofencesEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull GeofencesEntity oldItem, @NonNull GeofencesEntity newItem) {
            return newItem.IsTheSameContentOf(oldItem);
        }
    };
    private OnItemClickListener onItemClickListener ;
    private OnItemLongClickListener onItemLongClickListener ;
    private final List<GeofencesEntity> FullGeofencesList = new ArrayList<>() ;
    public  boolean Full_data = false ;
    private String SearchTxt = "" ;
    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<GeofencesEntity> FilterGeofences = new ArrayList<>();
            if (constraint == null || constraint.length()==0) {
                SearchTxt = "" ;
                FilterGeofences.addAll(FullGeofencesList) ;
            }
            else {
                SearchTxt = constraint.toString().trim().toLowerCase() ;
                for (GeofencesEntity geofencesEntity : FullGeofencesList) {
                    if (geofencesEntity.getDescription().trim().toLowerCase().contains(SearchTxt))
                        FilterGeofences.add(geofencesEntity);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = FilterGeofences ;
            return filterResults ;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<GeofencesEntity> FilterGeofences = (List<GeofencesEntity>) results.values;
            Full_data = false ;
            submitList(FilterGeofences);
            notifyDataSetChanged();
        }
    };

    public LocationTasksAdapter(Context context) {
        super(diffCallback);
        this.context = context ;
    }

    @Override
    public void submitList(@Nullable List<GeofencesEntity> list) {
        if (list!=null) {
            if (Full_data) {
                FullGeofencesList.clear();
                FullGeofencesList.addAll(list);
            }
            super.submitList(list);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_task_item_view , parent , false) ;
        return new MyViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.e("ab_dohh" , "onBindViewHolder");
         GeofencesEntity geofencesEntity = getItem(position) ;
         if (SearchTxt.trim().length()!=0) {
             String description = geofencesEntity.getDescription().trim().toLowerCase() ;
            int StartIndex , EndIndex ;
            if (description.contains(SearchTxt)) {
                StartIndex = description.indexOf(SearchTxt) ;
                EndIndex = StartIndex + SearchTxt.length() ;
                SpannableStringBuilder spannableString = new SpannableStringBuilder(geofencesEntity.getDescription()) ;
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), StartIndex , EndIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.Description.setText(spannableString);
            }
            else holder.Description.setText(geofencesEntity.getDescription());
         }
         else
         holder.Description.setText(geofencesEntity.getDescription());
         if (!geofencesEntity.isSelected()) {
            holder.BackgroundItemView.setBackgroundColor(Color.WHITE);
         }
         else holder.BackgroundItemView.setBackgroundColor(ContextCompat.getColor(context , R.color.Ripple_select));
         HandleIsFinishedTask(holder, geofencesEntity);
         HandleTaskOptions(holder, geofencesEntity);
    }
    private void HandleIsFinishedTask(@NonNull MyViewHolder holder, GeofencesEntity geofencesEntity) {
        if (geofencesEntity.IsFinished()) {
            holder.EarthImg.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_checked));
            holder.EarthImg.clearAnimation();
            holder.FinishedTaskText.setVisibility(View.VISIBLE);
            holder.EarthImg.setHasTransientState(false);
        }
        else {
            holder.EarthImg.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_globe));
            holder.EarthImg.setAnimation(AnimationUtils.loadAnimation(context , R.anim.earth_rotate));
            holder.FinishedTaskText.setVisibility(View.GONE);
            Log.e("ab_dohh" , "Whatttttttttttttttt");
            holder.EarthImg.setHasTransientState(true);
        }
    }
    private void HandleTaskOptions(@NonNull MyViewHolder holder, GeofencesEntity geofencesEntity) {
        if (geofencesEntity.isShowEnterTransition()) holder.EnterLocation.setVisibility(View.VISIBLE);
        else holder.EnterLocation.setVisibility(View.GONE);
        if (geofencesEntity.isShowExitTransition()) holder.ExitLocation.setVisibility(View.VISIBLE);
        else holder.ExitLocation.setVisibility(View.GONE);
        if (geofencesEntity.isShowDwellTransition()) {
            holder.DwellLocation.setVisibility(View.VISIBLE);
            Log.e("ab_do" , "str " + geofencesEntity.getDwellTimeStr());
            String dwellLoc = "Stay In The Location for " + geofencesEntity.getDwellTimeStr() ;
            holder.DwellLocation.setText(dwellLoc);
        }
        else holder.DwellLocation.setVisibility(View.GONE);

        if (geofencesEntity.IsEnterTransitionNotified())
            holder.EnterLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.done_loc), null);
        else
            holder.EnterLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_gps), null);
        holder.EnterLocation.setCompoundDrawablePadding(20);
        if (geofencesEntity.IsExitTransitionNotified())
            holder.ExitLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.done_loc), null);
        else
            holder.ExitLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_gps), null);
        holder.ExitLocation.setCompoundDrawablePadding(20);
        if (geofencesEntity.IsDwellTransitionNotified())
            holder.DwellLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.done_loc), null);
        else
            holder.DwellLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_gps), null);
        holder.DwellLocation.setCompoundDrawablePadding(20);
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        Typeface typeface = Typeface.createFromAsset(context.getAssets() , "fonts/Calistoga-Regular.ttf");
        TextView Description ;
        TextView Description_Header , OptionsHeader ;
        TextView EnterLocation , ExitLocation , DwellLocation ;
        ImageView EarthImg ;
        View BackgroundItemView  ;
        TextView FinishedTaskText ;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            FinishedTaskText = itemView.findViewById(R.id.FinishedText) ;
            BackgroundItemView = itemView.findViewById(R.id.changeBackground) ;
            Description = itemView.findViewById(R.id.Description) ;
            Description_Header = itemView.findViewById(R.id.descriptionheader) ;
            OptionsHeader = itemView.findViewById(R.id.task_option_header);
            EnterLocation = itemView.findViewById(R.id.EnterLocation) ;
            ExitLocation = itemView.findViewById(R.id.ExitLocation) ;
            DwellLocation = itemView.findViewById(R.id.DwellInTheLocation) ;
            EarthImg = itemView.findViewById(R.id.EarthImg) ;
            Description_Header.setTypeface(typeface);
            OptionsHeader.setTypeface(typeface);
            EarthImg.setAnimation(AnimationUtils.loadAnimation(context , R.anim.earth_rotate));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition()!=RecyclerView.NO_POSITION)
                    onItemClickListener.onClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                     if (getAdapterPosition()!=RecyclerView.NO_POSITION)
                     return onItemLongClickListener.OnLongClick(getAdapterPosition());
                     else return false ;
                }
            });
        }
    }
    public GeofencesEntity GetGeofenceByPos (int pos) {
        return getItem(pos) ;
    }
    public interface OnItemClickListener {
        void onClick(int pos) ;
    }
    public void SetOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener ;
    }
    public interface OnItemLongClickListener {
        boolean OnLongClick (int pos) ;
    }
    public void SetOnLongClickListener (OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener ;
    }
}
