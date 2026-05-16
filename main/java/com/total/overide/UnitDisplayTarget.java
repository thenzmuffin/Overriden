package com.total.overide;

import static android.view.View.GONE;

import android.graphics.Paint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.total.overiden.FacingButton;
import com.total.overiden.Game;
import com.total.overiden.HeatView;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.LockButton;
import com.total.overiden.MainActivity;
import com.total.overiden.MoveButton;
import com.total.overiden.OptionButton;
import com.total.overiden.PhaseItemAdapter;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.TooltipDialogFragment;
import com.total.overiden.Turn;
import com.total.overiden.TurnPhaseAdapter;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.TurnViewModel;
import com.total.overiden.UpdatePlayerActions;

import java.util.ArrayList;
import java.util.List;

public class UnitDisplayTarget extends UnitDisplay {

    public UnitDisplayTarget(UnitData data, int forceNumber){
        super(data,forceNumber);
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        super.setDisplayFields(itemView,callback,fragment);
        // we need to reset these views to visible here as they can get overwritten in
        // subclasses to be invisible and if not reset erratic behaviour can be seen
        itemView.findViewById(R.id.heat_label).setVisibility(View.VISIBLE);
        itemView.findViewById(R.id.predicted_heat).setVisibility(View.VISIBLE);
        itemView.findViewById(R.id.textView9).setVisibility(View.VISIBLE);

        TextView text = itemView.findViewById(R.id.skill_display);
        String desc = Integer.toString(unit.getPilot().getGunnery());
        text.setText(desc);
        text = itemView.findViewById(R.id.amm_display);
        desc = Integer.toString(unit.getTurn().getAMM());
        text.setText(desc);

        // only needs to be done the first time
        OptionButton ecmButton = itemView.findViewById(R.id.ecm);
        ecmButton.setOnClickListener(callback);

        // set up the dropdown list for the selected target
        setTargetList(Game.current.getForce(forceNumber==0?1:0),itemView ); // generate the list of possible targets
        setTargetDetails(itemView,callback); //update the selected target details on the screen

        // if the target is marked as indirect then generate the spotter list.
        if (unit.getTurn().getPrimaryTarget().isIndirect())
            generateSpotterList(itemView.findViewById(R.id.spotter), itemView);
        else itemView.findViewById(R.id.spotter).setVisibility(View.INVISIBLE);

        // set the value of the hex range?
        TextView value = itemView.findViewById(R.id.value);
        if (value!=null){
            desc = Integer.toString(unit.getTurn().getPrimaryTarget().getRange());
            value.setText(desc);
        }
    }

