package com.total.overide;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.total.overiden.ConsciousnessCheck;
import com.total.overiden.DamageMessage;
import com.total.overiden.DamageRecord;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.GenericCheck;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDisplay;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.Pilot;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.Turn;
import com.total.overiden.UnitMove;
import com.total.overiden.UnitTurn;
import com.total.overiden.UpdatePlayerActions;

import java.util.List;

public abstract class UnitData extends OVUnitDesign implements IUnitData {
    protected static OVSegment.OVLocation[] locationStd;
//    protected static OVSegment.OVLocation[] locationLeft;
//    protected static OVSegment.OVLocation[] locationRight;
    protected static OVSegment.OVLocation[] locationPunch;
    protected static final OVSegment.OVLocation[] locationKick = {
            OVSegment.OVLocation.RIGHTLEG,
            OVSegment.OVLocation.RIGHTLEG,
            OVSegment.OVLocation.RIGHTLEG,
            OVSegment.OVLocation.LEFTLEG,
            OVSegment.OVLocation.LEFTLEG,
            OVSegment.OVLocation.LEFTLEG};
    protected OVState state;
    protected UnitTurn turn;
    public UnitData(int designKey){
        super();
        state = new OVState(designKey);
    }
    public UnitData(Cursor cur){
        super(cur);
        state = new OVState(cur);
    }
    public UnitData(List<String> list, String deviceName){
        super(list);
        for (String line : list){
            String[] parts = line.split(":");
            if (parts[0].equals("STATE")){
                state = new OVState(parts[1], deviceName);
                break;
                //only one state line for a unit so quit
            }
        }
        linkSegments();
    }

