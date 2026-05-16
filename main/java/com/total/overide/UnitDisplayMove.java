package com.total.overide;

import static android.view.View.GONE;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.total.overiden.AddPsrDialogFragment;
import com.total.overiden.Game;
import com.total.overiden.IEquipment;
import com.total.overiden.LockButton;
import com.total.overiden.MainActivity;
import com.total.overiden.MoveButton;
import com.total.overiden.OptionButton;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.Pilot;
import com.total.overiden.R;
import com.total.overiden.Turn;
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.UnitMove;
import com.total.overiden.UpdatePlayerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnitDisplayMove extends UnitDisplay {
    public UnitDisplayMove(UnitData data, int forceNumber){
        super(data,forceNumber);
    }
    protected void setupMoveButtons(View itemView,
                                    View.OnClickListener callback){
        ((MoveButton)itemView.findViewById(R.id.move_mode_still)).
                setupButton("Stand", " Still", 0xff101010);
        ((MoveButton)itemView.findViewById(R.id.move_mode_crawl)).
                setupButton("Crawl", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.CRAWL)), 0xffffffff);
        ((MoveButton)itemView.findViewById(R.id.move_mode_walk)).
                setupButton("Walk", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.WALK)), 0xffffffff);
        ((MoveButton)itemView.findViewById(R.id.move_mode_run)).
                setupButton("Run", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN)), 0xffffff10);
        ((MoveButton)itemView.findViewById(R.id.move_mode_jump)).
                setupButton("Jump", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.JUMP)), 0xffff1010);
        //set listeners
        itemView.findViewById(R.id.move_mode_still).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_crawl).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_walk).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_run).setOnClickListener(callback);
        itemView.findViewById(R.id.move_mode_jump).setOnClickListener(callback);

    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        super.setDisplayFields(itemView,callback,fragment);
        // we need to reset these views to visible here as they can get overwritten in
        // subclasses to be invisible and if not reset erratic behaviour can be seen
        itemView.findViewById(R.id.heat_label).setVisibility(View.VISIBLE);

        setupMoveButtons(itemView,callback);
        // set the current movetype
        UnitMove.MoveType type = unit.getTurn().getMove();
        ((RadioGroup)itemView.findViewById(R.id.move_mode_group)).clearCheck();
        int activeButtonID = -1;
        switch (type){
            case WALK:  activeButtonID = R.id.move_mode_walk; break;
            case RUN:   activeButtonID = R.id.move_mode_run; break;
            case CRAWL: activeButtonID = R.id.move_mode_crawl; break;
            case JUMP:  activeButtonID = R.id.move_mode_jump; break;
            case STILL: activeButtonID = R.id.move_mode_still; break;
        }
        if (activeButtonID>0){
            MoveButton butt = itemView.findViewById(activeButtonID);
            butt.setChecked(true);
        }
        hideNonUseableMoveButtons(itemView);

        displayHexButtons(!Game.current.isHexless(), itemView, callback);
        setupTmmButton(itemView);
        ((CheckBox) itemView.findViewById(R.id.prone)).setChecked(unit.getState().isProne());
        itemView.findViewById(R.id.prone).setOnClickListener(view -> {
            // update the state when the prone value is updated
            unit.getState().setProne(((CheckBox) itemView.findViewById(R.id.prone)).isChecked());
        });

        itemView.findViewById(R.id.add_psr).setOnClickListener(callback);
        setEnhancedMoveButtons(itemView,callback);
