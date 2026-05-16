package com.total.overide;

import com.total.overiden.CriticalHit;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.TwoDSix;

import java.util.Random;

public class OVVehicleSegment extends OVSegmentInst{
    private enum VehicleCrits{
        MOTIVE,
        CREWHIT,
        WEAPONDESTROYED,
        STUNNED,
        AMMUNITION;
    }
    private static final VehicleCrits[] frontCrits = {
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.WEAPONDESTROYED,
            VehicleCrits.CREWHIT,
            VehicleCrits.STUNNED};
    private static final VehicleCrits[] sideCrits = {
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE};
    private static final VehicleCrits[] rearCrits = {
            VehicleCrits.AMMUNITION,
            VehicleCrits.AMMUNITION,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE,
            VehicleCrits.MOTIVE};

    private boolean stabiliserDamaged = false;
    public OVVehicleSegment(OVLocation location, int armour, int structure, int rear, ArmourType type){
        super(location, armour, structure, rear, type);
    }

    public OVVehicleSegment(String data) {
        super(data);
        String[] parts = data.split(",");
        if (parts.length>=12)stabiliserDamaged = Boolean.parseBoolean(parts[11]);
    }

    public void checkForCrit(IUnitData unit,CriticalHit crit, IDamageRecord parent) {
        VehicleCrits[] crits;
        switch (getLocation()){
            case FRONT:
            case TURRET:
                crits = frontCrits;
                break;
            case LEFT:
            case RIGHT:
                crits = sideCrits;
                break;
            case REARSIDE:
                crits = rearCrits;
                break;
            default:
                crits = frontCrits;
        }

        // determine if a crit has occurred
        TwoDSix check = new TwoDSix(1, TwoDSix.RollType.LOCATION);
        boolean resolved = false;
        OVWeaponInstance weapon;
        int diceRoll = check.getTotal()-1;
        do {
            switch (crits[diceRoll]){
                case MOTIVE:
                    int pen = unit.getState().getMotive()+2;
                    unit.getState().setMotive(Math.min(pen,unit.getHeader().getWalk()));
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Motive Crit", check, null, null, parent));
                    resolved = true; // temporary until crits are implemented
                    break;
                case CREWHIT:
                    // +2 to driver skill rolls, after first treat as crew stunned
                    unit.getState().getPilot().addInjury(1);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Crew Injured", check, null, null, parent));
                    resolved = true;
                    break;
                case STUNNED:
                    // one turn of max cruise speed and no weapons fire per hit
                    // if already had either a Driver hit and/or a commander hit then treat as crew killed
                    unit.getState().setStunned(true);
                    resolved = true;
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Crew Stunned", check, null, null, parent));
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
            }
            diceRoll++;
            if (diceRoll>=6)resolved = true;
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
//                ((OVVehicleSegment) unit.getSegment(newLocation)).applyNumberOfCrits(unit, check, no);
//            }
//        }
//
//    }
    public int shootingToHitMod(IUnitData unit){
        // actuator damage on the arms can create a mod, also children can have additional
        // modes, such as stabilizer damage on a vehicle
        return 0;
    }

    public int updateDamageFromStream(String[] parts) {
        int count = super.updateDamageFromStream(parts);
        if (parts.length>count){

        }
        return count;
    }
    protected String getStreamContent(){
        return super.getStreamContent();
    }
}
