package com.total.overiden;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameSingleForceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameSingleForceFragment extends GameSingleFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHASE_PARAM1 = "phase";

    private Turn.Phase phase;
    private int forceNumber = -1;

    public GameSingleForceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameSingleForceFragment.
     */
    public static GameSingleForceFragment newInstance(Turn.Phase phase, int forceNumber) {
        GameSingleForceFragment fragment = new GameSingleForceFragment();
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
        // determine the player type (manual, via bluetooth or AI)
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        boolean controlAdapter = true;
        int layoutID = R.layout.fragment_game_single_force;
        if (forceNumber == 1){
            switch (model.getGame().getForceTwoType()){
                case AI:
                    layoutID = R.layout.fragment_game_single_ai;
                case BLUETOOTH:
                    controlAdapter = false;
                    break;
            }
        }
        // Inflate the layout for this fragment
        Fragment parent = getParentFragment();
        if (parent == null) parent = this;
        View view = inflater.inflate(layoutID, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.forcePanel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getActivity() != null) {

//            if (forceNumber == 1 && model.getGame().getForceTwoType() == Game.PlayerType.BLUETOOTH) {
            if (!controlAdapter){
                BluetoothPhaseAdapter forceAdapter = new BluetoothPhaseAdapter(phase, parent);
                recyclerView.setAdapter(forceAdapter);
            } else {
                TurnPhaseAdapter forceAdapter = new TurnPhaseAdapter(parent, forceNumber, phase);
                recyclerView.setAdapter(forceAdapter);
            }

        }
        return view;
    }
//    public void refreshContents(){
//        if (getView()!=null)getView().findViewById(R.id.forcePanel).invalidate();
//    }
//    public void alertDataChanged() {
//        if (getView()!=null) {
//            RecyclerView recyclerView = getView().findViewById(R.id.forcePanel);
//            RecyclerView.Adapter<?> turn = (TurnPhaseAdapter) recyclerView.getAdapter();
//            turn.notifyDataSetChanged();
//        }
//    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // this may not be needed if the forcenumber is saved by the parent fragment
        outState.putInt("selectedForce", forceNumber);

    }

//    @Override
//    public void processMessage(List<String> list, String deviceName) {
//
//    }

    @Override
    public void updateScreen() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (getView()!=null) {
                    ((RecyclerView) getView().findViewById(R.id.forcePanel)).getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
}