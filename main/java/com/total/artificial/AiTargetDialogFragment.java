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

import com.total.overiden.MainActivity;
import com.total.overiden.R;

public class AiTargetDialogFragment extends DialogFragment implements View.OnClickListener {
    private AiTargetChoice target;

    public AiTargetDialogFragment(AiTargetChoice target) {
        super();
        this.target = target;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.dialog_ai_target, null);
        EditText text = view.findViewById(R.id.question);
        text.setText(target.getDesignation());

        RecyclerView instructions = view.findViewById(R.id.result_list);
        instructions.setAdapter(new AiTargetTypeAdapter(target.getTargetList()));

        instructions.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.add_target_type).setOnClickListener(v -> addTargetType());

        builder.setView(view).setPositiveButton("Back",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                View view = getView();
                if (view!=null) {
                    EditText text = view.findViewById(R.id.question);

                    target.setDesignation(text.getText().toString());
                }
                getParentFragmentManager().setFragmentResult("update",new Bundle());
            }
        });
        return builder.create();
    }
    public void addTargetType(){
        target.getTargetList().add(new AiCommander.TargetType("Default"));
        if (getDialog()!=null) {

            RecyclerView instructions = getDialog().findViewById(R.id.result_list);
            AiTargetTypeAdapter adapter = (AiTargetTypeAdapter) instructions.getAdapter();
            if (adapter != null) adapter.notifyItemInserted(target.getTargetList().size() - 1);
        }
    }

    @Override
    public void onClick(View view) {

    }
}
