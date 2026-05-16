package com.total.artificial;

import android.database.Cursor;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.Game;
import com.total.overiden.Turn;

import java.util.ArrayList;
import java.util.List;

public class AiCommanderInstructions {
    // An instance of this class holds a single instruction held against a commander card.
    // These are typically applied at the beginning of a turn but can be used to adjust
    // the movement rules or target rules for the units under this command.
    private static final int MAX_PRIORITY = 10000;
//    public enum CommInst {
//        INIT_MOD, // apply a modifier to the initiative roll for the AI
//        MOVE_FIRST, // apply a priority modifier to ensure a chosen unit moves first
//        MOVE_LAST, // apply a priority modifier to ensure a chosen unit moves last
//        MOVE_INSTR; // priority move instructions (inserted before pilot card instructions)
//
//    }
    private final AiEnums.CommInst type;
    private final AiTargetChoice rules;
    private final int modifier;
    public AiCommanderInstructions(){
        type = AiEnums.CommInst.MOVE_LAST;
        rules = new AiTargetChoice("");
        modifier = 2;
    }
    public AiCommanderInstructions(String data){
        // we use : as the first separator because we are reusing the AiTargetChoice parser which uses ,
        String[] parts = data.split(",");
        type = AiEnums.CommInst.valueOf(parts[0]);
        modifier = Integer.parseInt(parts[1]);
        if (parts.length>2)
            rules = new AiTargetChoice(parts[2]);
        else rules = null;
    }
    public AiCommanderInstructions(Cursor cur){
        // we use : as the first separator because we are reusing the AiTargetChoice parser which uses ,

        type = AiEnums.CommInst.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_ORDERS_TYPE));
        modifier = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ORDERS_MOD);
        String ruleString = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_ORDERS_CHOICE);
        if (ruleString.length()>2)
            rules = new AiTargetChoice(ruleString);
        else rules = null;
    }
    public void applyInstruction(Turn.Phase phase){
        if (phase == Turn.Phase.INITIATIVE) {
            switch (type) {
                case MOVE_FIRST:
                    if (rules != null) {
                        ArtificialPilot pilot = rules.findMatch(Game.current.getAiForce());
                        if (pilot != null) pilot.setPriorityModifier(-MAX_PRIORITY);
                    }
                    break;
                case MOVE_LAST:
                    if (rules != null) {
                        ArtificialPilot pilot = rules.findMatch(Game.current.getAiForce());
                        if (pilot != null) pilot.setPriorityModifier(MAX_PRIORITY);
                    }
                    break;
                case INIT_MOD:
                    Game.current.getForce(1).setInitiativeModifier(modifier);
                    break;
            }

        }
    }
    public int getModifier(){
        return modifier;
    }
    public AiTargetChoice getChoice(){
        return rules;
    }
}
