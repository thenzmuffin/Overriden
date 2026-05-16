package com.total.overide;

import android.content.ContentValues;

import com.total.overiden.DamageMessage;
import com.total.overiden.ForceList;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.PilotCheck;
import com.total.overiden.TwoDSix;
import com.total.overiden.UnitMove;

public class OVEquipment implements IEquipment {
    @Override
    public int compareTo(IEquipment iEquipment) {
        int result = type.compareTo(iEquipment.getType());
        if (result==0){
            result = location.compareTo(iEquipment.getLocation());
        }
        return result;
    }

    public enum EquipmentType {
        AMMO("Ammunition"),
        ARTEMISIV("Artemis IV"),
        ARTEMISV("Artemis V"),
        MASC("MASC"),
        TSM("TSM"),
        SUPERCHARGER("SuperCharger"),
        TARGETING("Targeting Computer"),
        WEAPON("Weapon"),
        HATCHET("Hatchet"),
        ENGINE("Engine"),
        GYRO("Gyro"),
        LIFESUPPORT("Life Support"),
        SENSORS("Sensors"),
        COCKPIT("cockpit"),
        STEALTHARM("Stealth Armour"),
        HEATSINK("Heat Sink"),
        JUMPJET("Jump Jet"),
        ECM("ECM"),
        ACTUATOR("Actuator"),
        CASE("CASE"),
        NARC("NARC Pod");
        private final String name;
        EquipmentType(String name){
            this.name = name;
        }
        String getName(){return name;}
    }
    private String name;
    private int id;
    private int index = -1; // orders the equipment in crit list
    private OVSegment.OVLocation location;
    private final EquipmentType type;
    private int special = 0; //stores special state information for different equip types
    private int special2 = 0; //stores special state information for different equip types
    private boolean operational;
    private int numberOfCrits = 1;

    private boolean sent = true;
    public OVEquipment() {
        super();
        name = "No Name";
        location = null;
        type = EquipmentType.AMMO;
        operational = true;
    }
    public OVEquipment(EquipmentType type, int id, OVSegment.OVLocation location, String name) {
        super();
        this.id = id;
        this.type = type;
        if (name == null)this.name = type.getName();
        else this.name = name;
        this.location = location;
        operational = true;
    }
    /*
     * creating equipment from an external source (part of unit creation)
     */
    public OVEquipment(OVEquipment.EquipmentType type, String[] parts){
        super();
        this.type = type;
        name = parts[1];
        id = Integer.parseInt(parts[2]);

        location = OVSegment.OVLocation.valueOf(parts[3]);
        // use the setter for the special flag as this is overiden for TW actuators to set additional flags
        setSpecial(Integer.parseInt(parts[4]));
//        special = Integer.parseInt(parts[4]);
        special2 = Integer.parseInt(parts[5]);
        operational = Boolean.parseBoolean(parts[6]);
        index = Integer.parseInt(parts[7]);
    }

    public boolean isOperational() {
        return operational;
    }

    public void setSpecial(int spec) {
        this.special = spec;
        sent = false;
    }

    public int getSpecial() {
        return special;

    }
    public void setSpecialTwo(int spec) {
        this.special2 = spec;
        sent = false;
    }

