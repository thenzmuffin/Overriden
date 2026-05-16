package com.total.overide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.EditOrgUnitFragment;
import com.total.overiden.EditPilotDialogFragment;
import com.total.overiden.EquipmentFragment;
import com.total.overiden.ForceList;
import com.total.overiden.ForceListAdapter;
import com.total.overiden.IMechViewer;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDesign;
import com.total.overiden.IUnitHeader;
import com.total.overiden.MainActivity;
import com.total.overiden.MechViewModel;
import com.total.overiden.R;
import com.total.overiden.UnitCatalogAdapter;
import com.total.overiden.WeaponListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditListFragment extends Fragment implements View.OnClickListener, IRefreshFragment, IMechViewer, EditOrgUnitFragment.IUpdateForce {

    //    private String[] tabNames = {"Overview","Armour","Configuration","Criticals"};
    private int forceListKey = -1;
    private ForceList.ForceType forceType = ForceList.ForceType.OV;

    public EditListFragment() {
        // Required empty public constructor
    }

    public static EditListFragment newInstance() {
        EditListFragment fragment = new EditListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("forceKey", forceListKey);
        outState.putString("forceType", forceType.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            forceListKey = getArguments().getInt("forceKey");
            forceType = ForceList.ForceType.valueOf(getArguments().getString("forceType"));
        }
        if (forceListKey < 0 && savedInstanceState != null) {
            forceListKey = savedInstanceState.getInt("forceKey");
        }
        MechViewModel mech = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        if (mech.getMech() == null) {
            try (OVDatabaseUnit db = new OVDatabaseUnit(getActivity())) {
//            db.onUpgrade(null,1,1);
                OVUnitDesign design = db.getUnitDesign(1, ForceList.ForceType.OV);
                if (design == null) {
                    design = OVMtfReader.readMTF(getResources().openRawResource(R.raw.archerarc_2k));
                    if (design != null) db.addUnit(design);
                }
                mech.setMech(design);
            }
        }
    }

    private OVOverviewFragment overviewFragment;
    private OVArmourFragment armourFragment;
    private EquipmentFragment equipFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_list2, container, false);

        // on below line creating a child fragment
        overviewFragment = OVOverviewFragment.newInstance(this);
        armourFragment = OVArmourFragment.newInstance(this);
        equipFragment = EquipmentFragment.newInstance();

        // on below line creating a fragment transaction and initializing it.
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // on below line replacing the fragment in child container with child fragment.
        transaction.replace(R.id.mechContainerView, overviewFragment).commit();
        UnitCatalogAdapter catalogAdapter = new UnitCatalogAdapter(this);
        RecyclerView recyclerView = view.findViewById(R.id.catalog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(catalogAdapter);

        ForceListAdapter forceAdapter = new ForceListAdapter(this, forceListKey, forceType);
        if (forceListKey < 0) forceListKey = forceAdapter.getForceKey();
        recyclerView = view.findViewById(R.id.units);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(forceAdapter);

        EditText forceName = view.findViewById(R.id.forceName);
        forceName.setText(forceAdapter.getForceName());
        forceName.addTextChangedListener(forceAdapter);
        ((TextView) view.findViewById(R.id.force_type)).setText(forceAdapter.getTypeDescriptor());
        Button addUnit = view.findViewById(R.id.addUnit);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.removeUnit);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.add_group);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.del_group);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.overview);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.armour);
        addUnit.setOnClickListener(this);

        addUnit = view.findViewById(R.id.equipment);
        addUnit.setOnClickListener(this);
        addUnit = view.findViewById(R.id.reset);
        addUnit.setOnClickListener(this);

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
        unitTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                View viewMain = getView();
                if (viewMain!=null)setupUnitCatalog(viewMain);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                View viewMain = getView();
                if (viewMain!=null)setupUnitCatalog(viewMain);
            }
        });
