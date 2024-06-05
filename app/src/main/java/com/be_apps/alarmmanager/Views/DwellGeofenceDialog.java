package com.be_apps.alarmmanager.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.be_apps.alarmmanager.R;

public class DwellGeofenceDialog extends DialogFragment {

    private OnDwellGeofenceDialogClickListener listener ;
    private int PickerValue ;
    private int PickerTypeValue ;
    private static  DwellGeofenceDialog dwellGeofenceDialog ;

    public DwellGeofenceDialog() {
        // you must Create an empty public constructor for your fragment
        // so ur fragment will be able to be destroyed and recreated by the Android framework
    }

    public static DwellGeofenceDialog getInstance(int pickerValue , int pickerTypeValue) {
        Bundle bundle = new Bundle();
        bundle.putInt("pickerVal" , pickerValue);
        bundle.putInt("pickerType" , pickerTypeValue);
        if (dwellGeofenceDialog==null) {
            dwellGeofenceDialog = new DwellGeofenceDialog();
        }
        dwellGeofenceDialog.setArguments(bundle);
        return dwellGeofenceDialog ;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnDwellGeofenceDialogClickListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle!=null) {
            PickerValue = bundle.getInt("pickerVal" , -1);
            PickerTypeValue = bundle.getInt("pickerType" , -1);
        }
        Log.e("ab_do"  , "onCreateDialog") ;
        View view = requireActivity().getLayoutInflater().inflate(R.layout.pickerfragment, null);
        NumberPicker numberPicker = view.findViewById(R.id.NumberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        String[] arr = {"Minute", "Hour" , "Day"};
        NumberPicker TypePicker = view.findViewById(R.id.StringPicker);
        TypePicker.setMinValue(0);
        TypePicker.setMaxValue(2);
        TypePicker.setDisplayedValues(arr);
        numberPicker.setValue(PickerValue!=-1 ? PickerValue : 1);
        TypePicker.setValue(PickerTypeValue!=-1 ? PickerTypeValue : 1);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Duration for Stay in the position");
        builder.setIcon(R.drawable.ic_question);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.CancelDialog();
                dialog.dismiss();
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        // when the user touch the screen the dialog will be canceled
        listener.CancelDialog();
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        // when the dialog disappear ( on cancel or click any actions )
        super.onDismiss(dialog);
    }

    public interface OnDwellGeofenceDialogClickListener {
        void SetDialogClick(int number , int type) ;
        void CancelDialog() ;
    }
}