    public void setStatusComplete(View itemView, boolean complete){
        itemView.findViewById(R.id.point_blank).setEnabled(!complete);
        itemView.findViewById(R.id.short_range).setEnabled(!complete);
        itemView.findViewById(R.id.medium_range).setEnabled(!complete);
        itemView.findViewById(R.id.long_range).setEnabled(!complete);
        itemView.findViewById(R.id.extreme_range).setEnabled(!complete);
//            LockButton lock = itemView.findViewById(R.id.target_locked);
//            lock.setEnabled(false);
//            lock.setOnClickListener(null);
        itemView.findViewById(R.id.target).setEnabled(!complete);
        itemView.findViewById(R.id.partial_cover).setEnabled(!complete);
        itemView.findViewById(R.id.other).setEnabled(!complete);
        itemView.findViewById(R.id.spotter).setEnabled(!complete);
        itemView.findViewById(R.id.indirect).setEnabled(!complete);
        itemView.findViewById(R.id.facing).setEnabled(!complete);

        PhaseItemAdapter phaseAdapter = (PhaseItemAdapter) ((RecyclerView) itemView.findViewById(R.id.item_list)).getAdapter();
        if (phaseAdapter != null)
            phaseAdapter.setTargetingComplete(complete);
    }
    private boolean canComplete(View itemView) {
        boolean indirectOK = true;
        // don't allow the unit to be locked if indirect with no spotter identified
        if (unit.getTurn().getPrimaryTarget().isIndirect()){
            Spinner spotter = itemView.findViewById(R.id.spotter);
            indirectOK = ((TurnPhaseAdapter.TargetItem)spotter.getSelectedItem()).getKey() != -1;
        }
        return indirectOK && unit.getTurn().getPrimaryTarget().getRange() > 0;
    }
    public boolean setPhaseComplete(View itemView){
        boolean complete = ((LockButton)itemView.findViewById(R.id.target_locked)).isChecked();
        if (!complete){
            // unlocking the target
            unit.getTurn().setTargetingComplete(false);
            setStatusComplete(itemView,false);

            UpdatePlayerActions.targetActionReverted(unit);
        } else if (canComplete(itemView)) {
            unit.getTurn().setTargetingComplete(true);
            setStatusComplete(itemView,true);

            for (TargetWeapon weapon : unit.getTurn().getWeaponList()) {
                if (weapon.getWeapon().getWeaponMode()== IWeapon.WeaponMode.AUTO){
                    // this is a defensive weapon (AMS) save it as active
                    unit.getTurn().setDefensiveWeapon(weapon);
                }
            }

            UpdatePlayerActions.targetActionCompleted(unit);
            if (Game.current.isSoundEffects())
                MainActivity.currentActivity.playSound(MainActivity.Sounds.COCKED);
        } else {
            // this unit is not in a state where it can be locked
            ((LockButton) itemView.findViewById(R.id.target_locked)).setChecked(false);
            complete = false;
        }
        return complete;
    }
    public void updateScreen(View itemView){
                //update selected TargetData from screen for :
                // other, partial cover, range, indirect fire, ecm, spotter changed
                targetUpdated(itemView);
    }
    private void targetUpdated(View itemView){
        TargetData target = unit.getTurn().getPrimaryTarget();
        Spinner spinner = itemView.findViewById(R.id.other);
        Object selected = spinner.getSelectedItem();
        if (selected != null)
            target.setOther(Integer.parseInt(selected.toString()));
        // only mechs can have partial cover so don't copy over partial ticked from a mech to a vehicle
        if (target.getTarget().getHeader().getUnitType()== IUnitHeader.UnitType.MECH)
            target.setPartialCover(((CheckBox)itemView.findViewById(R.id.partial_cover)).isChecked());
        if (((OptionButton)itemView.findViewById(R.id.indirect)).getSelectedIndex()==1){
            //indirect fire is active
            target.setIndirect(true);
            Spinner spotter = itemView.findViewById(R.id.spotter);
            target.setSpotterKey(((TurnPhaseAdapter.TargetItem)spotter.getSelectedItem()).getKey());
        } else {
            target.setIndirect(false);
        }
        // move already updated from the adapter

        //facing
        target.setFacing(((FacingButton)itemView.findViewById(R.id.facing)).getSelectedMode());

        //ecm
        setDisplayECM(itemView);
    }