//        unitTypeFilter.setOnItemSelectedListener();
        Button filter = view.findViewById(R.id.filter);
        filter.setOnClickListener(this);
        // Inflate the layout for this fragment
        return view;
    }

    private void setupUnitCatalog(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.catalog);
        UnitCatalogAdapter catalogAdapter = (UnitCatalogAdapter) recyclerView.getAdapter();
        String name = ((EditText) view.findViewById(R.id.name_filter)).getText().toString();
        IUnitHeader.UnitType type = (IUnitHeader.UnitType) ((Spinner) view.findViewById(R.id.unit_type)).getSelectedItem();
        if (catalogAdapter != null) catalogAdapter.refreshCatalog(name, type);
    }

    @Override
    public void onClick(View view) {
        View mainView = this.getView();
        assert mainView != null;
        if (view.getId() == R.id.addUnit) {
            RecyclerView recyclerView = mainView.findViewById(R.id.catalog);
            int designKey = ((UnitCatalogAdapter) recyclerView.getAdapter()).getSelectedKey();
            recyclerView = mainView.findViewById(R.id.units);
            IUnitData data = null;
            ForceListAdapter adapter = ((ForceListAdapter) recyclerView.getAdapter());
            if (adapter != null) data = adapter.addUnit(designKey);
            EditPilotDialogFragment pilot;
            if (data!=null) pilot = new EditPilotDialogFragment(data.getPilot());
            else pilot = new EditPilotDialogFragment();

            pilot.show(this.getParentFragmentManager(), "Edit Pilot");
            MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
            mViewModel.setMech(data);
        } else if (view.getId() == R.id.removeUnit) {
            RecyclerView recyclerView = mainView.findViewById(R.id.units);
            ForceListAdapter adapter = ((ForceListAdapter) recyclerView.getAdapter());
            if (adapter != null) adapter.removeUnit();
        } else if (view.getId() == R.id.add_group) {
            EditOrgUnitFragment pilot = new EditOrgUnitFragment();
            pilot.setCallback(this);
            pilot.show(this.getParentFragmentManager(), "Add Org Unit");

        } else if (view.getId() == R.id.del_group) {
            RecyclerView recyclerView = mainView.findViewById(R.id.units);
            ForceListAdapter adapter = ((ForceListAdapter) recyclerView.getAdapter());
            if (adapter != null) adapter.removeUnit();
        } else if (view.getId() == R.id.overview) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, overviewFragment).commit();

        } else if (view.getId() == R.id.armour) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, armourFragment).commit();

        } else if (view.getId() == R.id.equipment) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            // on below line replacing the fragment in child container with child fragment.
            transaction.replace(R.id.mechContainerView, equipFragment).commit();

        } else if (view.getId() == R.id.reset) {
            MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);

            // undo any damage to the mech
            clearDamage(mViewModel.getMech());
        } else if (view.getId() == R.id.filter) {
            // regenerate the catalog based on the filter values
            setupUnitCatalog(getView());
        }
    }

    private void clearDamage(IUnitDesign unit) {
        if (unit instanceof IUnitData) {
            for (OVSegment seg : unit.getSegments()) {
                seg.reset();
            }

            try (OVDatabaseForce forceDb = new OVDatabaseForce(getActivity())) {
                forceDb.updateUnitData((IUnitData) unit);
            }
            resetData(null);
        }
    }

    public View resetData(View pView) {
        View view = getView();
        if (view == null)view = pView;
        if (view != null) {
            RecyclerView recyclerView = view.findViewById(R.id.catalog);
            ((UnitCatalogAdapter) (recyclerView.getAdapter())).clearSelected();
            recyclerView = view.findViewById(R.id.units);
            ((ForceListAdapter) (recyclerView.getAdapter())).clearSelected();
        }
        overviewFragment.resetData(pView);
        armourFragment.resetData(pView);
        equipFragment.resetData(pView);
        return pView;
    }

    public IUnitDesign getDisplayMech() {
        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        if (mViewModel.getMech() == null)
            mViewModel.setMech(OVMtfReader.readMTF(MainActivity.currentActivity.getResources().openRawResource(R.raw.archerarc_2k)));
        return mViewModel.getMech();
    }

    public WeaponListAdapter getWeaponListAdapter() {
        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        return mViewModel.getAdapter();
    }

    public void setWeaponListAdapter(WeaponListAdapter adapter) {
        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        mViewModel.setAdapter(adapter);
    }
    public void addGroup(String name) {
        View mainView = getView();
        if (mainView != null) {
            RecyclerView recyclerView = mainView.findViewById(R.id.units);
            ForceListAdapter adapter = ((ForceListAdapter) recyclerView.getAdapter());
            if (adapter != null) adapter.addGroup(name);
        }
    }
}