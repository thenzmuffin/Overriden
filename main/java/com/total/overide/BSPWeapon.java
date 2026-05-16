package com.total.overide;

import android.content.ContentValues;

import com.total.overiden.DamageRecord;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.TargetData;

import java.util.Collections;
import java.util.List;

public class BSPWeapon implements IWeapon {
    private int damageValue; // how much damage does each group do?
    private int damageNumber; // how many damage groups are there?
    private int shortRange;
    private int mediumRange;
    private int longRange;

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public boolean isMoveModifier() {
        return false;
    }

    @Override
    public int getSpecial() {
        return 0;
    }

    @Override
    public void setSpecial(int special) {

    }

    @Override
    public int getSpecialTwo() {
        return 0;
    }

    @Override
    public void setSpecialTwo(int special) {

    }

    @Override
    public boolean activateEquipment(IUnitData unit) {
        return false;
    }

    @Override
    public void resolveTurn() {

    }

    @Override
    public int getHeat() {
        return 0;
    }

    @Override
    public void fireWeapon() {
        // no ammo for BSP vehicles
    }

    @Override
    public void checkWeaponJam(int rolled) {
        // no weapon jamming for BSP vehicles
    }

    @Override
    public boolean isJammed() {
        // can't jam
        return false;
    }

    @Override
    public void hit(TargetData target, TargetData.LocTable table) {
        int noOfHits = damageNumber;
        DamageRecord dmg = new DamageRecord(this, target);
        while (noOfHits > 0) {
                dmg.addGrouping(damageValue);
                noOfHits--;
        }
        target.getTarget().getTurn().addDamage(dmg);
        dmg.applyDamage(target.getTarget());
    }

    @Override
    public WeaponMode getWeaponMode() {
        return WeaponMode.STD;
    }

    @Override
    public boolean setWeaponMode(WeaponMode mode) {
        return false;
    }

    @Override
    public List<WeaponMode> getAvailableModes() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public OVSegment.OVLocation getLocation() {
        return OVSegment.OVLocation.NONE;
    }

    @Override
    public OVEquipment.EquipmentType getType() {
        return OVEquipment.EquipmentType.WEAPON;
    }

    @Override
    public void setStatus(boolean operational) {
        // cannot crit a BSP Vehicles weapon so this is not required.
    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
        // cannot crit a BSP Vehicles weapon so this is not required.
    }

    @Override
    public void reverseCrit() {

    }

    @Override
    public boolean isOperational() {
        return true;
    }

    @Override
    public int getHealth() {
        return 1;
    }

    @Override
    public int getDamage() {
        return 1;
    }

    @Override
    public int getRangeMod(int range) {
        int mod = 20;
        // this is for hexed:
        if (range <= shortRange)
            mod = 0;
        else         if (range <= mediumRange)
            mod = 2;
        else if (range <= longRange)
            mod = 4;
        return mod;
    }

    @Override
    public int getHeatDamage() {
        return 0;
    }

    @Override
    public DamageType getDamageType() {
        return null;
    }

    @Override
    public OVWeapon.WeaponType getWeaponType() {
        return null;
    }

    @Override
    public String getLocationText() {
        return "Vehicle";
    }

    @Override
    public boolean isMultiMode() {
        return false;
    }

    @Override
    public List<OVAmmunition> getAmmo() {
        return Collections.emptyList();
    }

    @Override
    public boolean isIndirect() {
        return false;
    }

    @Override
    public String getStreamValue() {
        return "";
    }

    @Override
    public void updateFromStream(String[] data) {

    }

    @Override
    public boolean alreadySent() {
        return false;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public void setIndex(int ind) {

    }

    @Override
    public void markAsSent() {

    }

    @Override
    public int getCritSlots() {
        return 0;
    }

    @Override
    public void setDatabase(ContentValues cv) {

    }

    @Override
    public String getDamageText() {
        return damageValue + "x" + damageNumber;
    }

    @Override
    public boolean hasArtemis() {
        return false;
    }

    @Override
    public void setArtemis(boolean art) {

    }

    @Override
    public MainActivity.Sounds getSoundEffect() {
        return null;
    }

    @Override
    public int getClusterDamage() {
        return damageValue;
    }

    @Override
    public int compareTo(IEquipment iEquipment) {
        return 0;
    }
}
