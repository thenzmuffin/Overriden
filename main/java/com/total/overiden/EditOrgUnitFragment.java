package com.total.overiden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.total.overide.OVDatabaseForce;

public class EditOrgUnitFragment extends DialogFragment {
    public interface IUpdateForce {
        void addGroup(String name);
    }

    Context listener;
    private String name = "Bob Ivanovich";

    private IUpdateForce callback = null;
    public EditOrgUnitFragment(){
        super();
    }
    public EditOrgUnitFragment( IUpdateForce back){
        super();
        name = "Default";
        callback = back;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.pilot_details, null);
        EditText vname = view.findViewById(R.id.pilot_name);
        vname.setText(this.name);
        view.findViewById(R.id.gunnery_skill).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.piloting_skill).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.injuries).setVisibility(View.INVISIBLE);


        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText vname = view.findViewById(R.id.pilot_name);
                        name = vname.getText().toString();
                        updatePilot(name);
                    }
                });
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        EditPilotDialogFragment.this.getDialog().cancel();
//                    }
//                });
        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface.
        try {
            // Instantiate the NoticeDialogListener so you can send events to
            // the host.
            listener = context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface. No update possible

        }
    }

    public void setCallback(IUpdateForce back) {
        callback = back;
    }
    private void updatePilot(String name) {
        if (callback!=null)callback.addGroup(name);
    }
}
