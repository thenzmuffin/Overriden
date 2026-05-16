package com.total.overiden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.total.overide.OVDatabaseForce;

import java.util.Locale;

public class EditPilotDialogFragment extends DialogFragment {
    private final Integer[] skills = {1,2,3,4,5,6,7,8};
    private final Integer[] boboos = {0,1,2,3,4,5,6};
    private final Pilot pilot;
    public EditPilotDialogFragment(){
        super();
        pilot = new Pilot("Bob Ivanovich",3,5, -1);
    }
    public EditPilotDialogFragment(Pilot pilot){
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
        View view = inflater.inflate(R.layout.pilot_details, null);
        EditText vname = view.findViewById(R.id.pilot_name);
        vname.setText(pilot.getPilotName());
        setUpSpinner(view,R.id.gunnery_skill, pilot.getGunnery() - 1, skills);
        setUpSpinner(view,R.id.piloting_skill, pilot.getPilotSkill() - 1, skills);
        setUpSpinner(view,R.id.injuries, pilot.getInjuries(), boboos);
        view.findViewById(R.id.addEdge).setOnClickListener(arg0 -> addEdge());
        view.findViewById(R.id.addGunnery).setOnClickListener(arg0 -> addGunnery());
        view.findViewById(R.id.addPiloting).setOnClickListener(arg0 -> addPiloting());
        view.findViewById(R.id.addEdgeSkill).setOnClickListener(arg0 -> addEdgeSkill());
        ((CheckBox)view.findViewById(R.id.namedPilot)).setChecked(pilot.isNamedPilot());
        setNamedPilotVisibility(view,pilot.isNamedPilot());
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.ok_button, (dialog, id) -> {
                    Spinner spinner = view.findViewById(R.id.piloting_skill);
                    pilot.setPilotSkill((Integer) spinner.getSelectedItem());
                    spinner = view.findViewById(R.id.gunnery_skill);
                    pilot.setGunnery((Integer) spinner.getSelectedItem());
                    spinner = view.findViewById(R.id.injuries);
                    pilot.setInjuries(spinner.getSelectedItemPosition());
//                    EditText vname1 = view.findViewById(R.id.pilot_name);
                    pilot.setPilotName(vname.getText().toString());
                    try(OVDatabaseForce db = new OVDatabaseForce(MainActivity.currentActivity)){
                        db.updatePilot(null,pilot,pilot.getId());
                    }
                });