    public static UnitData newInstance(List<String> list, String deviceName) {
        switch (list.get(0)) {
            case "STARTOVMECH":
                return new OVUnitData(list, deviceName);
            case "STARTOVVEHICLE":
                return new OVVehicleData(list, deviceName);
            case "STARTOVBUILDING":
                return new OVBuildingData(list,deviceName);
            case "STARTTWMECH":
                return new TWUnitData(list, deviceName);
            case "STARTTWVEHICLE":
                return new TWVehicleData(list,deviceName);
        }
        return null;
    }
    public static UnitData newInstance(Cursor cur) {
        switch (ForceList.ForceType.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_HEADER_TYPE))) {
            case OV:
                switch (IUnitHeader.UnitType.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_HEADER_UNIT_TYPE))) {
                    case MECH:
                        return new OVUnitData(cur);
                    case HOVER:
                    case TANK:
                        return new OVVehicleData(cur);
                    case BUILDING:
                        return new OVBuildingData(cur);
                }
            case TW:
                switch (IUnitHeader.UnitType.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_HEADER_UNIT_TYPE))) {
                    case MECH:
                    return new TWUnitData(cur);
                    case HOVER:
                    case TANK:
                        return new TWVehicleData(cur);
                }
        }
        return null;
    }
    @NonNull
    public OVState getState() {
        return state;
    }

    @Override
    public boolean isActive() {
        return state.isActive();
    }
    protected abstract boolean isMechDestroyed();
    @Override
    public boolean endPhase(Turn.Phase phase) {
        boolean dbUpdate, stillActive = true;
        if (!isActive()) return false; // mech was destroyed in a previous phase so we don't need to recalculate

        // this method carries out any activities needed at the end of a phase
        // primarily this means setting the active flag on the state if the unit was destroyed
        // during this phase as simultaneous combat means it should complete all actions for the
        // current turn before being destroyed
        // isMechDestroyed is actually is mech alive - misnamed
        if (!isMechDestroyed()) dbUpdate = checkPilotState();
        else {
            state.setActive(false);
            dbUpdate = true;
            stillActive = false;
        }
        if (phase== Turn.Phase.INITIATIVE && isImmobile()){
            // an immobile unit is not displayed in the movement phase so needs to be set as phase complete (standstill)
            turn.getMoveData().setType(UnitMove.MoveType.STILL,state.isProne());
            turn.getMoveData().setHexesMoved(0);
            turn.getMoveData().setMoveLocked(true);
        }
        if (phase== Turn.Phase.PHYSICAL && state.getExternalID()<0){
            // it is a local unit so needs to have the resolution of the turn run
            getTurn().resolveTurn();
            // will send any status updates or newly created checks to the other device and update to DB
            UpdatePlayerActions.updateCheckRecord(this);
            UpdatePlayerActions.turnResolved(this);
        }
        // If state was updated then adjust the DB, doesn't need to be sent over Bluetooth as
        // should happen independently on both ends of the connection
        if (dbUpdate)UpdatePlayerActions.updateState(this);
        return stillActive;
    }

    @Override
    public UnitTurn getTurn() {
        if (turn == null){
            turn = new UnitTurn(this);
        }
        return turn;
    }
    public UnitTurn resetTurn() {
        turn = new UnitTurn(this);
        return turn;
    }
    @Override
    public int hashCode() {
        return getKey();
    }
    @Override
    public void applyDamage(IWeapon weapon, DamageRecord record, DamageRecord.DamageGrouping damage, TargetData target) {
        boolean startsActive = isMechDestroyed(); // store whether the mech was active before damage applied
        // If in partial cover then check to see if the hit is to the legs in which case it is actually a miss
        if (target.isPartialCover() && ( damage.getConvertedLocation() == OVSegment.OVLocation.LEFTLEG || damage.getConvertedLocation() == OVSegment.OVLocation.RIGHTLEG) ) {
            damage.partialCoverMiss = true;
            return;
        }
        // loop at damage application until there isn't any damage left to apply transfering
        // remaining damage to next location
        int points = damage.damage;
        OVSegment.OVLocation loc = damage.getConvertedLocation();
        OVSegment.OVLocation oldloc;
        do {
            oldloc = loc;
            points = getSegment(loc).applyDamage(weapon, points, this, loc, record);
            if (points>0){

                loc = transferDestroyedLocations(damage.getConvertedLocation(), target.getFacing());
            }
        } while (points > 0 && oldloc != loc);
        // is active isn't updated until the end of the phase so to avoid death rattle occurring several times
        // get result from isMechDestroyed which recalculates without reseting active flag
        if(startsActive && isMechDestroyed()){
            if (Game.current.isSoundEffects())
                MainActivity.currentActivity.playSound(MainActivity.Sounds.BOOM);
            turn.addDamage(new DamageMessage("Mech Destroyed", -1, record));
            // clear any pending checks on this unit (conciousness or piloting)
            getTurn().clearChecks();
        }

    }
    public abstract OVSegment.OVLocation transferDestroyedLocations(OVSegment.OVLocation location, TargetData.LocTable facing);

    public abstract OVSegment.OVLocation convertLocation(int twoDSix, TargetData.LocTable facing, TargetData.LocTable table, boolean registerCrit, IDamageRecord parent);

    @Override
    public int getKey() {
        return state.getKey();
    }

    @Override
    public Pilot getPilot() {
        return state.getPilot();
    }

    public int getCurrentTMM(){
        // get the selected movement type

        return getTurn().getTMM();
    }

    @Override
    public int getAdjustedHeat() {
        int addedHeat = getState().getHeat(); // get the heat level we started the turn with
        addedHeat += getTurn().getMoveData().getMoveHeat(); // add heat generated through movement
        // Get heated generated through weapons
        for (TargetWeapon target : turn.getWeaponList()) {
            // 3 basic scenarios for weapons generating heat: a weapon fired(normal), Streak hit,
            // AMS triggered(will have status of hit so can go in the normal bucket)
            if (target.getWeapon().getWeaponType()== OVWeapon.WeaponType.STREAK) {
                // special case for Streak, add hit before firing or if it hit
                if (target.isLocked() && target.getStatus() != TargetWeapon.ShotStatus.MISS) {
                    addedHeat += target.getWeapon().getHeat();
                }
            } else if (target.getWeapon().getWeaponType()== OVWeapon.WeaponType.DEFENCE){
                // for defensive weapons (AMS) only add heat if the are HITS
                // (they never miss so this means they were triggered)
                if (target.getStatus()== TargetWeapon.ShotStatus.HIT)
                    addedHeat += target.getWeapon().getHeat();
            } else if (target.isLocked()) {
                // otherwise if it is locked(general case)
                addedHeat += target.getWeapon().getHeat();
            }
        }
        // add any heat through flamers/plasma weapons
        int extHeat = turn.getExternalHeat();
        if (extHeat > 0 && !Game.current.isSmartHeat())
            extHeat = Math.max(Math.floorDiv(extHeat+3,5),1); // does a minimum of 1 heat to target

        addedHeat += Math.min(extHeat, (Game.current.isSmartHeat()?15:3));

        // what if the engine has taken damage?
        addedHeat += state.getEngine() * (Game.current.isSmartHeat()?5:1); //add 1 or 5 heat per engine hit depending on heat scale being used
        // now remove the heat through heatsinks
        addedHeat -= getHeader().getHeatSinks();
        // remove destroyed heatsinks
        addedHeat += getState().getDestroyedHeatSinks() * (getHeader().isDoubleHeatSinks()?2:1);
//        if (addedHeat<0)addedHeat = 0;
        return  addedHeat ;
    }
    @Override
    public String getAdjustedHeatTooltip(){
        StringBuilder tip = new StringBuilder("Starting Heat: " + getState().getHeat() + "\n");
        int calcHeat = getTurn().getMoveData().getMoveHeat();
        if (calcHeat != 0) tip.append("Heat From Movement: ").append(calcHeat).append("\n");
        for (TargetWeapon target : turn.getWeaponList()) {
            if (target.getWeapon().getWeaponType()== OVWeapon.WeaponType.STREAK) {
                // special case for Streak, add hit before firing or if it hit
                if (target.isLocked() && target.getStatus() != TargetWeapon.ShotStatus.MISS) {
                    tip.append(target.getWeapon().getName()).append(" : ").append(target.getWeapon().getHeat()).append("\n");
                }
            } else if (target.getWeapon().getWeaponType()== OVWeapon.WeaponType.DEFENCE){
                // for defensive weapons (AMS) only add heat if the are HITS
                // (they never miss so this means they were triggered)
                if (target.getStatus()== TargetWeapon.ShotStatus.HIT)
                    tip.append(target.getWeapon().getName()).append(" : ").append(target.getWeapon().getHeat()).append("\n");
            } else if (target.isLocked()) {
                // otherwise if it is locked(general case)
                tip.append(target.getWeapon().getName()).append(" : ").append(target.getWeapon().getHeat()).append("\n");
            }

        }
        calcHeat = Math.min(turn.getExternalHeat(), (Game.current.isSmartHeat()?15:3));
        if (calcHeat!=0) { //allow for negative heat items in the future
            tip.append("External Heat : ").append(calcHeat).append("\n");
        }
        calcHeat = state.getEngine() * (Game.current.isSmartHeat()?5:1);
        if (calcHeat!=0) { //allow for negative heat items in the future
            tip.append("Engine Damage : ").append(calcHeat).append("\n");
        }
//        int heatSinks = getHeader().getHeatSinks() -
//                (getState().getDestroyedHeatSinks() * (getHeader().isDoubleHeatSinks()?2:1));
        tip.append("\nHeat Sinks : -").append(getHeader().getHeatSinks());
        tip.append("\nTotal Heat : ").append(getAdjustedHeat());
        return tip.toString();
    }

    public PhysicalWeapon.PhysicalHitGrouping getPhysicalAttackGrouping(PhysicalWeapon.PhysicalWeaponType selected) {
        PhysicalWeapon.PhysicalHitGrouping grouping = null;
        switch (selected){
            case PUNCH:
                grouping = PhysicalWeapon.PhysicalHitGrouping.TOP;
                break;
            case KICK:
                grouping = PhysicalWeapon.PhysicalHitGrouping.BOTTOM;
                break;
            case CHARGE:
                grouping = PhysicalWeapon.PhysicalHitGrouping.FULL;
                break;
            case DFA:
                grouping = PhysicalWeapon.PhysicalHitGrouping.TOP;
                break;
            case NONE:
                break;
        }
        return grouping;
    }
    /*
     * determine damage from a fall
     * add damage to current turn
     * set the state to prone
     * Add the conciousness check from falling (TW would be a roll to avoid injury)
     */
    public abstract void addFallDamage(DamageRecord dr, int levels);

    public void endTurn(){
        for (OVSegment seg : getSegments()){
            seg.resolveTurn();
        }
    }
    @Override
    public String getSpecialTargetModsTooltip(int range) {
        String tip = "";
        int mod = getSpecialTargetMods(range);
        if (mod > 0 && getHeader().getArmourType()== OVSegment.ArmourType.STEALTH){
            tip += "Stealth Armour: " + mod + "\n";
        }
        return tip;
    }
    @Override
    public int getSpecialTargetMods(int range) {
        // this method needs to check for special modifiers to the units TMM for example Stealth
        // armour
        int adj = 0;
        if (getHeader().getArmourType()== OVSegment.ArmourType.STEALTH){
            // is the stealth armour activated?
            if (range >=OVRange.me) {//does nothing at short range or closer
                for (IEquipment equip : getEquipment()) {
                    if (equip.getType() == OVEquipment.EquipmentType.STEALTHARM) {
                        if (equip.getSpecial() == 2) { //special is 2 when activated (1 is ECM mode)
                            if (range == OVRange.me) adj++;
                            if (range >= OVRange.lo) adj += 2;
                        }
                        break;
                    }
                }
            }
        }
        return adj;
    }
    public boolean addMovementCheck(UnitMove.MoveType type){
        if (type == UnitMove.MoveType.WALK || type == UnitMove.MoveType.RUN) {
            if (getState().isProne()) {
                getState().setProne(false);
                getTurn().getMoveData().setStood(true);
            }
        }
        return false;
    }
    public int noOfRemainingLegs() {
        int legs = 2;
        if (getSegment(OVSegment.OVLocation.LEFTLEG).isDestroyed()) legs--;
        if (getSegment(OVSegment.OVLocation.RIGHTLEG).isDestroyed()) legs--;
        return legs;
    }
    public int getAdjustedMovement(UnitMove.MoveType type) {
        int walk = 0;
        if (type == UnitMove.MoveType.STILL
                || type == UnitMove.MoveType.NONE) return walk;
        else if (type == UnitMove.MoveType.CRAWL) return 1;

        int legsLeft = noOfRemainingLegs();
        boolean legDestroyed = legsLeft < 2;
        if (legsLeft == 0)
            return 0; // no legs, no move!
        if (type == UnitMove.MoveType.JUMP) {
            if (!legDestroyed)walk = getHeader().getJump() - getState().getDestroyedJets(); // can't jump with one leg
        } else if (!legDestroyed){
            walk = getHeader().getWalk();
            walk -= getState().getHeatMovementPenalty();
            if (getHeader().isTsm())walk += getState().getTsmBonus();
            float runMod = 1.5f;
                // now get modifiers for movement crits
                walk = getActuatorDamage(walk);

            if (walk < 0) walk = 0;
            if (type == UnitMove.MoveType.RUN) {
                // check if any positive modifiers have been activated
                for (IEquipment equip : getActivityEnhancers(Turn.Phase.MOVE)) {
                    // if another modifier other than supercharger and masc is introduced this will need to be changed
                    if (equip.getSpecial() > 0) runMod += 0.5F;

                }
                walk = (int) Math.ceil(walk * runMod);

            }
            if (state.isProne()||getTurn().getMoveData().isStood())walk-=2;//it takes 2 inches of movement to stand
            if (walk<1)walk = 1; //minimum move
        } else walk = 1; // if one leg is destroyed then walk and run are both 1

        return walk;
    }
    protected abstract int getActuatorDamage(int inWalk);
    public List<String> getStreamValue(){
        List<String> stream = super.getStreamValue();
        stream.add(state.getStreamValue());

        return stream;
    }

    protected static OVSegment.OVLocation[] generateSingleLocTableFromResource(int resourceID){
        String[] locs = MainActivity.currentActivity.getResources().getStringArray(resourceID);
        OVSegment.OVLocation[] generated = new OVSegment.OVLocation[locs.length];
        for (int i = 0;i<locs.length;i++){
            generated[i] = OVSegment.OVLocation.valueOf(locs[i]);
        }
        return generated;
    }
    protected boolean checkPilotState(){
        //pilot dead?
        boolean dbUpdate = false;
        if (state.isActive()) {
            state.setActive(getPilot().getInjuries()<6);
            dbUpdate = !state.isActive();
        }

        // also we need to set the pilot's consciousness state if they failed a check this phase
        if (getPilot().isConscious()) {
            for (GenericCheck check : getTurn().getTurnChecks()) {
                if (check instanceof ConsciousnessCheck && check.getStatus() == TargetWeapon.ShotStatus.MISS) {
                    getPilot().setConscious(false);
                    dbUpdate = true;
                    break; // don't need to keep checking
                }
            }
        }
        return dbUpdate;
    }
    public boolean isNarced(){
        boolean narc = false;
        for (IEquipment equip : getEquipment()) {
            if (!equip.isOperational()) continue;//will be deactivated if the location is destroyed
            if (equip.getType() == OVEquipment.EquipmentType.NARC && equip.getSpecial() == 0) {
                narc = true;
            }
        }
        return narc;
    }
    public int getSensorDamageMod(){
        int ret = 0;
        // for TW if one sensor hit then +2 to hit, 2 sensor hits and no shooting is possible
        if (getState().getSensors()>0) ret = ((getState().getSensors()==1)?2:30);
        return ret;
    }
    public abstract void addWeapon(Cursor cur);
    public boolean isImmobile(){
        boolean ret = getState().isShutdown() || !getState().getPilot().isConscious();
        // Also immobile if it has no arms and legs left
        if (!ret) ret = getSegment(OVSegment.OVLocation.RIGHTARM).isDestroyed() &&
                getSegment(OVSegment.OVLocation.RIGHTLEG).isDestroyed() &&
                getSegment(OVSegment.OVLocation.LEFTARM).isDestroyed() &&
                getSegment(OVSegment.OVLocation.LEFTLEG).isDestroyed();
        return ret;
    }
    protected IUnitDisplay currentDisplay = null;
    public IUnitDisplay getDisplayObject(int forceList, Turn.Phase phase) {
//        IUnitDisplay ret;
        if (!isActive()) {
            return null; // need to create a Destroyed unitdisplay class
        }
        switch (phase) {
            case MOVE:
                if (!(currentDisplay instanceof UnitDisplayMove))
                    currentDisplay = new UnitDisplayMove(this, forceList);
                break;
            case TARGET:
                if (!(currentDisplay instanceof UnitDisplayTarget))
                    currentDisplay = new UnitDisplayTarget(this, forceList);
                break;
            case RESOLVE:
                if (!(currentDisplay instanceof UnitDisplayResolve))
                    currentDisplay = new UnitDisplayResolve(this, forceList);
                break;
            case PHYSICAL:
                if (!(currentDisplay instanceof UnitDisplayPhysical))
                    currentDisplay = new UnitDisplayPhysical(this, forceList);
                break;
            case SHOOT:
                if (!(currentDisplay instanceof UnitDisplayShoot))
                    currentDisplay = new UnitDisplayShoot(this, forceList);
                break;
            default:
                if (!(currentDisplay instanceof UnitDisplay))
                    currentDisplay = new UnitDisplay(this, forceList);
        }
        return currentDisplay;
    }
    public OVSegmentInst getCoreSegment(){
        return null;
    }
    public PhysicalWeapon.PhysicalWeaponType[] getPhysicalWeaponTypes() {
        // do not include charge or DFA as they are predeclared
        PhysicalWeapon.PhysicalWeaponType[] types;
        boolean punch = turn.armsFree();
        boolean kick = turn.legsFree();
        boolean melee = hasEquipment(OVEquipment.EquipmentType.HATCHET);
        int num = 1; //start with no attack option
        if (punch) num++;
        if (kick) num++;
        if (melee) num++;
        types = new PhysicalWeapon.PhysicalWeaponType[num];
        num = 0;
        types[num++] = PhysicalWeapon.PhysicalWeaponType.NONE;
        if (punch) types[num++] = PhysicalWeapon.PhysicalWeaponType.PUNCH;
        if (kick) types[num++] = PhysicalWeapon.PhysicalWeaponType.KICK;
        if (melee) types[num] = PhysicalWeapon.PhysicalWeaponType.HATCHET;
        return types;
    }

    @NonNull
    @Override
    public String toString() {
        return getHeader().getName() + ":" + getPilot().getPilotName();
    }

    @Override
    public int getCompValue() {
        return 0;
    }
    @Override
    public boolean isActiveForPhase(Turn.Phase phase){
        if (phase== Turn.Phase.SETUP)return true;
        boolean ret = true;
        // Show even destroyed units in the resolve phase
        if (phase!= Turn.Phase.RESOLVE)ret = isActive();
        if (ret){
            switch (phase){
                case MOVE:
                    ret = !isImmobile();
                    break;
                case TARGET:
                    // does the unit have any weapons that can be targetted?
                    ret = false;
                    for (IWeapon weapon : getWeapons()){
                        if (weapon.isOperational()){
                            ret = true;
                            break;
                        }
                    }
                    break;
                case SHOOT:
                    // WAS HIDING IF THEY DON'T HAVE A WEAPON TO SHOOT BUT THEY CAN STILL RECEIVE DAMAGE
                    // does the unit have any targeted weapons?
                    // assumes target list is sorted to have targets with weapons
                    // locked on at the top so if there are no targets on the first
                    // item then this unit isn't active for the shooting phase
//                    TargetData td = turn.getAllTargets().get(0);
//                    if (td==null || !td.hasWeapons())
//                        ret = false;
                    break;
                case PHYSICAL:
                    //can this unit make a physical attack?
                    if (getPhysicalWeaponTypes().length==0 &&
                            turn.getReservePhysicalAttack()== PhysicalWeapon.PhysicalWeaponType.NONE){
                        //there are no available physical attack types
                        ret = false;
                    }
                    break;
            }
        }

        return ret;
    }
}
