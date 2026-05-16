package com.total.overide;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.total.overiden.CriticalHit;
import com.total.overiden.DamageRecord;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.TargetData;
import com.total.overiden.TwoDSix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OVSegment {
    public enum ArmourType {
        STANDARD,
        HARDENED,
        BALLISTIC, //ballistic reinforced
        REFLECTIVE, //laser reflective
        FERROLAM,
        STEALTH
    }
    public enum OVLocation {
        NONE("None","N",false),
        HEAD("Head","H",false),
        LEFTARM("Left Arm","LA",false),
        RIGHTARM("Right Arm","RA",false),
        TORSO("Torso","T",false),
        LEFTLEG("Left Leg","LL",false),
        RIGHTLEG("Right Leg","RL",false),
        REAR("Rear Torso","T(R)",true), // included for identifying rear facing weapons
        //add TW locations
        CENTRETORSO("Centre Torso","CT",false),
        LEFTTORSO("Left Torso","LT",false),
        RIGHTTORSO("Right Torso","RT",false),
        CTREAR("Centre Torso Rear","CT(R)",true),
        LTREAR("Left Torso Rear","LT(R)",true),
        RTREAR("Right Torso Rear","RT(R)",true),
        //include vehicle locations
        FRONT("Front", "F",false),
        BODY("Body","B",false),
        RIGHT("Right Side", "RS",false),
        LEFT("Left Side", "LS",false),
        REARSIDE("Rear", "R",false),
        TURRET("Turret","Tu",false),
        BUILDING("Building","Bld",false);

        private final String name;
        private final String shortName;
        private final boolean isRear;
        OVLocation(String name, String shortN, boolean rear){
            this.name = name;
            shortName = shortN;
            isRear = rear;
        }
        @NonNull
        public String toScreen(){
            return name;
        }
        public String getShortName(){ return shortName;}
        public boolean isRear(){return isRear;}
    }
    private OVLocation location;
    private int armour;
    private int rear;
    private int structure;

    private ArmourType armourType;

    private final List<IEquipment> equipment;

    public OVSegment(OVLocation location, int armour, int structure, int rear, ArmourType type){
        super();
        int multiplier = 1;
        if (type == ArmourType.HARDENED)multiplier = 2;
        this.location = location;
        this.armour = armour * multiplier;
        this.structure = structure;
        this.rear = rear * multiplier;
        equipment = new ArrayList<>();
        armourType = type;
    }

    public OVSegment(String data){
        super();
        String[] parts = data.split(",");
        location = OVLocation.valueOf(parts[0]);
        armour = Integer.parseInt(parts[1]);
        structure = Integer.parseInt(parts[2]);
        rear = Integer.parseInt(parts[3]);
        equipment = new ArrayList<>();
        armourType = ArmourType.valueOf(parts[4]);
    }

    public void setArmour(int armour, int structure){
        this.armour = armour;
        this.structure = structure;
    }
    public int getArmour(){
        return armour;
    }
    public int getArmourRear(){
        return rear;
    }
    public int getStructure(){
        return structure;
    }
    public void setRearArmour(int armour){
        rear = armour;
    }

    public OVLocation getLocation() {
        return location;
    }

    public void setLocation(OVLocation location) {
        this.location = location;
    }

    public ArmourType getArmourType() {
        return armourType;
    }

    public void setArmourType(ArmourType armourType) {
        this.armourType = armourType;
    }

    public void addEquipment(IEquipment equip) {
        equipment.add(equip);
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        // segment is "equal" to a location if the segment is for that location
        boolean equals = false;
        if (obj instanceof OVLocation) {
            OVLocation loc = (OVLocation) obj;
            // if the location is rear then this should match to the TORSO segment
            if (loc.isRear) {
                if (loc == OVLocation.REAR) loc = OVLocation.TORSO;
                else if (loc == OVLocation.LTREAR) loc = OVLocation.LEFTTORSO;
                else if (loc == OVLocation.CTREAR) loc = OVLocation.CENTRETORSO;
                else if (loc == OVLocation.RTREAR) loc = OVLocation.RIGHTTORSO;
            }
            if (location == loc) equals = true;
        } else if (obj instanceof OVSegment)
            if (location == ((OVSegment) obj).location)
                equals = true;

        return equals;
    }

    public List<IEquipment> getEquipment() {
        return equipment;
    }

    public IEquipment getEquipmentType(OVEquipment.EquipmentType type) {
        for (int i = 0; i < equipment.size(); i++) {
            if (equipment.get(i).getType() == type){
                return equipment.get(i);
            }
        }
        return null;
    }
    private IDamageRecord findCrit(OVEquipment.EquipmentType type, int num, TwoDSix check, IDamageRecord parent){
        Random ran = new Random();
        int selected = ran.nextInt(num);
        for (int i = 0; i < equipment.size(); i++) {
            if (equipment.get(i).isOperational())
                if (type != null && equipment.get(i).getType() == type) {
                    if (selected == 0){
                        // crit this one
//                        equipment.get(i).setStatus(false);
                        return new CriticalHit(getLocation().name() + "- " + equipment.get(i).getName() + " destroyed", check, null, equipment.get(i), parent);
                    } else {
                        selected--;
                    }
                } else if (type == null && equipment.get(i).getType() != OVEquipment.EquipmentType.WEAPON
                        && equipment.get(i).getType() != OVEquipment.EquipmentType.AMMO
                        && equipment.get(i).getType() != OVEquipment.EquipmentType.ACTUATOR
                        && equipment.get(i).getType() != OVEquipment.EquipmentType.GYRO
                        && equipment.get(i).getType() != OVEquipment.EquipmentType.ENGINE)
                    if (selected == 0){
                        // crit this one
//                        equipment.get(i).setStatus(false);
                        return new CriticalHit(getLocation().name() + "- " + equipment.get(i).getName() + " destroyed", check, null, equipment.get(i), parent);
                    } else {
                        selected--;
                    }
        }
            return null;
    }

    public void checkForCrit(IUnitData unit,CriticalHit crit, IDamageRecord parent) {
//        if (unit.getHeader().getType()== ForceList.ForceType.TW){
//            twCheckForCrit(unit);
//            return;
//        }
        System.out.println("Critical hit resolution started");
//        TwoDSix dSix = null;
        TwoDSix check = new TwoDSix();
        // first check if a crit has occurred
        IDamageRecord out = null;
        if (check.getTotal() >= 8) {
            int ammo = 0;
            int weapon = 0;
            int other = 0;
            // now determine which crit types are available for this segment
            for (int i = 0; i < equipment.size(); i++) {
                if (equipment.get(i).isOperational())
                    switch (equipment.get(i).getType()) {
                        case ENGINE:
                        case GYRO: // don't need to capture gyro or engine as it is always there
                            break;
                        case AMMO:
                            ammo++;
                            break;
                        case WEAPON:
                            weapon++;
                            break;
                        default:
                            other++;
                            break;
                    }
            }

            // if the head is critted the mech is destroyed so check this before doing anything else
            switch (location){
                case HEAD:
                // mech destroyed
                    applyDamage(null,21, unit, OVLocation.HEAD, parent); // this is enough damage to destroy any Mech's head
                    return;
                case REAR:
                case TORSO:
                    out = checkForCritTorso(unit, ammo, weapon, other, check, parent);
                    break;
                case LEFTARM:
                case RIGHTARM:
                    out = checkForCritArm(unit,ammo,weapon,other, check, parent);
                    break;
                case LEFTLEG:
                case RIGHTLEG:
                    out = checkForCritLeg(unit,ammo,weapon,other, check, parent);
                    break;
            }
        }
        // if we failed to generate a crit then no crit is applied
        if (out==null) unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-No Crit", check, null, null, parent));
//        // we should now apply all generated crits - everything in the damage list should have already been applied
        else out.applyDamage(unit);
    }
    private IDamageRecord checkForCritTorso(IUnitData unit, int ammo, int weapon, int other, TwoDSix check, IDamageRecord parent) {
//        TwoDSix check = new TwoDSix();
        TwoDSix dSix;
        IDamageRecord out;
        // If engine is not standard then an additional roll is required to check for an engine crit
        OVCoreEquipment engine = (OVCoreEquipment)getEquipmentType(OVEquipment.EquipmentType.ENGINE);
        if (engine != null && engine.getCritTarget() > 0) {
            dSix = new TwoDSix(1, TwoDSix.RollType.LOCATION);
            if (dSix.getTotal() >= engine.getCritTarget()) {
                out = engine.addCrit(check, parent);
                unit.getTurn().addDamage(out);
                return out;
            }
        }

        dSix = new TwoDSix(1, TwoDSix.RollType.LOCATION);
        switch (dSix.getTotal()) {
            case 1: // always check for ammo first
                if (ammo>0) {
                    out = findCrit(OVEquipment.EquipmentType.AMMO,ammo, check, parent);
                    if (out!=null)unit.getTurn().addDamage(out);
                    break;
                }
            case 2: //weapon
                if (weapon>0) {
//                                return addCrit(weapon);
                    out = findCrit(OVEquipment.EquipmentType.WEAPON,weapon, check, parent);
                    if (out!=null)unit.getTurn().addDamage(out);
                    break;
                } else if (other > 0) {
                    out = findCrit(null, other, check, parent);
                    if (out != null) unit.getTurn().addDamage(out);
                    break;
                }
            case 3:
            case 4:
                OVCoreEquipment gyro = (OVCoreEquipment) getEquipmentType(OVEquipment.EquipmentType.GYRO);
                out = gyro != null ? gyro.addCrit(check, parent) : null;
                if (out != null) unit.getTurn().addDamage(out);
                break;
            default:
                out = engine != null ? engine.addCrit(check, parent) : null;
                if (out != null) unit.getTurn().addDamage(out);
                break;
        }
        return out;
    }
    private IDamageRecord checkForCritArm(IUnitData unit, int ammo, int weapon, int other, TwoDSix check, IDamageRecord parent){
        IDamageRecord out = null;
        TwoDSix dSix = new TwoDSix(1, TwoDSix.RollType.LOCATION);
        switch (dSix.getDice(1)) {
            case 1:
                if (ammo >0) {
                    out = findCrit(OVEquipment.EquipmentType.AMMO,ammo, check, parent);
                    unit.getTurn().addDamage(out);
                    break;
                }
            case 2:
                if (other>0) {
                    out = findCrit(null,other, check, parent);
                    unit.getTurn().addDamage(out);
                    break;
                }
            default:
                if (weapon>0) {
                    out = findCrit(OVEquipment.EquipmentType.WEAPON,weapon, check, parent);
                } else if (other>0) {
                    out = findCrit(null,other, check, parent);
                } else if (ammo > 0) {
                    out = findCrit(OVEquipment.EquipmentType.AMMO,ammo, check, parent);
                }
                if (out!=null)unit.getTurn().addDamage(out);
                break;
        }
        return out;
    }
    private IDamageRecord checkForCritLeg(IUnitData unit, int ammo, int weapon, int other, TwoDSix check, IDamageRecord parent){
        IDamageRecord out = null;
        TwoDSix dSix = new TwoDSix(1, TwoDSix.RollType.LOCATION);
        if (dSix.getDice(1)==1) {
            if (ammo > 0) {
                out = findCrit(OVEquipment.EquipmentType.AMMO, ammo, check, parent);
            } else if (weapon > 0) {
                out = findCrit(OVEquipment.EquipmentType.WEAPON, weapon, check, parent);
            } else if (other > 0) {
                out = findCrit(null, other, check, parent);
            }
        }
        if (out==null){
                IEquipment equip = getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
                if (equip!=null)
                    System.out.println("check for crit leg - " + location + ":" + equip.getName());
                else
                    System.out.println("No equipment found! location:" + location);
                if (equip instanceof OVCoreEquipment)
                    out = ((OVCoreEquipment) equip).addCrit(check, parent);
                else
                    System.out.println("Actuator returned that isn't OVCoreEquipment");
        }
        if (out!=null)unit.getTurn().addDamage(out);
        return out;
    }
    protected void destroyLocation(IUnitData unit, IDamageRecord parent){
        for (IEquipment iEquipment : equipment) {
            iEquipment.setStatus(false);
        }
        if (unit != null) {
            switch (getLocation()) {
                case RIGHTLEG:
                case LEFTLEG:
                    // can only fall if not already lying down
                    if (!unit.getState().isProne()) {
                        //leg destroyed means an autofall
                        DamageRecord dr = new DamageRecord(new TargetData(null, unit));
                        unit.addFallDamage(dr, 1);
//                getUnit().getTurn().addDamage(dr);
                        dr.applyDamage(unit);
                    }
                    break;
                // when a side torso is destroyed so is the attached arm
                case RIGHTTORSO:
                    unit.getSegment(OVLocation.RIGHTARM).destroyLocation(unit, parent);
                    break;
                case LEFTTORSO:
                    unit.getSegment(OVLocation.LEFTARM).destroyLocation(unit, parent);
                    break;
            }
        }
    }
    /*
     * This method is here for the child class only
     */
    public boolean isDestroyed(){
        return false;
    }
    public void resolveTurn(){}
//    public IDamageRecord applyDamage(int damage){return null;}
    public int applyDamage(IWeapon weapon, int damage, IUnitData unit, OVLocation loc, IDamageRecord parent){return 0;}

    public void storeDamage(ContentValues cv){
        // simplifies OVDatabaseForce function to have an overidable method
        cv.put(OVDatabaseForce.COLUMN_SEG_ARMOUR_DMG, armour);
        cv.put(OVDatabaseForce.COLUMN_SEG_REAR_DMG, rear);
        cv.put(OVDatabaseForce.COLUMN_SEG_STRUCTURE_DMG, structure);
        cv.put(OVDatabaseForce.COLUMN_SEG_ARMOUR_TURN_DMG, armour);
        cv.put(OVDatabaseForce.COLUMN_SEG_REAR_TURN_DMG, rear);
        cv.put(OVDatabaseForce.COLUMN_SEG_STRUCTURE_TURN_DMG, structure);
    }
    public String getStreamValue(){
        String stream = "SEGMENT:" + location.toString();
        stream += "," + armour;
        stream += "," + structure;
        stream += "," + rear;
        stream += "," + armourType.toString();
        return stream;
    }

    public void reset(){
        // clear any damage set on equipment - reset ammo to full
        for (IEquipment equip : equipment){
            equip.setStatus(true);
            if (equip instanceof OVAmmunition){
                OVAmmunition ammo = ((OVAmmunition)equip);
                ammo.setRemaining(ammo.getFull());
            }
        }

    }
    public void markSegmentDestroyed(IDamageRecord parent){

    }
    public void initialiseIndex(){
        int lastIndex=0;
        for (IEquipment equip : equipment){
            lastIndex = Math.max(equip.getIndex(),lastIndex);
        }
        for (IEquipment equip : equipment){
            if (equip.getIndex()==-1)equip.setIndex(++lastIndex);
        }
        sortEquipment();
    }
    public void sortEquipment(){
        equipment.sort((equip, t1) -> {
            int comp = Boolean.compare(equip.isOperational(),t1.isOperational());
            if (comp==0) {
                comp = Integer.compare(equip.getIndex(), t1.getIndex());
            }
            return comp;
        });
    }
    protected int getRemainingCritSlots(){
        int slots = 0;
        for (IEquipment equip : equipment){
            if (equip.isOperational()){
                slots+=equip.getCritSlots();
            }
        }
        return slots;
    }

    public void incrementDamage(boolean structure, boolean plus){
        // do nothing, method is here to be overriden
    }
    public int shootingToHitMod(IUnitData unit){return 0;}
}
