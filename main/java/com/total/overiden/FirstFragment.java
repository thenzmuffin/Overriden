package com.total.overiden;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.total.overide.OVDatabaseUnit;
import com.total.overide.OVMtfReader;
import com.total.overide.OVUnitDesign;
import com.total.overiden.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment implements View.OnClickListener{
    public static final String uuid = "55b9f3b0-d0ea-45ae-9bd9-c00d22a11362";
    private FragmentFirstBinding binding;
//private BluetoothSocket socket = null;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        MechViewModel mech = new ViewModelProvider(this).get(MechViewModel.class);
        if (mech.getMech()==null) {
            try (OVDatabaseUnit db = new OVDatabaseUnit(getActivity())) {
//            db.onUpgrade(null,1,1);
                OVUnitDesign design = db.getUnitDesign(1, ForceList.ForceType.OV);
                if (design == null) {
                    design = OVMtfReader.readMTF(getResources().openRawResource(R.raw.archerarc_2k));
                    if (design !=null)db.addUnit(design);
                }
                mech.setMech(design);
            }
        }
        MainActivity main = (MainActivity)getActivity();
        if (main != null)main.setOptionsMenu(false);
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        // clear any in memory game
        FragmentActivity act = getActivity();
        if (act!=null) {
            TurnViewModel model = new ViewModelProvider(act).get(TurnViewModel.class);
            model.setGame(null);
            Game.current = null;
        }
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        MechViewModel viewModel = new ViewModelProvider(getActivity()).get(MechViewModel.class);
//        OVArmourView arm = view.findViewById(R.id.mechID);

        binding.catalog.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_Catalog)
        );

        binding.force.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_ForceList)
        );

        binding.newGame.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_NewGame)
        );

        binding.continueGame.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_SelectGame)
        );

//        binding.continueGame.setOnClickListener(v ->
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_SelectGame)
//        );
//        binding.bluetoothServer.setOnClickListener(v -> establishBluetoothConnection(true));
        binding.loadAi.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_MaintainAI) );
        binding.loadScenario.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_MaintainScenario) );

        binding.createMap.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.create_map_link) );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {

    }
}