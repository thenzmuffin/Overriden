package com.total.overide;

import static android.view.View.GONE;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.total.overiden.FacingButton;
import com.total.overiden.Game;
import com.total.overiden.IEquipment;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.OptionButton;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.TooltipDialogFragment;
import com.total.overiden.TurnPhaseAdapter;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.TwoDSix;
import com.total.overiden.TwoDSixView;
import com.total.overiden.UpdatePlayerActions;


public class UnitDisplayPhysical extends UnitDisplay {

    public UnitDisplayPhysical(UnitData data, int forceNumber){
        super(data,forceNumber);
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        super.setDisplayFields(itemView, callback, fragment);
        setTargetList(Game.current.getForce(forceNumber==0?1:0),itemView );
        setTargetDetails(itemView,callback);
        updateTarget(itemView.findViewById(R.id.target));

        // set display numbers for target number calculation
        TextView text = itemView.findViewById(R.id.skill_display);
        String desc = Integer.toString(unit.getPilot().getPilotSkill());
        text.setText(desc);
        text = itemView.findViewById(R.id.amm_display);
        desc = Integer.toString(unit.getTurn().getAMM());
        text.setText(desc);

        // only display the buttons if a physical attack type has been selected
        setVisibilityForTargetViews(unit.getTurn().getPhysicalAttack() != null, itemView);
        itemView.findViewById(R.id.miss_button).setOnClickListener(callback);
        itemView.findViewById(R.id.hit_button).setOnClickListener(callback);
        itemView.findViewById(R.id.auto_button).setOnClickListener(callback);

        // should the location button be displayed for the current physical attack type?
        displayHitLocation(itemView);
        setupAttackOptions(itemView);

    }

