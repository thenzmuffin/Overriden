package com.total.scenario;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.total.overiden.R;

public class ScenarioWaypointDialog extends DialogFragment {
    /*
     * This dialog displays the flavour text for a waypoint
     */
    private String tip;

    public ScenarioWaypointDialog(String tip) {
        super();
        this.tip = tip;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.fragment_tooltip, null);
        TextView text = view.findViewById(R.id.tooltip);
        text.setText(tip);
        builder.setView(view).setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });;
        return builder.create();
    }
}
