package com.total.overide;

import android.database.Cursor;

import com.total.overiden.DamageRecord;
import com.total.overiden.GenericCheck;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.PilotCheck;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.TwoDSix;
import com.total.overiden.UnitMove;

import java.util.ArrayList;
import java.util.List;

public class TWUnitData extends UnitData implements IUnitData {

    public TWUnitData(int designKey){
        super(designKey);
//        state = new OVState(designKey);
    }
    public TWUnitData(Cursor cur){
        super(cur);
//        state = new OVState(designKey);
    }

    public TWUnitData(List<String> list, String deviceName){
        super(list, deviceName);
    }
    protected boolean isMechDestroyed(){
        // this method carries out any activities needed at the end of a phase
        // primarily this means setting the active flag on the state if the unit was destroyed
        // during this phase as simultaneous combat means it should complete all actions for the
        // current turn before being destroyed
        boolean active = !getSegment(OVSegment.OVLocation.HEAD).isDestroyed();
        // head destroyed?
        if (active)active = !getSegment(OVSegment.OVLocation.CENTRETORSO).isDestroyed();

        // cockpit destroyed?
        if (active)
            active = getSegment(OVSegment.OVLocation.HEAD).getEquipmentType(OVEquipment.EquipmentType.COCKPIT).isOperational();
        // engine destroyed?
        if (active) active = state.getEngine() < 3;

        return !active;
    }

    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing){
        //more complicated for TW
        OVSegment.OVLocation converted;
        switch (location){
            case RIGHTARM:
                converted = OVSegment.OVLocation.RIGHTTORSO;
                break;
            case RIGHTLEG:
                converted = OVSegment.OVLocation.RIGHTTORSO;
                break;
            case LEFTARM:
                converted = OVSegment.OVLocation.LEFTTORSO;
                break;
            case LEFTLEG:
                converted = OVSegment.OVLocation.LEFTTORSO;
                break;
            case RIGHTTORSO:
                converted = OVSegment.OVLocation.CENTRETORSO;
                // make sure the right arm is destroyed as well
//                getSegment(OVSegment.OVLocation.RIGHTARM).markSegmentDestroyed();
                break;
            case LEFTTORSO:
                converted = OVSegment.OVLocation.CENTRETORSO;
                // make sure the right arm is destroyed as well
//                getSegment(OVSegment.OVLocation.LEFTARM).markSegmentDestroyed();
                break;
            default:
                // everything else goes to the CT, head shouldn't but the mech is already dead!
                converted = OVSegment.OVLocation.CENTRETORSO;
        }
        return converted;
    }
    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {
        OVSegment.OVLocation location = null;
                switch (table) {
                    case PUNCH:
                        location = locationPunch[Math.min(twoDSix -1,5)];
                        break;
                    case KICK:
                        location = locationKick[Math.min(twoDSix -1,5)];
                        break;
                    default:
                        location = locationStd[twoDSix - 2];
                        if (twoDSix == 2) { // through armour crit, reroll location
                            location = locationStd[(new TwoDSix()).getTotal() - 2];
                            if (registerCrit) getSegment(location).checkForCrit(this, null, parent);
                        }
                }
                switch (facing){
                    case REAR:
                    switch (location) {
                        case CENTRETORSO:
                            location = OVSegment.OVLocation.CTREAR;
                            break;
                        case LEFTTORSO:
                            location = OVSegment.OVLocation.LTREAR;
                            break;
                        case RIGHTTORSO:
                            location = OVSegment.OVLocation.RTREAR;
                            break;
                    }
                    break;
                    case LEFT:
                        switch (location) {
                            case RIGHTARM:
                                location = OVSegment.OVLocation.LEFTARM;
                                break;
                            case RIGHTLEG:
                                location = OVSegment.OVLocation.LEFTLEG;
                                break;
                            case RIGHTTORSO:
                                location = OVSegment.OVLocation.LEFTTORSO;
                                break;
                        }
                        break;
                    case RIGHT:
                        switch (location) {
                            case LEFTARM:
                                location = OVSegment.OVLocation.RIGHTARM;
                                break;
                            case LEFTLEG:
                                location = OVSegment.OVLocation.RIGHTLEG;
                                break;
                            case LEFTTORSO:
                                location = OVSegment.OVLocation.RIGHTTORSO;
                                break;
                        }
                        break;
                }

        return location;
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {

        int damage = 0;
        switch (selected){
            case PUNCH:
                damage = Math.floorDiv(getHeader().getMass() + 5,10);
                break;
            case KICK:
                damage = Math.floorDiv(getHeader().getMass(),5);
                break;
            case CHARGE:
                damage = Math.floorDiv(getHeader().getMass() + 5,10) * getTurn().getMoveData().getHexesMoved();
                break;
            case DFA:
                damage = Math.floorDiv(getHeader().getMass() + 5,10) * 3; //dfa * 3
                break;
            case HATCHET:
                damage = Math.floorDiv(getHeader().getMass(),5);
                break;
            case NONE:
                break;
        }
        return damage;
    }

    /*
     * determine damage from a fall
     * add damage to current turn
     * set the state to prone
     * Add the conciousness check from falling (TW would be a roll to avoid injury)
     */
    public void addFallDamage(DamageRecord dr, int levels){
        int calcDamage;

        calcDamage = Math.floorDiv(getHeader().getMass() + 5, 10) * levels;
        while (calcDamage > 0) {
            if (calcDamage > 5) {
                dr.addGrouping(5);
                calcDamage -= 5;
            } else {
                dr.addGrouping(calcDamage);
                calcDamage = 0;
            }
        }
        // falling added an injury so we need to check if the pilot is taking a nap
        getTurn().getTurnChecks().add(new PilotCheck(this, PilotCheck.PilotCheckType.FALL, dr));

        dr.setWeapon(new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.FALL,calcDamage, PhysicalWeapon.PhysicalHitGrouping.FULL));
        getTurn().addDamage(dr);
        getState().setProne(true);

        // find any pending pilot checks and default to passed as can't fall twice
        for (GenericCheck check : getTurn().getTurnChecks()){
            if (check.getStatus()== TargetWeapon.ShotStatus.NOTFIRED &&
                check.getCheckType()== GenericCheck.CheckType.PILOT &&
                    ((PilotCheck)check).getType()!= PilotCheck.PilotCheckType.FALL){
                // this is a falling check and the mech has already fallen!
                check.setSuccess(true); // mark as passed since otherwise it will result in additional fall damage
            }
        }


    }
    public boolean addMovementCheck(UnitMove.MoveType move){
        // need to save if the mech was prone as this will be erased by the superclass implementation
        boolean startedProne = getState().isProne();
        boolean ret = false;
        super.addMovementCheck(move);
        // if there is actuator damage then jumping can require a pilot check
        TWActuator actL = (TWActuator) getSegment(OVSegment.OVLocation.LEFTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
        TWActuator actR = (TWActuator) getSegment(OVSegment.OVLocation.RIGHTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
        boolean check = false;
        PilotCheck.PilotCheckType type = null;
        switch (move){
            case RUN:
                check = !actL.isTop() || !actR.isTop();
                type = PilotCheck.PilotCheckType.RUN;
                // run on to the standing check
            case WALK:
                if (startedProne){
                    getTurn().addCheck(new PilotCheck(this, PilotCheck.PilotCheckType.STAND, null));
                    ret = true;
                }
                break;
            case JUMP:
                 check = (actL.getDamage() + actR.getDamage()) > 0;
                 type = PilotCheck.PilotCheckType.JUMP;
                break;
        }
        if (check){
            getTurn().addCheck(new PilotCheck(this, type, null));
            ret = true;
        }
        return ret;
    }
    protected int getActuatorDamage(int inWalk) {
        int walk = inWalk;
        // get each set of leg actuators and check for move pens
        TWActuator actuator = (TWActuator) getSegment(OVSegment.OVLocation.RIGHTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
        walk = actuator.getMovementMod(walk);
        actuator = (TWActuator) getSegment(OVSegment.OVLocation.LEFTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
        walk = actuator.getMovementMod(walk);
        return walk;
    }

    public static void configureLocationTables(){
        locationStd = generateSingleLocTableFromResource(R.array.tw_front_locations);
        locationPunch = generateSingleLocTableFromResource(R.array.tw_punch_locations);
        //left and right are only used for TW - using playtest rules, don't need these
//        locationLeft = generateSingleLocTableFromResource(R.array.tw_left_locations);
//        locationRight = generateSingleLocTableFromResource(R.array.tw_right_locations);
    }
    @Override
    public List<String> getStreamValue() {
        List<String> stream = new ArrayList<>();
        stream.add("STARTTWMECH\n");
        stream.addAll(super.getStreamValue());
        stream.add("ENDTWMECH\n");
        return stream;
    }
    public void addWeapon(Cursor cur){
        getEquipment().add(new TWWeaponInstance(
                OVSegment.OVLocation.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_WEAPON_LOCATION)),
                OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_WEAPON_ID),
                OVMtfReader.findOVWeaponByID(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_WEAPON_KEY)),
                OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_WEAPON_STATE)==1,
                IWeapon.WeaponMode.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_WEAPON_MODE)))
        );
    }
//    public int getAdjustedHeat() {return 0;}

    @Override
    public int getHealth() {
            OVSegmentInst torso = (OVSegmentInst) getSegment(OVSegment.OVLocation.CENTRETORSO);

            return torso.getArmourDmg() + torso.getStructureDmg();
    }

    public OVSegmentInst getCoreSegment(){
        return (OVSegmentInst)getSegment(OVSegment.OVLocation.CENTRETORSO);
    }
}
