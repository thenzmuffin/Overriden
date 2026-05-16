package com.total.artificial;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVSegmentInst;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AiUnitCard {
    private static class TacticLink{
        public AiEnums.Tactic tactic;
        public int startAt;
        TacticLink(AiEnums.Tactic name, int link){
            tactic = name;
            startAt = link;
        }
        TacticLink(String data){
            String[] input = data.split(":");
            tactic = AiEnums.Tactic.valueOf(input[0]);
            startAt = Integer.parseInt(input[1]);
        }
        public String formatted(){
            return tactic.name() + ":" + startAt;
        }
    }
    private int id;
    private final int priority;
    private final int priorityMod;
    private final AiEnums.PriorityModType priorityModType;
//    private IUnitData unit = null;
    private final List<AiInstruction> mainMoves;
    private final List<String> rangeBracket = new ArrayList<>();
    private final List<TacticLink> tactics;
    private final List<AiCommander.TargetType> targetRules;
    private final List<AiEnums.WeaponPriority> weaponPriorityList;
    private ArtificialPilot pilot = null;
    private int defaultHeat = 0, critHeat = 0,killHeat = 0, nearDeath = 0;

    public AiUnitCard(String data){
        super();
        String[] parts = data.split(",");
        id = -1;
        priority = Integer.parseInt(parts[0]);
        mainMoves = new ArrayList<>();
        tactics = new ArrayList<>();
        priorityMod = Integer.parseInt(parts[1]);
        priorityModType = AiEnums.PriorityModType.valueOf(parts[2]);
        targetRules = new ArrayList<>();
        weaponPriorityList = new ArrayList<>();
    }
    public AiUnitCard(Cursor cur){
        super();
        mainMoves = new ArrayList<>();
        id = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);
        priority = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_PRIORITY);
        tactics = new ArrayList<>();
        targetRules = new ArrayList<>();
        weaponPriorityList = new ArrayList<>();
        priorityMod = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_PRIORITY_MOD);
        priorityModType = AiEnums.PriorityModType.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_CARD_PRIORITY_TYPE));
        nearDeath = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_HEAT_DEATH);
        defaultHeat = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_HEAT_DEF);
        critHeat = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_HEAT_CRIT);
        killHeat = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_CARD_HEAT_KILL);
        parseWeapons(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_CARD_WEAPON));
        setTargetRules(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_CARD_TARGETS));
        parseTactics(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_CARD_TACTICS));
        parseBrackets(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_CARD_BRACKETS));
    }

    public List<AiInstruction> getMainMoves() {
        return mainMoves;
    }

    @NonNull
    @Override
    public String toString() {
        return "Priority - " + priority;
    }

    public int getPriority() {
        int adjusted = priority;
        if (pilot != null) {
            switch (priorityModType) {
                case PREV_TARGET:
                    IUnitData last = pilot.getLastTarget();
                    if (last!=null&&!last.getTurn().getMoveData().isMoveLocked())
                        adjusted += priorityMod;
                    break;
                case SHOT_BY_ALL:
                case REAR_SHOT:
                    for (TargetData shoot : pilot.getWhoShotMe()) {
                        if (priorityModType==AiEnums.PriorityModType.REAR_SHOT &&
                                shoot.getFacing()!= TargetData.LocTable.REAR)continue;
                        if (!shoot.getShooter().getTurn().getMoveData().isMoveLocked()) {
                            adjusted += priorityMod;
                            break;
                        }
                    }
                    break;
            }
        }
        return adjusted;
    }
    public int getPriorityMod() {
        return priorityMod;
    }
    public String getPriorityModType() {
        return priorityModType.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public AiInstruction findInstructionById(int id){
        for (AiInstruction move : mainMoves){
            if (move.getIndex()==id){
//                if (move.isAuto() && !move.isResolved()){
                move.autoResolve();
//                }
                return move;
            }
        }
        return null;
    }
//    public List<AiUnitAnalysis> getTargetList(){
//        //  instructions to determine the selected target
//        return pilot.getCommander().getRankedTargets(targetRules);
//
//    }
    public void fillOnScreenInstructions(List<AiInstruction> onScreen){
        // we need to store some state for the instruction set currently in use so that it can be regenerated
        int startingInstruction = 0;
        for ( TacticLink link : tactics) {
            if (link.tactic == pilot.getCurrentTactic()){
                startingInstruction = link.startAt;
                break;
            }
        }
        AiInstruction instruction = findInstructionById(startingInstruction);

        instruction.setPilot(pilot);
        onScreen.add(0,instruction);
        while (instruction.autoResolve()){
//        while (instruction.isAuto() || instruction.getSelected() >= 0) {
//            if (instruction.getSelected() < 0) {
//                // this is an auto resolved and it isn't yet resolved then action it now.
//                instruction.autoResolve();
//            }
            instruction = findInstructionById(instruction.getSelectedChoice().getNextInstruction());
            if (instruction != null) {
                instruction.setPilot(pilot);
                onScreen.add(0,instruction);
            } else break;
        }

    }

    public void setPilot(ArtificialPilot pilot){
        this.pilot = pilot;
        for(AiInstruction inst : mainMoves){
            inst.setPilot(pilot);
        }
    }
    public void addTactic(AiEnums.Tactic tact, int link){

        tactics.add(new TacticLink(tact, link));
    }
    public void setTargetRules(String data){
        String[] parts = data.split(",");
        for (String part : parts){
            AiCommander.TargetType type;
            if (part.contains(":")){
                String[] desigParts = part.split(":");
                if (desigParts[0].equalsIgnoreCase("RANGE")){
                    // create a range bracket check
                    rangeBracket.addAll(Arrays.asList(desigParts[1].split("-")));
                    continue;
                } else {
                    type = new AiCommander.TargetType(desigParts[0]);
                    type.setTag(desigParts[1]);
                }
            } else {
                type = new AiCommander.TargetType(part);
            }
            targetRules.add(type);
        }
    }
    public void parseTactics(String data){
        String[] parts = data.split(",");
        for (String part : parts){
            TacticLink tactic;
            tactics.add(new TacticLink(part));
        }
    }
    public void parseBrackets(String data){
        rangeBracket.addAll(Arrays.asList(data.split("-")));
    }

    public List<AiCommander.TargetType> getTargetRules() {
        return targetRules;
    }

    public List<AiEnums.WeaponPriority> getWeaponPriorityList(){return weaponPriorityList;}
    public int getHeatThreshold(TargetWeapon weapon){
        // Determine how hot the unit is prepared to get when shooting
        int threshold = defaultHeat;
        // get the remaining health in the CT for the target (armour and structure)
        int remArmour = weapon.getTargetData().getTarget().getCoreSegment().getArmourTurnDmg();
        int remStructure = weapon.getTargetData().getTarget().getCoreSegment().getStructureTurnDmg();
        // does the weapon damage (cluster size) has enough wump to take out the armour and structure?
        if (weapon.getWeapon().getDamage()> (remArmour + remStructure))threshold = killHeat;
            // if no then armour + 1?
        else if (weapon.getWeapon().getDamage()> (remArmour +1))threshold = critHeat;

        // what about the shooter? Can 10 damage to the torso kill it?
        if (nearDeath > threshold){
            OVSegmentInst inst = weapon.getTargetData().getShooter().getCoreSegment();
            if (inst.getArmourTurnDmg() + inst.getStructureTurnDmg() < 11)threshold = nearDeath;
        }
        if (Game.current.isSmartHeat())threshold = Math.floorDiv(threshold,5);
        return threshold;
    }
    public void parseHeat(String data){
        String[] parts = data.split(",");
        if (parts.length>=4){
            defaultHeat = Integer.parseInt(parts[0]);
            critHeat = Integer.parseInt(parts[1]);
            killHeat = Integer.parseInt(parts[2]);
            nearDeath = Integer.parseInt(parts[3]);
        }
    }

    public void parseWeapons(String data){
        String[] parts = data.split(",");
        for (String part : parts){
            weaponPriorityList.add(AiEnums.WeaponPriority.valueOf(part));
        }
    }

    public int getDefaultHeat() {
        return defaultHeat;
    }

    public int getCritHeat() {
        return critHeat;
    }

    public int getKillHeat() {
        return killHeat;
    }

    public int getNearDeath() {
        return nearDeath;
    }

    public String getWeaponFormatted(){
        StringBuilder ret = new StringBuilder();
        for (AiEnums.WeaponPriority weap : weaponPriorityList){
            if (ret.length()>1) ret.append(",");
            ret.append(weap.name());
        }
        return ret.toString();
    }
    public String getTargetsFormatted(){
        StringBuilder ret = new StringBuilder();
        for (AiCommander.TargetType type : targetRules){
            if (ret.length()>1) ret.append(",");
            ret.append(type.getStorage());
        }
        return ret.toString();
    }
    public String getBracketsFormatted(){
        StringBuilder ret = new StringBuilder();
        for (String bracket : rangeBracket){
            if (ret.length()>1) ret.append("-");
            ret.append(bracket);
        }
        return ret.toString();
    }
    public String getTacticsFormatted(){
        StringBuilder ret = new StringBuilder();
        for (TacticLink tactic : tactics){
            if (ret.length()>1) ret.append(",");
            ret.append(tactic.formatted());
        }
        return ret.toString();
    }

    public int getTactic(AiEnums.Tactic tact){
        for (TacticLink link : tactics){
            if (link.tactic == tact){
                return link.startAt;
            }
        }
        return 0;
    }

    public void setDefaultHeat(int defaultHeat) {
        this.defaultHeat = defaultHeat;
    }

    public void setCritHeat(int critHeat) {
        this.critHeat = critHeat;
    }

    public void setKillHeat(int killHeat) {
        this.killHeat = killHeat;
    }

    public void setNearDeath(int nearDeath) {
        this.nearDeath = nearDeath;
    }

    public List<String> getRangeBracket(){
        return rangeBracket;
    }
    public void endTurn(){
        for (AiInstruction inst : mainMoves){
            inst.endTurn();
        }
    }
}