//        setStatusComplete(unit.getTurn().getMoveData().isMoveLocked());

        setupPhysicalAttackButton(itemView);
        // reset the hexes moved value from data
        TextView value = itemView.findViewById(R.id.value);
        if (value!=null){
            value.setText(String.format(Locale.getDefault(),"%d",unit.getTurn().getMoveData().getHexesMoved()));
        }
    }
    private void setupTmmButton(View itemView){

        Pilot.EdgeSkill skill = Pilot.EdgeSkill.NONE;
        if (unit.getTurn().getMove()== UnitMove.MoveType.WALK||unit.getTurn().getMove()== UnitMove.MoveType.RUN)
            skill = unit.getPilot().hasEdge(unit.turn, Pilot.EdgeTrigger.MOVE_CHECK, Turn.Phase.MOVE, unit.getCurrentTMM() > 2);
        String desc = Integer.toString(unit.getCurrentTMM());
        TextView tmmText = itemView.findViewById(R.id.tmm);
        tmmText.setText(desc);
        if (unit.turn.getMoveData().getEdgeSpent()>0) {
            tmmText.setBackground(convertToIcon(R.drawable.edge));
            if (unit.getTurn().getMoveData().isMoveLocked()) tmmText.setOnClickListener(null);
                else tmmText.setOnClickListener(arg -> executeNimble(false, itemView));
        }else if (skill== Pilot.EdgeSkill.NONE) {
            tmmText.setBackground(null);
            tmmText.setOnClickListener(null);
        }else {
            tmmText.setBackground(convertToIcon(R.drawable.edge_grey));
            if (unit.getTurn().getMoveData().isMoveLocked()) tmmText.setOnClickListener(null);
            else tmmText.setOnClickListener(arg -> executeNimble(true, itemView));
        }
    }
    private void executeNimble(boolean set, View view){
        int spend = 0;
        if (set)spend = unit.getPilot().spendEdge(Pilot.EdgeSkill.NIMBLE,unit.getCurrentTMM());
        else unit.getPilot().reverseEdge(Pilot.EdgeSkill.NIMBLE,unit.getCurrentTMM());
        unit.getTurn().getMoveData().setEdgeSpent(spend);
        updateScreen(view);
    }

    private static final int[] moveItems = {R.id.move_mode_crawl,R.id.move_mode_still,
            R.id.move_mode_walk, R.id.move_mode_run, R.id.move_mode_jump,
            R.id.supercharger,R.id.masc,R.id.prone,R.id.sub_five,R.id.sub_one,
            R.id.add_one,R.id.add_five,R.id.physical, R.id.add_psr};
    public void setStatusComplete(View itemView, boolean complete){
        for (int moveItem : moveItems) {
            itemView.findViewById(moveItem).setEnabled(!complete);
        }
    }
    public boolean setPhaseComplete(View itemView){
        boolean locked = ((LockButton)itemView.findViewById(R.id.target_locked)).isChecked();
        boolean damageUpdate = false;
        // if trying to lock is it valid?
        if (locked) locked = unit.getTurn().getMove() != UnitMove.MoveType.NONE;
        // set the move locked flag
        unit.getTurn().getMoveData().setMoveLocked(locked);
        if (locked) {
            for (IEquipment equip : unit.getActivityEnhancers(Turn.Phase.MOVE)) {
                if (equip.getSpecial() > 0) {
                    damageUpdate = true;
                    //equipment is activated
                    if (equip.activateEquipment(unit)) {
                        // Need to recalculate the movement button texts as criticals have occurred with immediate effect
                        ((MoveButton) itemView.findViewById(R.id.move_mode_walk)).
                                setupButton("Walk", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.WALK)), 0xffffffff);
                        ((MoveButton) itemView.findViewById(R.id.move_mode_run)).
                                setupButton("Run", Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN)), 0xffffff10);
                    }
                }
            }
            itemView.findViewById(R.id.tmm).setOnClickListener(null);
        } else {// reset the locked button to unlocked
            ((LockButton) itemView.findViewById(R.id.target_locked)).setChecked(false);
            setupTmmButton(itemView);
        }
        setPhysicalAttackOptions(itemView,locked);

        UpdatePlayerActions.moveActionCompleted(unit, damageUpdate);
        // set the status complete
        setStatusComplete(itemView, locked);
        return locked;
    }
    protected void setPhysicalAttackOptions(View itemView, boolean locked){
        OptionButton physChoice = itemView.findViewById(R.id.physical);
        PhysicalWeapon.PhysicalWeaponType physType = PhysicalWeapon.PhysicalWeaponType.NONE;
        if (physChoice.getSelectedIndex() > 0) {
            if (locked) {
                physType = PhysicalWeapon.PhysicalWeaponType.valueOf(physChoice.getSelected().getKey());
            }
            unit.getTurn().setTargetingComplete(locked);
            // if charging or DFAing the unit cannot use any weapon attacks this turn
        }
        unit.getTurn().setReservePhysicalAttack(physType);
    }
    public void updateScreen(View itemView){
        super.updateScreen(itemView);
        String desc = Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN));
        // refresh the move button
        ((MoveButton) itemView.findViewById(R.id.move_mode_run)).
                setupButton("Run", desc, 0xffffff10);
        UnitMove.MoveType type = unit.getTurn().getMove();
        int movement = unit.getAdjustedMovement(type);
        // check for different checks required due to damage
        if (unit.addMovementCheck(type)) {
            RecyclerView recycle = itemView.findViewById(R.id.damage_record_list);
            recycle.getAdapter().notifyDataSetChanged();
        }

        ((CheckBox) itemView.findViewById(R.id.prone)).setChecked(unit.getState().isProne());
        unit.getTurn().setHexesMoved(movement);
        desc = Integer.toString(movement);
        ((TextView) itemView.findViewById(R.id.value)).setText(desc);
        desc = Integer.toString(unit.getCurrentTMM());
        ((TextView) itemView.findViewById(R.id.tmm)).setText(desc);

        // update the selected physical attack options
        setupPhysicalAttackButton(itemView);

        setupTmmButton(itemView);
    }

    protected void hideNonUseableMoveButtons(View itemView){
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
    private void setEnhancedMoveButtons(View itemView,
                                        View.OnClickListener callback) {
        OptionButton mascButton = itemView.findViewById(R.id.masc);
        OptionButton superButton = itemView.findViewById(R.id.supercharger);
        if (mascButton != null && superButton != null) {
            mascButton.setVisibility(Button.GONE);
            superButton.setVisibility(Button.GONE);
            for (IEquipment equip : unit.getActivityEnhancers(Turn.Phase.MOVE)) {
                if (equip.getType() == OVEquipment.EquipmentType.MASC) {
                    setupEnhancedMoveButton(itemView, equip, mascButton, callback);
                } else if (equip.getType() == OVEquipment.EquipmentType.SUPERCHARGER) {
                    setupEnhancedMoveButton(itemView, equip, superButton, callback);
                }
            }
        }
    }
    private void setupEnhancedMoveButton(View itemView, IEquipment equip, OptionButton button,
                                         View.OnClickListener callback) {
        //Used for MASC/Supercharger options
        List<OptionButton.OptionButtonChoice> opts = new ArrayList<>();
        Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
        gen.setColor(itemView.getResources().getColor(R.color.DarkRed, null));
        opts.add(new OptionButton.OptionButtonChoice(equip.getType().name(), gen));
        gen = new Paint(Paint.ANTI_ALIAS_FLAG);
        gen.setColor(itemView.getResources().getColor(R.color.Green, null));
        opts.add(new OptionButton.OptionButtonChoice(equip.getType().name(), gen));
        button.setVisibility(View.VISIBLE);
        button.setOptions(opts);
        button.setEquipment(equip);
        button.setText(equip.getName());
        button.setOnClickListener(callback);
    }
    private void setupPhysicalAttackButton(View itemView){

        List<OptionButton.OptionButtonChoice> opts = new ArrayList<>();
        opts.add(getPhysicalChoice(PhysicalWeapon.PhysicalWeaponType.NONE,R.color.Green,itemView));
        switch (unit.getTurn().getMove()){
            case WALK:
            case RUN:
                opts.add(getPhysicalChoice(PhysicalWeapon.PhysicalWeaponType.CHARGE,R.color.Blue,itemView));
                break;
            case JUMP:
                opts.add(getPhysicalChoice(PhysicalWeapon.PhysicalWeaponType.DFA,R.color.Red,itemView));
                break;
        }
        OptionButton button = itemView.findViewById(R.id.physical);
        if (button!=null){
            if (opts.size() <=1) button.setVisibility(View.INVISIBLE);
            else {
                button.setVisibility(View.VISIBLE);

                button.setSelectedIndex(
                        (unit.getTurn().getReservePhysicalAttack()!= PhysicalWeapon.PhysicalWeaponType.NONE)?1:0);
                button.setOptions(opts);
                button.invalidate();
            }
        }
    }
    private static OptionButton.OptionButtonChoice getPhysicalChoice(PhysicalWeapon.PhysicalWeaponType choice, int rColor,View itemView){
        Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
        gen.setColor(itemView.getResources().getColor(rColor, null));
        return new OptionButton.OptionButtonChoice("Physical: " + choice.toDisplay(), gen,
                choice.toString());
    }
    public boolean updateHexValue(View itemView, int change){
//            super.updateValue(adjust);
        int value = unit.getTurn().getMoveData().getHexesMoved();
        int maxValue = unit.getAdjustedMovement(unit.getTurn().getMove());

        value += change;
        if (value<0)value = 0;
        else if (value>maxValue)value = maxValue;
        unit.getTurn().setHexesMoved(value);
        String desc = Integer.toString(value);
        ((TextView)itemView.findViewById(R.id.value)).setText(desc);
        desc = Integer.toString(unit.getCurrentTMM());
        ((TextView)itemView.findViewById(R.id.tmm)).setText(desc);
        return false;
    }
    private void moveButtonClicked(UnitMove.MoveType type, View itemView){
        // Note that the setMove function resets the prone flag if we are changing the move type
        // from one where we stood to one where we are still lying down.
        if(type != unit.getTurn().getMove()) {
            unit.getTurn().setMove(type);
            setupPhysicalAttackButton(itemView);
            updateScreen(itemView);
        }
    }
    private void updateMovementMods(View itemView) {
        //called when MASC or Supercharger is activated
        ((MoveButton)itemView.findViewById(R.id.move_mode_run)).
                setupButton("Run",
                        Integer.toString(unit.getAdjustedMovement(UnitMove.MoveType.RUN)),
                        0xffffff10);
    }
    private void addManualPsrCheck(View itemView){
        // display pop up to select PSR type
        AddPsrDialogFragment popup = new AddPsrDialogFragment(unit);;
        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

        mgr.setFragmentResultListener("reset",MainActivity.currentActivity,new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported.
                RecyclerView recycle = itemView.findViewById(R.id.damage_record_list);
                recycle.getAdapter().notifyDataSetChanged();
            }
        });

        popup.show(mgr, "Select PSR Check");
        //add PSR to the unit
    }
    @Override
    public boolean processButtonClick(View view, View itemView) {
        super.processButtonClick(view,itemView);
//        if (view.getId()==R.id.heat_label) displayHeatScale();

        if (view.getId()==R.id.move_mode_still){
            moveButtonClicked(UnitMove.MoveType.STILL, itemView);
        } else if (view.getId()==R.id.move_mode_walk){
            moveButtonClicked(UnitMove.MoveType.WALK, itemView);
        }else if (view.getId()==R.id.move_mode_run){
            moveButtonClicked(UnitMove.MoveType.RUN, itemView);
        }else if (view.getId()==R.id.move_mode_jump){
            moveButtonClicked(UnitMove.MoveType.JUMP, itemView);
        }else if (view.getId()==R.id.move_mode_crawl){
            moveButtonClicked(UnitMove.MoveType.CRAWL, itemView);
        }else if (view.getId()==R.id.add_psr) { addManualPsrCheck(itemView);
        } else if (view.getId()==R.id.add_five) { updateHexValue(itemView,5); }
        else if (view.getId()==R.id.add_one) { updateHexValue(itemView,1); }
        else if (view.getId()==R.id.sub_one) { updateHexValue(itemView,-1); }
        else if (view.getId()==R.id.sub_five) { updateHexValue(itemView,-5); }
        else if (view.getId()==R.id.masc||view.getId()==R.id.supercharger)
            updateMovementMods(itemView);
        return false;
    }

    protected void displayHexButtons(boolean display, View itemView, View.OnClickListener callback){
        super.displayHexButtons(display,itemView,callback);

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


}
