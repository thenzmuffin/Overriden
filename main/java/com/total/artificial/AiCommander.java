package com.total.artificial;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.fragment.NavHostFragment;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.GamePlayFragment;
import com.total.overiden.IForceItem;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.Turn;

import java.util.ArrayList;
import java.util.List;

public class AiCommander {

    public static class TargetType{
        private String desigTag;
        private AiEnums.Tag tag;
        public TargetType(String param){
            super();
            try {
                tag = AiEnums.Tag.valueOf(param);
            } catch (IllegalArgumentException ex) {
                // if the value isn't one of the predefined values for the enumerated type then
                // use the designation setting
                tag = AiEnums.Tag.DESIGNATION;
                desigTag = param;
            }
        }

        public void setTag(String tag){
            desigTag = tag;
        }
        public String getTag(){
            return desigTag;
        }
        public String getStorage(){
            return tag==AiEnums.Tag.DESIGNATION?desigTag:tag.name();
        }
        public AiEnums.Tag getEnum(){return tag;}
        public void setEnum(AiEnums.Tag tag){this.tag = tag;}
    }

    // controls designation of targets, including objective hunting
    private final String name;
    private int key;
    private AiEnums.Tactic currentTactic;
    private boolean tacticDetermined = false;
    private final List<AiInstruction> tacticAnalysis;
    private final List<AiCommanderCard> cards;
    private AiCommanderCard currentCard = null;

    public AiCommander(String data){
        super();
        key = -1;
        name = data;
        currentTactic = AiEnums.Tactic.AGGRESSIVE;

        tacticAnalysis = new ArrayList<>();
        cards = new ArrayList<>();
    }
    public AiCommander(Cursor cur){
        super();
        key = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);
        name = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_COMMANDER_DESC);

        tacticAnalysis = new ArrayList<>();
        cards = new ArrayList<>();
    }

    public void generateTargets(){
        // how to define rules for a target list?
        currentCard.generateTargets();
    }
    public void addCard(AiCommanderCard card){
        cards.add(card);
        if (currentCard==null)currentCard = card;
    }
    public AiUnitAnalysis getDesignatedTarget(TargetType type){

        return currentCard.getDesignatedTarget(type);
    }

    public AiEnums.Tactic getCurrentTactic(){
        return currentTactic;
    }
    public void setCurrentTactic(AiEnums.Tactic tactic){
        currentTactic = tactic;
        tacticDetermined = true;
    }
    public void startTurn(){
        currentCard.startTurn();
//        // the commander might have some orders that need to be applied at the start of a new turn
//        for (AiCommanderInstructions instruct : commanderInstructions){
//            instruct.applyInstruction(Turn.Phase.INITIATIVE);
//        }
    }

    public void endTurn(List<IUnitData> myDead, List<IUnitData> theirDead){
        tacticDetermined = false;

        generateTargets();
        // clear the selected options from the tactic analysis
        for (AiInstruction inst : tacticAnalysis){
            inst.setSelected(-1);
        }
    }


    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
        for (AiCommanderCard card : cards){
            card.setParentKey(key);
        }
    }

    public List<AiInstruction> getTacticAnalysis() {
        return tacticAnalysis;
    }

    public List<AiTargetChoice> getTargetAnalysis() {
        return currentCard.getTargetAnalysis();
    }

    public String getName() {
        return name;
    }
    public boolean isPhaseComplete(Turn.Phase phase){
        if (phase== Turn.Phase.INITIATIVE){
            return tacticDetermined;
        }
        return currentCard.isPhaseComplete(phase);
    }
    public List<AiCommanderCard> getCards(){
        return cards;
    }
    public AiInstruction findInstructionById(int id){
        for (AiInstruction move : tacticAnalysis){
            if (move.getIndex()==id){
//                if (move.isAuto() && !move.isResolved()){
                move.autoResolve();
//                }
                return move;
            }
        }
        return null;
    }
    public void fillOnScreenInstructions(List<AiInstruction> onScreen){
        // we need to store some state for the instruction set currently in use so that it can be regenerated
        // this is for the tactic instruction set only
        int startingInstruction = 1;
        AiInstruction instruction = findInstructionById(startingInstruction);
        if (instruction!=null)onScreen.add(instruction);
        while ((instruction!=null)&&instruction.getSelected()>0){
            instruction = findInstructionById(instruction.getSelectedChoice().getNextInstruction());
            if (instruction!=null)onScreen.add(0,instruction);
        }
        if ((instruction!=null)&&instruction.getHolders().size()==1){
            instruction.setSelected(0);
            setCurrentTactic(instruction.getTactic());
            try(DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
                db.saveLiveCommander(((AiForceList)Game.current.getForce(1)));
            }
        }
    }
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public List<AiInstruction> getPreMoves(){
        return currentCard.getPreMoves();
    }

    public List<AiCommanderInstructions> getCommanderInstructions() {
        return currentCard.getCommanderInstructions();
    }

    public boolean checkRules(){
        // does the current card require a change in card for the next turn?
        // if so update the current card
        int nextCard = currentCard.checkRules();
        if (currentCard.isResolved()) {
//            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
//
//            mgr.setFragmentResultListener("commanderrule",MainActivity.currentActivity,new FragmentResultListener() {
//                @Override
//                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
//                    // has the card been selected? if not
//                    checkRules();
//                }
//            });
//        } else {
            processRuleResult(nextCard);
        } else {
            // if the card rules aren't resolved that means there is a manual dialog popped up???
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();

            mgr.setFragmentResultListener("commanderrule",MainActivity.currentActivity,new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    // the dialog has been completed, need to restart the check
                    if (checkRules()){
                        // rules have been fully resolved so complete the end phase
                        // there is probably a better way to do this?
                        for (Fragment frag : MainActivity.currentActivity.getSupportFragmentManager().getFragments()){
                            if (frag instanceof NavHostFragment){
                                for (Fragment subFrag : frag.getChildFragmentManager().getFragments()){
                                    if (subFrag instanceof GamePlayFragment){
                                        ((GamePlayFragment)subFrag).completeResolvePhase(null);
                                        Game.current.reorderUnits();
                                        ((GamePlayFragment)subFrag).displayPhase();
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }
        return currentCard.isResolved();
    }
    private void processRuleResult(int nextCard){
        // a new commander card might have been selected. Check if the card number is
        // different from the current card and switch if needed, set the new card to resolved
        // so that the new card isn't immediately checked. It will be reset in the end phase work
        if (nextCard!= currentCard.getKey()) {
            // update the currentCard
            for (AiCommanderCard card : cards) {
                if (card.getKey() == nextCard) {
                    currentCard = card;
                    currentCard.setResolved(true);
                    break;
                }
            }
        }
    }
}
