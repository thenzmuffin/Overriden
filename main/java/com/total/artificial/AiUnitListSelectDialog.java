package com.total.artificial;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.R;

import java.util.ArrayList;
import java.util.List;

public class AiUnitListSelectDialog extends DialogFragment {
    private final AiTargetInstruction move;
    private final List<IUnitData> list;
    private IUnitData selected;

    public AiUnitListSelectDialog(AiTargetInstruction move) {
        super();
        this.move = move;
        list = new ArrayList<>();
        addValidChoices();
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.dialog_ai_unit_list_select, null);
        EditText text = view.findViewById(R.id.question);
        text.setText(move.getQuestion());
//        addValidChoices();

        RecyclerView tags = view.findViewById(R.id.unit_list);
        tags.setAdapter(new AiUnitListAdapter(list));
        tags.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.selected).setOnClickListener(arg0 -> onSelectedClicked());
        builder.setView(view);

        return builder.create();
    }

    private boolean addValidChoices(){
        list.clear();
        AiEnums.MovedStatus status = move.getBracketStatus();
        for (IUnitData unit : Game.current.getForce(0).getAllUnits()) {
            switch (status) {
                case MOVED:
                    if (unit.getTurn().getMoveData().isMoveLocked())
                        list.add(unit);
                    break;
                case NOT_MOVED:
                    if (!unit.getTurn().getMoveData().isMoveLocked())
                        list.add(unit);
                    break;
                default:
                    list.add(unit);
            }
        }
        if (list.isEmpty()){
            // no units to select so try the next option
            if (move.nextBracket()) {
                addValidChoices();
            } else return false;
        }
        return true;
    }
    public void onSelectedClicked(){
        // get the selected units
        Dialog view = getDialog();
        if (view !=null) {
            RecyclerView tags = view.findViewById(R.id.unit_list);
            if (tags != null) {
                AiUnitListAdapter adapter = (AiUnitListAdapter) tags.getAdapter();
                if (adapter != null) {
                    List<IUnitData> list = adapter.getSelectedItems(null);
                    if (list.isEmpty()) {
                        // try the next iteration
                        if (move.nextBracket()){
                            if (addValidChoices()) {
                                EditText text = view.findViewById(R.id.question);
                                text.setText(move.getQuestion());
//                                addValidChoices();
                                adapter.notifyDataSetChanged();
                                return; // return here stops the dialog from closing
                            }
                        }
                        // if there is no valid next bracket to go to then set the choice
                        // to 0 (nothing found) and complete the dialog
                        move.setSelectedHolder(0);
                    } else {
                        // we have some matching items within the range bracket specified so finish here
                        move.setSelectedList(list);
                        move.setSelectedHolder(1);
                    }
                }
            }
        }
        // close the dialog and fire the callback
        getParentFragmentManager().setFragmentResult("selected",new Bundle());
        this.dismiss();
    }

    public List<IUnitData> getSelectedUnits(){
        Dialog view = getDialog();
        if (view !=null) {
            RecyclerView tags = view.findViewById(R.id.unit_list);
            if (tags != null) {
                AiUnitListAdapter adapter = (AiUnitListAdapter) tags.getAdapter();
                if (adapter != null) {
                    return adapter.getSelectedItems(null);
                }
            }
        }
        return new ArrayList<>();
    }
    public boolean validToDisplay(){
        return move.isResolved();
    }
}
