package com.total.artificial;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AiTargetInstruction extends AiInstruction {
    /*
     * A target instruction enhances the normal instruction type with a set of rules
     * for refining target selection
     *
     */
    private class Bracket {
        private String display;
        private AiEnums.MovedStatus status;
        public Bracket(String data){
            super();
            // first 2 chars are 1 char status and a space, the rest is the descriptor
            switch (data.charAt(0)){
                case 'M':
                    status = AiEnums.MovedStatus.MOVED;
                    break;
                case 'N':
                    status = AiEnums.MovedStatus.NOT_MOVED;
                    break;
                case 'A':
                    status = AiEnums.MovedStatus.ALL;
                    break;
            }
            display = data.substring(2);
        }

        @NonNull
        @Override
        public String toString() {
            String out;
            switch (status){
                case MOVED:
                    out = "M";
                    break;
                case NOT_MOVED:
                    out = "N";
                    break;
                default:
                    out = "A";
                    break;
            }
            return out + " " + display;
        }
    }

//    private MovedStatus status;
    private List<Bracket> brackets;
    private int index = 0;
    private List<IUnitData> list;
    public AiTargetInstruction(String part){
        super(1);
        setQuestion("Select units within");
        brackets = new ArrayList<>();
        list = new ArrayList<>();

        setAutoRes("UNIT_LIST");
        String[] bracks = part.split(":");

        // this is an auto resolved instruction
        for (String brack : bracks) {
            brackets.add(new Bracket(brack));
        }
    }
    public AiTargetInstruction(String[] parts){
        super(parts);

        brackets = new ArrayList<>();
        list = new ArrayList<>();
        if (parts.length>5){
            String[] bracks = parts[5].split(":");

            // this is an auto resolved instruction
            for (String brack : bracks) {
                brackets.add(new Bracket(brack));
            }
        }

    }
//    public AiTargetInstruction(AiInstruction copy){
//        super(copy);
//    }
//    public AiTargetInstruction(int i){
//        super(i);
//    }
    public AiTargetInstruction(Cursor cur){
        super(cur);

        list = new ArrayList<>();
        brackets = new ArrayList<>();
        String[] bracks = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_MOVE_QUALIFIERS).split(":");

        // this is an auto resolved instruction
        for (String brack : bracks) {
            brackets.add(new Bracket(brack));
        }
    }

    @Override
    public String getQuestion() {
        String base = super.getQuestion();
        return base + " " + brackets.get(index).display;
    }
    @NonNull
    public AiEnums.MovedStatus getBracketStatus(){
        if (index<=brackets.size())
            return brackets.get(index).status;
        else
            return AiEnums.MovedStatus.ALL;
    }
    public boolean nextBracket(){
        // returns true if there is another bracket to check
        index++;
        return ((index) < brackets.size());
    }
    public void setSelectedList(List<IUnitData> selected){
        list = selected;
    }
    public List<IUnitData> getSelectedList(){
        return list;
    }

    public boolean autoResolve() {
        if (getSelected() < 0) {
            // determine if there are no options to select
            while (index<brackets.size()) {
                if (brackets.get(index).status== AiEnums.MovedStatus.ALL)break;
                boolean found = false;
                for (IUnitData unit : Game.current.getForce(0).getAllUnits()) {
                    switch (brackets.get(index).status) {
                        case MOVED:
                            if (unit.getTurn().getMoveData().isMoveLocked())
                                found = true;
                            break;
                        case NOT_MOVED:
                            if (!unit.getTurn().getMoveData().isMoveLocked())
                                found = true;
                            break;
                    }
                    if (found)break;
                }
                if (found)break;
                // if we reach here then no valid choices were found so go to the next bracket
                index++;
            }
            // we have run out of brackets without finding a possible match so auto-resolve
            if (index>=brackets.size())
                setSelected(getHolders().get(0).getNextInstruction()); // set the selected option to nothing found
        }
        return getSelected()>=0;
    }
    public void setContent(ContentValues cv){
        super.setContent(cv);
        StringBuilder quals = new StringBuilder();
        for (Bracket bracket : brackets) {
            if (quals.length()>1) quals.append(":");
            quals.append(bracket.toString());
        }
        cv.put(DatabaseAI.COLUMN_MOVE_QUALIFIERS, quals.toString());
    }
}
