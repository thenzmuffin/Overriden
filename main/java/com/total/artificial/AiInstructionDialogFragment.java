package com.total.artificial;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.R;

public class AiInstructionDialogFragment extends DialogFragment {
    private AiInstruction move;

    public AiInstructionDialogFragment(AiInstruction move) {
        super();
        this.move = move;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.dialog_ai_instruction, null);
        EditText text = view.findViewById(R.id.question);
        text.setText(move.getQuestion());
        TextView textView = view.findViewById(R.id.card_id);
        textView.setText(Integer.toString(move.getIndex()));
        RecyclerView instructions = view.findViewById(R.id.result_list);
        instructions.setAdapter(new AiResultAdapter(move.getHolders()));

        instructions.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView tags = view.findViewById(R.id.tag_list);
        tags.setAdapter(new AiTargetTypeAdapter(move.getParams()));

        tags.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.add_result).setOnClickListener(v -> addResult());
        view.findViewById(R.id.add_tag).setOnClickListener(v -> addTag());
        builder.setView(view).setPositiveButton("Done",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Dialog view = getDialog();
                if (view!=null) {
                    EditText text = view.findViewById(R.id.question);

                    move.setQuestion(text.getText().toString());
                }
                getParentFragmentManager().setFragmentResult("instruction",new Bundle());
            }
        });
        return builder.create();
    }
    public void addResult(){
        move.getHolders().add(new AiInstruction.MoveChoice("Default", 0));
        if (getDialog()!=null) {
            RecyclerView instructions = getDialog().findViewById(R.id.result_list);
            AiResultAdapter adapter = (AiResultAdapter) instructions.getAdapter();
            if (adapter != null) adapter.notifyItemInserted(move.getHolders().size() - 1);
        }
    }
    public void addTag(){
        move.getParams().add(new AiCommander.TargetType("Default"));
        if (getDialog()!=null) {
            RecyclerView instructions = getDialog().findViewById(R.id.tag_list);
            AiTargetTypeAdapter adapter = (AiTargetTypeAdapter) instructions.getAdapter();
            if (adapter != null) adapter.notifyItemInserted(move.getParams().size() - 1);
        }
    }
}
