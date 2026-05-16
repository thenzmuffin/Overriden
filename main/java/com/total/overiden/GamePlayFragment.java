package com.total.overiden;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.total.artificial.AiForceList;
import com.total.overide.OVDatabaseForce;

import java.util.List;

/**
 * This is effectively a shell fragment to contain the game view,
 * holds the game view model and a container for the individual
 */
public class GamePlayFragment extends Fragment implements View.OnClickListener, IBluetoothMessage {
    private LinearLayout stepIndicatorsLayout;
    private TextView[] stepIndicators;
    private TextView   turnTitle;
    private Turn.Phase phase = Turn.Phase.INITIATIVE;
    private int nextButtonID = -1;
    private final String[] steps = {"Initiative","Movement","Targeting","Shooting","Physical","Resolve Turn"};
    public GamePlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GamePlayFragment.
     */

    public static GamePlayFragment newInstance() {
        GamePlayFragment fragment = new GamePlayFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            phase = Turn.Phase.valueOf( savedInstanceState.getString("PHASE") );
        }
        MainActivity main = ((MainActivity)getActivity());
        if (main!=null&&getArguments() != null) {
            // load the game
            TurnViewModel model = new ViewModelProvider(main).get(TurnViewModel.class);
            if (model.getGame()==null) {
                try (DatabaseGame gameDB = new DatabaseGame(getActivity())) {
                    model.setGame(gameDB.loadGame(getArguments().getInt("gameID")));
                }
                if (Game.current.getForceTwoType()== Game.PlayerType.AI){
                    //we need to initialise the AI
                    AiForceList list=(AiForceList) Game.current.getForce(1);
                    list.getCommander().generateTargets();
                }
                phase = model.getGame().getCurrentPhase();
            }

        }

