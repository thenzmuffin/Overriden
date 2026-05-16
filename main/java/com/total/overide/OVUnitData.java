package com.total.overide;

import android.database.Cursor;

import com.total.overiden.ConsciousnessCheck;
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
import com.total.overiden.Turn;
import com.total.overiden.TwoDSix;

import java.util.ArrayList;
import java.util.List;

public class OVUnitData extends UnitData implements IUnitData {
//    private static OVSegment.OVLocation[] locationStd;
//    private static OVSegment.OVLocation[] locationPunch;
//    private static final OVSegment.OVLocation[] locationKick = {
//            OVSegment.OVLocation.RIGHTLEG,
//            OVSegment.OVLocation.RIGHTLEG,
//            OVSegment.OVLocation.RIGHTLEG,
//            OVSegment.OVLocation.LEFTLEG,
//            OVSegment.OVLocation.LEFTLEG,
//            OVSegment.OVLocation.LEFTLEG};

    public OVUnitData(int designKey) {
        super(designKey);
    }
    public OVUnitData(Cursor cur){
        super(cur);
//        state = new OVState(designKey);
    }
    public OVUnitData(List<String> list, String deviceName) {
        super(list, deviceName);
    }

    protected boolean isMechDestroyed() {
        // this method carries out any activities needed at the end of a phase
        // primarily this means setting the active flag on the state if the unit was destroyed
        // during this phase as simultaneous combat means it should complete all actions for the
        // current turn before being destroyed
        boolean active = !getSegment(OVSegment.OVLocation.HEAD).isDestroyed();
        // head destroyed?
        if (active) active = !getSegment(OVSegment.OVLocation.TORSO).isDestroyed();
        if (active) active = state.getEngine() < 2;
        return !active;
    }

    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing) {
        return facing == TargetData.LocTable.FRONT ? OVSegment.OVLocation.TORSO : OVSegment.OVLocation.REAR;
    }

    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {
        OVSegment.OVLocation location = null;
        switch (table) {
            case PUNCH:
                location = locationPunch[twoDSix - 1];
                break;
            case KICK:
                location = locationKick[twoDSix - 1];
                break;
            default: //covers FULL and any invalid settings
                location = locationStd[twoDSix - 2];
                if (twoDSix == 2) { // through armour crit, reroll location
                    System.out.println("Through Armour Crit");
                    location = locationStd[(new TwoDSix()).getTotal() - 2];

                    if(registerCrit)getSegment(location).checkForCrit(this, null, parent);
//                getTurn().addDamage(getSegment(location).checkForCrit(turn));
                }
                break;
        }
        if (facing == TargetData.LocTable.REAR && location == OVSegment.OVLocation.TORSO) {
            location = OVSegment.OVLocation.REAR;
        }

        return location;
    }


    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {
        int damage = 0;
        switch (selected) {
            case PUNCH:
                damage = Math.floorDiv(getHeader().getMass() + 25, 30);
                if(getHeader().isTsm() && getState().getHeat() >= 9)
                    damage *= 2;
                break;
            case KICK:
                damage = Math.floorDiv(getHeader().getMass() + 10, 15);
                if(getHeader().isTsm() && getState().getHeat() >= 9)
                    damage *= 2;
                break;
            case CHARGE:
                int moved = Math.floorDiv(getTurn().getMoveData().getHexesMoved() * 2, 3);
                damage = Math.floorDiv(getHeader().getMass() + 25, 30) * moved;
                break;
            case DFA:
                damage = Math.floorDiv(getHeader().getMass() + 25, 30) * 2; //dfa *2 for OV
                break;
//            case "melee":
//            if(getHeader().isTsm() && getState().getHeat() >= 9)
//                damage *= 2;
//                break;
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
    public void addFallDamage(DamageRecord dr, int levels) {
        int calcDamage;
        calcDamage = Math.floorDiv(getHeader().getMass() + 25, 30) * levels;
        dr.addGrouping(calcDamage);
        getPilot().addInjury(1);
        // falling added an injury so we need to check if the pilot is taking a nap
        getTurn().getTurnChecks().add(new ConsciousnessCheck(this, -1, dr));
        dr.setWeapon(new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.FALL, calcDamage, PhysicalWeapon.PhysicalHitGrouping.FULL));
        getTurn().addDamage(dr);
        getState().setProne(true);

        // find any pending pilot checks and default to failed
        for (GenericCheck check : getTurn().getTurnChecks()) {
            if (check.getStatus() == TargetWeapon.ShotStatus.NOTFIRED &&
                    check.getCheckType() == GenericCheck.CheckType.PILOT &&
                    ((PilotCheck) check).getType() != PilotCheck.PilotCheckType.FALL) {
                // this is a falling check and the mech has already fallen!
                check.setSuccess(true); // mark as passed since otherwise it will result in additional fall damage
            }
        }
    }

    protected int getActuatorDamage(int inWalk) {
        return inWalk - (getState().getMotive() * 2);
    }

    public static void configureLocationTables() {
        locationStd = generateSingleLocTableFromResource(R.array.ov_front_locations);
        locationPunch = generateSingleLocTableFromResource(R.array.ov_punch_locations);
    }
    @Override
    public boolean endPhase(Turn.Phase phase) {
        boolean stillActive = super.endPhase(phase);
        if (stillActive && phase== Turn.Phase.RESOLVE){
            //Check to see if the unit is now in forced withdrawal
            OVSegmentInst torso = (OVSegmentInst) getSegment(OVSegment.OVLocation.TORSO);
            if (torso.getArmourTurnDmg()==0 && torso.getStructureTurnDmg()<=4){
                // The unit is now in forced withdrawal
                getState().setForcedWithdrawal(true);
            }
        }
        return stillActive;
    }
    @Override
    public List<String> getStreamValue() {
        List<String> stream = new ArrayList<>();
        stream.add("STARTOVMECH\n");
        stream.addAll(super.getStreamValue());
        stream.add("ENDOVMECH\n");
        return stream;
    }

    @Override
    public int getHealth() {
        OVSegmentInst torso = (OVSegmentInst) getSegment(OVSegment.OVLocation.TORSO);

        return torso.getArmourDmg() + torso.getStructureDmg();
    }

    public void addWeapon(Cursor cur){
        getEquipment().add(new OVWeaponInstance(
                OVSegment.OVLocation.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_WEAPON_LOCATION)),
                OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_WEAPON_ID),
                OVMtfReader.findOVWeaponByID(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_WEAPON_KEY)),
                OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_WEAPON_STATE)==1,
                IWeapon.WeaponMode.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_WEAPON_MODE)))
        );
    }
    public OVSegmentInst getCoreSegment(){
        return (OVSegmentInst)getSegment(OVSegment.OVLocation.TORSO);
    }
}
