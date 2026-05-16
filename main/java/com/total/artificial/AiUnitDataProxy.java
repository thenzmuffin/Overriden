package com.total.artificial;

import com.total.overide.OVEquipment;
import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;
import com.total.overide.OVState;
import com.total.overiden.DamageRecord;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.Pilot;
import com.total.overiden.TargetData;
import com.total.overiden.Turn;
import com.total.overiden.UnitMove;
import com.total.overiden.UnitTurn;

import java.util.Collections;
import java.util.List;

public class AiUnitDataProxy implements IUnitData {
    private IUnitData unit;
    public AiUnitDataProxy(IUnitData unit){
        super();
        this.unit = unit;
    }

    @Override
    public boolean isActive() {
        return unit.isActive();
    }

    @Override
    public boolean isActiveForPhase(Turn.Phase phase) {
        return unit.isActiveForPhase(phase);
    }

    @Override
    public UnitTurn getTurn() {
        return unit.getTurn();
    }

    @Override
    public void applyDamage(IWeapon weapon, DamageRecord record, DamageRecord.DamageGrouping damage, TargetData target) {
        unit.applyDamage(weapon, record,damage,target);
    }

    @Override
    public void addFallDamage(DamageRecord dr, int levels) {
        unit.addFallDamage(dr,levels);
    }

    @Override
    public int getKey() {
        return unit.getKey();
    }

    @Override
    public Pilot getPilot() {
        return unit.getPilot();
    }

    @Override
    public boolean endPhase(Turn.Phase phase) {
        return unit.endPhase(phase);
    }

    @Override
    public UnitTurn resetTurn() {
        return unit.resetTurn();
    }

    @Override
    public OVState getState() {
        return unit.getState();
    }

    @Override
    public int getCurrentTMM() {
        return unit.getCurrentTMM();
    }

    @Override
    public boolean addMovementCheck(UnitMove.MoveType move) {
        return unit.addMovementCheck(move);
    }

    @Override
    public int noOfRemainingLegs() {
        return unit.noOfRemainingLegs();
    }

    @Override
    public int getAdjustedHeat() {
        return unit.getAdjustedHeat();
    }

    @Override
    public String getAdjustedHeatTooltip() {
        return unit.getAdjustedHeatTooltip();
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {
        return unit.getPhysicalAttackDamage(selected);
    }

    @Override
    public PhysicalWeapon.PhysicalHitGrouping getPhysicalAttackGrouping(PhysicalWeapon.PhysicalWeaponType selected) {
        return unit.getPhysicalAttackGrouping(selected);
    }

    @Override
    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {
        return unit.convertLocation(twoDSix,facing,table,registerCrit, parent);
    }

    @Override
    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing) {
        return unit.transferDestroyedLocations(location,facing);
    }

    @Override
    public int getAdjustedMovement(UnitMove.MoveType type) {
        return unit.getAdjustedMovement(type);
    }

    @Override
    public void endTurn() {
        unit.endTurn();
    }

    @Override
    public int getSpecialTargetMods(int range) {
        return unit.getSpecialTargetMods(range);
    }

    @Override
    public String getSpecialTargetModsTooltip(int range) {
        return unit.getSpecialTargetModsTooltip(range);
    }

    @Override
    public int getSensorDamageMod() {
        return unit.getSensorDamageMod();
    }

    @Override
    public List<String> getStreamValue() {
        return unit.getStreamValue();
    }

    @Override
    public boolean isNarced() {
        return unit.isNarced();
    }

    @Override
    public boolean isImmobile() {
        return unit.isImmobile();
    }

    @Override
    public IUnitDisplay getDisplayObject(int forceList, Turn.Phase phase) {
        return unit.getDisplayObject(forceList,phase);
    }

    @Override
    public OVSegmentInst getCoreSegment() {
        return unit.getCoreSegment();
    }

    @Override
    public int getCompValue() {
        return unit.getCompValue();
    }

    @Override
    public int getHealth() {
        return unit.getHealth();
    }

    @Override
    public PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes() {
        return unit.getPhysicalWeaponTypes();
    }

    @Override
    public IUnitHeader getHeader() {
        return unit.getHeader();
    }

    @Override
    public List<IEquipment> getEquipment() {
        return unit.getEquipment();
    }

    @Override
    public boolean hasEquipment(OVEquipment.EquipmentType type) {
        return unit.hasEquipment(type);
    }

    @Override
    public List<IWeapon> getWeapons() {
        return unit.getWeapons();
    }

    @Override
    public List<IEquipment> getActivityEnhancers(Turn.Phase phase) {
        return unit.getActivityEnhancers(phase);
    }

    @Override
    public OVSegment getSegment(OVSegment.OVLocation location) {
        return unit.getSegment(location);
    }

    @Override
    public List<OVSegment> getSegments() {
        return unit.getSegments();
    }

    @Override
    public boolean hasIndirectWeapons() {
        return unit.hasIndirectWeapons();
    }

    public IUnitData getSubUnit(){
        return unit;
    }
}