        // set up listeners
        view.findViewById(R.id.namedPilot).setOnClickListener(arg0->onNamedPilot());
        return builder.create();
    }
    private void onNamedPilot(){
        Dialog view = getDialog();
        if (view != null) {
            // update the flag on the pilot and refresh the screen
            pilot.setNamedPilot(((CheckBox)view.findViewById(R.id.namedPilot)).isChecked());
            setNamedPilotVisibility(view,pilot.isNamedPilot());
        }
    }
    private void addEdge(){
        // add one to Edge tokens for this pilot
        if (pilot.adjustEdgeTokens(1)){
            Dialog view = getDialog();
            if (view !=null)
                setNamedPilotVisibility(view, true);
        }
    }
    private void addEdgeSkill(){
        // add one to Edge tokens for this pilot
        if (pilot.addEdgeSkill()){
            EgdeSkillDialogFragment dia = new EgdeSkillDialogFragment(pilot);
            dia.show(this.getParentFragmentManager(), "Add Skill");
            this.getParentFragmentManager().
                    setFragmentResultListener("refreshPilot", MainActivity.currentActivity, (requestKey, bundle) -> {
                        // We use a String here, but any type that can be put in a Bundle is supported.
                        Dialog view = getDialog();
                        if (view != null)
                            setNamedPilotVisibility(view, true);
                    });
        }
    }
    private void addGunnery(){
        // add one to Edge tokens for this pilot
        if (pilot.adjustGunnery()){
            Dialog view = getDialog();
            if (view !=null)
                setNamedPilotVisibility(view, true);
        }
    }
    private void addPiloting(){
        // add one to Edge tokens for this pilot
        if (pilot.adjustPiloting()){
            Dialog view = getDialog();
            if (view !=null)
                setNamedPilotVisibility(view, true);
        }
    }
    private void setNamedPilotVisibility(View view, boolean visibility){
        int unused = pilot.getUnusedExperience();
        view.findViewById(R.id.totalSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.unusedSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.gunnerySP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.pilotingSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.edgeSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.skillSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        setupButton(view.findViewById(R.id.addEdge),visibility,pilot.getTokenCost(),unused);
        setupButton(view.findViewById(R.id.addEdgeSkill),visibility,pilot.getSkillCost(),unused);
        setupButton(view.findViewById(R.id.addGunnery),visibility,pilot.getGunneryCost(),unused);
        setupButton(view.findViewById(R.id.addPiloting),visibility,pilot.getPilotingCost(),unused);
        view.findViewById(R.id.edgeTokens).setVisibility(visibility?View.VISIBLE:View.INVISIBLE);
        view.findViewById(R.id.edgeSkills).setVisibility(visibility?View.VISIBLE:View.INVISIBLE);
        view.findViewById(R.id.gunnery_skill).setEnabled(!visibility);
        view.findViewById(R.id.piloting_skill).setEnabled(!visibility);
        if (visibility) {
            ((TextView) view.findViewById(R.id.edgeTokens)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Tokens  : %d", pilot.getEdge()));
            ((TextView) view.findViewById(R.id.totalSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Total SP  : %d", pilot.getExperience()));
            ((TextView) view.findViewById(R.id.unusedSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Unused SP : %d", unused));
            ((TextView) view.findViewById(R.id.gunnerySP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Gunnery SP : %d", pilot.getGunneryXP()));
            ((TextView) view.findViewById(R.id.pilotingSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Piloting SP : %d", pilot.getPilotingXP()));
            ((TextView) view.findViewById(R.id.edgeSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Token SP : %d", pilot.getEdgePointsXP()));
            ((TextView) view.findViewById(R.id.skillSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Skill SP : %d", pilot.getEdgeSkillsXP()));
            StringBuilder skills= new StringBuilder();
            for (Pilot.EdgeSkill skill : pilot.getSkills()){
                if (skills.length()>0)skills.append(",");
                skills.append(skill.toString());
            }
            ((TextView) view.findViewById(R.id.edgeSkills)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Skills : %s", skills));
        }
    }
    private void setNamedPilotVisibility(Dialog view, boolean visibility){
        int unused = pilot.getUnusedExperience();
        view.findViewById(R.id.totalSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.unusedSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.gunnerySP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.pilotingSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.edgeSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        view.findViewById(R.id.skillSP).setVisibility(visibility?View.VISIBLE:View.GONE);
        setupButton(view.findViewById(R.id.addEdge),visibility,pilot.getTokenCost(),unused);
        setupButton(view.findViewById(R.id.addEdgeSkill),visibility,pilot.getSkillCost(),unused);
        setupButton(view.findViewById(R.id.addGunnery),visibility,pilot.getGunneryCost(),unused);
        setupButton(view.findViewById(R.id.addPiloting),visibility,pilot.getPilotingCost(),unused);
        view.findViewById(R.id.edgeTokens).setVisibility(visibility?View.VISIBLE:View.INVISIBLE);
        view.findViewById(R.id.edgeSkills).setVisibility(visibility?View.VISIBLE:View.INVISIBLE);
        view.findViewById(R.id.gunnery_skill).setEnabled(!visibility);
        view.findViewById(R.id.piloting_skill).setEnabled(!visibility);
        if (visibility) {
            ((TextView) view.findViewById(R.id.edgeTokens)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Tokens  : %d", pilot.getEdge()));
            ((TextView) view.findViewById(R.id.totalSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Total SP  : %d", pilot.getExperience()));
            ((TextView) view.findViewById(R.id.unusedSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Unused SP : %d", unused));
            ((TextView) view.findViewById(R.id.gunnerySP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Gunnery SP : %d", pilot.getGunneryXP()));
            ((TextView) view.findViewById(R.id.pilotingSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Piloting SP : %d", pilot.getPilotingXP()));
            ((TextView) view.findViewById(R.id.edgeSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Token SP : %d", pilot.getEdgePointsXP()));
            ((TextView) view.findViewById(R.id.skillSP)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Skill SP : %d", pilot.getEdgeSkillsXP()));
            StringBuilder skills= new StringBuilder();
            for (Pilot.EdgeSkill skill : pilot.getSkills()){
                if (skills.length()>0)skills.append(",");
                skills.append(skill.toString());
            }
            ((TextView) view.findViewById(R.id.edgeSkills)).setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "Edge Skills : %s", skills));
        }
    }
    private void setupButton(Button butt,boolean visibility,int cost, int unused){
        if (!visibility || cost > unused) {
            // Don't have enough points
            butt.setVisibility(View.INVISIBLE);
        } else {
            butt.setVisibility(View.VISIBLE);
            butt.setText(
                    String.format(Locale.getDefault(Locale.Category.FORMAT), "%d", pilot.getTokenCost())
            );
        }
    }
    private void setUpSpinner(View view, int id, int index, Integer[] list){
        Spinner spinner = view.findViewById(id);
        ArrayAdapter<Integer> ad = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_spinner_item,
                list
        );
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        spinner.setAdapter(ad);
        spinner.setSelection(index);

    }
}