    public void setTargetDetails(View itemView, View.OnClickListener callback){
        super.setTargetDetails(itemView,callback);
        TargetWeapon phys = unit.getTurn().getPhysicalAttack();
        if (phys!=null) {
            phys.calculateTargetNumber();
            int toHit = phys.getToHit();
            ((TextView) itemView.findViewById(R.id.weapon_name)).setText(phys.getWeapon().getName());
            String desc = Integer.toString(unit.getPhysicalAttackDamage(((PhysicalWeapon)phys.getWeapon()).getPhysType()));
            ((TextView) itemView.findViewById(R.id.weapon_damage)).setText(desc);
            ((TextView) itemView.findViewById(R.id.weapon_to_hit)).setText(toHit > 12 ? "None" : Integer.toString(toHit));
            itemView.findViewById(R.id.weapon_to_hit).setOnClickListener(callback);
        }
    }
    public void displayTooltip(TargetWeapon weapon){
        TooltipDialogFragment tooltip = new TooltipDialogFragment(weapon.calculateTargetNumberTooltip());

        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
        tooltip.show(mgr, "To Hit Details");
    }
//    public void setStatusComplete(View itemView, boolean complete){
//
//    }
    public boolean setPhaseComplete(View itemView, boolean complete){
        return complete;
    }
//    public void updateScreen(View itemView){
//
//    }
    public void updateTarget(View itemView){
        TargetData targetData = unit.getTurn().getPrimaryTarget();
        // set the tmm display
        TextView text = itemView.findViewById(R.id.tmm_display);
        if (text!=null) {
            String desc = Integer.toString(targetData.getTargetMovementMod());
            text.setText(desc);
        }
//        // set up the facing button
//        FacingButton facing = itemView.findViewById(R.id.facing);
//        if (facing!=null) {
//            facing.setBasicOptions(unit.getHeader().getType());
//            facing.setSelectedMode(targetData.getFacing());
//            facing.setOnClickListener(callback);
//        }

        // update other selection
        Spinner otherSpinner = itemView.findViewById(R.id.other);
        if (otherSpinner!=null) {
            int other = targetData.getOther();
            otherSpinner.setSelection(other + 3);
        }
    }
    @Override
    public void setListCallbacks(View itemView, AdapterView.OnItemSelectedListener callback) {
        super.setListCallbacks(itemView,callback);
        Spinner spin = itemView.findViewById(R.id.physical_type);
        spin.setOnItemSelectedListener(callback);
        spin = itemView.findViewById(R.id.target);
        spin.setOnItemSelectedListener(callback);
    }
    private void setVisibilityForTargetViews(boolean hasTarget, View itemView) {
        System.out.println("Unit - " + unit.getHeader().getName() + " || visibility " + hasTarget);
        setVisibilityForSingleView(R.id.weapon_name, hasTarget, itemView);
        setVisibilityForSingleView(R.id.weapon_to_hit, hasTarget, itemView);
        setVisibilityForSingleView(R.id.weapon_damage, hasTarget, itemView);
        setVisibilityForSingleView(R.id.to_hit_dice, hasTarget, itemView);
        boolean shot = false;
        if (hasTarget) {
            shot = !(unit.getTurn().getPhysicalAttack().getStatus() == TargetWeapon.ShotStatus.NOTFIRED);
        }
        setVisibilityForSingleView(R.id.hit_button, hasTarget && !shot, itemView);
        setVisibilityForSingleView(R.id.miss_button, hasTarget && !shot, itemView);
        setVisibilityForSingleView(R.id.auto_button, hasTarget && !shot, itemView);
        setVisibilityForSingleView(R.id.success, hasTarget && shot, itemView);

        setVisibilityForSingleView(R.id.to_hit_dice, hasTarget && shot, itemView);
        setVisibilityForSingleView(R.id.success, hasTarget && shot, itemView);
        setVisibilityForSingleView(R.id.hit_miss_ind, hasTarget && shot, itemView);


        if (shot) {
            TwoDSix dice = unit.getTurn().getPhysicalAttack().getRolled();
            if (dice != null) {
                TwoDSixView toHitDice = itemView.findViewById(R.id.to_hit_dice);
                toHitDice.setDice(dice, unit.getTurn().getPhysicalAttack().getToHit());
            }
            switch (unit.getTurn().getPhysicalAttack().getStatus()) {
                case HIT:
                    ((TextView) itemView.findViewById(R.id.success)).setText(R.string.hit);
                    itemView.findViewById(R.id.hit_miss_ind).setBackgroundColor(itemView.getResources().getColor(R.color.Chartreuse, null));
                    break;
                case MISS:
                    ((TextView) itemView.findViewById(R.id.success)).setText(R.string.miss);
                    itemView.findViewById(R.id.hit_miss_ind).setBackgroundColor(itemView.getResources().getColor(R.color.Red, null));
                    break;
            }
            itemView.findViewById(R.id.target).setEnabled(false);
            itemView.findViewById(R.id.physical_type).setEnabled(false);
        }
    }
    private void setVisibilityForSingleView(int id, boolean hasTarget, View itemView) {
        itemView.findViewById(id).setVisibility(hasTarget ? View.VISIBLE : View.INVISIBLE);
    }
    @Override
    public boolean processButtonClick(View view, View itemView) {
        super.processButtonClick(view, itemView);
        if (view.getId() == R.id.miss_button) {
            clickAttack("MISS", itemView);
        } else if (view.getId() == R.id.hit_button) {
            clickAttack("HIT", itemView);
        } else if (view.getId() == R.id.auto_button) {
            clickAttack("AUTO", itemView);
        } else if (view.getId() == R.id.weapon_to_hit){
            TargetWeapon phys = unit.getTurn().getPhysicalAttack();
            if (phys!=null)
                displayTooltip(phys);
        }
        return true;
    }
    private void clickAttack(String button, View itemView) {
//            Spinner physical = itemView.findViewById(R.id.physical_type);
//            String selected = (String) physical.getSelectedItem();

        //           IWeapon physicalWeapon = new PhysicalWeapon("Type",unit.getPhysicalAttackDamage(selected), unit.getPhysicalAttackGrouping(selected));
        // 1. generate a TargetWeapon and add to the TargetData record
        TargetWeapon weapon = unit.getTurn().getPhysicalAttack();
        // make sure the location table has been updated in case the user has changed it
        String sel = ((OptionButton)itemView.findViewById(R.id.location_table)).getSelected().getKey();
        weapon.setLocationTable(TargetData.LocTable.valueOf(sel));
        if (weapon.shoot(button)) {
            // adds unit data for the target to the DB only
            UpdatePlayerActions.updateWeaponHit(unit.getTurn().getPrimaryTarget().getTarget().getTurn());
        }

        UpdatePlayerActions.updateDatabasePhysical(unit);
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
    public int[] processListSelection(int listId, View itemView){
        int[] ret = null;
        if (listId == R.id.other){
            Spinner spinner = itemView.findViewById(R.id.other);
            Object selected = spinner.getSelectedItem();
            if (selected != null) {
                // update the targetData object
                unit.getTurn().getPrimaryTarget().setOther(Integer.parseInt(selected.toString()));
                TargetWeapon phys = unit.getTurn().getPhysicalAttack();
                if (phys != null) {
                    // a physical attack has been selected so update to hit number with the new 'other' value
                    phys.calculateTargetNumber();
                    String desc = Integer.toString(phys.getToHit());
                    ((TextView) itemView.findViewById(R.id.weapon_to_hit)).setText(desc);
                }
            }
        } else if (listId == R.id.target){
            Spinner spinner = itemView.findViewById(R.id.target);
            TurnPhaseAdapter.TargetItem item = (TurnPhaseAdapter.TargetItem) spinner.getSelectedItem();

            // update the primary target to the newly selected one
            unit.getTurn().setSelectedTarget(item.getKey());
            updateTarget(itemView);

            // set the locally stored(notDB) selected target variable so that we know what was previously selected
            // this is the key of the selected item not it's index in the list
            int key = ((TurnPhaseAdapter.TargetItem) spinner.getSelectedItem()).getKey();
            unit.getTurn().setSelectedTarget(key);
            TargetWeapon weapon = unit.getTurn().getPhysicalAttack();
            if (weapon != null) {
                weapon.setTarget(unit.getTurn().getPrimaryTarget());
                updateAttackDisplay(itemView,null,weapon);
            }
        } else if (listId == R.id.physical_type){
            Spinner spinner = itemView.findViewById(R.id.physical_type);
            PhysicalWeapon.PhysicalWeaponType selected = (PhysicalWeapon.PhysicalWeaponType) spinner.getSelectedItem();
            TargetWeapon sword = unit.getTurn().getPhysicalAttack();
            if (sword!=null) {
                IWeapon club = sword.getWeapon();
                if (club instanceof PhysicalWeapon) {
                    if (((PhysicalWeapon) club).getPhysType() != selected) {
                        TargetWeapon weapon = setPhysicalAttackType(itemView, selected);
                        displayHitLocation(itemView);
                        //Adjust the weapon selected row (set the display etc)
                        // generate and display target numbers, damage etc
                        setVisibilityForTargetViews(weapon != null, itemView);
                        ret = new int[]{-1};
                    }
                }
            }

        }
        return ret; // no refresh required
    }
    private void displayHitLocation(View itemView){
        TargetWeapon weapon =unit.getTurn().getPhysicalAttack();
        PhysicalWeapon physical = null;
        if (weapon!= null)physical = (PhysicalWeapon)weapon.getWeapon();
        if (physical!=null &&physical.getHitLocationTables().size()>1){
            itemView.findViewById(R.id.location_table).setVisibility(View.VISIBLE);
            ((OptionButton)itemView.findViewById(R.id.location_table)).setOptions(physical.getHitLocationTables());
        }else{
            itemView.findViewById(R.id.location_table).setVisibility(GONE);
        }
    }
    private void setupAttackOptions(View itemView) {
        // determine which physical attacks are available this turn and setup the physical attack type dropdown
        PhysicalWeapon.PhysicalWeaponType[] types;
        // set the list of physical attack options as well
        if (unit.getTurn().getReservePhysicalAttack() != PhysicalWeapon.PhysicalWeaponType.NONE) {
            // physical attack type was nominated in the movement phase so cannot be changed here
            types = new PhysicalWeapon.PhysicalWeaponType[1];
            types[0] = unit.getTurn().getReservePhysicalAttack();
            if (unit.getTurn().getPhysicalAttack() == null) {
                setPhysicalAttackType(itemView,types[0]);
            }

        } else {
            types = unit.getPhysicalWeaponTypes();
        }
        Spinner physical = itemView.findViewById(R.id.physical_type);
        ArrayAdapter<PhysicalWeapon.PhysicalWeaponType> ad = new ArrayAdapter<>(
                itemView.getContext(),
                R.layout.simple_overide_spinner_dropdown,
                types
        );
        int currentPhysicalAttack = 0;

        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        TargetWeapon weap = unit.getTurn().getPhysicalAttack();
        if (weap != null) {
            for (int i = 0; i < types.length; i++) {
                if (weap.equals(types[i])) {
                    currentPhysicalAttack = i;
                    break;
                }
            }
        }
//            setVisibilityForTargetViews(currentPhysicalAttack!=0);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        physical.setAdapter(ad);
        if (unit.getTurn().getReservePhysicalAttack() != PhysicalWeapon.PhysicalWeaponType.NONE) {
            // don't set the listener when the physical attack is already set, disable it as well
            physical.setSelection(0);
            physical.setEnabled(false);
        } else {
            physical.setSelection(currentPhysicalAttack);
        }
    }
    public TargetWeapon setPhysicalAttackType(View itemView, PhysicalWeapon.PhysicalWeaponType selected){
        // physical attack type has changed, get the current attack
        TargetWeapon weapon = unit.getTurn().getPhysicalAttack();
        PhysicalWeapon physWeapon = null;
        if (weapon!=null) physWeapon = (PhysicalWeapon) weapon.getWeapon();
        // check if the selected physical weapon needs to be updated
        if (physWeapon == null || selected != physWeapon.getPhysType()) {
            // the physical attack type has changed so create a new physical weapon, target weapon and
            if (selected== PhysicalWeapon.PhysicalWeaponType.NONE){
                // deselected a physical attack so clear the weapon.
                unit.getTurn().setPhysicalAttack(null);
                weapon = null; // reset since now have no weapon
            } else {
                // 1. generate a TargetWeapon and add to the TargetData record
                PhysicalWeapon melee = null;
                // if a weapon is being used get the detail from the equipment list
                if (selected== PhysicalWeapon.PhysicalWeaponType.HATCHET) {
                    for (IEquipment equip : unit.getEquipment()) {
                        if (equip instanceof PhysicalWeapon) {
                            melee = (PhysicalWeapon) equip;
                            break;
                        }
                    }
                } else {
                    // generate a physical attack
                    melee = new PhysicalWeapon(selected,
                            unit.getPhysicalAttackDamage(selected),
                            unit.getPhysicalAttackGrouping(selected));
                }
                // generate the new TargetWeapon
                weapon = new TargetWeapon(unit.getTurn().getPrimaryTarget(), melee);
                unit.getTurn().setPhysicalAttack(weapon);
                updateAttackDisplay(itemView,selected,weapon);
                // if we have gone from no attack to a new attack so need to display the weapons
//                itemView.findViewById(R.id.weapon_to_hit).setOnClickListener(arg0 -> displayTooltip(unit.getTurn().getPhysicalAttack()));
            }
        }
        return weapon;
    }
    private void updateAttackDisplay(View itemView, PhysicalWeapon.PhysicalWeaponType weaponType, TargetWeapon weapon){
        PhysicalWeapon.PhysicalWeaponType selected;
        if (weaponType !=null) selected = weaponType;
        else {
            // attack type not passed in so get from the screen
            Spinner typeSpinner = itemView.findViewById(R.id.physical_type);
            selected = (PhysicalWeapon.PhysicalWeaponType) typeSpinner.getSelectedItem();
        }
        ((TextView) itemView.findViewById(R.id.weapon_name)).setText(selected.toDisplay());
        String desc = Integer.toString(unit.getPhysicalAttackDamage(selected));
        ((TextView) itemView.findViewById(R.id.weapon_damage)).setText(desc);
        int toHit = weapon.getToHit();
        ((TextView) itemView.findViewById(R.id.weapon_to_hit)).setText(toHit > 12 ? "None" : Integer.toString(toHit));
    }
}