        if (main!= null)main.setOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity()==null)return null;
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        View view = inflater.inflate(R.layout.fragment_game_play, container, false);

        Fragment phaseFrag;
        if (phase == Turn.Phase.INITIATIVE){
            phaseFrag = InitiativeFragment.newInstance();
        } else {
            phaseFrag = TurnPhaseFragment.newInstance(phase);
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // on below line replacing the fragment in child container with child fragment.
        transaction.replace(R.id.game_window, phaseFrag).commit();
        stepIndicatorsLayout = view.findViewById(R.id.progress);

        initializeStepIndicators(model);

        // hide the top app bar back button as it is easy to tap it when you don't want to leave the game

        ActionBar bar = MainActivity.currentActivity.getSupportActionBar();
        if (bar!=null){
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowHomeEnabled(false);   //disable back button
            bar.setHomeButtonEnabled(false);
        }

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("PHASE",phase.toString());
    }

    private Turn.Phase convertIntToPhase(int num){
        Turn.Phase ret;
        switch (num){
            case 0:
                ret = Turn.Phase.INITIATIVE;
                break;
            case 2:
                ret = Turn.Phase.TARGET;
                break;
            case 3:
                ret = Turn.Phase.SHOOT;
                break;
            case 4:
                ret = Turn.Phase.PHYSICAL;
                break;
            case 5:
                ret = Turn.Phase.RESOLVE;
                break;
            case 1:
            default:
                ret = Turn.Phase.MOVE;
                break;
        }
        return ret;
    }

    public void updateStepIndicators(TurnViewModel model){
        boolean phaseComplete = true;
        TurnViewModel modelInst = model;
        if (modelInst == null){
            if (getActivity()==null)return;
            modelInst = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        }
        String title = "Turn " + modelInst.getGame().getThisTurn().getTurnNumber();
        turnTitle.setText(title);
        for (int i = 0; i < stepIndicators.length; i++) {

            if ((phaseComplete) && (phaseComplete = modelInst.getGame().getThisTurn().isPhaseComplete(convertIntToPhase(i))))
                stepIndicators[i].setBackgroundResource(R.drawable.circle_green);
            else stepIndicators[i].setBackgroundResource(R.drawable.circle_gray);
        }
    }
    public void initializeStepIndicators(TurnViewModel modelP) {
        boolean phaseComplete = true;
        TurnViewModel modelInst;
        if (modelP == null){
            if (getActivity()==null)return;
            modelInst = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        } else modelInst = modelP;
        turnTitle = new TextView(this.getContext());
        String title = "Turn " + modelInst.getGame().getThisTurn().getTurnNumber();
        turnTitle.setText(title);
        turnTitle.setTextColor(getResources().getColor(R.color.black,null));
        turnTitle.setTextSize(18);
        stepIndicatorsLayout.addView(turnTitle);
        Button previous = new Button(this.getContext());
        previous.setText(R.string.previous_phase);
        previous.setOnClickListener(arg0 -> gotoPreviousPhase());
        setParams(previous);
        stepIndicatorsLayout.addView(previous);
        Button next = new Button(this.getContext());
        nextButtonID = View.generateViewId();
        next.setId(nextButtonID);
        next.setText(R.string.next_phase);
        next.setOnClickListener(this);
        setParams(next);
        stepIndicatorsLayout.addView(next);
        stepIndicators = new TextView[steps.length];
        for (int i = 0; i < steps.length; i++) {
            TextView stepIndicator = new TextView(this.getContext());
            stepIndicator.setText(steps[i]);
            stepIndicator.setTextColor(getResources().getColor(R.color.white,null));
            stepIndicator.setTextSize(18);
            if ((phaseComplete) && (phaseComplete = modelInst.getGame().getThisTurn().isPhaseComplete(convertIntToPhase(i))))
                stepIndicator.setBackgroundResource(R.drawable.circle_green);
            else stepIndicator.setBackgroundResource(R.drawable.circle_gray);
            stepIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
            setParams(stepIndicator);
            stepIndicatorsLayout.addView(stepIndicator);
            stepIndicators[i] = stepIndicator;

            if (i < steps.length - 1) {
                addArrowIndicator(stepIndicatorsLayout);
            }
        }


    }
    private static void setParams(View view){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // The Margins are set like this
        // Left margin: 10 pixels
        // Top margin: 0 pixels (no margin)
        // Right margin: 10 pixels
        // Bottom margin: 0 pixels (no margin)
        params.setMargins(10, 0, 10, 0);
        params.weight = 0;
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
    }
    private void addArrowIndicator(LinearLayout stepIndicatorsLayout) {
        ImageView arrow = new ImageView(this.getContext());
        // to add this create a new drawable resource file in res->drawable
        arrow.setImageResource(R.drawable.ic_arrow_right);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40,
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
                40
        );
        params.gravity = Gravity.TOP;
        params.weight = 0;
        params.setMargins(10, 5, 10, 5);
        arrow.setLayoutParams(params);
        stepIndicatorsLayout.addView(arrow);
    }

    private void gotoPreviousPhase() {
        if (phase != Turn.Phase.INITIATIVE) {
            if (phase == Turn.Phase.RESOLVE && getView()!=null){
                Button nextButton = getView().findViewById(nextButtonID);
                if (nextButton != null){
                    nextButton.setText(R.string.next_phase);
                }
            }
            phase = Turn.Phase.getLastPhase(phase);
            displayPhase();
        }
    }
    private boolean checkForAirCoverBSP(){
        //did either side deploy air attacks
        List<BSPStrike> airStrikes = Game.current.getForce(1).getBspStrikes(Turn.Phase.COUNTER);
        for (int i = airStrikes.size()-1; i >= 0;i--){
            if (airStrikes.get(i).getType()!= BSPStrikeTemplate.BSPStrikeType.AIRSTRIKE &&
                    airStrikes.get(i).getType()!= BSPStrikeTemplate.BSPStrikeType.STRAFING &&
                    airStrikes.get(i).getType()!= BSPStrikeTemplate.BSPStrikeType.BOMBING){
                airStrikes.remove(i);
            }
        }
        if (!airStrikes.isEmpty()) {
            // if so does the other side have air cover available
            List<BSPStrike> counter = Game.current.getForce(0).getBspStrikes(Turn.Phase.COUNTER);
            if (!counter.isEmpty()) {
                //display dialog to assign air cover
                (new BSPAirCoverDialogFragment(airStrikes,counter)).show(MainActivity.currentActivity.getSupportFragmentManager(),"counter");

                FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

                mgr.setFragmentResultListener("counter",MainActivity.currentActivity, (requestKey, bundle) -> {
                    // We use a String here, but any type that can be put in a Bundle is supported.
                    phase = Turn.Phase.SHOOT;
                    TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
                    model.getGame().reorderUnits();
                    displayPhase();
                });
                return true;
            }
        }
        return false;

    }
    @Override
    public void onClick(View view) {
        if (getActivity()==null)return;
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);

        if (!model.getGame().isPhaseComplete(phase)) return;
