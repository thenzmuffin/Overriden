package com.total.artificial;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVHeader;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.UnitMove;

import java.util.ArrayList;
import java.util.List;

public class AiInstruction {
    public static class MoveChoice {
        private int nextInstruction;
        private String label;
        private boolean deckLink = false;
        public MoveChoice(String label, int inst){
            super();
            this.label = label;
            this.nextInstruction = inst;
        }
        public MoveChoice(String[] parts){
            super();
//            String[] parts = data.split(",");
            this.label = parts[0];
            try {
                this.nextInstruction = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                deckLink = true;
                this.nextInstruction = -1;
            }
        }

        public int getNextInstruction() {
            return nextInstruction;
        }

        public void setLabel(String label){
            this.label = label;
        }
        public void setNextInstruction(int next){
            this.nextInstruction = next;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }

        public boolean isDeckLink() {
            return deckLink;
        }
    }
    private final int index; //specifies order of instructions
//    private InstructionType type;
    private UnitMove.MoveType move;
    private AiEnums.Tactic tactic = AiEnums.Tactic.NONE;
    private String question;
    private int selected = -1;//this is the id of the next instruction in the selected path
    private final List<MoveChoice> holders;
    private AiEnums.ResolutionAction autoRes = AiEnums.ResolutionAction.NONE;
    private List<AiCommander.TargetType> autoParam;
//    private String designation = "";
    private ArtificialPilot pilot = null;
    private int listItem = -1;
    private final List<AiCommander.TargetType> qualifiers;
    public static AiInstruction newInstance(String data){
        String[] parts = data.split(",");
        if (parts.length>4){
            // this is an auto resolved instruction
            AiEnums.ResolutionAction autoRes = AiEnums.ResolutionAction.valueOf(parts[4]);
            if (autoRes == AiEnums.ResolutionAction.UNIT_LIST) return new AiTargetInstruction(parts);
        }
        return new AiInstruction(parts);
    }

    public AiInstruction(String[] parts){
        super();
        qualifiers = new ArrayList<>();
        index = Integer.parseInt(parts[0]);
        question = parts[1];
        parseOutput(parts[2]);
//        move = UnitMove.MoveType.valueOf(parts[2]);
        autoParam = new ArrayList<>();
        if (parts.length>3){
            getParamsFromString(parts[3]);
        }
        if (parts.length>4){
            // this is an auto resolved instruction
            autoRes = AiEnums.ResolutionAction.valueOf(parts[4]);

        }
        if (parts.length>5 && autoRes==AiEnums.ResolutionAction.TARGET_RULE){
            // read the list of qualifiers
            String[] quals = parts[5].split("-");
            for (String qual : quals)
                qualifiers.add(new AiCommander.TargetType(qual));
        }
        holders = new ArrayList<>();
    }
    public AiInstruction(AiInstruction copy){
        // we can use straight links to the originals as these values are not changing except for
        // selected, holders, pilot and listItem
        // this is used as to get commander orders set on individual pilots
        index = copy.index;
        question = copy.question;
        move = copy.move;
        tactic = copy.tactic;
        autoRes = copy.autoRes;
        autoParam = copy.autoParam;
//        for (AiCommander.TargetType targType : copy.autoParam) {
//            autoParam.add(targType);
//        }
        holders = copy.getHolders();
        qualifiers = copy.qualifiers;

    }
    public AiInstruction(int i){
        super();
        index = i;
        question = "Default";
        move = UnitMove.MoveType.WALK;
        holders = new ArrayList<>();
        qualifiers = new ArrayList<>();
    }
    public static AiInstruction newInstance(Cursor cur){
        AiEnums.ResolutionAction autoRes = AiEnums.ResolutionAction.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_AUTO_RULE));
        if (autoRes==AiEnums.ResolutionAction.UNIT_LIST){
            return new AiTargetInstruction(cur);
        } else {
            return new AiInstruction(cur);
        }
    }
    public AiInstruction(Cursor cur){
        super();
        index = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_MOVE_ID);
        question = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_DESC);
        parseOutput(OVDatabaseForce.getCursorString(cur, DatabaseAI.COLUMN_MOVE_TYPE));
        autoRes = AiEnums.ResolutionAction.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_AUTO_RULE));
        String temp = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_AUTO_PARAM);
        autoParam = new ArrayList<>();
        getParamsFromString(temp);
