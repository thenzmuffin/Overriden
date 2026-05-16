package com.total.overide;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.total.overiden.EditPilotDialogFragment;
import com.total.overiden.Game;
import com.total.overiden.IMechViewer;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDesign;
import com.total.overiden.PilotHealthView;
import com.total.overiden.R;
import com.total.overiden.WeaponListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OVOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OVOverviewFragment extends Fragment implements IRefreshFragment, View.OnTouchListener {
private Fragment parent = null;
    public OVOverviewFragment() {
        super();
        // Required empty public constructor
    }

    private void setParent(Fragment par) {
        parent = par;
    }
    public static OVOverviewFragment newInstance(Fragment parent) {
        OVOverviewFragment fragment = new OVOverviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setParent(parent);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return resetData(inflater.inflate(R.layout.fragment_o_v_overview, container, false));
    }

    @Override
    public View resetData(View pView) {
        View view;
        boolean editable = true;
        if (pView == null) view = this.getView();
        else view = pView;
        if (parent != null && view != null) { // view might be null if the fragment hasn't yet been inflated

            PilotHealthView pilot = view.findViewById(R.id.pilotHealthView);
            IUnitDesign mech = ((IMechViewer)parent).getDisplayMech(); //mViewModel.getMech();
            if (mech instanceof IUnitData) {
                pilot.setPilot(((IUnitData) mech).getPilot());
                pilot.setVisibility(View.VISIBLE);
                pilot.invalidate();
                pilot.setOnTouchListener(this);
                editable = false;
            } else {
                if (pilot!=null)
                    pilot.setVisibility(View.GONE);
            }

            setupTextView(view, R.id.mechID, mech.getHeader().getVariant() + " - " + mech.getHeader().getName(), editable);
            setupTextView(view, R.id.walking, Integer.toString(mech.getHeader().getWalk()), editable);
            setupTextView(view, R.id.running, Integer.toString(mech.getHeader().getRun()), editable);
            setupTextView(view, R.id.jumping, Integer.toString(mech.getHeader().getJump()), editable);
            setupTextView(view, R.id.heatSinks, getHeatSinkDisplay(mech), editable);

            RecyclerView recyclerView = view.findViewById(R.id.equipmentList);
            if (recyclerView!=null) {
                WeaponListAdapter weaponAdapter = new WeaponListAdapter(mech.getWeapons(), mech.getEquipment());
                ((IMechViewer) parent).setWeaponListAdapter(weaponAdapter);

                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(weaponAdapter);
            }
        }
        // Inflate the layout for this fragment
        return view;
    }
    private String getHeatSinkDisplay(IUnitDesign mech){
        // display either TW heatsinks, OV heatsinks or both if we aren't in a game
        String textOut = "";
        if (Game.current == null || Game.current.isSmartHeat()){
            int sinks = mech.getHeader().getTwHeatSinks();
            textOut = Integer.toString(sinks);
            if (mech.getHeader().isDoubleHeatSinks()){
                textOut += " (" + sinks * 2 + ")";
            }
        }
        if (Game.current == null || !Game.current.isSmartHeat()){
            if (Game.current == null) textOut += " : ";
            textOut += Integer.toString(mech.getHeader().getHeatSinks());
        }
        return textOut;
    }

    private static void setupTextView(View view, int rID, String update, boolean editable) {
        TextView text = view.findViewById(rID);
        if (text != null) {
            text.setText(update);
            if (editable) {
                text.setFocusable(false);
                text.setClickable(false);
            }
        }
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.performClick();
        IUnitData mech = (IUnitData)((IMechViewer)parent).getDisplayMech();
        if (mech != null) {
            EditPilotDialogFragment pilot = new EditPilotDialogFragment(mech.getPilot());
            pilot.show(this.getParentFragmentManager(), "Edit Pilot");
        }
        return false;
    }

}