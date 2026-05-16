package com.total.overiden;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.databinding.FragmentBluetoothConnectBinding;

import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BluetoothConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothConnectFragment extends Fragment implements IBluetoothMessage {
    private FragmentBluetoothConnectBinding binding;
    private boolean loaded = false;

    public BluetoothConnectFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BluetoothConnectFragment newInstance(/*String param1, String param2*/) {
        BluetoothConnectFragment fragment = new BluetoothConnectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // in case there is no game object created yet generate a new one now
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        if (getArguments() != null) {
            int gameID = getArguments().getInt("gameID");
            // load the game
            if (model.getGame() == null && gameID >= 0) {
                try (DatabaseGame gameDB = new DatabaseGame(getActivity())) {
                    model.setGame(gameDB.loadGame(gameID));
                }
                loaded = true;
            }

        }
        if (!loaded) {
            if (model.getGame() == null) {
                model.setGame(new Game());
            }
            model.getGame().setForceOneType(Game.PlayerType.CLIENT); // we are the client so mark it as such
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String game_name = null;
        int forceKey1 = -1;
        if (loaded) {
            TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
            Game game = model.getGame();
            forceKey1 = game.getForce(0).getKey();
            game_name = game.getName();
        }
        int forceIndex1 = 0;
        List<ForceCatalogAdapter.CatalogEntry> forceCatalog;
        try (OVDatabaseForce db = new OVDatabaseForce(getActivity())) {
            forceCatalog = db.getCatalog(loaded, null);
        }
        ForceCatalogAdapter.CatalogEntry[] forces = new ForceCatalogAdapter.CatalogEntry[forceCatalog.size()];
        for (int i = 0; i < forceCatalog.size(); i++) {
            forces[i] = forceCatalog.get(i);
            if (forces[i].key == forceKey1) forceIndex1 = i;
        }
        binding = FragmentBluetoothConnectBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        Spinner spinner = view.findViewById(R.id.force_one);
        ArrayAdapter<ForceCatalogAdapter.CatalogEntry> ad = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_spinner_item,
                forces
        );

        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner

        spinner.setAdapter(ad);
        if (loaded) {
            spinner.setEnabled(false);
            EditText name = view.findViewById(R.id.game_label);
            if (name != null && game_name != null) {
                name.setText(game_name);
                name.setEnabled(false);
            }
        }
        spinner.setSelection(forceIndex1);
        BluetoothDeviceAdapter devAdapter = null;
        BluetoothManager mgr = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        if (adapter != null) { //device has bluetooth functionality
            if (!adapter.isEnabled()) {
            } else {
                if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    // connect as a client
//                BluetoothDevice chosenDevice = null;
                    // first we need a bluetoothdevice
                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                    adapter.cancelDiscovery();
                    devAdapter = new BluetoothDeviceAdapter(pairedDevices);
                }
            }
        }
        if (devAdapter != null) {
            RecyclerView recyclerView = binding.deviceList;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(devAdapter);
        }


        Button butt = view.findViewById(R.id.bluetooth_server);
        butt.setOnClickListener(arg0 -> findBluetoothServer());
        // Inflate the layout for this fragment
        return view;
    }

    private void findBluetoothServer() {
        BluetoothManager mgr = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();

        RecyclerView recyclerView = binding.deviceList;

        BluetoothDevice chosenDevice = ((BluetoothDeviceAdapter) recyclerView.getAdapter()).getSelectedItem();

        // then we need to create a socket
//                    try {
        if (chosenDevice != null) {
            int forceOneId = -1;
            Spinner spinner = this.getView().findViewById(R.id.force_one);
            ForceCatalogAdapter.CatalogEntry catEntry = (ForceCatalogAdapter.CatalogEntry) spinner.getSelectedItem();
            if (catEntry != null) {
                forceOneId = catEntry.key;
            }
            ForceList force;
            try (OVDatabaseForce forceDB = new OVDatabaseForce(this.getActivity())) {
                force = forceDB.getList(forceOneId, false /*Bluetooth games do not support Ai yet*/);
            }
//            BluetoothClientAccept client = new BluetoothClientAccept(chosenDevice,adapter,force,this);
//            client.start();
//            BluetoothSendThread client = new BluetoothSendThread(chosenDevice, adapter, null, force.getStreamValue());
//            client.start();

//            if (client.getSocket()!= null) {
            TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
//                model.getGame().setSocket(client.getSocket());
            // start the listener now as well

            BluetoothInputThread client = new BluetoothInputThread(chosenDevice, adapter, null);
            model.getGame().setComms(client);
            client.start();
            client.write(force.getStreamValue());
//            }

        }
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    public void onClick(View view) {
        /*
         * we need to have received the other players force to start the game (and have selected
         * and sent our own but the other force will only arrive once we have sent ours since
         * they are the host))
         */
        int forceOneId = -1;
        int forceTwoId = -2;
        TurnViewModel model = new ViewModelProvider(getActivity()).get(TurnViewModel.class);
        Game game = model.getGame();
        // create a new game instance then call the start game action
        Spinner spinner = this.getView().findViewById(R.id.force_one);
        ForceCatalogAdapter.CatalogEntry catEntry = (ForceCatalogAdapter.CatalogEntry) spinner.getSelectedItem();
        if (catEntry != null) {
            // Player one is always the local player
            forceOneId = catEntry.key;
        }

        if (game.getForce(1) != null)
            forceTwoId = game.getForce(1).getKey();


        if (forceTwoId >= 0 && forceOneId >= 0 && forceTwoId != forceOneId) {
            try (OVDatabaseForce forceDB = new OVDatabaseForce(this.getActivity())) {
                // set the player force - this hasn't been done yet
                if (!loaded) {
                    game.setForce(0, forceDB.getList(forceOneId, false /*Bluetooth games do not support Ai yet*/));

                    game.setForceTwoType(Game.PlayerType.BLUETOOTH);

                    try (DatabaseGame gameDB = new DatabaseGame(getActivity())) {
                        gameDB.addGame(game);
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putInt("gameID", game.getGameKey());
                bundle.putString("gameName", game.getName());
                if (game.isBspStrikeActive()){
                    NavHostFragment.findNavController(BluetoothConnectFragment.this)
                            .navigate(R.id.action_bsp_select, bundle);
                } else {
                    NavHostFragment.findNavController(BluetoothConnectFragment.this)
                            .navigate(R.id.action_start_game, bundle);
                }
                game.getForce(0).setInUse(true);
                forceDB.addHeader(game.getForce(0));
                game.getForce(1).setInUse(true);
                forceDB.addHeader(game.getForce(1));
            }
        }
    }

    @Override
    public void updateScreen() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                onClick(getView());
            }
        });
    }
}