//        String designation = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_DESIGNATION);
//        if (designation!=null)autoParam.setTag(designation);
//        auto = autoRes.isAuto();
        holders = new ArrayList<>();
        qualifiers = new ArrayList<>();
        if (autoRes==AiEnums.ResolutionAction.TARGET_RULE){
            String[] quals = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_QUALIFIERS).split("-");
            for (String qual : quals)
                qualifiers.add(new AiCommander.TargetType(qual));
        }

    }
    private void getParamsFromString(String input){
        String[] params = input.split(":");
        for (String param : params) {
            autoParam.add(new AiCommander.TargetType(param));
        }
    }
    public void parseOutput(String data){
        try {
            move = UnitMove.MoveType.valueOf(data);
        } catch (IllegalArgumentException e) {
            move = UnitMove.MoveType.NONE;
            try{
                tactic = AiEnums.Tactic.valueOf(data);
            } catch (IllegalArgumentException ex) {
                tactic = AiEnums.Tactic.NONE;
            }
        }
    }
    public String getDesignation(){
        AiCommander.TargetType param = null;
        for (AiCommander.TargetType temp : autoParam){
            if (temp.getEnum() == AiEnums.Tag.DESIGNATION) {
                param = temp;
                break;
            }
        }
        return param==null?"UNKNOWN":param.getTag();
    }
    public String parseInput(){
        // Generate a string representing the instruction data
        String data;
        if (move!= UnitMove.MoveType.NONE)data = move.name();
        else data = tactic.name();
        return data;
    }

    public int getIndex() {
        return index;
    }

    public void addResult(String label, int choice){
        holders.add(new MoveChoice(label, choice));
    }
    public MoveChoice addResult(String[] data){
        MoveChoice choice = new MoveChoice(data);
        holders.add(choice);
        return choice;
    }

    public UnitMove.MoveType getMove() {
        return move;
    }
    public void setQuestion(String question){
        this.question = question;
    }
    public String getQuestion() {
        String formatted = question;
        if (pilot!=null && formatted.contains("<TAG>")){
            AiUnitAnalysis unit = null;
            for (AiCommander.TargetType param : autoParam) {
                String code;
                switch (param.getEnum()) {
                    case MIN_RANGE:
                        code = pilot.getRange("MIN");
                        break;
                    case BEST_RANGE:
                        code = pilot.getRange("BEST");
                        break;
                    case MAX_RANGE:
                        code = pilot.getRange("MAX");
                        break;
                    case SPOTTER:
                        if (pilot.getLinkedAlly() == null) {
                            // find a spotter if there is one
                            pilot.setLinkedAlly(Game.current.getAiForce().getDeckRole(OVHeader.UnitRole.SPOTTER));
                        }
                        if (pilot.getLinkedAlly() != null) {
                            code = pilot.getLinkedAlly().toString();
                        } else code = "No Spotter";
                        break;
                    case ENEMY_SPRINT:
                        if (unit!=null){
                            code = unit.getAdjustedMovement(UnitMove.MoveType.RUN) + "\"";
                        } else {
                            code = "???";
                        }
                        break;
                    case ENEMY_RANGE:
                        if (unit!=null){
                            code = unit.getOptimumRangeToken();
                        } else {
                            code = "???";
                        }
                        break;
                    default:
                        unit = pilot.getDesignatedTarget(param);
                        if (unit != null)
                            code = unit.toString();
                        else code = "Unknown";
                }
                formatted = formatted.replaceFirst("<TAG>", code);
            }
        }
        return formatted;
    }