//        if (!model.getGame().getThisTurn().isPhaseComplete(phase)) return;
        model.getGame().endPhase(phase);
        switch (phase){
            case INITIATIVE:
                phase = Turn.Phase.MOVE;
                break;
            case MOVE:
                if (Game.current.eventExists(Scenario.ScenarioEvent.SCAN_MOVE)){
                    // TODO: Need a dialog pop up for user to confirm whether a scan occurs or not
                }
                if (Game.current.eventExists(Scenario.ScenarioEvent.NEAR_END)){
                    // TODO: Need a dialog pop up for user to confirm whether a waypoint check is required or not
                }
                phase = Turn.Phase.TARGET;
                break;
            case TARGET:
                // if BSP cards are in use and either side deployed air strikes then
                // check to see if the other force has air cover that could be deployed
                if (checkForAirCoverBSP())return;
                phase = Turn.Phase.SHOOT;
                break;
            case SHOOT:
                phase = Turn.Phase.PHYSICAL;
                // when leaving the shooting phase we need to apply all recorded damage

//                model.getGame().applyDamage(this);
                break;
            case PHYSICAL:
                phase = Turn.Phase.RESOLVE;
//                model.getGame().applyDamage(this);
                ((Button)view).setText(R.string.complete_turn);
                // run set up for the end of turn, generates any required heat/pilot checks
//                model.getGame().getThisTurn().completeTurn();
                break;
            case RESOLVE:
                if (Game.current.getForce(1) instanceof AiForceList){
                    //only continue if the AI rules are resolved
                    if (!Game.current.getAiForce().getCommander().checkRules())return;
                }
                completeResolvePhase(view);
//                phase = Turn.Phase.INITIATIVE;
//                ((Button)view).setText(R.string.next_phase);
//                try (DatabaseGame dbGame = new DatabaseGame(getActivity());
//                     OVDatabaseForce dbForce = new OVDatabaseForce(getActivity())) {
//                    // save the old turn in its complete status
//                    dbGame.updateTurn(null, model.getGame());
//                    model.getGame().endTurn();
//
//                    dbForce.updateAllUnits(model.getForceList(0));
//                    dbForce.updateAllUnits(model.getForceList(1));
//
//                    // now save the new turn to the database
//                    dbGame.addTurn(null, model.getGame().getThisTurn(), model.getGame().getGameKey());
//                }
//
//                this.updateStepIndicators(null);
                break;
            default:
                //what the!!! just bail
                return;
        }
        model.getGame().reorderUnits();
        displayPhase();
    }
    public void completeResolvePhase(View view){
        if (view==null){
            if(getView()!=null) {
                Button butt = getView().findViewById(nextButtonID);
                if (butt != null) butt.setText(R.string.next_phase);
            }
        } else {
            ((Button) view).setText(R.string.next_phase);
        }

        phase = Turn.Phase.INITIATIVE;
        if (Game.current.eventExists(Scenario.ScenarioEvent.SCAN_END)){
            // TODO: Need a dialog pop up for user to confirm whether a scan occurs or not
        }
        if (Game.current.eventExists(Scenario.ScenarioEvent.NEAR_END)){
            // TODO: Need a dialog pop up for user to confirm whether a waypoint check is required or not
        }
        try (DatabaseGame dbGame = new DatabaseGame(getActivity());
             OVDatabaseForce dbForce = new OVDatabaseForce(getActivity())) {
            // save the old turn in its complete status
            dbGame.updateTurn(null, Game.current);
            Game.current.endTurn();

            dbForce.updateAllUnits(Game.current.getForce(0));
            dbForce.updateAllUnits(Game.current.getForce(1));

            // now save the new turn to the database
            dbGame.addTurn(null, Game.current.getThisTurn(), Game.current.getGameKey());
        }
        Game.current.eventTriggered(Scenario.ScenarioEvent.TURN, Game.current.getTurnNumber(),null, this);

        this.updateStepIndicators(null);
    }
    public void displayDetailView(int forceNum){
        if (getActivity()==null)return;
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        MechViewModel mech = new ViewModelProvider(getActivity()).get(MechViewModel.class);
        mech.setMech(model.getForceList(forceNum).getUnit(0));
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.game_window, ViewForceListFragment.newInstance(forceNum)).commit();
    }
    public void displayPhase(){
        Fragment phaseFrag;
        if (phase== Turn.Phase.INITIATIVE) {
            phaseFrag = InitiativeFragment.newInstance();
        } else {
            phaseFrag = TurnPhaseFragment.newInstance(phase);

        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.game_window, phaseFrag).commit();
    }

    @Override
    public void updateScreen() {
        for (Fragment frag : this.getChildFragmentManager().getFragments()) {
            if (frag instanceof IBluetoothMessage) {
                ((IBluetoothMessage) frag).updateScreen();
            }
        }
        if (getActivity()!=null)
           updateStepIndicators(new ViewModelProvider(getActivity()).get(TurnViewModel.class));
    }
}