    public void changeTarget(View itemView, View.OnClickListener callback) {
        Spinner selection = itemView.findViewById(R.id.target_list);
        // callback for when the selected target has changed
        TargetData oldTarget = unit.getTurn().getPrimaryTarget();
//                set the new target
        TurnPhaseAdapter.TargetItem item = (TurnPhaseAdapter.TargetItem) selection.getSelectedItem();

        // update the primary target to the newly selected one
        unit.getTurn().setSelectedTarget(item.getKey());
        // get the new targets range
        TargetData target = unit.getTurn().getPrimaryTarget();
        // populate the new target with user selected values if they have not yet been set previously
        if (target.getRange()<OVRange.pb && oldTarget!=null){
            // range wasn't selected on the new target, set all setable values the same as the old target
            target.setRange(oldTarget.getRange());
            target.setOther(oldTarget.getOther());
            // only mechs can have partial cover so don't copy over partial ticked from a mech to a vehicle
            if (target.getTarget().getHeader().getUnitType()== IUnitHeader.UnitType.MECH)
                target.setPartialCover(oldTarget.isPartialCover());
            target.setFacing(oldTarget.getFacing());
            target.setIndirect(oldTarget.isIndirect());
            target.setEcmBubble(oldTarget.getEcmBubble());
            // set the spotter as well if it was selected?? ??What about tagged or narced targets???
            if (target.isIndirect())target.setSpotter(oldTarget.getSpotter());
        }
        setTargetDetails(itemView,callback);

        //setDisplayECM();
    }
    public void setTargetDetails(View itemView, View.OnClickListener callback){
        super.setTargetDetails(itemView,callback);
        /*
         * This method is required everytime the target is changed. We need to:
         *   update header fields for target (TMM, partial, range, other, facing)
         *   This method does update the weapons list for unlocked weapons only
         */
        TargetData targetData = unit.getTurn().getPrimaryTarget();
        // partial cover flag
        // only mechs can have partial cover so don't copy over partial ticked from a mech to a vehicle
        if (targetData.getTarget().getHeader().getUnitType()== IUnitHeader.UnitType.MECH) {
            ((CheckBox) itemView.findViewById(R.id.partial_cover)).setChecked(targetData.isPartialCover());
            itemView.findViewById(R.id.partial_cover).setOnClickListener(callback);
            itemView.findViewById(R.id.partial_cover).setVisibility(View.VISIBLE);
        } else {
            itemView.findViewById(R.id.partial_cover).setVisibility(View.INVISIBLE);
        }

        targetUpdateWeapons(itemView);

        // set up the range display
        displayHexButtons(!Game.current.isHexless(), itemView,callback);


        // update predicted heat
        ((HeatView) itemView.findViewById(R.id.predicted_heat)).setHeatLevel(unit.getAdjustedHeat());
        itemView.findViewById(R.id.predicted_heat).setOnClickListener(callback);
        configureIndirectTargetFields(itemView,callback);
    }
    private void recalculateToHitForWeapons(View itemView){
        // update the calculated to hit for all weapons assigned to the current target
        RecyclerView item_list = itemView.findViewById(R.id.item_list);

        PhaseItemAdapter adapter = (PhaseItemAdapter)item_list.getAdapter();
        int count = 1; // start at one to skip the header line
        for (TargetWeapon weapon : unit.getTurn().getWeaponList()) {
            if (weapon.getTargetData()==unit.getTurn().getPrimaryTarget()) {
                weapon.calculateTargetNumber();
                if (adapter!=null)adapter.notifyItemChanged(count);
            }
            count++;
        }
    }
    private void targetUpdateWeapons(View itemView){
        // update the assigned target for unlocked weapons
        RecyclerView item_list = itemView.findViewById(R.id.item_list);

        PhaseItemAdapter adapter = (PhaseItemAdapter)item_list.getAdapter();
        int count = 1;
        for (TargetWeapon weapon : unit.getTurn().getWeaponList()) {
            if (!weapon.isLocked()) {
                weapon.setTarget(unit.getTurn().getPrimaryTarget());
                weapon.calculateTargetNumber();
                if (adapter!=null)adapter.notifyItemChanged(count);
            }
            count++;
        }
    }
    protected void configureIndirectTargetFields(View itemView, View.OnClickListener callback){
        Spinner spotter = itemView.findViewById(R.id.spotter);
        OptionButton option = itemView.findViewById(R.id.indirect);
        if (unit.hasIndirectWeapons()){
            List<OptionButton.OptionButtonChoice> modes = new ArrayList<>();
            option.setVisibility(View.VISIBLE);
            Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(itemView.getResources().getColor(R.color.Gray, null));
            modes.add(new OptionButton.OptionButtonChoice("Indirect",gen));
            gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(itemView.getResources().getColor(R.color.Green, null));
            modes.add(new OptionButton.OptionButtonChoice("Indirect",gen));
            option.setOptions(modes);
            if (unit.getTurn().getPrimaryTarget()!=null)
                option.setSelectedIndex(unit.getTurn().getPrimaryTarget().isIndirect()?1:0);
            option.setOnClickListener(callback);

            spotter.setVisibility(unit.getTurn().getPrimaryTarget().isIndirect() ? View.VISIBLE : GONE);
        } else {
            option.setVisibility(GONE);
            spotter.setVisibility(GONE);
        }
    }
    private void setRangeButton(String label, boolean selected, MoveButton button, View.OnClickListener callback) {
        button.setupButton(label, "", MainActivity.currentActivity.getResources().getColor(R.color.Green, null));
        button.setChecked(selected);
        button.setOnClickListener(callback);
    }

