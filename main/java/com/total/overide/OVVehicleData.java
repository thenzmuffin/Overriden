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

public class OVVehicleData extends UnitData implements IUnitData {
    protected static OVSegment.OVLocation[] vehicleStd;

    public OVVehicleData(int designKey){
        super(designKey);
//        state = new OVState(designKey);
    }
    public OVVehicleData(Cursor cur){
        super(cur);
//        state = new OVState(designKey);
    }

    public OVVehicleData(List<String> list, String deviceName){
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
            location = vehicleStd[twoDSix - 2];
            if (facing== TargetData.LocTable.REAR && location== OVSegment.OVLocation.FRONT)
                location = OVSegment.OVLocation.REARSIDE;
        if (registerCrit) {
            // through armour crit on a 2, 12
            if (twoDSix == 2 || twoDSix == 12) {
                //through armour crit, no floating crits for vehicles
                getSegment(location).checkForCrit(this, null, parent);
            } else if (location== OVSegment.OVLocation.LEFT || location== OVSegment.OVLocation.RIGHT){
                // Motive check required
                // get mods
                TwoDSix motiveDice = new TwoDSix();
                int roll = motiveDice.getTotal();
                String message = "Motive-No Impact";
                if (motiveDice.getTotal()>=8){
                    //-1 cruising speed, +2 mod to piloting
                    int pen = getState().getMotive()+2;
                    getState().setMotive(Math.min(pen,getHeader().getWalk()));
                    message = "Motive-Crit";
                }
                getTurn().addDamage(new CriticalHit(message,motiveDice, parent));
            }

        }
        return location;
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {

        int damage = 0;
        if (selected == PhysicalWeapon.PhysicalWeaponType.CHARGE){
            damage = Math.floorDiv(getHeader().getMass() + 5,30);
        }
        return damage;
    }
    public int getSensorDamageMod(){
        return 0;
    }
    /*
     * determine damage from a fall
     * add damage to current turn
     * set the state to prone
     * Add the conciousness check from falling (TW would be a roll to avoid injury)
     */
    public void addFallDamage(DamageRecord dr, int levels){

    }
    public boolean addMovementCheck(UnitMove.MoveType move){
        // need to save if the mech was prone as this will be erased by the superclass implementation
        return false;
    }
    protected int getActuatorDamage(int inWalk) {
        return inWalk;
    }

    public static void configureLocationTables(){
        vehicleStd = generateSingleLocTableFromResource(R.array.ov_vehicle_locations);
    }
    @Override
    public List<String> getStreamValue() {
        List<String> stream = new ArrayList<>();
        stream.add("STARTOVVEHICLE\n");
        stream.addAll(super.getStreamValue());
        stream.add("ENDOVVEHICLE\n");
        return stream;
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

    @Override
    public int noOfRemainingLegs() {
        return 2;
    }
}
