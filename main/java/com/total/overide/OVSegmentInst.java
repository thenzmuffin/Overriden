package com.total.overide;

import android.content.ContentValues;
import android.database.Cursor;

import com.total.overiden.ConsciousnessCheck;
import com.total.overiden.DamageMessage;
import com.total.overiden.ForceList;
import com.total.overiden.IChildLink;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;

public class OVSegmentInst extends OVSegment{
    private int armourDmg;
    private int structureDmg;
    private int rearDmg;
    private int armourTurnDmg;
    private int structureTurnDmg;
    private int rearTurnDmg;
    private boolean active = true; // flag to say whether the segment is destroyed or not
    private boolean sent = true; // if playing via bluetooth has it been sent since being updated?

    public OVSegmentInst(OVLocation location, int armour, int structure, int rear, ArmourType type){
        super(location, armour, structure, rear, type);
        armourTurnDmg = this.armourDmg = armour;
        structureTurnDmg = this.structureDmg = structure;
        rearTurnDmg = this.rearDmg = rear;
    }
    public OVSegmentInst(String data){
        super(data);
        String[] parts = data.split(",");
        if (parts.length>=11) {
            armourDmg = Integer.parseInt(parts[5]);
            structureDmg = Integer.parseInt(parts[6]);
            rearDmg = Integer.parseInt(parts[7]);
            armourTurnDmg = Integer.parseInt(parts[8]);
            structureTurnDmg = Integer.parseInt(parts[9]);
            rearTurnDmg = Integer.parseInt(parts[10]);
        }
    }
    public static OVSegmentInst newInstance(String data, OVUnitDesign unit){
        OVSegmentInst inst;
        if (unit.getHeader().getType()== ForceList.ForceType.OV){
            switch (unit.getHeader().getUnitType()){
                case HOVER:
                case BUILDING:
                case TANK:
                    inst = new OVVehicleSegment(data);
                    break;
                default:
                    inst = new OVSegmentInst(data);
            }
        } else {
            switch (unit.getHeader().getUnitType()){
                case MECH:
                    inst = new TWSegmentInst(data);
                    break;
                case HOVER:
                case BUILDING:
                case TANK:
                    inst = new TWVehicleSegment(data);
                    break;
                default:
                    inst = new TWSegmentInst(data);
            }
        }
        return inst;
    }
    public static OVSegmentInst newInstance(OVLocation location, int armour, int structure, int rear, ArmourType type, OVUnitDesign unit){
        OVSegmentInst inst;
        if (unit.getHeader().getType()== ForceList.ForceType.OV){
            switch (unit.getHeader().getUnitType()){
                case HOVER:
                case TANK:
                    inst = new OVVehicleSegment(location,armour,structure,rear,type);
                    break;
                default:
                    inst = new OVSegmentInst(location,armour,structure,rear,type);
            }

        } else {
            switch (unit.getHeader().getUnitType()){
                case HOVER:
                case TANK:
                    inst = new TWVehicleSegment(location,armour,structure,rear,type);
                    break;
                default:
                    inst = new TWSegmentInst(location,armour,structure,rear,type);
            }
        }
        return inst;
    }
    @Override
    public void storeDamage(ContentValues cv){
        cv.put(OVDatabaseForce.COLUMN_SEG_ARMOUR_DMG, armourDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_REAR_DMG, rearDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_STRUCTURE_DMG, structureDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_ARMOUR_TURN_DMG, armourTurnDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_REAR_TURN_DMG, rearTurnDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_STRUCTURE_TURN_DMG, structureTurnDmg);
        cv.put(OVDatabaseForce.COLUMN_SEG_ACTIVE, active?1:0);
    }
    public int getArmourDmg() {
        int retVal = armourDmg;
        if (getArmourType()==ArmourType.HARDENED)
            retVal = Math.floorDiv(armourDmg,2);
        return retVal;
    }

    public void setArmourDmg(int armourDmg) {
        // hardened armour is doubled internally
        if (getArmourType()==ArmourType.HARDENED)
            this.armourDmg = armourDmg*2;
        else
            this.armourDmg = armourDmg;
    }