    public boolean updateHexValue(View itemView, int adjust){
            // get the heat before changing the range
        int heatLevel = Math.max(0, unit.getAdjustedHeat());

        int range = unit.getTurn().getPrimaryTarget().getRange();
        range += adjust;
        if (range < 0) range = 0;
        unit.getTurn().getPrimaryTarget().setRange(range);
        String desc = Integer.toString(range);
        ((TextView) itemView.findViewById(R.id.value)).setText(desc);

        recalculateToHitNumbers(unit.getTurn().getPrimaryTarget(), itemView);
        RecyclerView item_list = itemView.findViewById(R.id.item_list);
        PhaseItemAdapter phaseAdapter = (PhaseItemAdapter) item_list.getAdapter();
        if (phaseAdapter != null) phaseAdapter.updateContents();

        // predicted heat level can change if a weapon is moved out of range
        return (heatLevel != Math.max(0, unit.getAdjustedHeat()));

    }
    private void displayTooltip(){
        TooltipDialogFragment tooltip = new TooltipDialogFragment(unit.getAdjustedHeatTooltip());

        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
        tooltip.show(mgr, "Turn Heat");
    }
    @Override
    public boolean processButtonClick(View view, View itemView) {
        boolean changes = false;
        if (view.getId()==R.id.heat_label) displayHeatScale();
        else if (view.getId()==R.id.ecm) switchECM(itemView);
        else if (view.getId()==R.id.predicted_heat) displayTooltip();
        else if (view.getId() == R.id.point_blank) {
            changes = updateSelectedRange(OVRange.pb,itemView);
        } else if (view.getId() == R.id.short_range) {
            changes = updateSelectedRange(OVRange.sh,itemView);
        } else if (view.getId() == R.id.medium_range) {
            changes = updateSelectedRange(OVRange.me,itemView);
        } else if (view.getId() == R.id.long_range) {
            changes = updateSelectedRange(OVRange.lo,itemView);
        } else if (view.getId() == R.id.extreme_range) {
            changes = updateSelectedRange(OVRange.ex,itemView);
        } else if (view.getId() == R.id.facing) {
            unit.getTurn().getPrimaryTarget()
                    .setFacing(((FacingButton)itemView.findViewById(R.id.facing)).getSelectedMode());
        } else if (view.getId() == R.id.partial_cover) {
            CheckBox partial = itemView.findViewById(R.id.partial_cover);
            unit.getTurn().getPrimaryTarget().setPartialCover(partial.isChecked());
        } else if (view.getId() == R.id.indirect) indirect(itemView);
        else if (view.getId()==R.id.add_five) changes = updateHexValue(itemView,5);
        else if (view.getId()==R.id.add_one) changes = updateHexValue(itemView,1);
        else if (view.getId()==R.id.sub_one) changes = updateHexValue(itemView,-1);
        else if (view.getId()==R.id.sub_five) changes = updateHexValue(itemView,-5);
        recalculateToHitForWeapons(itemView);
        return changes; // tell the screen to refresh or not
    }