    public int getSpecialTwo() {
        return special2;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public OVSegment.OVLocation getLocation() {
        return location;
    }
    public void setLocation(OVSegment.OVLocation location) {
        this.location = location;
    }
    public int getID(){return id;}

    @Override
    public boolean isMoveModifier() {
        return type == EquipmentType.MASC;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EquipmentType getType(){return type;}

    @Override
    public void setStatus(boolean operational) {
        if (this.operational != operational)sent = false;
        this.operational = operational;
    }
    public void reverseCrit(){
        // assumes crits are never applied to an already destroyed equipment
        setStatus(true);
    }
    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
        // deactivate the component
        setStatus(false);
        // does this equipment have any other effects when critted? Ammo? Exploding weapon?
        switch (type){
            case MASC:
                break;
            case ARTEMISIV:
                break;
            case TARGETING:
                break;
        }
    }
    public boolean activateEquipment(IUnitData unit){
        boolean recalc = false;
        // for equipment like MASC or Supercharger an action must be carried out when equipment is activated
        switch (type){
            case MASC:
                TwoDSix test = new TwoDSix();
                special2++; //add 1 to the number or consecutive turns the equipment has been activated
                int pass = special2 * 2;
                pass++;
                if (special2 >= 4)pass+=2;
                String msg = test.getTotal() < pass?"MASC Failure - Target " + pass:
                                                    "MASC Successfully Activated - Target " + pass;
                DamageMessage result = new DamageMessage(msg, -1, null);
                result.setDice(test);
                unit.getTurn().addDamage(result);
                if (test.getTotal() < pass){
                    //critical hit to each leg occurs
                    OVSegment seg = unit.getSegment(OVSegment.OVLocation.LEFTLEG);
                    OVCoreEquipment core = ((OVCoreEquipment)seg.getEquipmentType(OVEquipment.EquipmentType.ACTUATOR));
                    unit.getTurn().addDamage(core.addCrit(test, null));
                    seg = unit.getSegment(OVSegment.OVLocation.RIGHTLEG);
                    core = ((OVCoreEquipment)seg.getEquipmentType(OVEquipment.EquipmentType.ACTUATOR));
                    unit.getTurn().addDamage(core.addCrit(test, null));
                    unit.getTurn().setHexesMoved(unit.getAdjustedMovement(UnitMove.MoveType.RUN)); //reset move to recalculate TMM
                    recalc = true;
                    unit.getTurn().getTurnChecks().add(new PilotCheck(unit,
                            PilotCheck.PilotCheckType.ACTUATOR, null));
                }
            break;
            case ECM:
                special = special==0?1:0;
                // record if ecm is active
                unit.getState().setEcmActive(special==1);
                break;
            case STEALTHARM:
                special++;
                if (special > 2)special = 0;
        }
        return recalc;
    }

    @Override
    public void resolveTurn() {
        if (type==EquipmentType.NARC){
            special = 0; // when a NARC Pod attaches it is marked as 1 on the special to designate it hit this turn
            //setting it to 0 effectively activates it.
        }
    }

    protected int getExplosionDamage() {
        return 0;
    }
    @Override
    public int getHealth() {
        return 1;
    }

    @Override
    public int getDamage() {
        return isOperational()?0:1;
    }

    public String getStreamValue(){
        String stream = "EQUIPMENT:" + type.toString() + "," + name;
        stream += "," + id;
        stream += "," + location.toString();
        stream += "," + special;
        stream += "," + special2;
        stream += "," + operational;
        stream += "," + index;
        return stream;
    }

    @Override
    public void updateFromStream(String[] data) {
        // we don't reset the sent flag here as this is called from a remotely received message
        special = Integer.parseInt(data[4]);
        special2 = Integer.parseInt(data[5]);
        operational = Boolean.parseBoolean(data[6]);
        System.out.println("Equipment update:spec="+special+" spec2="+special2 + " op="+operational);
    }

    public static IEquipment newEquipment(String data, ForceList.ForceType forceType){
        IEquipment newEquip;
        String[] parts = data.split(",");
        OVEquipment.EquipmentType type = OVEquipment.EquipmentType.valueOf(parts[0]);
        switch (type) {
            case AMMO:
                newEquip = new OVAmmunition(parts);
                break;
            case WEAPON:
                newEquip = new OVWeaponInstance(parts);
                break;
            case ACTUATOR:
                if (forceType== ForceList.ForceType.TW) {
                    newEquip = new TWActuator(parts);
                    break;
                }
                //if not TW just flow on and create a regular OVCoreEquipment object instead
            case GYRO:
            case HEATSINK:
            case ENGINE:
                newEquip = new OVCoreEquipment(type, parts);
                break;
            default:
                newEquip = new OVEquipment(type, parts);
        }
        return newEquip;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    public boolean alreadySent() {
        return sent;
    }

    public void markAsSent() {
        sent = true;
    }
    protected void setSent(boolean mark){
        sent = mark;
    }
    public void setCritSlots(int slots){
        numberOfCrits = slots;
    }
    public int getCritSlots(){
        return numberOfCrits;
    }
    @Override
    public void setDatabase(ContentValues cv){
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_ID, id);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_LOCATION, location.toString());
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_SLOTS, numberOfCrits);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_NAME, name);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_TYPE, type.toString());
    }
}
