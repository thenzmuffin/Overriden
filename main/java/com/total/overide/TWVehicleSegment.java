package com.total.overide;

import com.total.overiden.CriticalHit;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.Pilot;
import com.total.overiden.TwoDSix;

import java.util.Random;

public class TWVehicleSegment extends OVSegmentInst{
    private enum VehicleCrits{
        NONE,
        DRIVER,
        WEAPONMALFUNCTION,
        STABILIZER,
        SENSORS,
        COMMANDER,
        WEAPONDESTROYED,
        KILLED,
        CARGO,
        STUNNED,
        ENGINE,
        FUEL,
        AMMUNITION,
        JAM,
        LOCKED,
        BLOWNOFF;
    }
    private static final VehicleCrits[] frontCrits = {
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.DRIVER,
            VehicleCrits.WEAPONMALFUNCTION,
            VehicleCrits.STABILIZER,
            VehicleCrits.SENSORS,
            VehicleCrits.COMMANDER,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.KILLED};
    private static final VehicleCrits[] sideCrits = {
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.CARGO,
            VehicleCrits.WEAPONMALFUNCTION,
            VehicleCrits.STUNNED,
            VehicleCrits.STABILIZER,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.ENGINE,
            VehicleCrits.FUEL};
    private static final VehicleCrits[] rearCrits = {
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.WEAPONMALFUNCTION,
            VehicleCrits.CARGO,
            VehicleCrits.STABILIZER,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.ENGINE,
            VehicleCrits.AMMUNITION,
            VehicleCrits.FUEL};
    private static final VehicleCrits[] turretCrits = {
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.NONE,
            VehicleCrits.STABILIZER,
            VehicleCrits.JAM,
            VehicleCrits.WEAPONMALFUNCTION,
            VehicleCrits.LOCKED,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.AMMUNITION,
            VehicleCrits.BLOWNOFF};

    private boolean stabiliserDamaged = false;
    public TWVehicleSegment(OVLocation location, int armour, int structure, int rear, ArmourType type){
        super(location, armour, structure, rear, type);
    }

    public TWVehicleSegment(String data) {
        super(data);
        String[] parts = data.split(",");
        if (parts.length>=12)stabiliserDamaged = Boolean.parseBoolean(parts[11]);
    }

