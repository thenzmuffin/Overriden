package com.total.artificial;

import static android.view.View.GONE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overide.OVHeader;
import com.total.overide.OVRange;
import com.total.overiden.DamageRecordAdapter;
import com.total.overiden.FacingButton;
import com.total.overiden.Game;
import com.total.overiden.HeatView;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.LockButton;
import com.total.overiden.MainActivity;
import com.total.overiden.MoveButton;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.Turn;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.UnitMove;
import com.total.overiden.UnitTypeIconView;
import com.total.overiden.UpdatePlayerActions;

public class ArtificialPhasePanel extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ArtificialPilot unit;
    private int unitIndex;
    private Turn.Phase phase;
    private Fragment topLevel= null;
    public ArtificialPhasePanel() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameSingleForceFragment.
     */
    public static ArtificialPhasePanel newInstance( int unit_index, Fragment topLevel, Turn.Phase phase) {
        ArtificialPhasePanel fragment = new ArtificialPhasePanel();
        fragment.setUnitIndex(unit_index);
        fragment.setTopLevel(topLevel);
        fragment.setPhase(phase);
//        fragment.unit = unit;
        return fragment;
    }

    public void setTopLevel(Fragment top) {
        topLevel = top;
    }
    public void setPhase(Turn.Phase phase){
        this.phase = phase;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState!=null) {
            int key = savedInstanceState.getInt("UNIT_KEY", -1);
            if (key >= 0 && unitIndex == key) {
                setUnitIndex(key);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("UNIT_KEY",unitIndex);
    }

    private int getLayoutId(){
        int layoutID;
        switch(phase){
            case MOVE:
                layoutID = R.layout.artificial_move_layout;
                break;
            case TARGET:
                layoutID = R.layout.artificial_target_layout;
                break;
            case RESOLVE:
            case SHOOT:
                layoutID = R.layout.artificial_shoot_layout;
                break;
            case PHYSICAL:
                layoutID = R.layout.artificial_physical_layout;
                break;
            default:
                layoutID = 0;
        }
        return layoutID;
    }
    private void setupPhaseScreen(View view){
        String display;

        UnitTypeIconView icon = view.findViewById(R.id.unit_icon);
        if (icon!=null)icon.setUnit(unit);
        display = unit.getHeader().getName() + " - " + unit.getPilot().getPilotName();
        ((TextView)view.findViewById(R.id.unitName)).setText(display);
        HeatView heat = view.findViewById(R.id.heat_label);
        if (heat != null) heat.setHeatLevel(unit.getState().getHeat());
        // Set the damage list if needed
        RecyclerView damage_list = view.findViewById(R.id.damage_record_list);
        if (damage_list != null) {
            DamageRecordAdapter phaseAdapter = new DamageRecordAdapter(unit, null, false);
            damage_list.setAdapter(phaseAdapter);
            damage_list.setLayoutManager(new LinearLayoutManager(view.getContext()));
        }
        switch (phase){
            case MOVE:
                hideNonUsableMoveButtons(view);
                setupMoveButtons(view);
                displayHexButtons(!Game.current.isHexless(),view,null);
                updateSelectedMoveType(view);
                ((CheckBox) view.findViewById(R.id.prone)).setChecked(unit.getState().isProne());
                view.findViewById(R.id.prone).setEnabled(false);
                view.findViewById(R.id.target_locked).setEnabled(false);
                break;
            case TARGET:
                unit.getTurn().generateTargetData(Game.current.getForce(0));
                TextView text = view.findViewById(R.id.skill_display);
                if (text!=null)text.setText(unit.getPilot().getGunneryDisplay());
                text = view.findViewById(R.id.amm_display);
                display = Integer.toString(unit.getTurn().getAMM());
                if (text!=null)text.setText(display);
                setupTargetFields(view);
                setupIfRecycler(view);

                updateSelectedTarget(view);
                break;
            case RESOLVE:
            case SHOOT:
                break;
            case PHYSICAL:
                TextView physText = view.findViewById(R.id.skill_display);
                if (physText!=null)physText.setText(unit.getPilot().getPilotSkillDisplay());
                physText = view.findViewById(R.id.amm_display);
                display = Integer.toString(unit.getTurn().getAMM());
                if (physText!=null)physText.setText(display);
                updatePhysicalScreen(view);
                break;
        }
    }
    public void setupIfRecycler(View passedView) {
        View view = (passedView == null) ? getView() : passedView;
        if (view != null) {
            RecyclerView recycler = view.findViewById(R.id.if_list);
            TargetData targ = null;
            if (unit.getTurn().getSelectedTarget()>=0) {
                targ = unit.getTurn().getPrimaryTarget();
            }
            recycler.setAdapter(new AiIfRangeAdapter(unit.getSpottingForTargetting((targ!=null)?targ.getTarget():null)));
            recycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        }
    }
    public void setupTargetFields(View passedView){
        View view = (passedView==null)?getView():passedView;
        if (view!=null) {
            FacingButton facing = view.findViewById(R.id.facing);
            facing.setBasicOptions(unit.getHeader().getType());
            Spinner otherSpinner = view.findViewById(R.id.other);
            if (otherSpinner != null && otherSpinner.getAdapter()==null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        view.getContext(),
                        R.array.other_values_array,
                        R.layout.simple_overide_spinner_dropdown
                );
// Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
                otherSpinner.setAdapter(adapter);
                otherSpinner.setOnItemSelectedListener(this);
            }
            view.findViewById(R.id.target_locked).setOnClickListener(this);

            setRangeButton("PB", false, view.findViewById(R.id.point_blank),this);
            setRangeButton("Sh", false, view.findViewById(R.id.short_range),this);
            setRangeButton("Me", false, view.findViewById(R.id.medium_range),this);
            setRangeButton("Lo", false, view.findViewById(R.id.long_range),this);
            setRangeButton("Ex", false, view.findViewById(R.id.extreme_range),this);
        }
    }
    private void setRangeButton(String label, boolean selected, MoveButton button, View.OnClickListener callback) {
        button.setupButton(label, "", MainActivity.currentActivity.getResources().getColor(R.color.Green, null));
        button.setChecked(selected);
        button.setOnClickListener(callback);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        // display the unit details in the top panel
        setupPhaseScreen(view);

        return view;
    }

    public void setUnitIndex(int index){
        this.unitIndex = index;
        this.unit = ((AiForceList)Game.current.getForce(1)).getPilotData(unitIndex);
        if (getView()!=null) {
            setupPhaseScreen(getView());
        }
    }
    protected void displayHexButtons(boolean display, View itemView, View.OnClickListener callback){
        itemView.findViewById(R.id.sub_five).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.sub_one).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.add_five).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.add_one).setVisibility(display?View.VISIBLE:GONE);
        itemView.findViewById(R.id.value).setVisibility(display?View.VISIBLE:GONE);
        if (display){
            itemView.findViewById(R.id.sub_five).setOnClickListener(callback);
            itemView.findViewById(R.id.sub_one).setOnClickListener(callback);
            itemView.findViewById(R.id.add_five).setOnClickListener(callback);
            itemView.findViewById(R.id.add_one).setOnClickListener(callback);
        }

        /* This method displays the movement and range hex buttons in place of the hexless versions
         */
        if (!display){
            // this is constant so don't need to worry about making them visible if it changes!
            itemView.findViewById(R.id.hexes_moved_label).setVisibility(GONE);
        } else {
            String desc = Integer.toString(unit.getAdjustedMovement(unit.getTurn().getMove()));
            ((TextView) itemView.findViewById(R.id.value)).setText(desc);
        }
    }
    public void updateScreen(){
        switch (phase){
            case MOVE:
                updateSelectedMoveType(null);
                break;
            case TARGET:
                updateSelectedTarget(getView());
                break;
            case PHYSICAL:
                updatePhysicalScreen(getView());
                break;
        }
    }
    public void updatePhysicalScreen(View view) {
        if (view!=null){
            TargetData primary = unit.getTurn().getPrimaryTarget();
            if (primary!=null){
                TextView text = view.findViewById(R.id.tmm_display);
                String display = Integer.toString(primary.getTargetMovementMod());
                text.setText(display);
            }
            view.findViewById(R.id.ai_target).setVisibility(primary!=null?View.VISIBLE:GONE);
            view.findViewById(R.id.tmm_label).setVisibility(primary!=null?View.VISIBLE:GONE);
            view.findViewById(R.id.tmm_display).setVisibility(primary!=null?View.VISIBLE:GONE);
            view.findViewById(R.id.physical_attack).setVisibility(GONE);
            view.findViewById(R.id.hit_miss_ind).setVisibility(GONE);
            view.findViewById(R.id.weapon_name).setVisibility(GONE);
            view.findViewById(R.id.weapon_to_hit).setVisibility(GONE);
            view.findViewById(R.id.weapon_damage).setVisibility(GONE);
            view.findViewById(R.id.auto_button).setVisibility(GONE);
            view.findViewById(R.id.location_table).setVisibility(GONE);
            view.findViewById(R.id.to_hit_dice).setVisibility(GONE);
            view.findViewById(R.id.success).setVisibility(GONE);

        }
    }
    public void updateSelectedTarget(View view){
        // if there was no possible target then set to locked
        if (view!=null) {
            LockButton lock = view.findViewById(R.id.target_locked);
            if (lock != null) lock.setChecked(unit.getTurn().isTargetingComplete());
        }
        if (unit.getTurn().getSelectedTarget()>=0){
            // a target has been selected so display the manually entered details
            // range / partial cover / other / facing
            if (view!=null){
//                LockButton lock = view.findViewById(R.id.target_locked);
//                if (lock!=null)lock.setChecked(unit.getUnit().getTurn().isTargetingComplete());
                targetButtons(view,false);
                TargetData data = unit.getTurn().getPrimaryTarget();
                TextView text = view.findViewById(R.id.ai_target);
                String name = data.getTarget().getHeader().getName() + " : " + data.getTarget().getPilot().getPilotName();
                text.setText(name);

                text = view.findViewById(R.id.tmm);
                String display = "TMM: " + data.getTargetMovementMod();
                text.setText(display);
                CheckBox check = view.findViewById(R.id.partial_cover);
                check.setChecked(data.isPartialCover());
                FacingButton facing = view.findViewById(R.id.facing);
                facing.setSelectedMode(data.getFacing());
                Spinner other = view.findViewById(R.id.other);
                other.setSelection(data.getOther()+3);
                RecyclerView recycler = view.findViewById(R.id.if_list);
                AiIfRangeAdapter rangeAd = (AiIfRangeAdapter) recycler.getAdapter();
                if (rangeAd!=null){
                    TargetData targ = unit.getTurn().getPrimaryTarget();
                    rangeAd.updateList(unit.getSpottingForTargetting((targ!=null)?targ.getTarget():null));
                }
            }
            // place here in case somehow this happens in the background and the range needs to be updated
            setRange(view);
        } else {
            // no target selected so hide options
            targetButtons(view,true);
        }
    }
    private void setRange(View view){
        TargetData data = unit.getTurn().getPrimaryTarget();
        if (data.getRange()<OVRange.pb){
            for (TargetData targetsShots : data.getTarget().getTurn().getAllTargets()){
                if (targetsShots.getTarget().getKey()==unit.getKey()){
                    data.setRange(targetsShots.getRange());
                }
            }
        }

        if (view!=null){
            ((RadioGroup)view.findViewById(R.id.radioGroup)).clearCheck();
            int id;
            switch (data.getRange()){
                case OVRange.pb:
                    id = R.id.point_blank;
                    break;
                case OVRange.sh:
                    id = R.id.short_range;
                    break;
                case OVRange.me:
                    id = R.id.medium_range;
                    break;
                case OVRange.lo:
                    id = R.id.long_range;
                    break;
                case OVRange.ex:
                    id = R.id.extreme_range;
                    break;
                default:
                    id = 0;
            }
            if (id>0) {
                MoveButton moveButton = view.findViewById(id);
                if (moveButton != null) moveButton.setChecked(true);
            }
        }
    }
    private void targetButtons(View view,boolean hide){

        if (view!=null){
            view.findViewById(R.id.ai_target).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.other_label).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.other).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.partial_cover).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.facing).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.range_label).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.radioGroup).setVisibility(hide?View.GONE:View.VISIBLE);
            view.findViewById(R.id.target_locked).setEnabled(!hide);
        }
    }
    public void updateSelectedMoveType(View passed){
        View view = (passed!=null)?passed:getView();
        if (view==null)return;
        UnitMove.MoveType type = unit.getTurn().getMove();
        ((RadioGroup)view.findViewById(R.id.move_mode_group)).clearCheck();
        int activeButtonID = -1;
        switch (type){
            case WALK:  activeButtonID = R.id.move_mode_walk; break;
            case RUN:   activeButtonID = R.id.move_mode_run; break;
            case CRAWL: activeButtonID = R.id.move_mode_crawl; break;
            case JUMP:  activeButtonID = R.id.move_mode_jump; break;
            case STILL: activeButtonID = R.id.move_mode_still; break;
        }
        if (activeButtonID>0) ((MoveButton)view.findViewById(activeButtonID)).setChecked(true);
        String desc = Integer.toString(unit.getCurrentTMM());
        ((TextView)view.findViewById(R.id.tmm)).setText(desc);

        ((LockButton)view.findViewById(R.id.target_locked)).setChecked(
                unit.getTurn().getMoveData().isMoveLocked()
        );
        if (topLevel instanceof TurnPhaseFragment){
            ((TurnPhaseFragment) topLevel).updateStepIndicators();
        }
    }

    protected void hideNonUsableMoveButtons(View itemView){
        // scenarios: does the mech have jump jets ?
        //            is the mech prone (if yes no jump and crawl available)
        //            has the mech lost a leg? (if yes only crawl is available)
        itemView.findViewById(R.id.move_mode_jump).setVisibility((!unit.getState().isProne() && //can't jump if you are lying down!
                !unit.getTurn().getMoveData().isStood() && //if we started the turn prone
                unit.getHeader().canJump())?View.VISIBLE:GONE); // only display jump button if the mech has jump jets!
        itemView.findViewById(R.id.move_mode_crawl).setVisibility(
                (unit.getState().isProne()||unit.getTurn().getMoveData().isStood())?View.VISIBLE:View.GONE);//Can only crawl if lying down
        itemView.findViewById(R.id.move_mode_walk).setVisibility(unit.noOfRemainingLegs() >= 2 ? View.VISIBLE : GONE);
        itemView.findViewById(R.id.move_mode_run).setVisibility(unit.noOfRemainingLegs() >= 2 ? View.VISIBLE : GONE);

    }
    protected void setupMoveButtons(View itemView) {
        ((MoveButton) itemView.findViewById(R.id.move_mode_still)).
                setupButton("Stand", " Still", 0xff101010);
        ((MoveButton) itemView.findViewById(R.id.move_mode_crawl)).
                setupButton("Crawl", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.CRAWL)), 0xffffffff);
        ((MoveButton) itemView.findViewById(R.id.move_mode_walk)).
                setupButton("Walk", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.WALK)), 0xffffffff);
        ((MoveButton) itemView.findViewById(R.id.move_mode_run)).
                setupButton("Run", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN)), 0xffffff10);
        ((MoveButton) itemView.findViewById(R.id.move_mode_jump)).
                setupButton("Jump", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.JUMP)), 0xffff1010);
    }

    @Override
    public void onClick(View view) {
        View itemView = getView();
        if (itemView!=null) {
            if (view.getId() == R.id.point_blank) {
                unit.getTurn().getPrimaryTarget().setRange(OVRange.pb);
            } else if (view.getId() == R.id.short_range) {
                unit.getTurn().getPrimaryTarget().setRange(OVRange.sh);
            } else if (view.getId() == R.id.medium_range) {
                unit.getTurn().getPrimaryTarget().setRange(OVRange.me);
            } else if (view.getId() == R.id.long_range) {
                unit.getTurn().getPrimaryTarget().setRange(OVRange.lo);
            } else if (view.getId() == R.id.extreme_range) {
                unit.getTurn().getPrimaryTarget().setRange(OVRange.ex);
            } else if (view.getId() == R.id.facing) {
                unit.getTurn().getPrimaryTarget()
                        .setFacing(((FacingButton)view).getSelectedMode());
            } else if (view.getId() == R.id.partial_cover) {
                unit.getTurn().getPrimaryTarget()
                        .setPartialCover(((CheckBox)view).isChecked());
            } else if (view.getId() == R.id.target_locked){
                // lock the targeting phase and auto determine what shots the unit is going to take
                // calculate to hit numbers
                TargetData identified = unit.getTurn().getPrimaryTarget();
                if (identified!=null) {
                    recalculateToHitNumbers(identified);
                    //if this unit is a spotter with valid IF units attached then it shouldn't shoot
                    boolean isSpotting = false;

                    if (unit.getDeck().getDeckRole() == OVHeader.UnitRole.SPOTTER) {
                        isSpotting = doSpottingStuff(identified);
                    }
                    if (!isSpotting) {
                        unit.orderWeapons();
                        // overheating rules? crit seeking vs armour piercing? ammo usage vs chance to hit?
                        unit.lockInWeapons();
                    }
                }
                unit.getTurn().setTargetingComplete(true);
                UpdatePlayerActions.targetActionCompleted(unit);
            }

        }
    }
    private boolean doSpottingStuff(TargetData spottingTarget){
        boolean isSpotting = false;
        for (TargetData targ : unit.getIfTargets()){
            // if targeting is already complete then ignore this unit
            if (!targ.getShooter().getTurn().isTargetingComplete() && targ.getRange()>=OVRange.pb){
                // we have a valid if unit
                isSpotting = true;
                // set as the primary for the IF unit and determine the weapons used
                targ.getShooter().getTurn().setSelectedTarget(spottingTarget.getTarget().getKey());
                for (TargetWeapon weapon : targ.getShooter().getTurn().getWeaponList()){
                    weapon.setTarget(targ);
                }
                targ.setIndirect(true);
                targ.setSpotter(unit);
                // get the settings from the spotter that are needed
//                TargetData primary = unit.getUnit().getTurn().getPrimaryTarget();
//                if (primary!=null)
                targ.setOther(spottingTarget.getOther());
                targ.setPartialCover(spottingTarget.isPartialCover());
                recalculateToHitNumbers(targ);
                for (IUnitData pil : Game.current.getAiForce().getAllUnits()){
                    if (pil.getKey()==targ.getShooter().getKey()){
                        ((ArtificialPilot)pil).orderWeapons();
                        ((ArtificialPilot)pil).lockInWeapons();
                        pil.getTurn().setTargetingComplete(true);
                        UpdatePlayerActions.targetActionCompleted(pil);
                    }
                }
            }
        }
        return isSpotting;
    }
    private void recalculateToHitNumbers(TargetData targetData ){
//        TargetData targetData = unit.getUnit().getTurn().getPrimaryTarget();
        for (TargetWeapon weapon : targetData.getShooter().getTurn().getWeaponList()) {
            //recalculate the to hit number if the target for this weapon isn't set
            // (ones set to this weapon are taken care of in the setOther function)
            weapon.setTarget(targetData);
            weapon.calculateTargetNumber();
            // If spotting using a narc beacon then missiles must be NARC-enabled!
            if (weapon.getTargetData().isIndirect() &&
                weapon.getWeapon().getWeaponMode() != IWeapon.WeaponMode.NARC) {
                //check if the spotter is narc beacon and make it NA
                if (targetData.getSpotterKey() == -3) {
                    // narc
                    weapon.setToHit(24); // make it N/A
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        View itemView = getView();
        if (itemView!=null) {
            Spinner spinner = itemView.findViewById(R.id.other);
            Object selected = spinner.getSelectedItem();
            if (selected != null) {
                // update the targetData object
                TargetData primary = unit.getTurn().getPrimaryTarget();
                if (primary!=null)primary.setOther(Integer.parseInt(selected.toString()));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
