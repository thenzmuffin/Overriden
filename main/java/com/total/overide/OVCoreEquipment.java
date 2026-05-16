package com.total.overide;

import com.total.overiden.CriticalHit;
import com.total.overiden.Game;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.PilotCheck;
import com.total.overiden.TwoDSix;

/*
 * special equipment class: Engine, Gyro
 */
public class OVCoreEquipment extends OVEquipment {
    private OVHeader.EngineType engine = null;
    private OVState state = null;

//    public OVCoreEquipment() {
//        super(EquipmentType.GYRO, -1, OVSegment.OVLocation.TORSO, null);
//    }

    public OVCoreEquipment(OVHeader.EngineType type, int key, OVSegment.OVLocation loc) {
        super(EquipmentType.ENGINE, key, loc, null);
        engine = type;
    }
    public OVCoreEquipment(EquipmentType type,OVSegment.OVLocation location, int key) {
        super(type, key, location, type.getName());
        engine = null;
        if (type==EquipmentType.GYRO)setCritSlots(4);
        else if (type==EquipmentType.SENSORS ||
                 type==EquipmentType.LIFESUPPORT)setCritSlots(2);
    }
    public OVCoreEquipment(EquipmentType type, String[] parts){
        super(type, parts);
        if (!parts[8].equals("NULL"))
            engine = OVHeader.EngineType.valueOf(parts[8]);

    }

