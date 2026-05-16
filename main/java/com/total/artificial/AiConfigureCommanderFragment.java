package com.total.artificial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.MainActivity;
import com.total.overiden.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AiConfigureCommanderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AiConfigureCommanderFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "commanderId";
    private AiCommander commander;

    public AiConfigureCommanderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArtificialConfigureDeckFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AiConfigureCommanderFragment newInstance(int commander) {
        AiConfigureCommanderFragment fragment = new AiConfigureCommanderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, commander);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try (DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)) {
                commander = db.loadCommander(getArguments().getInt(ARG_PARAM1));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artificial_configure_commander, container, false);
        ((EditText)view.findViewById(R.id.commander_name)).setText(commander.getName());

        view.findViewById(R.id.new_tactic).setOnClickListener(this);
        RecyclerView instructions = view.findViewById(R.id.instruction_list);
        instructions.setAdapter(new AiInstructionAdapter(commander.getTacticAnalysis()));
        instructions.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView tactics = view.findViewById(R.id.target_list);
        tactics.setAdapter(new AiConfigureTargetAdapter(commander.getTargetAnalysis()));
        tactics.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.new_tactic).setOnClickListener(this);

        view.findViewById(R.id.add_instruction).setOnClickListener(this);
        view.findViewById(R.id.save_commander).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.new_tactic){
            commander.getTargetAnalysis().add(new AiTargetChoice("DEFAULT"));
            View main = getView();
            if (main!=null) {
                RecyclerView tactics = main.findViewById(R.id.target_list);
                AiConfigureTargetAdapter ad = (AiConfigureTargetAdapter)tactics.getAdapter();
                if (ad!=null)ad.notifyItemInserted(commander.getTargetAnalysis().size() - 1);
//                view.findViewById(R.id.new_tactic).invalidate();
            }
        } if (view.getId()==R.id.add_instruction){
            int highest = 1;
            for (AiInstruction inst : commander.getTacticAnalysis()){
                if (inst.getIndex()>=highest)highest = inst.getIndex()+1;
            }
            String newValue = highest + ",Question Text, AGGRESSIVE";
            commander.getTacticAnalysis().add(AiInstruction.newInstance(newValue));
            View main = getView();
            if (main!=null) {
                RecyclerView tactics = main.findViewById(R.id.instruction_list);
                AiInstructionAdapter ad = (AiInstructionAdapter)tactics.getAdapter();
                if (ad!=null)ad.notifyItemInserted(commander.getTacticAnalysis().size() - 1);
//                view.findViewById(R.id.new_tactic).invalidate();
            }
        }if (view.getId()==R.id.save_commander){
            try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
                db.saveCommander(commander);
            }
        }
    }

}