    public void checkForCrit(IUnitData unit,CriticalHit crit, IDamageRecord parent) {
        VehicleCrits[] crits;
        switch (getLocation()){
            case FRONT:
                crits = frontCrits;
                break;
            case LEFT:
            case RIGHT:
                crits = sideCrits;
                break;
            case REARSIDE:
                crits = rearCrits;
                break;
            case TURRET:
                crits = turretCrits;
                break;
            default:
                crits = frontCrits;
        }

        // determine if a crit has occurred
        TwoDSix check = new TwoDSix();
        boolean resolved = false;
        OVWeaponInstance weapon;
        int diceRoll = check.getTotal()-2;
        do {
            switch (crits[diceRoll]){
                case NONE:
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-No Crit", check, null, null, parent));
                    resolved = true; // temporary until crits are implemented
                    break;
                case DRIVER:
                    // +2 to driver skill rolls, after first treat as crew stunned
                    if (!unit.getState().getPilot().setPilotingMods(Pilot.PilotingMods.DRIVERCRIT)){
                        unit.getState().setStunned(true);
                    }
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Driver Injured", check, null, null, parent));
                    resolved = true;
                    break;
                case COMMANDER:
                    // first hit is +1 to gunnery +1 to piloting
                    // also same as crew stunned (for all times this crit is rolled)
                    unit.getState().getPilot().setPilotingMods(Pilot.PilotingMods.COMMANDER);
                    unit.getState().setStunned(true);
                    resolved = true;
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Commander Injured", check, null, null, parent));
                    break;
                case STUNNED:
                    // one turn of max cruise speed and no weapons fire per hit
                    // if already had either a Driver hit and/or a commander hit then treat as crew killed
                    if (!unit.getState().getPilot().getPilotingModsSet().contains(Pilot.PilotingMods.COMMANDER) &&
                            !unit.getState().getPilot().getPilotingModsSet().contains(Pilot.PilotingMods.DRIVERCRIT)){
                        unit.getState().setStunned(true);
                        resolved = true;
                        unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Crew Stunned", check, null, null, parent));
                        break;
                    }
                    // commander or driver crit already occurred so flow through to crew killed crit
                case KILLED:
                    // effectively vehicle is destroyed but can be salvaged after the game
                    unit.getState().setDestroyedCrit(true);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Crew Killed", check, null, null, parent));
                    resolved = true;
                    break;
                case WEAPONMALFUNCTION:
                    // randomly selected weapon in this location is disabled, can be repaired by
                    // spending a turn making no weapon attacks (one weapon per turn)
                    weapon = getRandomWeapon();
                    if (weapon!=null) {
                        weapon.setJammed(true);
                        unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Weapon Jammed", check, null, null, parent));
                        resolved = true;
                    }
                    break;
                case STABILIZER:
                    // weapons in this location now have double AMM penalty
                    if (!stabiliserDamaged) {
                        stabiliserDamaged = true;
                        unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Stabilser Damaged", check, null, null, parent));
                        resolved = true; // what if it is already damaged?
                    }
                    break;
                case SENSORS:
                    // sensor damage - up to 4 hits, +1 per hit, 4th hit weapons can't fire
                    if (unit.getState().getSensors()<4) {
                        unit.getState().setSensors(unit.getState().getSensors() + 1);
                        if (unit.getState().getSensors() >= 4)
                            unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Sensors Destroyed", check, null, null, parent));
                        else
                            unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Sensors Damaged", check, null, null, parent));
                        resolved = true;
                    }
                    break;
                case WEAPONDESTROYED:
                    // weapon destroyed (should be D6 1-3 owner picks the weapon, 4-6 shooter
                    // picks the weapon but will be randomly selected here)
                    weapon = getRandomWeapon();
                    if (weapon!=null) {
                        resolved = true;
                        unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Weapon Destroyed", check, null, weapon, parent));
                    }
                    break;
                case CARGO:
                    // if the unit carries cargo (including infantry) it is destroyed.
                    // for infantry they take the same damage as caused by the weapon causing
                    // the critical
                    break;
                case FUEL:
                    if (unit.getHeader().getEngine()== OVHeader.EngineType.ICE) {
                        // Vehicle destroyed in fuel explosion
                        unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Fuel Tank Explosion", check, null, null, parent));
                        unit.getState().setDestroyedCrit(true);
                        resolved = true;
                    }
                    break;
                case ENGINE:
                    // energy and pulse weapons are destroyed (no power source)
                    for (IEquipment equip : getEquipment()){
                        if (equip.getType()== OVEquipment.EquipmentType.WEAPON &&
                                equip.isOperational()){
                            if (equip instanceof OVWeaponInstance &&
                                    (((OVWeaponInstance)equip).getDamageType()== IWeapon.DamageType.DE ||
                                     ((OVWeaponInstance)equip).getDamageType()== IWeapon.DamageType.P))
                                equip.setStatus(false);
                        }
                    }
                    // unit is now immobile
                    unit.getState().setMotive(unit.getHeader().getWalk()); //TODO do we need to set an immobile flag
                    // turret lock
                    unit.getState().setTurret(OVState.TurretState.LOCKED);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Engine Hit", check, null, null, parent));
                    resolved = true;
                    break;
                case AMMUNITION:
                    // ALL ammunition explodes
                    // ammunition in vehicles is not stored in the segment it is stored in the body
                    // so needs to be retrieved from the unitdata object
                    for (IEquipment equip : unit.getEquipment()){
                        if (equip.getType()== OVEquipment.EquipmentType.AMMO){
                            resolved = true;
                            unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Ammo Explosion", check, null, equip, parent));
                        }
                    }
                    break;
                case JAM:
                    // Turret Jammed - can be repaired but a second jam is a turret lock
                    // second jam is taken care of in the setter method
                    unit.getState().setTurret(OVState.TurretState.JAMMED);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Turret Jammed", check, null, null, parent));
                    resolved = true;
                    break;
                case LOCKED:
                    // Turret Locked - cannot be repaired
                    unit.getState().setTurret(OVState.TurretState.LOCKED);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Turret Locked", check, null, null, parent));
                    resolved = true;
                    break;
                case BLOWNOFF: // turret blown off - vehicle destroyed
                    unit.getState().setDestroyedCrit(true);
                    resolved = true;
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Turret Destroyed", check, null, null, parent));
                    break;
            }
            diceRoll++;
            if (diceRoll>=10)diceRoll=0;
        } while (!resolved);
    }