    public void setSavedDamage(Cursor cur){
        this.armourDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_ARMOUR_DMG);
        this.structureDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_STRUCTURE_DMG);
        this.rearDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_REAR_DMG);
        this.armourTurnDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_ARMOUR_TURN_DMG);
        this.structureTurnDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_STRUCTURE_TURN_DMG);
        this.rearTurnDmg = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_REAR_TURN_DMG);
        active = OVDatabaseForce.getCursorInt(cur,OVDatabaseForce.COLUMN_SEG_ACTIVE)==1;
    }
    @Override
    public void setArmour(int armour, int structure){
        setArmourDmg( armour );
        this.structureDmg = structure;
    }

    public int getStructureDmg() {
        return structureDmg;
    }

    public void setStructureDmg(int structureDmg) {
        this.structureDmg = structureDmg;
    }

    public int getRearDmg() {
        int retVal = rearDmg;
        if (getArmourType()==ArmourType.HARDENED)
            retVal = Math.floorDiv(rearDmg,2);
        return retVal;
    }

    public void setRearDmg(int rearDmg) {
        if (getArmourType()==ArmourType.HARDENED)
            this.rearDmg = rearDmg*2;
        else
            this.rearDmg = rearDmg;
    }

    public int getArmourTurnDmg() {
        return armourTurnDmg;
    }

    public void markSegmentDestroyed(IDamageRecord parent){
        setArmourDmg(0);
        setStructureTurnDmg(0); //mark as destroyed
        destroyLocation(null, parent);//unit info is only used for legs (add fall) and at present
        // this is only used for arms when side torso is destroyed
    }
    public int getStructureTurnDmg() {
        return structureTurnDmg;
    }


    public int getRearTurnDmg() {
        return rearTurnDmg;
    }

    public void setArmourTurnDmg(int armourTurnDmg) {
        this.armourTurnDmg = armourTurnDmg;
    }
    public void setStructureTurnDmg(int structureTurnDmg) {
        this.structureTurnDmg = structureTurnDmg;
    }
    public void setRearTurnDmg(int rearTurnDmg) {
        this.rearTurnDmg = rearTurnDmg;
    }

    @Override
    public boolean isDestroyed(){

        return structureTurnDmg <=0; //assumes that structure starts at full and is reduced to 0
    }

    protected int adjustDamageForArmourType(IWeapon weapon, int damage){
        // TODO: through armour crits are removed for some armour types
        IWeapon.DamageType dType = IWeapon.DamageType.AE; //default to area effect for physical weapons, falls etc
        IWeapon.WeaponMode mode = IWeapon.WeaponMode.STD;
        if (weapon != null){
            dType = weapon.getDamageType();
            mode = weapon.getWeaponMode();
        }
        switch (getArmourType()){
            case HARDENED:
                // hardened armour is already doubled in the internal storage so
                // we don't need to do anything, damage is absorbed 2 -> 1 without change
                if (dType == IWeapon.DamageType.RE){
                    // Re-engineered lasers effectively do double damage against hardened armour
                    // but not the structure, so work out how much damage is used to completely
                    // destroy the remaining armour and double it then add the remaining damage
                    if (damage*2 <= armourTurnDmg) return damage*2;
                    else return damage - Math.floorDiv(armourTurnDmg+1,2) + armourTurnDmg;
                }
                break;
            case BALLISTIC:
                if (dType == IWeapon.DamageType.DB || dType == IWeapon.DamageType.M){
                    int adjDmg = Math.floorDiv(damage,2);
                    if (adjDmg > armourTurnDmg){
                        // any damage left over after punching through the armour gets returned
                        // to its original value
                        adjDmg += 2 * (adjDmg - armourTurnDmg);
                    }
                    return adjDmg;
                }
                break;
            case FERROLAM:
                // Should override this for TW - taken care of at the higher level
                // how will this work for Override? guide says reduce attacks by 20%
                // instead could reduce cluster attacks to 0 and anything doing 4 or more
                // damage reduce by 1
                if (armourTurnDmg > 0) {
                    // only check if there is armour left in this location
                    if (damage >= 4) return damage - 1;
                    else if (mode == IWeapon.WeaponMode.CLUS) return damage - 1;
                }
                break;
            case REFLECTIVE:
                if (dType == IWeapon.DamageType.DE){
                    int adjDmg = Math.floorDiv(damage,2);
                    if (adjDmg > armourTurnDmg){
                        // any damage left over after punching through the armour gets returned
                        // to its original value
                        adjDmg += 2 * (adjDmg - armourTurnDmg);
                    }
                    return adjDmg;
                } else if (dType == IWeapon.DamageType.AE){
                    int adjDmg = damage * 2;
                    if (adjDmg > armourTurnDmg){
                        adjDmg = damage - Math.floorDiv(armourTurnDmg+1,2) + armourTurnDmg;
                    }
                    return adjDmg;
                }
                break;
        }
        return damage;
    }

    @Override
    public int applyDamage(IWeapon weapon, int dmg, IUnitData unit, OVLocation loc, IDamageRecord parent){
        int remainder = 0;
        boolean rear = loc != getLocation();
        if (structureTurnDmg <= 0) remainder = dmg; //segment is already destroyed
        else {
            sent = false;
            int damage = dmg;
            if (getArmourType()!=ArmourType.STANDARD){
                damage = adjustDamageForArmourType(weapon,damage);
            }
            int result;
            if (rear) result = rearTurnDmg -= damage;
            else result = armourTurnDmg -= damage;
            if (result < 0) {
                structureTurnDmg += result; //armour damage is now negative so adding will subtract from structure
                if (rear) rearTurnDmg = 0;
                else armourTurnDmg = 0;
                if (structureTurnDmg <= 0) { // if it is 0 remainder will still be 0 and the destroyed segment message will be displayed
                    remainder -= structureTurnDmg;
                    structureTurnDmg = 0;
                    unit.getTurn().addDamage(new DamageMessage("Location Destroyed-" + getLocation().toScreen(), -1, parent));
                    destroyLocation(unit, parent);
                } else {
                    checkForCrit(unit, null, parent);
                }
            }
            if (structureTurnDmg > 0)
                locationDamage(unit, parent);
        }
        // if this is an ammo explosion and there is overflow damage check for case
        if (remainder>0 && weapon instanceof PhysicalWeapon){
            PhysicalWeapon.PhysicalWeaponType type = ((PhysicalWeapon) weapon).getPhysType();
            if (type == PhysicalWeapon.PhysicalWeaponType.AMMO || //ammo explosion
                type == PhysicalWeapon.PhysicalWeaponType.WEAPON){ //explosive weapon
                // is there CASE?
                if (getEquipmentType(OVEquipment.EquipmentType.CASE)!=null){
                    // case stops the damage overflowing!!
                    remainder = 0;
                }
            }
        }
        return remainder;
    }
    public void resolveTurn(){
        armourDmg = armourTurnDmg;
        structureDmg = structureTurnDmg;
        rearDmg = rearTurnDmg;
    }
    private void locationDamage(IUnitData unit, IChildLink parent){
        if (getLocation() == OVLocation.HEAD){
                unit.getState().getPilot().addInjury(1);
                unit.getTurn().getTurnChecks().add(new ConsciousnessCheck(unit, -1, parent));
        }
    }
    public String getStreamValue(){
        String stream = super.getStreamValue();
        stream += getStreamContent() + '\n';
        return stream;
    }
    protected String getStreamContent(){
        String stream = "," + armourDmg;
        stream += "," + structureDmg;
        stream += "," + rearDmg;
        stream += "," + armourTurnDmg;
        stream += "," + structureTurnDmg;
        stream += "," + rearTurnDmg;
        stream += "," + (active?"1":"0");
        return stream;
    }
    public String getDamageStream(){
        String stream = "SEGMENTDMG:" + super.getLocation();
        stream += getStreamContent() + '\n';
        return stream;
    }
    @Override
    protected void destroyLocation(IUnitData unit, IDamageRecord parent){
        if (!active)return; // segment was already destroyed
        super.destroyLocation(unit, parent);
//        structure isn't always set to 0 when called
        structureTurnDmg = 0;
        armourTurnDmg = 0;
        active = false;
    }
    public int updateDamageFromStream(String[] parts) {
        armourDmg = Integer.parseInt(parts[1]);
        structureDmg = Integer.parseInt(parts[2]);
        rearDmg = Integer.parseInt(parts[3]);
        armourTurnDmg = Integer.parseInt(parts[4]);
        structureTurnDmg = Integer.parseInt(parts[5]);
        rearTurnDmg = Integer.parseInt(parts[6]);
        active = Integer.parseInt(parts[7])==1;
        return 8; // returns index of next item
    }
    public boolean alreadySent() {
        return sent;
    }

    public void markAsSent() {
        sent = true;
    }
    public void setSentFlag(boolean flag){
        sent = flag;
    }
    public void reset(){
        armourTurnDmg    = armourDmg    = getArmour();
        structureTurnDmg = structureDmg = getStructure();
        rearTurnDmg      = rearDmg      = getArmourRear();
    }

    public void incrementDamage(boolean structure, boolean plus) {
        int change = plus ? 1 : -1;
        if (structure) {
            structureTurnDmg += change;
            // make sure it hasn't gone past the allowed bounds
            if (structureTurnDmg < 0) structureTurnDmg = 0;
            else if (structureTurnDmg > getStructure()) structureTurnDmg = getStructure();
            // if we have repaired more than the damage done in the current turn then adjust
            // the previous damage level as well
            if (structureDmg < structureTurnDmg) structureDmg = structureTurnDmg;
        } else {
            armourTurnDmg += change;
            // make sure it hasn't gone past the allowed bounds
            if (armourTurnDmg < 0) armourTurnDmg = 0;
            else if (armourTurnDmg > getArmour()) armourTurnDmg = getArmour();
            // if we have repaired more than the damage done in the current turn then adjust
            // the previous damage level as well
            if (armourDmg < armourTurnDmg) armourDmg = armourTurnDmg;

        }
    }
    public int shootingToHitMod(IUnitData unit){
        // actuator damage on the arms can create a mod, also children can have additional
        // modes, such as stabilizer damage on a vehicle
        int mod = 0;
        if (getLocation()==OVLocation.LEFTARM ||
            getLocation()==OVLocation.RIGHTARM) {
            IEquipment actuator = getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
            if (actuator!=null)
                mod = ((TWActuator) actuator).getShootingMod();
        }
        return mod;
    }
}