//    private IUnitData getUnitFromTag(AiCommander.TargetType tag, String desig){
//        return pilot.getDesignatedTarget(tag,desig).getUnit();
//    }

    public List<MoveChoice> getHolders() {
        return holders;
    }

    public int getSelected() {
        return selected;
    }
    public MoveChoice getSelectedChoice() {
        if (isResolved()) {
            return holders.get(findSelected());
        } else return null;
    }
    public void setSelectedHolder(int holderIndex){
        if (holderIndex<holders.size()){
            setSelected(holders.get(holderIndex).nextInstruction);
        }
    }

    public void setSelected(int next) {
        selected = next;

        if (autoRes == AiEnums.ResolutionAction.HAS_LOS) {
            // for has LOS question the first answer must always be yes
            boolean hasLos = (findSelected() == 0);
            for (AiCommander.TargetType param : autoParam)
                pilot.updateTargetLos(pilot.getDesignatedTarget(param), hasLos);
        }
    }
    private int findSelected(){
        int ret = -1;
        for (int i = 0; i < holders.size(); i++) {
            if (holders.get(i).nextInstruction == selected) {
                ret = i;
                break;
            }
        }
        return ret;
    }
    public void setContent(ContentValues cv){
        cv.put(DatabaseAI.COLUMN_MOVE_ID, index);
        cv.put(DatabaseAI.COLUMN_MOVE_TYPE, parseInput());
        cv.put(DatabaseAI.COLUMN_MOVE_DESC, question);
        cv.put(DatabaseAI.COLUMN_MOVE_AUTO_RULE, autoRes.name());
        cv.put(DatabaseAI.COLUMN_MOVE_AUTO_PARAM, generateParameterSaveString());
        if (autoRes==AiEnums.ResolutionAction.TARGET_RULE) {
            StringBuilder quals = new StringBuilder();
            for (AiCommander.TargetType qual : qualifiers) {
                if (quals.length()>1) quals.append("-");
                quals.append(qual.getTag());
            }
            cv.put(DatabaseAI.COLUMN_MOVE_QUALIFIERS, quals.toString());
        }
//        cv.put(DatabaseAI.COLUMN_MOVE_DESIGNATION, autoParam.getTag());
    }
    private String generateParameterSaveString(){
        StringBuilder temp = new StringBuilder();
        for (AiCommander.TargetType param : autoParam){
            if (temp.length()>1) temp.append(":");
            temp.append(param.getStorage());
        }
        return temp.toString();
    }
    public boolean isAuto() {
        return autoRes.isAuto();
    }

    public boolean autoResolve() {
        if (selected<0) {
            switch (autoRes) {
                case TARGET_RULE:
                    // a target rule refines the set of units provided by the UNIT_LIST type
                    if (pilot != null) {
                        List<IUnitData> list = pilot.getTargetList();
                        int i = 0;
                        while (list.size() > 1 && i < qualifiers.size()) {
                            AiCommander.TargetType type = qualifiers.get(i);
                            // we have more than one item in the list so use the rule to try and refine it down to a single item
                            list = AiTargetChoice.findTarget(type, list);
                            i++;

                        }
                        if (!list.isEmpty()) {
                            pilot.addDesignatedTarget(autoParam.get(0).getTag(), (AiUnitAnalysis) list.get(0));
                        }
                    }
                    setSelected(holders.get(0).nextInstruction);
                    break;
                case UNIT_MOVED:
                    // find the unit that has been selected
                    boolean moved = true;
                    for (AiCommander.TargetType param : autoParam) {
                        AiUnitAnalysis target = pilot.getDesignatedTarget(param);
                        if (target != null) {
                            if (moved) moved = target.getTurn().getMoveData().isMoveLocked();
                        }
                    }
                    setSelected(holders.get(moved ? 0 : 1).nextInstruction);
                    break;
                case UNIT_DESTROYED:
                    // This is a check against how many units have been destroyed in the friendly force
                    // output will be 0 - more than 75% destroyed
                    //                1 - more than 50% destroyed
                    //                2 - more than 25% destroyed
                    //                3 - at least 1 unit destroyed
                    //                4 - no units destroyed
                    int totalUnits = Game.current.getForce(1).getCount();
                    int remainingUnits = Game.current.getForce(1).getActiveCount() * 4;
                    int fraction = Math.floorDiv(remainingUnits, totalUnits);
                    setSelected(holders.get(fraction).nextInstruction);
                    break;
                case HAS_LINK:
                    // Is a spotter/IF unit linked?
                    setSelected(holders.get(pilot.getLinkedAlly() != null ? 0 : 1).nextInstruction);
                    break;
                case WAS_SHOT:
                    // check for shot previous turn
                    setSelected(holders.get(pilot.getWhoShotMe().isEmpty() ? 1 : 0).nextInstruction);
                    break;
                case ANALYSIS:
                    // Trying out creating a generic case - currently works for HAS_CORE_ARMOUR
                    setSelected(holders.get(pilot.getValue(autoParam.get(0)) > 0 ? 1 : 0).nextInstruction);
                    break;
            }
        }
        return selected>=0;
    }
    public void setPilot(ArtificialPilot pilot){
        this.pilot = pilot;
    }

    public AiEnums.Tactic getTactic() {
        return tactic;
    }

    public List<AiCommander.TargetType> getParams(){

        return autoParam;
    }
    public AiEnums.ResolutionAction getResAction(){return autoRes;}
//    public void setRange(int range){
//        AiUnitAnalysis anal = pilot.getDesignatedTarget(autoParam,designation);
//        pilot.updateRangeForTarget(autoParam,designation,range);
//    }
    public boolean isResolved(){return selected>=0;}
    public int getListItem(){
        return listItem;
    }
    public void setListItem(int item){listItem = item;}
    public void setAutoRes(String res){
        autoRes = AiEnums.ResolutionAction.valueOf(res);
    }
    public void endTurn(){
        selected = -1;
    }
}
