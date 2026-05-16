package com.total.overide;

import android.graphics.Paint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.FragmentManager;
import com.total.overiden.Game;
import com.total.overiden.IEquipment;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.OptionButton;
import com.total.overiden.R;
import com.total.overiden.TargetWeapon;
import com.total.overiden.TooltipDialogFragment;
import com.total.overiden.Turn;
import com.total.overiden.TurnPhaseFragment;

import java.util.ArrayList;
import java.util.List;


public class UnitDisplayResolve extends UnitDisplay {
    private static class ActionLink{
        public int buttonId;
        public Object obj;
        public ActionLink(int id, Object link){
            buttonId = id;
            obj = link;
        }
    }
    private final List<ActionLink> actionLinks;
    private boolean actionTaken = false;
    public UnitDisplayResolve(UnitData data, int forceNumber){
        super(data,forceNumber);
        actionLinks = new ArrayList<>();
    }
    public void setDisplayFields(View itemView,
                                 View.OnClickListener callback,
                                 TurnPhaseFragment fragment){
        super.setDisplayFields(itemView, callback, fragment);

        OptionButton ecmButton = itemView.findViewById(R.id.ecm);
//            OptionButton superButton = itemView.findViewById(R.id.supercharger);
        if (ecmButton != null) {
            ecmButton.setVisibility(Button.GONE);
            for (IEquipment equip : unit.getActivityEnhancers(Turn.Phase.RESOLVE)) {
                if (equip.getType() == OVEquipment.EquipmentType.STEALTHARM || equip.getType() == OVEquipment.EquipmentType.ECM) {
                    List<OptionButton.OptionButtonChoice> opts = new ArrayList<>();
                    Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                    gen.setColor(itemView.getResources().getColor(R.color.DarkRed, null));
                    opts.add(new OptionButton.OptionButtonChoice(equip.getType().name() + ":Off", gen));
                    gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                    gen.setColor(itemView.getResources().getColor(R.color.Green, null));
                    opts.add(new OptionButton.OptionButtonChoice(equip.getType().name() + ":ECM", gen));
                    if (equip.getType() == OVEquipment.EquipmentType.STEALTHARM) {
                        gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                        gen.setColor(itemView.getResources().getColor(R.color.DarkBlue, null));
                        opts.add(new OptionButton.OptionButtonChoice(equip.getType().name() + ":On", gen));
                    }
                    ecmButton.setVisibility(View.VISIBLE);
                    ecmButton.setOptions(opts);
                    ecmButton.setEquipment(equip);
                    ecmButton.setText(equip.getName());
                    ecmButton.setOnClickListener(callback);
                }
            }
        }
        OptionButton retreatButton = itemView.findViewById(R.id.retreated);
        if (retreatButton != null) {
            List<OptionButton.OptionButtonChoice> opts = new ArrayList<>();
            Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(itemView.getResources().getColor(R.color.Green, null));
            opts.add(new OptionButton.OptionButtonChoice("Active", gen));
            gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(itemView.getResources().getColor(R.color.DarkRed, null));
            opts.add(new OptionButton.OptionButtonChoice("Retreated", gen));
            retreatButton.setOptions(opts);
            retreatButton.setOnClickListener(callback);
        }

        // some activities can be carried out if the unit has not fired any weapons
        boolean weaponsFired = false;
        for (TargetWeapon weapon : unit.getTurn().getWeaponList()){
            if (weapon.getStatus()!= TargetWeapon.ShotStatus.NOTFIRED){
                weaponsFired=true;
                break;
            }
        }
        LinearLayout actions = itemView.findViewById(R.id.actions);
        actions.removeAllViews(); // clear any views added in a previous use of this viewholder
        actionLinks.clear();
        if (!weaponsFired && !actionTaken){
            // add buttons for clearing locked turret or jammed weapons
            for (IWeapon weapon : unit.getWeapons()){
                //check if the weapon is jammed
                if (weapon.isJammed()) {
                    actionLinks.add(new ActionLink(addDynamicButton("UnJam:" + weapon.getName(), callback, actions), weapon));
                }
            }
            if (unit.getState().getTurret()== OVState.TurretState.JAMMED){
                // the player can attempt to unjam the Turret
                actionLinks.add(new ActionLink(addDynamicButton("UnJam:Turret", callback, actions), null));
            }
        }
    }
    private int addDynamicButton(String name, View.OnClickListener callback, LinearLayout actions){
        Button actBut = new Button(MainActivity.currentActivity);
        int id = View.generateViewId();
        actBut.setText(name);
        actBut.setId(id);
        actBut.setOnClickListener(callback);
        actions.addView(actBut);
        return id;
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

    @Override
    public void setListCallbacks(View itemView, AdapterView.OnItemSelectedListener callback) {

    }
    private void updateRetreated(View itemView) {
        OptionButton retreatButton = itemView.findViewById(R.id.retreated);
        if (retreatButton != null) {
            unit.getState().setActive(retreatButton.getSelectedIndex() == 0);
        }
    }
    public void updateEquipment(View itemView) {
        OptionButton ecmButton = itemView.findViewById(R.id.ecm);
        if (ecmButton!=null && ecmButton.getEquipment()!=null) {
            ecmButton.getEquipment().activateEquipment(unit);
        }
    }
    @Override
    public boolean processButtonClick(View view, View itemView) {
        super.processButtonClick(view,itemView);
        if (view.getId()==R.id.retreated){
            updateRetreated(itemView);
        } else if (view.getId()==R.id.ecm){
            updateEquipment(itemView);
        } else {
            for (ActionLink link : actionLinks){
                if (link.buttonId==view.getId()){
                    if (link.obj==null){
                        //turret Jam
                        unit.getState().setTurret(OVState.TurretState.JAMCLEARED);
                    } else if (link.obj instanceof OVWeaponInstance){
                        //weapon jammed
                        ((OVWeaponInstance)link.obj).setJammed(false);
                    }
                    LinearLayout actions = itemView.findViewById(R.id.actions);
                    actions.removeAllViews();
                    actionTaken = true;
                    break;
                }
            }
        }
        return false;
    }
//    private void displayHeatScale(){
//        String description;
//        if (Game.current.isSmartHeat())
//            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.heat_scale);
//        else
//            description = MainActivity.currentActivity.getApplicationContext().getString(R.string.ov_heat_scale);
//        TooltipDialogFragment tooltip = new TooltipDialogFragment(description);;
//        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
//        tooltip.show(mgr, "Turn Heat");
//    }

}
