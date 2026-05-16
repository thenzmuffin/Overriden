package com.total.overide;

import android.database.Cursor;

import com.total.overiden.CriticalHit;
import com.total.overiden.DamageRecord;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.R;
import com.total.overiden.TargetData;
import com.total.overiden.Turn;
import com.total.overiden.TwoDSix;
import com.total.overiden.UnitMove;

import java.util.ArrayList;
import java.util.List;

public class OVBuildingData extends UnitData implements IUnitData {
    protected static OVSegment.OVLocation[] vehicleStd;

    public OVBuildingData(int designKey){
        super(designKey);
//        state = new OVState(designKey);
    }
    public OVBuildingData(Cursor cur){
        super(cur);
//        state = new OVState(designKey);
    }

    public OVBuildingData(List<String> list, String deviceName){
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
//        Buildings can't move
        return 0;
    }

    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing){
        //no transfer required for buildings, there is only one location
        return OVSegment.OVLocation.BUILDING;
    }
    @Override
    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {
        if (registerCrit) {
            // leave here in case we implement building crits at some point.

        }
        return OVSegment.OVLocation.BUILDING;
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {
        return 0;
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
// buildings only fall over when they are dead
    }
    public boolean addMovementCheck(UnitMove.MoveType move){
        // need to save if the mech was prone as this will be erased by the superclass implementation
        return false;
    }
    protected int getActuatorDamage(int inWalk) {
        return inWalk;
    }

    @Override
    public List<String> getStreamValue() {
        List<String> stream = new ArrayList<>();
        stream.add("STARTOVBUILDING\n");
        stream.addAll(super.getStreamValue());
        stream.add("ENDOVBUILDING\n");
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
        if (phase== Turn.Phase.TARGET) {
            // use the vehicle display object??
                if (!(currentDisplay instanceof VehicleDisplayTarget))
                    currentDisplay = new VehicleDisplayTarget(this, forceList);
        } else {
            // current display updated in UnitData class
            super.getDisplayObject(forceList,phase);
        }
        return currentDisplay;
    }

    @Override
    public int getHealth() {
        OVSegmentInst core = (OVSegmentInst) getSegment(OVSegment.OVLocation.BUILDING);

        return core.getArmourDmg() + core.getStructureDmg();
    }

    public boolean isImmobile(){
        return true;
    }
    public PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes() {
        return new PhysicalWeapon.PhysicalWeaponType[]{PhysicalWeapon.PhysicalWeaponType.NONE};
    }
    public OVSegmentInst getCoreSegment(){
        return (OVSegmentInst)getSegment(OVSegment.OVLocation.BUILDING);
    }

    @Override
    public int noOfRemainingLegs() {
        return 2;
    }
}
