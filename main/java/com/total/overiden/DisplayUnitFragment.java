package com.total.overiden;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.total.artificial.AiUnitDataProxy;
import com.total.overide.ConfigTicsFragment;
import com.total.overide.OVArmourFragment;
import com.total.overide.OVDatabaseUnit;
import com.total.overide.OVMtfReader;
import com.total.overide.OVOverviewFragment;
import com.total.overide.OVUnitDesign;
import com.total.overide.TWBlkReader;

import java.io.FileNotFoundException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisplayUnitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayUnitFragment extends Fragment implements View.OnClickListener, IRefreshFragment, IMechViewer {


    public DisplayUnitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DisplayUnitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayUnitFragment newInstance() {
        DisplayUnitFragment fragment = new DisplayUnitFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//        }

    }

    //    private String[] tabNames = {"Overview","Armour","Configuration","Criticals","Equipment"};
    private OVOverviewFragment overviewFragment;
    private OVArmourFragment armourFragment;
    private ConfigTicsFragment configFragment;
    private EquipmentFragment equipFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity fragAct = getActivity();
        if (fragAct==null)return null;
        MechViewModel mech = new ViewModelProvider(fragAct).get(MechViewModel.class);
        if (mech.getMech() == null) {
            try (OVDatabaseUnit db = new OVDatabaseUnit(getActivity())) {
                OVUnitDesign design = db.getUnitDesign(1, ForceList.ForceType.OV);
                if (design == null) {
                    design = OVMtfReader.readMTF(getResources().openRawResource(R.raw.archerarc_2k));
//                    if (design != null)
                    db.addUnit(design);
                }
                mech.setMech(design);
            }
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_unit, container, false);

        // on below line creating a child fragment
        overviewFragment = OVOverviewFragment.newInstance(this);
        armourFragment = OVArmourFragment.newInstance(this);
        configFragment = ConfigTicsFragment.newInstance(this);
        equipFragment = EquipmentFragment.newInstance();


        // on below line creating a fragment transaction and initializing it.
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // on below line replacing the fragment in child container with child fragment.
        transaction.replace(R.id.mechContainerView, overviewFragment).commit();

//        UnitCatalogAdapter forceAdapter = new UnitCatalogAdapter(this);
//        UnitCatalogGroupAdapter forceAdapter = new UnitCatalogGroupAdapter(UnitCatalogGroupAdapter.CatalogGroup.MASS, this);
        UnitCatalogAdapter forceAdapter = new UnitCatalogAdapter( this);
        RecyclerView recyclerView = view.findViewById(R.id.force_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(forceAdapter);

        view.findViewById(R.id.add_mtf).setOnClickListener(this);
        view.findViewById(R.id.overview).setOnClickListener(this);

        view.findViewById(R.id.armour).setOnClickListener(this);
        view.findViewById(R.id.config).setOnClickListener(this);
        view.findViewById(R.id.equipment).setOnClickListener(this);
        view.findViewById(R.id.delete).setOnClickListener(this);

        Spinner unitTypeFilter = view.findViewById(R.id.unit_type);
        ArrayAdapter<IUnitHeader.UnitType> ad = new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_spinner_item,
                IUnitHeader.UnitType.values()
        );

        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        unitTypeFilter.setAdapter(ad);
//        unitTypeFilter.setOnItemSelectedListener();
        Button filter = view.findViewById(R.id.filter);
        filter.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.overview) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, overviewFragment).commit();

        } else if (view.getId() == R.id.armour) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, armourFragment).commit();

        } else if (view.getId() == R.id.config) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, configFragment).commit();

        } else if (view.getId() == R.id.equipment) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, equipFragment).commit();

        } else if (view.getId() == R.id.delete) {
            try (OVDatabaseUnit unitDb = new OVDatabaseUnit(MainActivity.currentActivity)){
                MechViewModel mech = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
                if (mech.getMech() != null)
                    unitDb.deleteUnit(mech.getMech().getHeader().getKey());
            }

        }else if (view.getId() == R.id.add_mtf) {
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);


        }else if (view.getId() == R.id.filter) {
            // regenerate the catalog based on the filter values
            setupUnitCatalog(getView());
        }
    }
    private void setupUnitCatalog(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.force_list);
        UnitCatalogAdapter catalogAdapter = (UnitCatalogAdapter) recyclerView.getAdapter();
        String name = ((EditText) view.findViewById(R.id.name_filter)).getText().toString();
        IUnitHeader.UnitType type = (IUnitHeader.UnitType) ((Spinner) view.findViewById(R.id.unit_type)).getSelectedItem();
        if (catalogAdapter != null) catalogAdapter.refreshCatalog(name, type);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        View view = getView();
        if (view == null) return;
        RecyclerView recyclerView = view.findViewById(R.id.force_list);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            try (OVDatabaseUnit db = new OVDatabaseUnit(this.getActivity())) {
                OVUnitDesign design = null;
                if (selectedfile != null && getContext() != null) {
                    try {
                        design = OVMtfReader.readMTF(getContext().getContentResolver().openInputStream(selectedfile));

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (design != null) {
                    db.addUnit(design);
                    UnitCatalogAdapter adapter = (UnitCatalogAdapter) recyclerView.getAdapter();
//                    UnitCatalogGroupAdapter adapter = (UnitCatalogGroupAdapter) recyclerView.getAdapter();
                    if (adapter != null) adapter.refreshCatalog(null,IUnitHeader.UnitType.NONE);
                    recyclerView.refreshDrawableState();
                }
            }
        }

        recyclerView.invalidate();

    }

    @Override
    public View resetData(View pView) {
        View localView = pView;
        if (localView == null) localView = getView();
        if (localView != null) {
            RecyclerView recyclerView = localView.findViewById(R.id.force_list);
            UnitCatalogAdapter adapter = (UnitCatalogAdapter)recyclerView.getAdapter();
//            UnitCatalogGroupAdapter adapter = (UnitCatalogGroupAdapter)recyclerView.getAdapter();
            if (adapter!=null)adapter.clearSelected();
            overviewFragment.resetData(localView);
            armourFragment.resetData(localView);
            configFragment.resetData(localView);
            equipFragment.resetData(localView);
        }
        return localView;
    }

    @Override
    public IUnitDesign getDisplayMech() {
        FragmentActivity fragAct = getActivity();
        if (fragAct != null) {
            MechViewModel mViewModel = new ViewModelProvider(fragAct).get(MechViewModel.class);
            if (mViewModel.getMech() == null && getContext()!=null)
                mViewModel.setMech(OVMtfReader.readMTF(getContext().getResources().openRawResource(R.raw.archerarc_2k)));
            IUnitDesign local = mViewModel.getMech();
            // if the display mech is wrapped in an AI proxy then we need to get the inner unit out for this display
            if (local instanceof AiUnitDataProxy) local = ((AiUnitDataProxy) local).getSubUnit();
            return local;
        }
        return null;
    }

    public WeaponListAdapter getWeaponListAdapter() {
        FragmentActivity fragAct = getActivity();
        if (fragAct != null) {
            MechViewModel mViewModel = new ViewModelProvider(fragAct).get(MechViewModel.class);
            return mViewModel.getAdapter();
        }
        return null;
    }

    public void setWeaponListAdapter(WeaponListAdapter adapter) {
        FragmentActivity fragAct = getActivity();
        if (fragAct != null) {
            MechViewModel mViewModel = new ViewModelProvider(fragAct).get(MechViewModel.class);
            mViewModel.setAdapter(adapter);
        }
    }
}