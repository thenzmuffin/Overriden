package com.total.overide;

import android.view.View;

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
import com.total.overiden.TurnPhaseFragment;
import com.total.overiden.UnitMove;
import com.total.overiden.UnitTurn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BSPUnitData implements IUnitData {
    public enum BSPSpecials {
        AI,
        AMS,
        APC,
        ARTILLERY,
        COMMANDER,
        CRIT_SEEKER,
        IMMOBILE,
        INDIRECT,
        MECHANIZED,
        NIMBLE,
        NOTURRET,
        SPOTTER,
        SWARM;
    }
    private int key;
    private int skill;
    private int groupSize;
    private int noOfGroups;
    private int move;
    private int tmm;
    private int threshold;
    private int check;
    private BSPWeapon weapon;
    private List<BSPSpecials> specials;
    private boolean active;
    public BSPUnitData(){
        super();
        specials = new ArrayList<>();
        skill = 6;
        tmm = 0;
        move = 2;
        groupSize = noOfGroups = threshold = check = 1;
        active = true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isActiveForPhase(Turn.Phase phase) {
        return true;
    }

    @Override
    public UnitTurn getTurn() {
        return null;
    }

    @Override
    public void applyDamage(IWeapon weapon, DamageRecord record, DamageRecord.DamageGrouping damage, TargetData target) {

    }

    @Override
    public void addFallDamage(DamageRecord dr, int levels) {
        // vehicles can't fall?
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public Pilot getPilot() {
        return null;
    }

    @Override
    public boolean endPhase(Turn.Phase phase) {
        return true;
    }

    @Override
    public UnitTurn resetTurn() {
        return null;
    }

    @Override
    public OVState getState() {
        return null;
    }

    @Override
    public int getCurrentTMM() {
        return tmm;
    }

    @Override
    public int noOfRemainingLegs() {
        return 2;
    }

    @Override
    public int getAdjustedHeat() {
        return 0;
    }

    @Override
    public String getAdjustedHeatTooltip() {
        return "";
    }

    @Override
    public int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected) {
        return 0;
    }

    @Override
    public PhysicalWeapon.PhysicalHitGrouping getPhysicalAttackGrouping(PhysicalWeapon.PhysicalWeaponType selected) {
        return null;
    }
    public boolean addMovementCheck(UnitMove.MoveType move){
        return false;
    }
    @Override
    public OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent) {
        return OVSegment.OVLocation.NONE;
    }

    @Override
    public OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing) {
        return OVSegment.OVLocation.NONE;
    }

    @Override
    public int getAdjustedMovement(UnitMove.MoveType type) {
        return 0;
    }

    @Override
    public void endTurn() {

    }

    @Override
    public int getSpecialTargetMods(int range) {
        return 0;
    }

    @Override
    public String getSpecialTargetModsTooltip(int range) {
        return "";
    }

    @Override
    public int getSensorDamageMod() {
        return 0;
    }

    @Override
    public List<String> getStreamValue() {
        return Collections.emptyList();
    }

    @Override
    public boolean isNarced() {
        return false;
    }

    @Override
    public boolean isImmobile() {
        return false;
    }

    @Override
    public IUnitDisplay getDisplayObject(int forceList, Turn.Phase phase) {
        return null;
    }

    @Override
    public OVSegmentInst getCoreSegment() {
        return null;
    }

    @Override
    public int getCompValue() {
        return 0;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes() {
        return new PhysicalWeapon.PhysicalWeaponType[0];
    }

    @Override
    public IUnitHeader getHeader() {
        return null;
    }

    @Override
    public List<IEquipment> getEquipment() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasEquipment(OVEquipment.EquipmentType type) {
        return false;
    }

    @Override
    public List<IWeapon> getWeapons() {
        return Collections.emptyList();
    }

    @Override
    public List<IEquipment> getActivityEnhancers(Turn.Phase phase) {
        return Collections.emptyList();
    }

    @Override
    public OVSegment getSegment(OVSegment.OVLocation location) {
        return null;
    }

    @Override
    public List<OVSegment> getSegments() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasIndirectWeapons() {
        return false;
    }
}