    public boolean updateSelectedRange(int range, View itemView) {
        //save the heat level in case it changes due to weapons no longer being in range to fire
        int heatLevel = Math.max(0, unit.getAdjustedHeat());
        // set the new range on the target
        if (unit.getTurn().getPrimaryTarget() != null)
            unit.getTurn().getPrimaryTarget().setRange(range);
        recalculateToHitNumbers( unit.getTurn().getPrimaryTarget(), itemView);

        return (heatLevel != Math.max(0, unit.getAdjustedHeat()));
    }

    private void recalculateToHitNumbers( TargetData targetData, View itemView){
        for (TargetWeapon weapon : unit.getTurn().getWeaponList()) {
            //recalculate the to hit number if the target for this weapon isn't set
            // (ones set to this weapon are taken care of in the setOther function)
            if (!weapon.isLocked() || weapon.getTargetData() == unit.getTurn().getPrimaryTarget()) {
                if (targetData != null) weapon.setTarget(targetData);
                weapon.calculateTargetNumber();
                // If spotting using a narc beacon then missiles must be NARC-enabled!
                if (weapon.getTargetData() != null && weapon.getTargetData().isIndirect() &&
                        weapon.getWeapon().getWeaponMode() != IWeapon.WeaponMode.NARC) {
                    //check if the spotter is narc beacon and make it NA
                    Spinner spotter = itemView.findViewById(R.id.spotter);
                    if (((TurnPhaseAdapter.TargetItem) spotter.getSelectedItem()).getKey() == -3) {
                        // narc
                        weapon.setToHit(24); // make it N/A
                    }
                }
            }
        }
    }
    private void indirect(View itemView){
        // get the targetdata object
        //toggle the targetdata to be an indirect attack
        OptionButton option = itemView.findViewById(R.id.indirect);
        unit.getTurn().getPrimaryTarget().setIndirect(option.getSelectedIndex()>0);
        Spinner spotter = itemView.findViewById(R.id.spotter);

        if (!unit.getTurn().getPrimaryTarget().isIndirect()){
            // we have deselected the indirect option
            // clear the spotter selection and let any selected spotter know they aren't needed now!
            IUnitData selectedSpotter = unit.getTurn().getPrimaryTarget().getSpotter();
            if (selectedSpotter!=null){
                // tell the spotter
                unit.getTurn().getPrimaryTarget().setSpotter(null); // this auto notifies the spotter
                resetSpotterPenalties(selectedSpotter);
            }
        } else {
            generateSpotterList(spotter, itemView);
        }
        // Indirect fire must use either a spotter, TAG or be targetting a mech that has been NARCed

        spotter.setVisibility(option.getSelectedIndex()>0?View.VISIBLE:GONE);
        recalculateToHitNumbers(null,itemView);

//          At present this isn't catered for
        RecyclerView item_list = itemView.findViewById(R.id.item_list);
        PhaseItemAdapter phaseAdapter = (PhaseItemAdapter) item_list.getAdapter();
        if (phaseAdapter!=null)
            phaseAdapter.updateContents();
    }