    private OVWeaponInstance getRandomWeapon(){
        // how many weapons in this location?
        int weaponCount = 0;
        for (IEquipment equip : getEquipment()){
            if (equip.getType()== OVEquipment.EquipmentType.WEAPON &&
                equip.isOperational()){
                weaponCount++;
            }
        }
        if (weaponCount>0) {
            int mal = new Random().nextInt(weaponCount);
            weaponCount = 0;
            for (IEquipment equip : getEquipment()) {
                if (equip.getType() == OVEquipment.EquipmentType.WEAPON) {
                    if (weaponCount == mal) {
                        //this is the one
                        return (OVWeaponInstance)equip;
                    }
                    weaponCount++;
                }
            }
        }
        return null; // if no weapon found
    }
//    private void applyNumberOfCrits(IUnitData unit, TwoDSix check, int no) {
//        IDamageRecord out;
//        for (int i = 0; i < no; i++) {
//            out = null;
//            //reorder the equipment to make sure all non-critable equipment is at the bottom of the list
//            sortEquipment();
//            //calculate how many valid crit locations are still present
//            int totalCritSlots = getRemainingCritSlots();
//            // randomly determine the hit location
//            if (totalCritSlots > 0) {
//                int loc = (new Random()).nextInt(totalCritSlots);
//                for (IEquipment equip : getEquipment()) {
//                    if (equip.isOperational()) {
//                        loc -= equip.getCritSlots();
//                        if (loc < 0) {
//                            out = new CriticalHit(getLocation().name() + "- " + equip.getName() + " destroyed", check, null, equip);
//                            unit.getTurn().addDamage(out);
//                            out.applyDamage(unit);
//                            break;
//                        }
//                    }
//                }
//            } else {
//                // we need to roll this to the next location inwards on the mech,
//                // typically this will only happen from side torso to centre torso
//                OVLocation newLocation = unit.transferDestroyedLocations(getLocation(), TargetData.LocTable.FRONT);
//                ((TWVehicleSegment) unit.getSegment(newLocation)).applyNumberOfCrits(unit, check, no);
//            }
//        }
//
//    }
    public int shootingToHitMod(IUnitData unit){
        // actuator damage on the arms can create a mod, also children can have additional
        // modes, such as stabilizer damage on a vehicle
        int mod = 0;
        if (stabiliserDamaged) {
            mod = unit.getTurn().getAMM();
        }
        return mod;
    }

    public boolean isStabiliserDamaged() {
        return stabiliserDamaged;
    }

    public void setStabiliserDamaged(boolean stabiliserDamaged) {
        this.stabiliserDamaged = stabiliserDamaged;
    }
    public int updateDamageFromStream(String[] parts) {
        int count = super.updateDamageFromStream(parts);
        if (parts.length>count){
            stabiliserDamaged = Integer.parseInt(parts[count++])==1;
        }
        return count;
    }
    protected String getStreamContent(){
        return super.getStreamContent() + "," + stabiliserDamaged;
    }
}
