package com.be_apps.alarmmanager.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.be_apps.alarmmanager.R;

public class RepeatChooserDialog extends DialogFragment {

   private onDialogClickListener listener ;
   private static RepeatChooserDialog repeatChooserDialog ;

    public RepeatChooserDialog() {
     // you must Create an empty public constructor for your fragment
     // so ur fragment will be able to be destroyed and recreated by the Android framework
    }

    public static RepeatChooserDialog getInstance(int pickerValue, int pickerTypeValue) {
     Bundle bundle = new Bundle() ;
     bundle.putInt("pickerValue" , pickerValue);
     bundle.putInt("pickerTypeValue" , pickerTypeValue);
     if (repeatChooserDialog==null)
         repeatChooserDialog = new RepeatChooserDialog();
     repeatChooserDialog.setArguments(bundle);
     return repeatChooserDialog ;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (onDialogClickListener) context;
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Log.e("ab_do" , "onCancel") ;
        listener.CancelDialogClick();
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {

        int PickerValue = -1 ;
        int PickerTypeValue = -1 ;
        Bundle bundle = getArguments() ;
        if (bundle!=null) {
            PickerValue = bundle.getInt("pickerValue" , -1);
            PickerTypeValue = bundle.getInt("pickerTypeValue" , -1);
        }
        Log.e("ab_do", "onCreateDialog");
        View view = requireActivity().getLayoutInflater().inflate(R.layout.pickerfragment, null);
        NumberPicker numberPicker = view.findViewById(R.id.NumberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(365);
        String[] arr = {"Minute", "Hour" , "Day", "Week" , "Month" , "Year"};
        NumberPicker TypePicker = view.findViewById(R.id.StringPicker);
        TypePicker.setMinValue(0);
        TypePicker.setMaxValue(5);
        TypePicker.setDisplayedValues(arr);
        numberPicker.setValue(PickerValue == -1 ? 1 : PickerValue);
        TypePicker.setValue(PickerTypeValue == -1 ? 2 : PickerTypeValue);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Repeat Task");
        builder.setIcon(R.drawable.ic_question);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                   listener.CancelDialogClick();
            }
        });
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  listener.SetDialogClick(numberPicker.getValue() , TypePicker.getValue());
            }
        });
        builder.setView(view);

        return builder.create();
    }
    public interface onDialogClickListener {
        void SetDialogClick(int number , int type) ;
        void CancelDialogClick ();
    }

    @Override
    public void onDetach() {
        Log.e("ab_do", "onDetach");
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.e("ab_do", "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Log.e("ab_do", "onDismiss");
        super.onDismiss(dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("ab_do", "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.e("ab_do", "onDestroy");
        super.onDestroy();
    }
}
