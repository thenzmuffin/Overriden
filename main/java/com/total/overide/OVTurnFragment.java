package com.total.overide;

//import android.graphics.Canvas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.DamageRecord;
import com.total.overiden.DamageRecordAdapter;
import com.total.overiden.DatabaseGame;
import com.total.overiden.DisplayTargetsAdapter;
import com.total.overiden.Game;
import com.total.overiden.IMechViewer;
import com.total.overiden.IRefreshFragment;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.MechViewModel;
import com.total.overiden.R;
import com.total.overiden.ShootItemAdapter;
import com.total.overiden.TurnPhaseAdapter;
import com.total.overiden.UnitTurn;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OVTurnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OVTurnFragment extends Fragment implements IRefreshFragment, View.OnClickListener {
    private boolean locked = true;
    private int turnDisplayed = -1;
    private Fragment parent;
     public OVTurnFragment() {
        // Required empty public constructor
    }
    private void setParent(Fragment par) {
        parent = par;
    }

    public void setTurnDisplayed(int turnDisplayed) {
        this.turnDisplayed = turnDisplayed;
    }

    public static OVTurnFragment newInstance(Fragment parent) {
        OVTurnFragment fragment = new OVTurnFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setParent(parent);
        fragment.setTurnDisplayed(Game.current.getTurnNumber());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_o_v_turn, container, false);
        RecyclerView item_list = view.findViewById(R.id.shooting_list);
//        ShootItemAdapter phaseAdapter = new ShootItemAdapter((IUnitData)((IMechViewer)parent).getDisplayMech(), null);
        DisplayTargetsAdapter phaseAdapter = new DisplayTargetsAdapter(((IUnitData) ((IMechViewer) parent).getDisplayMech()).getTurn());
        item_list.setAdapter(phaseAdapter);
        item_list.setLayoutManager(new LinearLayoutManager(view.getContext()));

        item_list = view.findViewById(R.id.damage_list);
        DamageRecordAdapter damageAdapter = new DamageRecordAdapter((IUnitData) ((IMechViewer) parent).getDisplayMech(), null, true);
        item_list.setAdapter(damageAdapter);
        item_list.setLayoutManager(new LinearLayoutManager(view.getContext()));

        Button butt = view.findViewById(R.id.previous);
        butt.setOnClickListener(this);
        butt = view.findViewById(R.id.next);
        butt.setOnClickListener(this);
        return view;
    }

    @Override
    public View resetData(View pView) {
        View view = pView;
        // displaying a new mech so reset to the current turn
        turnDisplayed = Game.current.getTurnNumber();
        if (view == null) view = this.getView();
        if (view != null) { // may not have inflated the fragment yet
            RecyclerView recycler = view.findViewById(R.id.shooting_list);
            if (recycler.getAdapter() instanceof DisplayTargetsAdapter) {
                DisplayTargetsAdapter shoot = (DisplayTargetsAdapter) recycler.getAdapter();
                if (shoot != null) {
                    shoot.updateContents(((IUnitData) ((IMechViewer) parent).getDisplayMech()).getTurn());
                }
            }
            recycler = view.findViewById(R.id.damage_list);
            DamageRecordAdapter damage = (DamageRecordAdapter) recycler.getAdapter();
            if (damage != null) {
                damage.updateContents(((IUnitData) ((IMechViewer) parent).getDisplayMech()).getTurn());
            }

            TextView text = getView().findViewById(R.id.textView6);
            if (text != null) text.setText("Shooting This Turn");
            text = getView().findViewById(R.id.textView7);
            if (text != null) text.setText("This Turn's Damage");
        }
        return pView;
    }

    @Override
    public void onClick(View view) {
         String shootText = "Shooting This Turn";
         String dmgText = "This Turn's Damage";
        int turnNo = Game.current.getTurnNumber();
        if (view.getId()==R.id.previous){
            // go to previous turn info
            if (turnDisplayed>1)turnDisplayed--;

        } else if (view.getId()==R.id.next){
            // go to next turn info
            turnDisplayed++;
        }
        UnitTurn display;
        if (turnDisplayed>=turnNo) {
            turnDisplayed = turnNo;
            display = ((IUnitData) ((IMechViewer) parent).getDisplayMech()).getTurn();
        } else {
            shootText = "Shooting on Turn " + turnDisplayed;
            dmgText = "Turn " + turnDisplayed + "'s Damage";
            try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)){
                display = gameDB.loadSingleUnitTurn(Game.current,(IUnitData) ((IMechViewer) parent).getDisplayMech(),turnDisplayed);
            }
        }

        //update the display with previous turn
        RecyclerView recycler = getView().findViewById(R.id.shooting_list);
        if (recycler.getAdapter() instanceof DisplayTargetsAdapter) {
            DisplayTargetsAdapter shoot = (DisplayTargetsAdapter) recycler.getAdapter();
            if (shoot != null) {
                shoot.updateContents(display);
            }
        }
        recycler = getView().findViewById(R.id.damage_list);
        DamageRecordAdapter damage = (DamageRecordAdapter)recycler.getAdapter();
        if (damage != null) {
            damage.updateContents(display);
        }

        TextView text = getView().findViewById(R.id.textView6);
        text.setText(shootText);
        text = getView().findViewById(R.id.textView7);
        text.setText(dmgText);
    }

}