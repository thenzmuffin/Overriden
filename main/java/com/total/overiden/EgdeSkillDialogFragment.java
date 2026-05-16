package com.total.overiden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EgdeSkillDialogFragment extends DialogFragment{
    private final Pilot pilot;

    public EgdeSkillDialogFragment(Pilot pilot) {
        super();
        this.pilot = pilot;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.dialog_edge_skill, null);

        Spinner spinner = view.findViewById(R.id.edgeSkillList);
        ArrayAdapter<Pilot.EdgeSkill> adapter = new ArrayAdapter<>(
                view.getContext(),
                R.layout.simple_overide_spinner_dropdown,
                Pilot.EdgeSkill.values()
        );
// Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
        spinner.setAdapter(adapter);

        builder.setView(view).setPositiveButton("Add", (dialog, id) -> {
            Dialog view1 = getDialog();
            if (view1 !=null) {
                Spinner spinner1 = view1.findViewById(R.id.edgeSkillList);
                Pilot.EdgeSkill selected = (Pilot.EdgeSkill)spinner1.getSelectedItem();
                boolean newSkill = selected != Pilot.EdgeSkill.NONE;
                for (Pilot.EdgeSkill skill : pilot.getSkills()){
                    if (skill.equals( selected)){
                        newSkill = false;
                        break;
                    }
                }
                if (newSkill)
                    pilot.addSkill(selected);

            }
            getParentFragmentManager().setFragmentResult("refreshPilot",new Bundle());
        }).setNegativeButton("Cancel", (dialog, id) -> {

        });
        return builder.create();
    }

}