    public void resetSpotterPenalties(IUnitData spotter){
        TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
        int index = 0;
        for (TargetWeapon weap : spotter.getTurn().getWeaponList()) {
            weap.calculateTargetNumber();
        }
        for (IUnitData iter : model.getForceList(forceNumber).getAllUnits()) {
            if (iter==spotter) {
                // how do we find the adapter?
//                notifyItemChanged(index);
                break;
            }
            index++;
        }
    }
    private void switchECM(View itemView){
        TargetData targ = unit.getTurn().getPrimaryTarget();
        OptionButton ecmButton = itemView.findViewById(R.id.ecm);
        if (targ!=null && ecmButton!=null){
            targ.setEcmBubble(ecmButton.getSelectedIndex());
        }
    }
    private void displayHeatScale(){
        String description;
        if (Game.current.isSmartHeat())
            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.heat_scale);
        else
            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.ov_heat_scale);
        TooltipDialogFragment tooltip = new TooltipDialogFragment(description);;
        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
        tooltip.show(mgr, "Turn Heat");
    }
    protected void displayHexButtons(boolean display, View itemView, View.OnClickListener callback){
        // super eityher hides or displays the range in hexes buttons
        super.displayHexButtons(display, itemView,callback);

        /* This method displays the range hex buttons in place of the hexless versions
         */
        if (!display) {
            // if using the range buttons
            TargetData targetData = unit.getTurn().getPrimaryTarget();
            int range = targetData.getRange();
            setRangeButton("PB", range == OVRange.pb, itemView.findViewById(R.id.point_blank),callback);
            setRangeButton("Sh", range == OVRange.sh, itemView.findViewById(R.id.short_range),callback);
            setRangeButton("Me", range == OVRange.me, itemView.findViewById(R.id.medium_range),callback);
            setRangeButton("Lo", range == OVRange.lo, itemView.findViewById(R.id.long_range),callback);
            setRangeButton("Ex", range == OVRange.ex, itemView.findViewById(R.id.extreme_range),callback);
            if (range < OVRange.pb) {
                ((RadioGroup) itemView.findViewById(R.id.radioGroup)).clearCheck();
            }
        } else {
            // hide the range buttons and set the displayed value in the range field.
            itemView.findViewById(R.id.point_blank).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.short_range).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.medium_range).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.long_range).setVisibility(View.INVISIBLE);
            itemView.findViewById(R.id.extreme_range).setVisibility(View.INVISIBLE);
            // set the value of the hex range?
            TextView value = itemView.findViewById(R.id.value);
            if (value!=null){
                String desc = Integer.toString(unit.getTurn().getPrimaryTarget().getRange());
                value.setText(desc);
            }
        }
    }

    private void setDisplayECM(View itemView) {
        OptionButton ecmButton = itemView.findViewById(R.id.ecm);
        // game option can switch ecm off if no units have it activated (i.e. only display the
        // ECM button if a unit has ECM active or if the option for external ECM has been switched on)
        if (Game.current.isExternalECM() || Game.current.isUnitECMActive()){
            ecmButton.setVisibility(View.VISIBLE);
            Paint gen;
            List<OptionButton.OptionButtonChoice> modes = new ArrayList<>();
            if (unit.getTurn().getPrimaryTarget()==null ||
                    !unit.getTurn().getPrimaryTarget().getTarget().getState().isEcmActive()) {
                 //set listener
                // if the currently targetted unit has ECM active then they are always in the bubble!
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(itemView.getResources().getColor(R.color.Gray, null));
                modes.add(new OptionButton.OptionButtonChoice("No ECM    Impact", gen));
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(itemView.getResources().getColor(R.color.Orange, null));
                modes.add(new OptionButton.OptionButtonChoice("Intervenin ECM", gen));
            }else if (unit.getTurn().getPrimaryTarget()!=null){
                unit.getTurn().getPrimaryTarget().setEcmBubble(2);//set the value to fixed now
                ecmButton.setOnClickListener(null);
            }
            gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(itemView.getResources().getColor(R.color.PaleVioletRed, null));
            modes.add(new OptionButton.OptionButtonChoice("Enemy in  ECM Bubble",gen));
            ecmButton.setOptions(modes);

        } else {
            ecmButton.setVisibility(GONE);
            ecmButton.setOnClickListener(null);
        }
    }

    private void generateSpotterList(Spinner spotter, View itemView) {
        TurnPhaseAdapter.TargetItem[] targets = null;
        List<TurnPhaseAdapter.TargetItem> spotterList = new ArrayList<>();
        // population of the list of valid targets

        if (unit != null) {
            TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
            if (unit.getTurn().getPrimaryTarget().getTarget().getTurn().isTaggedThisTurn()){
                //if the target has been tagged no other spotter needs to be designated
                spotterList.add(new TurnPhaseAdapter.TargetItem("TAG",-2,0));
            } else {
                // construct a list of potential spotters
                spotterList.add(new TurnPhaseAdapter.TargetItem("None", -1, 0));
                // check for NARC pod attached to target
                if (unit.getTurn().getPrimaryTarget().getTarget().isNarced()){
                    spotterList.add(new TurnPhaseAdapter.TargetItem("NARC Pod",-3,0));
                }
                for (IUnitData data : model.getForceList(forceNumber).getAllUnits()) {
                    if (data != unit && data.getTurn().getReservePhysicalAttack()== PhysicalWeapon.PhysicalWeaponType.NONE) {
                        spotterList.add(new TurnPhaseAdapter.TargetItem(data.getHeader().getName(), data.getKey(), 0));
                    }
                }
            }
            targets = new TurnPhaseAdapter.TargetItem[spotterList.size()];
            int i = 0;
            int selectedItem = 0;
            for (TurnPhaseAdapter.TargetItem item : spotterList){
                if (item.getKey()==unit.getTurn().getPrimaryTarget().getSpotterKey())selectedItem = i;
                targets[i++] = item;
            }
            ArrayAdapter<TurnPhaseAdapter.TargetItem> ad = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.simple_overide_spinner_dropdown,
                    targets
            );
            // Set simple layout resource file for each item of spinner
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
            spotter.setAdapter(ad);
            spotter.setSelection(selectedItem);
//            spotter.setOnItemSelectedListener(this);
        }
    }
    @Override
    public void setListCallbacks(View itemView, AdapterView.OnItemSelectedListener callback) {
        super.setListCallbacks(itemView,callback);
        Spinner spotter = itemView.findViewById(R.id.spotter);
        spotter.setOnItemSelectedListener(callback);
        Spinner targetSpin = itemView.findViewById(R.id.target);
        targetSpin.setOnItemSelectedListener(callback);
    }
    public int[] processListSelection(int listId, View itemView){
        super.processListSelection(listId,itemView);
        int[] ret = null;
        Spinner spin = itemView.findViewById(listId);
        if (listId == R.id.spotter) {

            // the selected spotter has changed!
            // 1) if there was a spotter previously selected then recalculate it's to hit
            // numbers since it is no longer spotting
            // 2) Recalculate the to hit number for the new spotter
            // 3) Recalculate the to hit numbers for the current unit with new spotter info
            TurnPhaseAdapter.TargetItem item = (TurnPhaseAdapter.TargetItem) spin.getSelectedItem();

            TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
            IUnitData oldSpotter = unit.getTurn().getPrimaryTarget().getSpotter();
            // find the selected spotter to add
            if (item.getKey() < 0)// NONE or TAG (shouldn't ever be tag as this would be the only item in the list
                unit.getTurn().getPrimaryTarget().setSpotter(null);

            // recalculate to hits for the old spotter and the new one (allow for either to be null)
            int oldIndex = model.getForceList(forceNumber).getIndexByKey(oldSpotter.getKey(), Turn.Phase.TARGET);; // used to capture the list index of the previous spotter
            int index = model.getForceList(forceNumber).getIndexByKey(item.getKey(), Turn.Phase.TARGET); // track the number in the list to recalculate to hit numbers
            int count = 0;
            for (IUnitData data : model.getForceList(forceNumber).getAllUnits()) {
                if (data.getKey() == item.getKey()) {
                    unit.getTurn().getPrimaryTarget().setSpotter(data);
                    // recalculate all the to hits for this unit
                    for (TargetWeapon weap : data.getTurn().getWeaponList())
                        weap.calculateTargetNumber();
                    // returning true would refresh this item in the list but we actually want to
                    // refresh the spotters panel otherwise this gets into an infinite loop
//                    index = count;
                    // if the spotter is already locked then resend updated to hit numbers
                    if (data.getTurn().isTargetingComplete())
                        UpdatePlayerActions.targetActionCompleted(data);
                    break;
                }
//                if (oldSpotter != null && data.getKey() == oldSpotter.getKey())
//                    oldIndex = count;
                count++;
            }
            if (oldSpotter!=null) {// could be null if there was no spotter before
                for (TargetWeapon weap : oldSpotter.getTurn().getWeaponList())
                    weap.calculateTargetNumber();
                // if the old spotter is already set to targetting complete then send to other device again!
                if (oldSpotter.getTurn().isTargetingComplete())
                    UpdatePlayerActions.targetActionCompleted(oldSpotter);
            }
            count = 0;
            if(index>=0)count++;
            if(oldIndex>=0)count++;
            ret = new int[count];
            if (oldIndex>0)ret[--count] = oldIndex;
            if (index>=0)ret[0] = index;
            // recalculate to hit numbers after changing the spotter (different AMM etc)
            recalculateToHitNumbers(unit.getTurn().getPrimaryTarget(), itemView);

            // redraw the list of weapons
            // check this is needed, too many updates!
            RecyclerView item_list = itemView.findViewById(R.id.item_list);
            PhaseItemAdapter phaseAdapter = (PhaseItemAdapter) item_list.getAdapter();
            if (phaseAdapter != null)
                phaseAdapter.updateContents();

        } else if (listId==R.id.target){
            // new Target selected
            // get the range/ other value / partial cover that was already set against the previous target
            TargetData oldTarget = unit.getTurn().getPrimaryTarget();
//                set the new target
            TurnPhaseAdapter.TargetItem item = (TurnPhaseAdapter.TargetItem) spin.getSelectedItem();

            // update the primary target to the newly selected one
            unit.getTurn().setSelectedTarget(item.getKey());
            // get the new targets range
            TargetData target = unit.getTurn().getPrimaryTarget();
            int newTargetRange = target.getRange();
            TargetData.LocTable loc = target.getFacing();
            ((FacingButton) itemView.findViewById(R.id.facing)).setSelectedMode(loc);
            //update with previously saved other and partial cover values
            ((CheckBox) itemView.findViewById(R.id.partial_cover)).setChecked(target.isPartialCover());
            Spinner otherSpinner = itemView.findViewById(R.id.other);
            otherSpinner.setSelection(target.getOther() + 3);
            // if the new target has no range set then keep the previously selected values
            if (newTargetRange < OVRange.pb) {
                newTargetRange = oldTarget.getRange();//not yet set
                target.setIndirect(oldTarget.isIndirect());
                if (target.isIndirect()) target.setSpotter(oldTarget.getSpotter());
            }
            // used to only call if the range is changed between the different targets
            // however if the TMM is different this would result in to hit numbers not
            // being updated
            updateSelectedRange(newTargetRange, itemView);

            TextView tmmDisp = itemView.findViewById(R.id.tmm_display);
            String desc = Integer.toString(target.getTargetMovementMod());
            tmmDisp.setText(desc);
            setDisplayECM(itemView);
            targetUpdateWeapons(itemView);
            if (target.getTarget().getHeader().getUnitType()!=oldTarget.getTarget().getHeader().getUnitType()){
                // when the unit type changes we might need to redraw the panel as the partial cover checkbox might come or go
                ret = new int[1];
                ret[0] = -1;//less than 0 value refreshes the current panel
            }
        } else if (listId == R.id.other) {
            Spinner spinner = itemView.findViewById(R.id.other);
            Object selected = spinner.getSelectedItem();
            if (selected != null) {
                // update the targetData object
                unit.getTurn().getPrimaryTarget().setOther(Integer.parseInt(selected.toString()));
            }
            recalculateToHitForWeapons(itemView);
        }
        return ret;
    }

}
