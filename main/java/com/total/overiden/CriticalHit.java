package com.total.overiden;

import com.total.overide.OVSegment;

import java.util.ArrayList;
import java.util.List;

public class CriticalHit extends DamageMessage{
    private final PilotCheck.PilotCheckType check;
    private TwoDSix dice;
    private IEquipment component;
    private boolean edgeUsed = false;
    private OVSegment.OVLocation location;
    public CriticalHit(String message, TwoDSix dice,IDamageRecord parent) {
        // create a motive type critical for vehicles
        super(message, -1, parent);
        setApplied(false);
        this.check = null;
        this.dice = dice;
        this.component = null;
        setParent(parent);
        location = OVSegment.OVLocation.BODY;
    }
    public CriticalHit(String message, TwoDSix dice, PilotCheck.PilotCheckType check, IEquipment component,IChildLink parent) {
        super(message, -1, parent);
        setApplied(false);
        this.check = check;
        this.dice = dice;
        this.component = component;
        if (component!=null)location = component.getLocation();
        else location = OVSegment.OVLocation.HEAD; // is this going to work? could cause issues if this is a vehicle
        setParent(parent);
    }
    public CriticalHit(IUnitData unit, String data) {
        super(unit,data);
        String[] parts = data.split(",");
        // called from stream input
        if (!parts[5].equals("NULL"))
            check = PilotCheck.PilotCheckType.valueOf(parts[5]);
        else check = null;
        if (!parts[7].equals("NULL"))
            for (IEquipment equip : unit.getEquipment()) {
                if (equip.getID() == Integer.parseInt(parts[7])) {
                    component = equip;
                    location = component.getLocation();
                    break;
                }
            }
        else location = unit.getCoreSegment().getLocation();
        if (parts[6].length() > 2) dice = new TwoDSix(parts[6].replace("|",","));
        edgeUsed = Boolean.parseBoolean(parts[8]);

    }
    @Override
    public void applyDamage(IUnitData unit) {
        if (!isApplied()) {
            super.applyDamage(unit);
            // if check is populated then we need to add pilot check
            if (check != null && !unit.getState().isProne())
                unit.getTurn().getTurnChecks().add(new PilotCheck(unit, check, this));
            // if there is a component object then needs to be critted.
            if (component != null)
                component.applyCrit(unit, this);
        }
    }
    public TwoDSix getClusterDice() {
        return dice;
    }
    public int getEquipmentKey(){
        return component == null?-1:component.getID();
    }

    public PilotCheck.PilotCheckType getCheck() {
        return check;
    }
    @Override
    protected String getSingleStreamValue(){
        String stream = super.getSingleStreamValue() + ",";
        stream += check!=null?check + ",":"NULL,";
        String diceSt = "";
        if (dice!=null)diceSt = dice.toString().replace(",","|");
        String compy = component!=null? String.valueOf(component.getID()) :"NULL";
        stream += diceSt + "," + compy + "," + edgeUsed;

        return stream;
    }
    @Override
    public List<String> getStreamValue() {
        List<String> list = new ArrayList<>();

        list.add("CRITICALHIT:" + getSingleStreamValue() + "\n");
        // for a critical hit include the equipment that was critted too
        if(component!=null)list.add(component.getStreamValue() + "\n");
        return list;
    }
    public void reverseCrit(UnitTurn turn){
        super.reverseCrit(turn);
        // undo the crit
        // 1) repair damage to the component
        // 2) revert any damage added by the crit (like ammo explosions or falls)
        // 3) undo any pilot damage
        // 3) recursive call to undo crits from that damage
        if (isApplied() && component!=null){
            component.setStatus(true);
        }
    }
    @Override
    public boolean isEdgeUsed() {
        return edgeUsed;
    }

    @Override
    public void setEdgeUsed(boolean edgeUsed) {
        this.edgeUsed = edgeUsed;
    }

    public OVSegment.OVLocation getLocation() {
        return location;
    }
}