    @Override
    public void setStatus(boolean operational) {
        super.setStatus(operational);
        if (getType()==EquipmentType.ENGINE && state!=null){
            // in the case of a side torso any engine locations need to have crits applied when the item
            // is made inoperable(happens when the side torso is destroyed)
            state.setEngine(state.getEngine() + getCritSlots());
        }
    }
    public void reverseCrit(){
        if (state!=null){
            switch (getType()) {
                case ENGINE:
                    state.setEngine(state.getEngine() - 1);
                    break;
                case GYRO:
                    state.setGyro(state.getGyro() - 1);
                    break;
                case ACTUATOR:
                    // TW crits handled in overriding class
                    state.setMotive(state.getMotive() - 1);
                    break;
                case SENSORS:
                    state.setSensors(state.getSensors() - 1);
                    if (state.getSensors()<2){
                        setStatus(true);
                    }
                    break;
                case HEATSINK:
                    state.setDestroyedHeatSinks(state.getDestroyedHeatSinks()-1);
                    this.setStatus(true);
                    break;
                case COCKPIT:
                case LIFESUPPORT:
                    setStatus(true);
                    break;
            }
        }
        setCritSlots(getCritSlots()+1);//reduce the number of critable locations by one
    }
    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
//        super.applyCrit(unit);
        if (state!=null){
            switch (getType()) {
                case ENGINE:
                    state.setEngine(state.getEngine() + 1);
                    break;
                case GYRO:
                    state.setGyro(state.getGyro() + 1);
                    unit.getTurn().addCheck(new PilotCheck(unit, PilotCheck.PilotCheckType.GYRO, parent));
                    break;
                case ACTUATOR:
                    // TW crits handled in overriding class
                    state.setMotive(state.getMotive() + 1);
                    unit.getTurn().addCheck(new PilotCheck(unit, PilotCheck.PilotCheckType.ACTUATOR, parent));
                    break;
                case SENSORS:
                    state.setSensors(state.getSensors() + 1);
                    if (state.getSensors()>=2){
                        setStatus(false);
                    }
                    break;
                case HEATSINK:
                    if (this.isOperational())
                        state.setDestroyedHeatSinks(state.getDestroyedHeatSinks()+1);
                    this.setStatus(false);
                    break;
                case COCKPIT:
                case LIFESUPPORT:
                    setStatus(false);
                    break;
            }
        }
        setCritSlots(getCritSlots()-1);//reduce the number of critable locations by one
    }

    public void setState(OVState state) {
        this.state = state;
    }

    public void setEngine(OVHeader.EngineType engine) {
        this.engine = engine;
    }
    public int getCritTarget(){
        int ret = 0;
        switch (engine){
//            case LIGHT:
            case CLANXL:
                ret = 5;
                break;
                case CLANXXL:
            case ISXL:
                ret = 4;
                break;
            case ISXXL:
                ret = 3;
                break;
        }
        return ret;
    }
    protected int numberOfPips(){
        switch (getType()){
            case ENGINE:
                if (Game.current != null) {
                    return Game.current.isGameOV() ? 2 : 3;
                }
                return 2;
            case ACTUATOR: return 4;
            case GYRO: return 2;

            default: return 1;
        }
    }

    public IDamageRecord addCrit(TwoDSix check, IDamageRecord parent){
        String message = "Critical Hit";
        PilotCheck.PilotCheckType type = null;
        if (state!=null){
            switch (getType()) {
                case ENGINE:
                    state.setEngine(state.getEngine() + 1);
                    if (state.getEngine() == numberOfPips()) message = "CRITICAL HIT- Engine Destroyed";
                    else message = getLocation().name() + "- Engine Damaged";
                    break;
                case GYRO:
                    state.setGyro(state.getGyro() + 1);
                    if (state.getGyro() == numberOfPips()) message = "CRITICAL HIT- Gyro Destroyed";
                    else message = getLocation().name() + "- Gyro Damaged";
                    // add critical hit which will generate a pilotcheck
                    type = PilotCheck.PilotCheckType.GYRO;
                    break;
                case ACTUATOR:
                    state.setMotive(state.getMotive() + 1);
                    message = getLocation().name() + "- Actuator Damage";
                    break;
                case HEATSINK:
                    state.setDestroyedHeatSinks(state.getDestroyedHeatSinks() + 1);
                    setStatus(false);
                    message = getLocation().name() + "- Heat Sink Destroyed";
                    break;
                case JUMPJET:
                    state.setDestroyedJets(state.getDestroyedJets() + 1);
                    setStatus(false);
                    message = getLocation().name() + "- Jump Jet Destroyed";
                    break;
            }
        }
        CriticalHit crit = new CriticalHit(message, check, type, this, null);
        //we've already applied the crit before creating it
        crit.setApplied(true);
        setSent(false);
        return crit;
    }

    @Override
    public int getHealth() {
//        int health = numberOfPips();
//        switch (getType()){
//            case ENGINE:
//                health = numberOfPips();
//                break;
//            case EquipmentType.ACTUATOR:
//            health = 4; //try 4 actuator pips as you can get multiple actuator hits
//                break;
//        }
        return numberOfPips();
    }

    @Override
    public int getDamage() {
        int damage = 0;
        if (state!=null){
            switch (getType()) {
                case ENGINE:
                    damage = state.getEngine();
                    break;
                case GYRO:
                    damage = state.getGyro();
                    break;
                case ACTUATOR:
                    damage = state.getMotive();
                    break;
                default:
                    if(!isOperational())damage++;
            }
        }
        return damage;
    }

    public void setDamage(int dam) {
        if (dam < 0) dam = 0;
        switch (getType()) {
            case ENGINE:
                state.setEngine(Math.min(dam, 2));
                break;
            case GYRO:
                state.setGyro(Math.min(dam, 2));
                break;
            case ACTUATOR:
                state.setMotive(Math.min(dam, 4));
                break;
            default:
                // by default has one pip which is either operational or not
                setStatus(dam==0);
        }
    }

    public String getStreamValue() {
        String stream = super.getStreamValue();
        if (engine!=null)
            stream += "," + engine.toString();
        else
            stream += ",NULL";
        return stream;
    }
//
//    public int getCrits() {
//        int ret = 0;
//        if (state != null) {
//            if (engine != null)
//                ret = state.getEngine();
//            else {
//                switch (getType()) {
//                    case GYRO:
//                        ret = state.getGyro();
//                        break;
//                    case ACTUATOR:
//                        ret = state.getMotive();
//                        break;
//                }
//            }
//        }
//        return ret;
//    }
//
//    public void setCrits(int crits) {
//        if (state != null) {
//            if (engine != null) {
//                state.setEngine(crits);
//                if (crits >= 2) setStatus(false);
//            } else {
//                switch (getType()) {
//                    case GYRO:
//                        state.setGyro(crits);
//                        if (crits >= 1) setStatus(false);
//                        break;
//                    case ACTUATOR:
//                        state.setMotive(crits);
//                        break;
//                }
//
//            }
//        }
//    }

}
