package com.total.overiden;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.total.artificial.AiEnums;
import com.total.artificial.AiForceList;
import com.total.artificial.AiPhaseAdapter;
import com.total.artificial.AiUnitListAdapter;
import com.total.artificial.ArtificialPhasePanel;
import com.total.artificial.ArtificialPilot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameSingleAiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameSingleAiFragment extends GameSingleFragment implements IRefreshFragment{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHASE_PARAM1 = "phase";

    private Turn.Phase phase;
    private int forceNumber = -1;
    private int unitIndex = -1;
    private ArtificialPilot pilot = null;

    public GameSingleAiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameSingleForceFragment.
     */
    public static GameSingleAiFragment newInstance(Turn.Phase phase, int forceNumber) {
        GameSingleAiFragment fragment = new GameSingleAiFragment();
        Bundle args = new Bundle();
        args.putString(PHASE_PARAM1, phase.toString());
        args.putInt("selectedForce", forceNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phase = Turn.Phase.valueOf(getArguments().getString(PHASE_PARAM1));
            forceNumber = getArguments().getInt("selectedForce");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //get saved state
        if (forceNumber < 0 && savedInstanceState!=null)
            forceNumber = savedInstanceState.getInt("selectedForce");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_single_ai, container, false);
        Button button = view.findViewById(R.id.prev_unit);
        if (button!=null)button.setOnClickListener(arg0 -> changeUnit(false));
        button = view.findViewById(R.id.next_unit);
        if (button!=null)button.setOnClickListener(arg0 -> changeUnit(true));
        // get the next unit to be moved
        AiForceList aiForce = Game.current.getAiForce();
//        ArtificialPilot unit = aiForce.getNextUnit(phase);
        unitIndex = aiForce.getNextUnitIndex(phase);

        pilot = aiForce.getPilotData(unitIndex);
        // Create the unit panel for the selected unit
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        ArtificialPhasePanel artificialPhasePanel = ArtificialPhasePanel.newInstance( unitIndex, getParentFragment(), phase);
        transaction.replace(R.id.unitTakingAction, artificialPhasePanel).commit();

        if (phase!= Turn.Phase.TARGET){
            view.findViewById(R.id.question).setVisibility(View.GONE);
            view.findViewById(R.id.selected).setVisibility(View.GONE);
        } else {
            // in the targeting phase the R.id.question field displays the action that should be taken
            initialiseQuestion(view);
            view.findViewById(R.id.selected).setOnClickListener(arg0 -> updateTargets());
        }
        // set up questions
        // check if AI adapter is required
        RecyclerView recyclerView = view.findViewById(R.id.aiPanel);
        if (recyclerView!=null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            RecyclerView.Adapter<?> adapter = AiPhaseAdapter.newInstance(phase,pilot, this);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }
    private final boolean bracketComplete = false;
    private int ruleIndex = 0;
    private final List<IUnitData> localTargetList = new ArrayList<>();
    private void initialiseQuestion(View pView){
        View view;
        if (pView==null)view = getView();
        else view = pView;
        assert view != null;
        // for the currently selected bracket/rule update the question being displayed
        String question;
        if (bracketComplete){
//            AiCommander.TargetType type = pilot.getCurrentCard().getTargetRules().get(ruleIndex);
//            pilot.getTopRankedTargets(type);
            question = "Targeting priorities applied";
        } else {
            question = "Select Units with LOS in range " + pilot.getCurrentCard().getRangeBracket().get(ruleIndex);
        }
        TextView text = view.findViewById(R.id.question);
        text.setText(question);
    }
    private void updateTargets(){
        // user has selected units within the bracket, determine if the bracket part of selection is complete
        View view = getView();
        if (view!=null) {
            RecyclerView recyclerView = view.findViewById(R.id.aiPanel);
            AiUnitListAdapter adapter = (AiUnitListAdapter)recyclerView.getAdapter();
            if (adapter!= null)adapter.getSelectedItems(localTargetList);
            else localTargetList.clear();
            if (localTargetList.isEmpty()) {
                // next bracket, if no more brackets then no possible target
                ruleIndex++;
                if (ruleIndex<pilot.getCurrentCard().getRangeBracket().size()) {
                    initialiseQuestion(view);
                    //new question, the list remains the same and the user needs to select again
                    return;
                }
            } else {
                pilot.setTargetList(localTargetList);
                // for now we need a special case for "can his targets rear"
                if (localTargetList.size()>1 &&
                        pilot.getCurrentCard().getTargetRules().get(0).getEnum()== AiEnums.Tag.HAVE_REAR_SHOT){
                    TextView text = view.findViewById(R.id.question);
                    text.setText(R.string.rear_arc_q);
                    //new question, the list remains the same and the user needs to select again
                    return;
                }
                pilot.applyTargetRules();
            }
            // replace the list of targets
            if (adapter!=null) {
                adapter.setEditable(false);
                adapter.updateSelectedItems(localTargetList);
            }
            view.findViewById(R.id.selected).setVisibility(View.GONE);
            if (localTargetList.isEmpty()) {
                pilot.getTurn().setTargetingComplete(true);
            } else {
                // target identified
                pilot.getTurn().setSelectedTarget(localTargetList.get(0).getKey());
            }
            resetData(null); // does the header need to be refreshed?
            UpdatePlayerActions.targetActionCompleted(pilot);
        }
    }
    private void changeUnit(boolean next){
        View view = getView();
        if (view !=null) {
            if (!next) {
                if (unitIndex > 0) unitIndex--;
            } else {
                if (unitIndex < Game.current.getForce(1).getActiveCount() - 1) unitIndex++;
            }
            // buttons may have been hidden by the previous unit so reset them
            view.findViewById(R.id.question).setVisibility(View.VISIBLE);
            view.findViewById(R.id.selected).setVisibility(View.VISIBLE);

            RecyclerView recyclerView = view.findViewById(R.id.aiPanel);
            AiForceList aiForce = (AiForceList) Game.current.getForce(1);
            pilot = aiForce.getPilotData(unitIndex);
            RecyclerView.Adapter<?> adapter = AiPhaseAdapter.newInstance(phase, pilot, this);
            recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged(); //not sure if this is needed
            Fragment panel = getChildFragmentManager().findFragmentById(R.id.unitTakingAction);
            if (panel instanceof ArtificialPhasePanel) {
                ((ArtificialPhasePanel) panel).setUnitIndex(unitIndex);
            }
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // this may not be needed if the forcenumber is saved by the parent fragment
        outState.putInt("selectedForce", forceNumber);

    }

    @Override
    public void updateScreen() {
        // We don't use bluetooth with AI (yet) so nothing required here
        new Handler(Looper.getMainLooper()).post(() -> {
//            if (getView()!=null) {
////                    ((RecyclerView) getView().findViewById(R.id.forcePanel)).getAdapter().notifyDataSetChanged();
//            }
        });
    }

    @Override
    public View resetData(View pView) {
        // the header data needs to be refreshed
        Fragment frag = getChildFragmentManager().findFragmentById(R.id.unitTakingAction);
        if (frag instanceof ArtificialPhasePanel){
            ((ArtificialPhasePanel) frag).updateScreen();
        }
        return null;
    }
}