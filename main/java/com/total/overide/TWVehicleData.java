package com.total.overide;

import android.database.Cursor;

import com.total.overiden.CriticalHit;
import com.total.overiden.DamageRecord;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.Pilot;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.Turn;
import com.total.overiden.TwoDSix;
import com.total.overiden.UnitMove;

import java.util.ArrayList;
import java.util.List;

public class TWVehicleData extends UnitData implements IUnitData {
    protected static OVSegment.OVLocation[] vehicleStd;
    protected static OVSegment.OVLocation[] vehicleSide;

    public TWVehicleData(int designKey){
        super(designKey);
//        state = new OVState(designKey);
    }
    public TWVehicleData(Cursor cur){
        super(cur);
//        state = new OVState(designKey);
    }

    public TWVehicleData(List<String> list, String deviceName){
        super(list, deviceName);
    }
    protected boolean isMechDestroyed(){
        // this method carries out any activities needed at the end of a phase
        // primarily this means setting the active flag on the state if the unit was destroyed
        // during this phase as simultaneous combat means it should complete all actions for the
        // current turn before being destroyed
        boolean destroyed = state.isDestroyedCrit();

        if (!destroyed) {
            for (OVSegment seg : getSegments()) {
                destroyed = seg.isDestroyed();
                if (destroyed) break;
            }
        }
        return destroyed;
    }

    @Override
    public int getAdjustedHeat() {
        return 0;
    }

