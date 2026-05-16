package com.total.overiden;

import android.view.View;

import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;
import com.total.overide.OVState;

import java.util.List;

public interface IUnitData extends IUnitDesign{
    boolean isActive();
    boolean isActiveForPhase(Turn.Phase phase);
    UnitTurn getTurn();
    void applyDamage(IWeapon weapon, DamageRecord record, DamageRecord.DamageGrouping damage, TargetData target);
    void addFallDamage(DamageRecord dr, int levels);
    int getKey();
    Pilot getPilot();
    boolean endPhase(Turn.Phase phase); // returns true if the unit is still active
    UnitTurn resetTurn();
    OVState getState();
    int getCurrentTMM();
    boolean addMovementCheck(UnitMove.MoveType move);
    public int noOfRemainingLegs();
    int getAdjustedHeat(); // returns heat next turn based on current selections
    String getAdjustedHeatTooltip();
    int getPhysicalAttackDamage(PhysicalWeapon.PhysicalWeaponType selected);
    PhysicalWeapon.PhysicalHitGrouping getPhysicalAttackGrouping(PhysicalWeapon.PhysicalWeaponType selected);
    OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent);
    OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing);
    int getAdjustedMovement(UnitMove.MoveType type);
    void endTurn();

    int getSpecialTargetMods(int range);
    String getSpecialTargetModsTooltip(int range);
    int getSensorDamageMod();
    List<String> getStreamValue();
    boolean isNarced();
    boolean isImmobile();
    IUnitDisplay getDisplayObject(int forceList, Turn.Phase phase);
    OVSegmentInst getCoreSegment();

    int getCompValue(); // return an integer value for sorting in a list (e.g. priority for AI)
    int getHealth(); //returns a numeric value for the units remaining health
    PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes();
}
