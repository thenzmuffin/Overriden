package com.total.overide;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.total.overiden.IMechViewer;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitDesign;
//import com.total.overiden.MechViewModel;
import com.total.overiden.R;
import com.total.overiden.WeaponListAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigTicsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigTicsFragment extends Fragment implements IRefreshFragment {
    private Fragment parent;


    public ConfigTicsFragment() {
        super();
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfigTicsFragment.
     */
    private void setParent(Fragment par) {
        parent = par;
    }
    public static ConfigTicsFragment newInstance(Fragment par) {
        ConfigTicsFragment fragment = new ConfigTicsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setParent(par);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return resetData(inflater.inflate(R.layout.fragment_config_tics, container, false));
    }

    public View resetData(View pView) {
        View view = null;
        if (pView==null)view = this.getView();
        else view = pView;
//        MechViewModel mViewModel = new ViewModelProvider(parent).get(MechViewModel.class);
//        if (mViewModel.getMech() == null)
//            mViewModel.setMech(OVMtfReader.readMTF(getContext().getResources().openRawResource(R.raw.archerarc_2k)));
        IUnitDesign mech = ((IMechViewer)parent).getDisplayMech();//mViewModel.getMech();
        WeaponListAdapter weaponAdapter = ((IMechViewer)parent).getWeaponListAdapter();
        if (weaponAdapter == null) {
            weaponAdapter = new WeaponListAdapter(mech.getWeapons(), mech.getEquipment());
            ((IMechViewer)parent).setWeaponListAdapter(weaponAdapter);
//                mViewModel.setAdapter(new WeaponListAdapter(mech.getWeapons(), mech.getEquipment()));
        }
        weaponAdapter.updateContents(mech.getWeapons(), mech.getEquipment());
//        if (mViewModel.getAdapter() == null) {
//            mViewModel.setAdapter(new WeaponListAdapter(mech.getWeapons(), null));
//        } else
//            ((WeaponListAdapter) mViewModel.getAdapter()).updateContents(mech.getWeapons(), null);

        if (view != null) {
            RecyclerView recyclerView = view.findViewById(R.id.weaponList);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new OVWeaponAdapter(mech, this));
            }
            recyclerView = view.findViewById(R.id.ticList);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(weaponAdapter);
            }
        }
        return view;
    }
    public void refreshTicList(){

//        MechViewModel mViewModel = new ViewModelProvider(parent).get(MechViewModel.class);
        WeaponListAdapter weaponAdapter = ((IMechViewer)parent).getWeaponListAdapter();
        weaponAdapter.updateContents(((IMechViewer)parent).getDisplayMech().getWeapons(), null);
        RecyclerView recyclerView = this.getView().findViewById(R.id.ticList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(weaponAdapter);
        recyclerView.invalidate();
//        recyclerView.refreshDrawableState();
    }
}