    @Override
    public int getAdjustedMovement(UnitMove.MoveType type) {
        int walk = 0;
        if (type == UnitMove.MoveType.STILL
                || type == UnitMove.MoveType.NONE) return walk;

        // Type must be cruise or flank (labelled walk or run still)
        // first determine current cruising speed and then we can multiply into flanking speed if required
        walk = getHeader().getWalk();
        // now get modifiers for movement crits
        walk -= getState().getMotive();

        if (walk < 0) walk = 0;
        if (type == UnitMove.MoveType.RUN) {
            float runMod = 1.5f;
            walk = (int) Math.ceil(walk * runMod);
        }
//        if (walk < 1) walk = 1; //minimum move
        return walk;
    }

    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing){
        //no transfer required for vehicles, if any one location is destroyed the unit is done
        return location;
    }
    @Override
    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {

        if (vehicleStd==null) configureLocationTables();
        OVSegment.OVLocation location;
        OVSegment.OVLocation[] tableArray;
        if (facing== TargetData.LocTable.FRONT || facing == TargetData.LocTable.REAR) tableArray = vehicleStd;
        else tableArray = vehicleSide;
        location = tableArray[twoDSix - 2];
        //what if the vehicle doesn't have a turret
        if (getSegment(location)==null)location = tableArray[0];

        // right side and rear shots need to be redirected
        if (facing== TargetData.LocTable.REAR && location== OVSegment.OVLocation.FRONT)
            location = OVSegment.OVLocation.REARSIDE;
        else if (facing== TargetData.LocTable.RIGHT && location== OVSegment.OVLocation.LEFT)
            location = OVSegment.OVLocation.RIGHT;

        if (registerCrit) {
            // through armour crit on a 2, 12 or 8 on the side
            if (twoDSix == 2 || twoDSix == 12 ||
                    ((facing == TargetData.LocTable.LEFT || facing == TargetData.LocTable.RIGHT) && twoDSix == 8)) {
                //through armour crit, no floating crits for vehicles
                getSegment(location).checkForCrit(this, null, parent);
            } else if (twoDSix == 3 || twoDSix == 4 || twoDSix == 5 || twoDSix == 9){
                // Motive check required
                // get mods
                int mod = 0;
                switch (getHeader().getUnitType()){
                    case HOVER:
                        mod+=3;
                        break;
                    case WHEEL:
                        mod+=2;
                        break;
                }
                switch(facing){
                    case REAR:
                        mod+=1;
                        break;
                    case LEFT:
                    case RIGHT:
                        mod+=2;
                        break;
                }
                TwoDSix motiveDice = new TwoDSix();
                int roll = motiveDice.getTotal() + mod;
                String message = "Motive-No Impact";
                if (roll>=12){
                    getState().setMotive(getHeader().getWalk());// set motive penalty to be full cruising speed
                    message = "Motive-Immobilised";
                } else if (roll>=10){
                    //half cruising speed, +3 mod to piloting
                    int pen = getState().getMotive();
                    pen += Math.floorDiv(getHeader().getWalk(),2);
                    getState().setMotive(Math.min(pen,getHeader().getWalk()));
                    getState().getPilot().setPilotingMods(Pilot.PilotingMods.HEAVYDMG);
                    message = "Motive-Heavy Damage";
                } else if (roll>=8){
                    //-1 cruising speed, +2 mod to piloting
                    int pen = getState().getMotive()+1;
                    getState().setMotive(Math.min(pen,getHeader().getWalk()));
                    getState().getPilot().setPilotingMods(Pilot.PilotingMods.MODERATEDMG);
                    message = "Motive-Moderate Damage";
                } else if (roll>=6){
                    //+1 mod to piloting
                    getState().getPilot().setPilotingMods(Pilot.PilotingMods.MINORDMG);
                    message = "Motive-Minor Damage";
                }
                getTurn().addDamage(new CriticalHit(message,motiveDice,parent));
            }

        }
        return location;
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {

        int damage = 0;
        if (selected == PhysicalWeapon.PhysicalWeaponType.CHARGE){
            damage = Math.floorDiv(getHeader().getMass() + 5,10);
        }
        return damage;
    }
    public int getSensorDamageMod(){
        int ret = getState().getSensors();
        // for TW if one sensor hit then +2 to hit, 2 sensor hits and no shooting is possible
        if (ret>4) ret = 30;
        return ret;
    }
    /*
     * determine damage from a fall
     * add damage to current turn
     * set the state to prone
     * Add the conciousness check from falling (TW would be a roll to avoid injury)
     */
    public void addFallDamage(DamageRecord dr, int levels){
//        int calcDamage;
//
//        calcDamage = Math.floorDiv(getHeader().getMass() + 5, 10) * levels;
//        while (calcDamage > 0) {
//            if (calcDamage > 5) {
//                dr.addGrouping(5);
//                calcDamage -= 5;
//            } else {
//                dr.addGrouping(calcDamage);
//                calcDamage = 0;
//            }
//        }
//        // falling added an injury so we need to check if the pilot is taking a nap
//        getTurn().getTurnChecks().add(new PilotCheck(this, PilotCheck.PilotCheckType.FALL));
//
//        dr.setWeapon(new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.FALL,calcDamage, PhysicalWeapon.PhysicalHitGrouping.FULL));
//        getTurn().addDamage(dr);
//        getState().setProne(true);
//
//        // find any pending pilot checks and default to failed
//        for (GenericCheck check : getTurn().getTurnChecks()){
//            if (check.getStatus()== TargetWeapon.ShotStatus.NOTFIRED &&
//                check.getCheckType()== GenericCheck.CheckType.PILOT &&
//                    ((PilotCheck)check).getType()!= PilotCheck.PilotCheckType.FALL){
//                // this is a falling check and the mech has already fallen!
//                check.setSuccess(true); // mark as passed since otherwise it will result in additional fall damage
//            }
//        }
    }
    public boolean addMovementCheck(UnitMove.MoveType move){
        // need to save if the mech was prone as this will be erased by the superclass implementation
        return false;
    }
    protected int getActuatorDamage(int inWalk) {
        return inWalk;
    }

    public static void configureLocationTables(){
        vehicleStd = generateSingleLocTableFromResource(R.array.tw_vehicle_locations);
        vehicleSide = generateSingleLocTableFromResource(R.array.tw_vehicle_side_locs);
    }
    @Override
    public List<String> getStreamValue() {
        List<String> stream = new ArrayList<>();
        stream.add("STARTTWVEHICLE\n");
        stream.addAll(super.getStreamValue());
        stream.add("ENDTWVEHICLE\n");
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
    public IUnitDisplay getDisplayObject(int forceList, Turn.Phase phase){
        switch (phase){
            case MOVE:
                if (!(currentDisplay instanceof VehicleDisplayMove))
                    currentDisplay = new VehicleDisplayMove(this,forceList);
                break;
            case TARGET:
                if (!(currentDisplay instanceof VehicleDisplayTarget))
                    currentDisplay = new VehicleDisplayTarget(this,forceList);
                break;
            default:
                super.getDisplayObject(forceList,phase);
        }
        return currentDisplay;
    }

    @Override
    public int getHealth() {
        TWVehicleSegment torso = (TWVehicleSegment) getSegment(OVSegment.OVLocation.FRONT);

        return torso.getArmourDmg() + torso.getStructureDmg();
    }

    public boolean isImmobile(){
        return (getAdjustedMovement(UnitMove.MoveType.WALK)<=0);
    }
    public PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes() {
        return new PhysicalWeapon.PhysicalWeaponType[]{PhysicalWeapon.PhysicalWeaponType.NONE};
    }
    public OVSegmentInst getCoreSegment(){
        return (OVSegmentInst)getSegment(OVSegment.OVLocation.FRONT);
    }
}
