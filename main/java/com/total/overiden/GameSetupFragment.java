package com.total.overiden;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.databinding.FragmentGameSetupBinding;
import com.total.scenario.ScenarioDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameSetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameSetupFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, IBluetoothMessage {
    private FragmentGameSetupBinding binding;
//    private BluetoothSocket socket = null;

    private boolean loaded = false;

    public GameSetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameSetupFragment.
     */
    public static GameSetupFragment newInstance() {
        GameSetupFragment fragment = new GameSetupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity()==null)return;
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        if (getArguments() != null) {
            int gameID = getArguments().getInt("gameID");
            // load the game
            if (model.getGame()==null && gameID >=0) {
                try(DatabaseGame gameDB = new DatabaseGame(getActivity())) {
                    model.setGame(gameDB.loadGame(gameID));
                }
                loaded = true;
            }

        }
        if (!loaded) {
            model.setGame(new Game());
        }
    }
    private void setupScenarioSpinner(View view){
        try(ScenarioDB db = new ScenarioDB(getContext())){
            List<ScenarioDB.ScenCat> cat = db.getScenarioCatalog(true);
            Spinner scenarioSelect = view.findViewById(R.id.scenario);
            ArrayAdapter<ScenarioDB.ScenCat> adapterScenario = new ArrayAdapter<>(
                    view.getContext(),
                    android.R.layout.simple_spinner_item,
                    cat
            );
            // Set simple layout resource file for each item of spinner
            adapterScenario.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            scenarioSelect.setAdapter(adapterScenario);
            scenarioSelect.setOnItemSelectedListener(this);
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameSetupBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        ForceList.ForceType selectedGameType = ForceList.ForceType.OV;
        // setup the game type spinner
        Spinner gameType = view.findViewById(R.id.game_type);
        ArrayAdapter<ForceList.ForceType> ad = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_spinner_item,
                new ForceList.ForceType[]{ForceList.ForceType.OV, ForceList.ForceType.TW}
        );
        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        gameType.setAdapter(ad);
        gameType.setOnItemSelectedListener(this);
        setupScenarioSpinner(view);

        int forceKey1 = -1;
        int forceKey2 = -1;
        String forceTwoName = "Waiting...";
        if (loaded && getActivity()!=null) {
            TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
            Game game = model.getGame();
            forceKey1 = game.getForce(0).getKey();
            forceKey2 = game.getForce(1).getKey();
            forceTwoName = game.getForce(1).getName();
            if (!game.isGameOV()) {
                gameType.setSelection(1);//set to TW
                selectedGameType = ForceList.ForceType.TW;
            }
            TextView gameName = view.findViewById(R.id.game_label);
            gameName.setText(game.getName());
            CheckBox smart = view.findViewById(R.id.smartHeat);
            if (smart !=null){
                smart.setChecked(game.isSmartHeat());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.pilotDamage);
            if (smart !=null){
                smart.setChecked(game.isPilotDamage());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.useTics);
            if (smart !=null){
                smart.setChecked(game.isUseTics());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.ov_forced_withdrawal);
            if (smart !=null){
                smart.setChecked(game.isOvForcedWithdrawal());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.balanced_init);
            if (smart !=null){
                smart.setChecked(game.isBalanceInitiative());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.hexless);
            if (smart !=null){
                smart.setChecked(game.isHexless());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            smart = view.findViewById(R.id.external_ecm);
            if (smart !=null){
                smart.setChecked(game.isExternalECM());
                smart.setEnabled(false); // don't allow the player to change heat mode in the middle of a game
            }
            // for an existing game BSP cannot be allocated so hide it
            view.findViewById(R.id.bsp_points).setVisibility(View.GONE);
            view.findViewById(R.id.bsp_total).setVisibility(View.GONE);
        } else {
            //set default display values
            CheckBox smart = view.findViewById(R.id.useTics);
            if (smart !=null) smart.setChecked(true);
            smart = view.findViewById(R.id.hexless);
            if (smart !=null) smart.setChecked(true);
            view.findViewById(R.id.bsp_total).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.bsp_points).setOnClickListener(this);
            NumberPicker bspTotal = view.findViewById(R.id.bsp_total);
            bspTotal.setMaxValue(160);
            bspTotal.setMinValue(16);
            bspTotal.setValue(32);
            bspTotal.setHorizontalScrollBarEnabled(true);
            bspTotal.setVerticalScrollBarEnabled(false);
        }
        setupForceListSelection(view,forceKey1,forceKey2,selectedGameType);

        Spinner spinner = view.findViewById(R.id.player_type);
        if (spinner != null) {
            Game.PlayerType[] types = {Game.PlayerType.MANUAL, Game.PlayerType.AI, Game.PlayerType.BLUETOOTH};
            ArrayAdapter<Game.PlayerType> playerType = new ArrayAdapter<>(
                    view.getContext(),
                    android.R.layout.simple_spinner_item,
                    types
            );

            spinner.setAdapter(playerType);
            spinner.setOnItemSelectedListener(this);
            if (loaded) {
                spinner.setEnabled(false);
                spinner.setSelection(2);
                ((TextView)view.findViewById(R.id.forceTwoName)).setText(forceTwoName);
            } else {
                spinner.setSelection(0);
            }
        }

        view.findViewById(R.id.bluetooth_server).setOnClickListener(arg0 -> setupBluetoothServer());
        // default the generate AI force button to not visible.  Selecting an AI scenario will show it.
        view.findViewById(R.id.generate).setVisibility(View.GONE);
        view.findViewById(R.id.generate).setOnClickListener(this);
        // Inflate the layout for this fragment
        return view;
    }

    private void setupForceListSelection(View view, int forceKey1, int forceKey2, ForceList.ForceType type){
        int forceIndex1 = 0;
        int forceIndex2 = 0;
        List<ForceCatalogAdapter.CatalogEntry> forceCatalog;
        try (OVDatabaseForce db = new OVDatabaseForce(getActivity())) {
            forceCatalog = db.getCatalog(loaded, type);
            for (int i=0;i< forceCatalog.size();i++) {
                if (forceCatalog.get(i).key == forceKey1) forceIndex1 = i;
                if (forceCatalog.get(i).key == forceKey2) forceIndex2 = i;
            }
        }
        Spinner spinner = view.findViewById(R.id.force_one);
        ArrayAdapter<ForceCatalogAdapter.CatalogEntry> ad = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_spinner_item,
                forceCatalog
        );

        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        spinner.setAdapter(ad);
        spinner.setSelection(forceIndex1);
        if (loaded)spinner.setEnabled(false);
        spinner = view.findViewById(R.id.force_two);
        spinner.setAdapter(ad);
        spinner.setSelection(forceIndex2);
        if (loaded)spinner.setEnabled(false);

    }
    private void setupBluetoothServer() {
        if (getContext()==null)return;

        View view = getView();
        // disable the connect button
        if (view!=null)
            view.findViewById(R.id.bluetooth_server).setEnabled(false);
        // this should be in a separate thread
        BluetoothManager mgr = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        if (adapter != null) { //device has bluetooth functionality
            if (adapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                try {
                    BluetoothServerSocket btsserver = adapter.listenUsingRfcommWithServiceRecord("overiden",
                            UUID.fromString(FirstFragment.uuid));
                    BluetoothSocket socket = btsserver.accept();
                    btsserver.close();

                    if (socket != null&&getActivity()!=null) {
                        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
                        BluetoothInputThread thread = new BluetoothInputThread(null, adapter, socket);
                        model.getGame().setComms(thread);
                        thread.start();
                        // disable the connect button, shows that the connect message is received

//                        Button butt = getView().findViewById(R.id.bluetooth_server);
//                        if (butt!=null){
//                            butt.setEnabled(false);
//                        }
                    }
                } catch (IOException e) {
                    //failed to set up listener
                }
            }
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.start.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (getActivity()==null ||getView()==null)return;
        if (view.getId()==R.id.start) {
            /*
             * Starting the game
             */
            int forceOneId = -1;
            int forceTwoId = -2;
            TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
            Game game = model.getGame();
            game.setSmartHeat(((CheckBox) getView().findViewById(R.id.smartHeat)).isChecked());
            game.setPilotDamage(((CheckBox) getView().findViewById(R.id.pilotDamage)).isChecked());
            game.setUseTics(((CheckBox) getView().findViewById(R.id.useTics)).isChecked());
            game.setOvForcedWithdrawal(((CheckBox) getView().findViewById(R.id.ov_forced_withdrawal)).isChecked());
            game.setBalanceInitiative(((CheckBox) getView().findViewById(R.id.balanced_init)).isChecked());
            game.setHexless(((CheckBox) getView().findViewById(R.id.hexless)).isChecked());
            game.setExternalECM(((CheckBox) getView().findViewById(R.id.external_ecm)).isChecked());
            // create a new game instance then call the start game action
            Spinner spinner = getView().findViewById(R.id.player_type);

            Game.PlayerType type = (Game.PlayerType) spinner.getSelectedItem();
            // set the scenario in use (-1 is handled in the setScenario method)
            spinner = getView().findViewById(R.id.scenario);
            game.setScenario(((ScenarioDB.ScenCat)spinner.getSelectedItem()).key);
            spinner = this.getView().findViewById(R.id.force_one);
            ForceCatalogAdapter.CatalogEntry catEntry = (ForceCatalogAdapter.CatalogEntry) spinner.getSelectedItem();
            if (catEntry != null) {
                forceOneId = catEntry.key;
            }
            if (type == Game.PlayerType.BLUETOOTH) {
                if (game.getForce(1) != null)
                    forceTwoId = game.getForce(1).getKey(); // set a force number that is high enough to reliably not be the same as the locally loaded force
            } else {
                spinner = this.getView().findViewById(R.id.force_two);
                catEntry = (ForceCatalogAdapter.CatalogEntry) spinner.getSelectedItem();
                if (catEntry != null) {
                    forceTwoId = catEntry.key;
                }
            }
            if (forceTwoId >= 0 && forceOneId >= 0 && forceTwoId != forceOneId) {
                // two different forces have been selected

                // set the name of the game
                EditText name = getView().findViewById(R.id.game_label);
                game.setName(name.getText().toString());


                try (OVDatabaseForce forceDB = new OVDatabaseForce(this.getActivity())) {
                    // for a new game set the player force
                    if (!loaded) game.setForce(0, forceDB.getList(forceOneId,type == Game.PlayerType.AI));

                    if (type == Game.PlayerType.MANUAL) {
                        // for bluetooth game to get to this point the
                        // opfor must have already been set
                        if (!loaded) game.setForce(1, forceDB.getList(forceTwoId,false));
                    } else if (type == Game.PlayerType.AI) {
                        // for bluetooth game to get to this point the
                        // opfor must have already been set
                        if (!loaded) {
                            game.setForce(1, forceDB.getAiList(forceTwoId));
                        }

//                        ((ArtificialForceList)game.getForce(1)).getCommander().generateTargets();
                    } else {
                        // for bluetooth we need to send the local force to the other device since we are
                        // the host - this will also act as th trigger to start the game on the other device???
                        List<String> output = new ArrayList<>();
                        output.add("GAMENAME:" + game.getName() + "," + game.isSmartHeat() + ","
                                + game.isPilotDamage() + "," + game.isUseTics() + "," + game.isOvForcedWithdrawal() + ","
                                + game.isHexless() + "," + game.isExternalECM() + "," + ((CheckBox)getView().findViewById(R.id.bsp_points)).isChecked() + "\n");
                        output.addAll(game.getForce(0).getStreamValue());
                        game.getComms().write(output);
                    }

                    // if this is a new game (not loading an existing game) save the game data to the DB
                    if (!loaded) {
                        game.setForceTwoType(type);
                        try (DatabaseGame gameDB = new DatabaseGame(getActivity())) {
                            gameDB.addGame(game);
                        }
                        // set the forces status to in use
                        game.getForce(0).setInUse(true);
                        forceDB.addHeader(game.getForce(0));
                        game.getForce(1).setInUse(true);
                        forceDB.addHeader(game.getForce(1));
                    }

                    // Now send to the next screen
                    Bundle bundle = new Bundle();
                    bundle.putInt("gameID", game.getGameKey());
                    bundle.putString("gameName", game.getName());
                    if (((CheckBox)getView().findViewById(R.id.bsp_points)).isChecked() ) {
                        // Go to the BSP selection screen
                        NavHostFragment.findNavController(GameSetupFragment.this)
                                .navigate(R.id.action_bsp_select, bundle);
                    } else if (game.getForceTwoType()== Game.PlayerType.AI){
                        NavHostFragment.findNavController(GameSetupFragment.this)
                                .navigate(R.id.action_configure_ai, bundle);
                    }else {
                        // start the game
                        NavHostFragment.findNavController(GameSetupFragment.this)
                                .navigate(R.id.action_start_game, bundle);
                    }

                }
            }
        } else if (view.getId()==R.id.bsp_points){
            // make the entered bsp_total visible if needed
            getView().findViewById(R.id.bsp_total).setVisibility(((CheckBox)view).isChecked()?View.VISIBLE:View.INVISIBLE);
        } else if (view.getId()==R.id.generate){
            // try and generate the op for force from the currently selected scenario and then add it to the force Two selection
            ScenarioDB.ScenCat cat = (ScenarioDB.ScenCat)((Spinner)getView().findViewById(R.id.scenario)).getSelectedItem();
            try(ScenarioDB db = new ScenarioDB(getContext())){
                int newForceKey = db.generateOpForForceList(cat, ForceList.ForceType.OV);
                ArrayAdapter<ForceCatalogAdapter.CatalogEntry> adapter = (ArrayAdapter<ForceCatalogAdapter.CatalogEntry>)((Spinner)getView().findViewById(R.id.force_two)).getAdapter();
                adapter.add(new ForceCatalogAdapter.CatalogEntry(newForceKey,"Generated",false,ForceList.ForceType.OV));
                // adding a new entry puts it at the end so set the current selection to the end
                ((Spinner)getView().findViewById(R.id.force_two)).setSelection(adapter.getCount()-1);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId()==R.id.game_type){
            // reset the forceList selections
            setupForceListSelection(getView(),0,0,(ForceList.ForceType) adapterView.getSelectedItem());
        } else if (adapterView.getId()==R.id.scenario){
            View fragView  = getView();
            assert (fragView!=null);

                // scenario changed, update force names, lock down second force type if required
            ScenarioDB.ScenCat cat = (ScenarioDB.ScenCat)adapterView.getSelectedItem();
            if (cat!=null && cat.key>=0){
                try(ScenarioDB db = new ScenarioDB(getContext())){
                    Scenario scenario = db.loadScenario(cat.key);
                    if (scenario!=null) {
                        ((TextView) fragView.findViewById(R.id.forceOneLabel)).setText(scenario.getForceOneName());
                        ((TextView) fragView.findViewById(R.id.forceTwoLabel)).setText(scenario.getForceTwoName());

                        if (scenario.isAIGame()) {
                            // set the second player type to AI and make the field readable only
                            ((Spinner) fragView.findViewById(R.id.player_type)).setSelection(1);
                            fragView.findViewById(R.id.player_type).setEnabled(false);
                        } else {
                            fragView.findViewById(R.id.player_type).setEnabled(true);
                        }
                        if (scenario.getOpForUnits().isEmpty()){
                            // hide the generate button as no preselected force exists
                            fragView.findViewById(R.id.generate).setVisibility(View.GONE);
                        } else {
                            // show the generate button
                            fragView.findViewById(R.id.generate).setVisibility(View.VISIBLE);
                            fragView.findViewById(R.id.generate).setOnClickListener(this);
                        }
                    }
                }
            } else {
                ((TextView)fragView.findViewById(R.id.forceOneLabel)).setText("Force One");
                ((TextView)fragView.findViewById(R.id.forceTwoLabel)).setText("Force Two");
                fragView.findViewById(R.id.player_type).setEnabled(true);
                fragView.findViewById(R.id.generate).setVisibility(View.GONE);
            }
        }else {
            if (getView() == null) return;
            // player type has been changed, manual and AI require the opfor to be selected
            // from forces available on this device (through dropdown force_two) where as
            // for the Bluetooth connection we need a device connected and the op for will
            // be supplied through the connection
            Spinner spinner = getView().findViewById(R.id.player_type);

            Game.PlayerType type = (Game.PlayerType) spinner.getSelectedItem();
            if (type != Game.PlayerType.BLUETOOTH) {
                getView().findViewById(R.id.force_two).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.bluetooth_server).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.forceTwoName).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.start).setEnabled(true);
            } else {
                getView().findViewById(R.id.force_two).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.bluetooth_server).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.forceTwoName).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.start).setEnabled(false);//disable start button until a connection has been made with another device
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void updateScreen() {
        if (getView()==null||getActivity()==null)return;
        // the only screen update we get here is a new force name for the op for
        new Handler(Looper.getMainLooper()).post(() -> {
            TextView forceName = getView().findViewById(R.id.forceTwoName);
            if (forceName != null) {
                TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
                Game game = model.getGame();
                if (game.getForce(1)!=null)
                    forceName.setText(game.getForce(1).getName());
            }
            getView().findViewById(R.id.start).setEnabled(true);
        });
    }
}