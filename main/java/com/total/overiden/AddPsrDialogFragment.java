package com.total.overiden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddPsrDialogFragment extends DialogFragment {
    private final IUnitData unit;
    private final PilotCheck.PilotCheckType[] psrList;

    public AddPsrDialogFragment(IUnitData unit) {
        super();
        this.unit = unit;
        if (unit.getHeader().getType()== ForceList.ForceType.OV){
            // for OV this is a chance to add additional PSRs that should have been auto detected but weren't?
            psrList = new PilotCheck.PilotCheckType[6];
            psrList[0] = PilotCheck.PilotCheckType.DAMAGE;
            psrList[1] = PilotCheck.PilotCheckType.ACTUATOR;
            psrList[2] = PilotCheck.PilotCheckType.GYRO;
            psrList[3] = PilotCheck.PilotCheckType.CHARGED;
            psrList[4] = PilotCheck.PilotCheckType.DFAD;
            psrList[5] = PilotCheck.PilotCheckType.KICKED;
        } else {
            // for TW this is a list of PSRs that cannot be auto-detected in the app during the move phase
            psrList = new PilotCheck.PilotCheckType[5];
            psrList[0] = PilotCheck.PilotCheckType.WATER1;
            psrList[1] = PilotCheck.PilotCheckType.WATER2;
            psrList[2] = PilotCheck.PilotCheckType.WATER3;
            psrList[3] = PilotCheck.PilotCheckType.RUBBLE;
            psrList[4] = PilotCheck.PilotCheckType.SKID;
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.fragment_add_psr_dialog, null);
        Spinner psrTypes = view.findViewById(R.id.psr_type);
        if (psrTypes != null) {
            ArrayAdapter<PilotCheck.PilotCheckType> adapter = new ArrayAdapter<>(MainActivity.currentActivity,R.layout.simple_overide_spinner_dropdown,psrList);
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner.
            psrTypes.setAdapter(adapter);
        }
        builder.setView(view).setPositiveButton(R.string.add_button, (dialog, id) -> {
            assert psrTypes!=null;
            PilotCheck.PilotCheckType type = (PilotCheck.PilotCheckType)psrTypes.getSelectedItem();
            unit.getTurn().addCheck(new PilotCheck(unit,type, null));
            getParentFragmentManager().setFragmentResult("reset",new Bundle());
        });
        builder.setNegativeButton(R.string.cancel_button, (dialog, id) -> {

        });
        return builder.create();
    }

}
