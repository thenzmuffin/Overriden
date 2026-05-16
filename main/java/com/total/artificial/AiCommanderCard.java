package com.total.artificial;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.Turn;

import java.util.ArrayList;
import java.util.List;

public class AiCommanderCard {

    // controls designation of targets, including objective hunting
    private int parentKey; //commander key
    private int key; //card key
    private boolean resolved = false;
    private final List<AiCommanderInstructions> commanderInstructions = new ArrayList<>();
    private final List<AiTargetChoice> targetAnalysis = new ArrayList<>();
    private final List<AiInstruction> preMoves = new ArrayList<>();
    private final List<AiCommanderChangeRule> changeUp = new ArrayList<>();
    public AiCommanderCard(String data, int parent){
        super();
        parentKey = parent;
        key = Integer.parseInt(data);
    }
    public AiCommanderCard(Cursor cur){
        super();
        parentKey = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);
        key = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);

    }

    public void addTargetRule(String data){
            targetAnalysis.add(new AiTargetChoice(data));
    }

    public void generateTargets(){
        // how to define rules for a target list?
        for (AiTargetChoice comm : targetAnalysis){
            comm.findTarget();
        }
    }

    public AiUnitAnalysis getDesignatedTarget(AiCommander.TargetType type){
        for (AiTargetChoice comm : targetAnalysis){
            if (comm.typeMatch(type)){
                return comm.getUnit();
            }
        }
        List<IUnitData> list = AiTargetChoice.findTarget(type,Game.current.getForce(0).getAllUnits());
        return (!list.isEmpty())?(AiUnitAnalysis) list.get(0):null;
    }

    public void startTurn(){
        // the commander might have some orders that need to be applied at the start of a new turn
        for (AiCommanderInstructions instruct : commanderInstructions){
            instruct.applyInstruction(Turn.Phase.INITIATIVE);
        }
    }

//    public int endTurn(List<IUnitData> myDead, List<IUnitData> theirDead){
//        generateTargets();
//        return key;
//    }
    public int getParentKey() {
    return parentKey;
}
    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }


    public List<AiTargetChoice> getTargetAnalysis() {
        return targetAnalysis;
    }

    public boolean isPhaseComplete(Turn.Phase phase){
//        if (phase== Turn.Phase.RESOLVE){
//            return resolved;
//        }
        return true;
    }

    public List<AiInstruction> getPreMoves(){
        return preMoves;
    }

    public List<AiCommanderInstructions> getCommanderInstructions() {
        return commanderInstructions;
    }
    public int checkRules(){
        resolved = true;
        // check each rule in turn to see if it needs a check
        for (AiCommanderChangeRule rule : changeUp) {
            if (!rule.isChecked()) {
                if (rule.checkChangeCard()){
                    return rule.getNewCard();
                }
                if (!rule.isChecked()){
                    resolved = false; // reset to false as couldn't resolve this step
                    break;
                }
            }
        }
        return key;
    }
    public void setResolved(boolean status){
        resolved = status;
    }

    public boolean isResolved() {
        return resolved;
    }

    public List<AiCommanderChangeRule> getChangeUp() {
        return changeUp;
    }

    public void setParentKey(int parentKey) {
        this.parentKey = parentKey;
    }
}
