package com.total.overiden;

import android.content.ContentValues;

import com.total.overide.OVAmmunition;
import com.total.overide.OVEquipment;
import com.total.overide.OVSegment;
import com.total.overide.OVWeapon;

import java.util.Collections;
import java.util.List;

public class BspWeapon implements IWeapon{
    private final BSPStrike strike;

    public BspWeapon(BSPStrike strike) {
        super();
        this.strike = strike;
    }

    @Override
    public int getID() {
        return strike.getKey() + 4000; // 4k range indicates BSPWeapon
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

    }

    @Override
    public void checkWeaponJam(int rolled) {

    }

    @Override
    public boolean isJammed() {
        return false;
    }

    @Override
    public void hit(TargetData target, TargetData.LocTable table) {

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
        return strike.getName();
    }

    @Override
    public OVSegment.OVLocation getLocation() {
        return null;
    }

    @Override
    public OVEquipment.EquipmentType getType() {
        return null;
    }

    @Override
    public void setStatus(boolean operational) {

    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {

    }

    @Override
    public void reverseCrit() {

    }

    @Override
    public boolean isOperational() {
        return false;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getDamage() {
        return 0;
    }

    @Override
    public int getRangeMod(int range) {
        return 0;
    }

    @Override
    public int getHeatDamage() {
        return 0;
    }

    @Override
    public DamageType getDamageType() {
        return DamageType.AE;
    }

    @Override
    public OVWeapon.WeaponType getWeaponType() {
        return null;
    }

    @Override
    public String getLocationText() {
        return "";
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
        return "";
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
        return 0;
    }

    @Override
    public int compareTo(IEquipment iEquipment) {
        return 0;
    }
}
