package com.total.overiden;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.total.overide.OVEquipment;
import com.total.overide.OVSegment;
import com.total.overide.TWActuator;

public class PilotCheck extends GenericCheck {
    public enum PilotCheckType {
        RUN("Running with Crits"),
        JUMP("Jumping with Crits"),
        DAMAGE("Excessive Damage"),
        SHUTDOWN("Mech Shutdown"),
        GYRO("Gyro Damaged"),
        HIP("Hip Destroyed"),
        ACTUATOR("Leg Actuator Destroyed"),
        KICKED("Have been Kicked"),
        PUSHED("Have been Pushed"),
        CHARGED("Have been Charged"),
        DFAD("Have been hit by a DFA"),
        MISSEDKICK("Missed a Kick"),
        CHARGER("After Charging"),
        DFAATTEMPT("Successful DFA"),
        WATER1("Entering Depth 1 Water"),
        WATER2("Entering Depth 2 Water"),
        WATER3("Entering Depth 3 Water"),
        STAND("Standing Up"),
        RUBBLE("Entering Rubble"),
        SKID("Skidding"),
        FALL("Mech has Fallen");
        private final String description;
        PilotCheckType(String desc) {
            this.description = desc;
        }
        public String getDescription() {
            return description;
        }
        @NonNull
        @Override
        public String toString() {
            return description;
        }

    }

    private final PilotCheckType type;
    private int levels = 1; //number of levels fallen
//    private int passNumber;
//    private TwoDSix rolledNumber;
//    private boolean passedCheck = false;
//    private boolean complete = false;
//    private IUnitData unit;

    public PilotCheck(IUnitData unit, PilotCheckType type, IChildLink parent) {
        super(unit,unit.getPilot().getPilotSkill() + unit.getPilot().getPilotSkillMod() + getModifier(type, unit),parent);
        this.type = type;
        if (type!=PilotCheckType.FALL && unit.getState().isProne()){
            // The mech is already on the ground so the pilot check isn't required, default to
            // passed as failed causes fall damage.
            setSuccess(true);
        }
    }

    public PilotCheck(IUnitData unit, Cursor cur) {
        super(unit, cur);
        type = PilotCheck.PilotCheckType.valueOf(DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_CHECK_SPECIAL));
    }
    public String getTypeDescription() {
        return type.getDescription();
    }
    public String getDescription() {
        return "Pilot Check: " + getTypeDescription();
    }
    private static int getActuatorMods(IUnitData unit){
        int mod = 0;
        if(unit.getHeader().getType()== ForceList.ForceType.TW) {
            TWActuator actuator = (TWActuator) unit.getSegment(OVSegment.OVLocation.RIGHTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
            mod = actuator.getPSRMods();
            actuator = (TWActuator) unit.getSegment(OVSegment.OVLocation.LEFTLEG).getEquipmentType(OVEquipment.EquipmentType.ACTUATOR);
            mod += actuator.getPSRMods();

            //check Gyro as well
            if (unit.getState().getGyro() == 1) mod += 3;
            else if (unit.getState().getGyro() == 2) mod += 12; //auto fail
        }
        return mod;
    }
    private static int getModifier(PilotCheckType type, IUnitData unit) {
        int mod = 0;
        if (unit.getHeader().getType()== ForceList.ForceType.TW) {
            switch (type) {
                case DAMAGE:
                case WATER3:
                    mod = 1;
                    break;
                case SHUTDOWN:
                    mod = 3;
                    break;
                case CHARGED:
                case CHARGER:
                    mod = 2;
                    break;
                case WATER1:
                    mod = -1;
                    break;
                case DFAATTEMPT:
                    mod = 4;
                    break;
            }
            mod += getActuatorMods(unit);
        } else {
            switch (type) {
                case DAMAGE:
                case GYRO:
                case ACTUATOR:
                    mod = 2;
                    break;
            }
        }
        return mod;
    }
    private static String getModifierToolTip(PilotCheckType type, IUnitData unit){
        // a bit messy, could be done cleaner
        int actMod = getActuatorMods(unit);
        int mod = getModifier(type,unit) - actMod;

        String tip = "Modifier :" + mod + "\n";
        tip += "Actuator Damage :" + actMod + "\n";

        return tip;
    }

    @Override
    public void reverseCrit(UnitTurn turn) {
        super.reverseCrit(turn);
        if (!isPassed()) {
            if (type == PilotCheckType.FALL)
                getUnit().getPilot().addInjury(-1);
            else {
                // what if the mech was already prone? passed flag should be true in that case
                getUnit().getState().setProne(false);
            }
        }
    }

    @Override
    public void setSuccess(boolean passed) {
        super.setSuccess(passed);
        if (!passed) {
            if (type == PilotCheckType.FALL) { // the fall check is to see if the pilot takes damage
//                unit.fall(0);
                getUnit().getPilot().addInjury(1);
                getUnit().getTurn().getTurnChecks().add(new ConsciousnessCheck(getUnit(),-1, this));
            } else {
                if (Game.current.isSoundEffects())MainActivity.currentActivity.playSound(MainActivity.Sounds.FALL);
                // pilot damage from the fall
                TargetData faller = new TargetData(null,getUnit());
                int fallDirection = (new TwoDSix(1, TwoDSix.RollType.LOCATION)).getTotal();
                TargetData.LocTable facing;
                if (Game.current.isGameOV()){
                    facing = (fallDirection!=4)?TargetData.LocTable.FRONT:TargetData.LocTable.REAR;
                } else {
                    switch (fallDirection) {
                        case 1:
                            facing = TargetData.LocTable.FRONT;
                            break;
                        case 2:
                        case 3:
                            facing = TargetData.LocTable.RIGHT;
                            break;
                        case 5:
                        case 6:
                            facing = TargetData.LocTable.LEFT;
                            break;
                        default:
                            facing = TargetData.LocTable.REAR;
                    }
                }
                faller.setFacing(facing);
                // add a message to inform the user which way the mech fell
                getUnit().getTurn().addDamage(new DamageMessage("Mech Falls in direction " + fallDirection + " : " + facing));
                DamageRecord dr = new DamageRecord(faller);
                getUnit().addFallDamage(dr,levels);
                dr.applyDamage(getUnit());

            }
        }

    }


    public PilotCheckType getType() {
        return type;
    }
    public String getSpecial(){
        return type.name();
    }
    public CheckType getCheckType(){
        return CheckType.PILOT;
    }

    public String getStreamValue(){

        return super.getStreamValue() + "," + type.name() + "," + levels + "\n";
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }
    public String calculateTargetNumberTooltip(){
        String tip = "Pilot Skill : " + getUnit().getPilot().getPilotSkill();
        if (getUnit().getPilot().getPilotSkillMod() > 0){
            tip += " + " + getUnit().getPilot().getPilotSkillMod();
        }
        tip += "\n";
        tip += getModifierToolTip(type,getUnit());
//        tip += type.description + " on " + getToHit() + "\n";
        return tip;
    }
}
