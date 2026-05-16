package com.total.overide;

//import android.graphics.Canvas;
import android.os.Bundle;

//import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.total.overiden.Game;
import com.total.overiden.IMechViewer;
import com.total.overiden.IRefreshFragment;
//import com.total.overiden.MechViewModel;
import com.total.overiden.IUnitData;
import com.total.overiden.R;
import com.total.overiden.UpdatePlayerActions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OVArmourFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OVArmourFragment extends Fragment implements IRefreshFragment {
    private boolean locked = true;
    private Fragment parent;
     public OVArmourFragment() {
        // Required empty public constructor
    }
    private void setParent(Fragment par) {
        parent = par;
    }
    public static OVArmourFragment newInstance(Fragment parent) {
        OVArmourFragment fragment = new OVArmourFragment();
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
    //    View view = resetData(inflater.inflate(R.layout.fragment_o_v_armour, container, false));

        return resetData(inflater.inflate(R.layout.fragment_o_v_armour, container, false));
    }

    @Override
    public View resetData(View pView) {
         View view = pView;
         if (view == null) view = this.getView();
         if (view != null) { // may not have inflated the fragment yet
             OVArmourView arm = view.findViewById(R.id.ovmech);
             if (arm != null) {
                 arm.setLocked(locked);
                 if (parent != null) {
//                 MechViewModel mViewModel = new ViewModelProvider(parent).get(MechViewModel.class);


                     arm.setMech((OVUnitDesign) ((IMechViewer) parent).getDisplayMech());
                     arm.invalidate();
                 }
             }
         }
        return pView;
    }
    public void setLocked(boolean locked){
         this.locked = locked;
         View view = getView();
         if (view != null) {
             OVArmourView arm = view.findViewById(R.id.ovmech);
             arm.setLocked(locked);
         }
         if (Game.current!=null & Game.current.getComms()!=null){
             //send update to other device for changes made
             UpdatePlayerActions.updateManualChanges((IUnitData)((IMechViewer) parent).getDisplayMech());
         }
    }
}