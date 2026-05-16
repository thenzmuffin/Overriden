package com.total.overiden;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.total.artificial.AiForceList;
import com.total.artificial.DatabaseAI;
import com.total.overide.OVDatabaseForce;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigureAIFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigureAIFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "gameID";
    private static final String ARG_PARAM2 = "gameName";

    private int mGameId;
    private String mGameName;

    public ConfigureAIFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfigureAIFragment.
     */
    public static ConfigureAIFragment newInstance(int gameId, String gameName) {
        ConfigureAIFragment fragment = new ConfigureAIFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, gameId);
        args.putString(ARG_PARAM2, gameName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGameId = getArguments().getInt(ARG_PARAM1);
            mGameName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configure_a_i, container, false);
        RecyclerView recycler = view.findViewById(R.id.config_ai);
        recycler.setAdapter(new AiConfigAdapter((AiForceList) Game.current.getForce(1)));
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.start).setOnClickListener(this);
        Spinner commander = view.findViewById(R.id.commander);
        commander.setAdapter(getCommanderItemArrayAdapter(commander));
        return view;
    }

    @NonNull
    private static ArrayAdapter<DatabaseAI.ListItem> getCommanderItemArrayAdapter(Spinner commander) {
        List<DatabaseAI.ListItem> list;
        try (DatabaseAI dbAi = new DatabaseAI(MainActivity.currentActivity)) {
            list = dbAi.getCommanderList();
        }
        ArrayAdapter<DatabaseAI.ListItem> ad = new ArrayAdapter<>(
                commander.getContext(),
                R.layout.simple_overide_target_dropdown,
                list
        );
        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return ad;
    }

    @Override
    public void onClick(View view) {
        View screen = getView();
        if (screen!=null) {
            Spinner commander = screen.findViewById(R.id.commander);
            DatabaseAI.ListItem comm = (DatabaseAI.ListItem) commander.getSelectedItem();
            AiForceList force = (AiForceList) Game.current.getForce(1);
            force.setCommander(comm.index);
            try (OVDatabaseForce db = new OVDatabaseForce(MainActivity.currentActivity)) {
                // update the force with the selected header
                db.addHeader(force);
            }
            // generate the target list
            force.getCommander().generateTargets();
            Game.current.getAiForce().initListOnLoad(); // do any setup work for dynamic data not stored in the DB
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_PARAM1, Game.current.getGameKey());
            bundle.putString(ARG_PARAM2, Game.current.getName());
            Game.current.getAiForce().saveLiveDeck();
            NavHostFragment.findNavController(ConfigureAIFragment.this)
                    .navigate(R.id.action_start_game, bundle);
        }
    }
}