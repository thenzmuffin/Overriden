package com.total.overiden;

//import android.graphics.Canvas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVSegment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EquipmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EquipmentFragment extends Fragment implements IRefreshFragment, AdapterView.OnItemSelectedListener {
//    private boolean updatable = true;

private final OVSegment.OVLocation[] ovLocations = {OVSegment.OVLocation.NONE,
        OVSegment.OVLocation.HEAD,
        OVSegment.OVLocation.LEFTARM,
        OVSegment.OVLocation.LEFTLEG,
        OVSegment.OVLocation.TORSO,
        OVSegment.OVLocation.RIGHTLEG,
        OVSegment.OVLocation.RIGHTARM};
    private final OVSegment.OVLocation[] twLocations = {OVSegment.OVLocation.NONE,
            OVSegment.OVLocation.HEAD,
            OVSegment.OVLocation.LEFTARM,
            OVSegment.OVLocation.LEFTLEG,
            OVSegment.OVLocation.LEFTTORSO,
            OVSegment.OVLocation.CENTRETORSO,
            OVSegment.OVLocation.RIGHTTORSO,
            OVSegment.OVLocation.RIGHTLEG,
            OVSegment.OVLocation.RIGHTARM};
    public EquipmentFragment() {
        // Required empty public constructor
    }

    public static EquipmentFragment newInstance() {
        EquipmentFragment fragment = new EquipmentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
//        fragment.setParent(parent);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    //    MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        // Inflate the layout for this fragment
    //    View view = resetData(inflater.inflate(R.layout.fragment_o_v_armour, container, false));
        View view = inflater.inflate(R.layout.fragment_equipment, container, false);
        setSegmentList(view);
        RecyclerView equipment_list = view.findViewById(R.id.equipment_display);
        if (equipment_list != null) {
            // can we access the "inUse" flag for the supplied force list
            EquipmentAdapter phaseAdapter = new EquipmentAdapter(true);
            equipment_list.setAdapter(phaseAdapter);
            equipment_list.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        return resetData(view);
    }
    private void setSegmentList(View view){
        Spinner locations = view.findViewById(R.id.location);
        if (locations != null /*&& locations.getAdapter()==null*/) {

            ArrayAdapter<OVSegment. OVLocation> adapter = new ArrayAdapter<OVSegment. OVLocation>(
                    view.getContext(),
                    R.layout.simple_overide_spinner_dropdown,
                    getSegmentList()
            );
// Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            locations.setOnItemSelectedListener(this);
// Apply the adapter to the spinner.
            locations.setAdapter(adapter);
        }
    }
    @Override
    public View resetData(View pView) {
         View view = pView;
         if (view == null) view = this.getView();
         if (view != null) { // may not have inflated the fragment yet
             setSegmentList(view);
             RecyclerView equip = view.findViewById(R.id.equipment_display);
             if (equip != null)
                 equip.getAdapter().notifyDataSetChanged();
         }
        return pView;
    }
    public void setLocked(boolean locked){
         View view = getView();
         if (view != null) {
             RecyclerView equipList = view.findViewById(R.id.equipment_display);
             if (equipList!=null && equipList.getAdapter() instanceof EquipmentAdapter){
                 EquipmentAdapter adapt = (EquipmentAdapter)equipList.getAdapter();
                 if (adapt!=null){
                     adapt.setEditMode(!locked);
                 }
             }
         }
    }

    public void setUpdatable(boolean updatable) {
//        this.updatable = updatable;
    }

    private OVSegment.OVLocation[] getSegmentList(){
        int segCount = 1; // 1 for the NONE entry
        MechViewModel mViewModel = new ViewModelProvider(MainActivity.currentActivity).get(MechViewModel.class);
        IUnitDesign design = mViewModel.getMech();
        if (design!=null){
            segCount += design.getSegments().size();
        }
        OVSegment.OVLocation[] list = new OVSegment.OVLocation[segCount];
        list[0] = OVSegment.OVLocation.NONE;
        if (design!=null) {
            segCount = 1;
            for (OVSegment seg : design.getSegments()) {
                list[segCount++] = seg.getLocation();
            }
        }
        return list;
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        assert getView()!=null;
        RecyclerView equipment_list = getView().findViewById(R.id.equipment_display);
        if (equipment_list != null) {
            EquipmentAdapter adapt = (EquipmentAdapter)equipment_list.getAdapter();
            assert adapt != null;
            adapt.setLocationDisplayed((OVSegment.OVLocation) adapterView.getSelectedItem());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}