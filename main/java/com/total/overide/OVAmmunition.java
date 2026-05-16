package com.total.overide;

import android.content.ContentValues;
import android.database.Cursor;

import com.total.overiden.ConsciousnessCheck;
import com.total.overiden.DamageMessage;
import com.total.overiden.DamageRecord;
import com.total.overiden.ForceList;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.TargetData;

import java.util.List;

public class OVAmmunition extends OVEquipment{
    //ammunition record for a single weapon
    private int full = 0;
    private int remaining = 0;
    private IWeapon.WeaponMode ammoType;
//    private boolean isSwitch = false; // has two different types of ammo - cluster
//    private int switchFull = 0;
//    private int switchRemaining = 0;
    private OVWeapon weaponType;

    public OVAmmunition() {
        super();
        remaining = full = 0;
        weaponType = null;
    }

    public OVAmmunition(OVSegment.OVLocation location, int id, OVWeapon type, boolean half, IWeapon.WeaponMode ammoMode) {
        super(EquipmentType.AMMO,id,location,"Ammo - " + (type != null?type.getName():"Unknown"));//name will be
        int total = 0;
        if (type != null) total = type.getAmmoPerTon();
        if (half)
            total = Math.floorDiv(total,2);
        this.ammoType = ammoMode;
//        isSwitch = special;
        this.full = this.remaining = total;
        this.weaponType = type;
    }
    public OVAmmunition(OVSegment.OVLocation location, int id, OVWeapon type, int capacity, IWeapon.WeaponMode ammoMode) {
        super(EquipmentType.AMMO,id,location,"Ammo - " + (type != null?type.getName():"Unknown"));//name will be
        this.ammoType = ammoMode;
//        isSwitch = special;
        this.full = this.remaining = capacity;
        this.weaponType = type;
    }
    public OVAmmunition(String[] parts){
        super(EquipmentType.AMMO,parts);
        full = Integer.parseInt(parts[8]);
        remaining = Integer.parseInt(parts[9]);
        ammoType = IWeapon.WeaponMode.valueOf(parts[10]);

        weaponType = OVMtfReader.findOVWeaponByID(Integer.parseInt(parts[11]));;
    }
    public OVAmmunition(Cursor cur, ForceList.ForceType fType){
        super(EquipmentType.AMMO,
                OVDatabaseForce.getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID),
                OVSegment.OVLocation.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_LOCATION)),
                "Ammo");//name will be
        full = remaining = OVDatabaseForce.getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_SPEC1);
        ammoType = IWeapon.WeaponMode.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_SPEC2));
        weaponType = OVMtfReader.findOVWeaponByID(OVDatabaseForce.getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_LINK));
        setName("Ammo - " + (weaponType != null?weaponType.getName():"Unknown"));
        // need a fiddle for Converting to OV locations
        if (fType!=null) setLocation(OVDatabaseUnit.convertTWtoOVLocation(fType,getLocation()));
    }

    public boolean fire( IWeapon.WeaponMode mode) {
        int rounds = mode.getRounds();
        // have to convert the mode as multifire weapons use STD ammo type
        IWeapon.WeaponMode modeConverted = mode;
        if (rounds > 1 || mode== IWeapon.WeaponMode.AUTO)modeConverted = IWeapon.WeaponMode.STD;

        boolean has = false; //should already be checked, but for ultras it needs to fire two rounds
        if (modeConverted == ammoType) {
            has = rounds <= remaining;
            if (has) {
                remaining -= rounds;
                setSent(false);
            }
        }
        return has;
    }

//    public boolean isSwitch() {
//        return isSwitch;
//    }

    public boolean hasAmmo() {
        return remaining>0;
    }

    public OVWeapon getWeaponType() {
        return weaponType;
    }

    @Override
    public String getName() {
        String description = super.getName() + " - " + getDetailText();
        if (ammoType != IWeapon.WeaponMode.STD)description += "(" + ammoType.getLabel() + ")";
        return description;
    }

    public String getDetailText() {
        return Integer.toString(remaining) + "(" + full + ")";
    }

    public int getFull() {
        return full;
    }
    public void setFull(int full) {
        this.remaining = this.full = full;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
        setSent(false); // needs to be sent to other device if connected via bluetooth
    }

    public int getRemaining() {
        return isOperational()?remaining:0;
    }
    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
        // deactivate the component
        setStatus(false);
        int damage = getExplosionDamage();
        if (damage > 0) {
            DamageRecord dr = new DamageRecord(null, new TargetData(null, unit));
            dr.setParent(parent);
            dr.addGrouping(damage, getLocation());
            dr.setWeapon(new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.AMMO, damage, PhysicalWeapon.PhysicalHitGrouping.FULL));
            unit.getTurn().addDamage(dr);
            dr.applyDamage(unit);

            //pilot damage
            unit.getPilot().addInjury(2);
            unit.getTurn().getTurnChecks().add(new ConsciousnessCheck(unit,-1,parent));
            unit.getTurn().addDamage(new DamageMessage("Pilot Injured from Ammo Explosion", -1, parent));
            remaining = 0;
        }

    }
    @Override
    protected int getExplosionDamage() {
        int damage = 0;
        if (weaponType.isExplosiveAmmo()>0){
            damage = getRemaining() * weaponType.isExplosiveAmmo();
        }
        return damage;
    }
    public IWeapon.WeaponMode getAmmoType(){
        return ammoType;
    }

    public String getStreamValue(){
        String stream = super.getStreamValue();
        stream += "," + full;
        stream += "," + remaining;
        stream += "," + ammoType.toString();
        stream += "," + weaponType.getId();
        return stream;
    }
    @Override
    public void updateFromStream(String[] data) {
        super.updateFromStream(data);
        remaining = Integer.parseInt(data[9]);
        // full ammo allotment, ammo type and weapon type should never change in game
    }

    public IWeapon.WeaponMode[] getAvailableAmmoTypes(){
        return weaponType.getAvailableAmmoTypes();
    }

    public void setAmmoType(IWeapon.WeaponMode ammoType) {
        this.ammoType = ammoType;
        //can't be called during a game so reset ammo to full and adjust
        // only relevant for Autocannons
        remaining = full = (int)(ammoType.getMultiplier() * weaponType.getAmmoPerTon());
    }

    @Override
    public void setDatabase(ContentValues cv) {
        super.setDatabase(cv);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_LINK, weaponType.getId());
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_SPEC1, full);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_SPEC2, ammoType.toString());
    }
}
