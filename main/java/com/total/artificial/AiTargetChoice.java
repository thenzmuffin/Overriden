package com.total.artificial;

import android.database.Cursor;

import androidx.annotation.Nullable;

import com.total.overide.OVDatabaseForce;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;

import java.util.ArrayList;
import java.util.List;

public class AiTargetChoice {
//    private int index = -1;
    private String designation;
    private final List<AiCommander.TargetType> types;
    private AiUnitAnalysis unit;
    public AiTargetChoice(String data){
        super();
        types = new ArrayList<>();
        String[] parts = data.split(",");
        if (parts.length==1)parts = data.split("-");
        designation = parts[0];
        for(int i = 1;i< parts.length;i++)
            types.add(new AiCommander.TargetType(parts[i]));
        unit = null;
    }
    public AiTargetChoice(Cursor cur){
        super();
        types = new ArrayList<>();
        designation = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_TARGET_DESIGNATION);
        String[] parts = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_TARGET_TYPE).split(",");
        for (String type : parts) {
            types.add(new AiCommander.TargetType(type));
        }
        unit = null;
    }
    public AiTargetChoice(String type, AiUnitAnalysis enemy){
        //used to generate a pilot level target
        super();
        types = new ArrayList<>();
        designation = type;
        types.add(new AiCommander.TargetType(type));
        unit = enemy;
    }

    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String desig){
        designation = desig;
    }
    public String toTargetTypeString(){
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (AiCommander.TargetType type : types){
            if (!first) out.append(",");
            out.append(type.getStorage());
            first = false;
        }
        return out.toString();
    }

    public List<AiCommander.TargetType> getTargetList(){
        return types;
    }
    public void findTarget(){
        List<IUnitData> targets = Game.current.getForce(0).getAllUnits();
        for (AiCommander.TargetType type : types){
            targets = findTarget(type,targets);
            if (targets.size()<=1)break;
        }
        if (!targets.isEmpty()) unit = (AiUnitAnalysis) targets.get(0);
    }
    public static List<IUnitData> findTarget(AiCommander.TargetType type,
                                                  List<IUnitData> analysis){
        List<IUnitData> targets = new ArrayList<>();
        float value = -1;
        for (IUnitData unit : analysis) {
//            AiUnitAnalysis unit;
            float newValue = ((AiUnitAnalysis)unit).getValue(type);
            if (value<0) {
                targets.add(unit);
                value = newValue;
            }else {
                if (type.getEnum().isHigh()){
                    if (newValue==value)targets.add(unit);
                    else if (newValue>value){
                        targets.clear();
                        targets.add(unit);
                        value = newValue;
                    }
                } else {
                    if (newValue==value)targets.add(unit);
                    else if (newValue<value){
                        targets.clear();
                        targets.add(unit);
                        value = newValue;
                    }
                }
            }
        }
        return targets;
    }
    public static List<IUnitData> findMatch(AiCommander.TargetType type,
                                            List<IUnitData> analysis){
        /* this method is designed for use with commander cards to determine the ally
         * which fulfils a specified criterion, even if the rules aren't sufficient to
         * specify a single ally it will always return no more than one unit */
        List<IUnitData> matches = new ArrayList<>();
        float value = -1;
        for ( IUnitData pilot: analysis) {
            float newValue = ((ArtificialPilot)pilot).getValue(type);
            if (value<0) {
                matches.add(pilot);
                value = newValue;
            }else {
                if (type.getEnum().isHigh()){
                    if (newValue==value)matches.add(pilot);
                    else if (newValue>value){
                        matches.clear();
                        matches.add(pilot);
                        value = newValue;
                    }
                } else {
                    if (newValue==value)matches.add(pilot);
                    else if (newValue<value){
                        matches.clear();
                        matches.add(pilot);
                        value = newValue;
                    }
                }
            }
        }
        return matches;
    }
    public ArtificialPilot findMatch(AiForceList analysis){
        List<IUnitData> matches = new ArrayList<>(analysis.getAllUnits());
        for (AiCommander.TargetType type : types){
            matches = findMatch(type,matches);
            if (matches.size()<=1)break;
        }
        if (!matches.isEmpty()) return (ArtificialPilot) matches.get(0);
        else return null;
    }

    public AiUnitAnalysis getUnit(){return unit;}
    public boolean typeMatch(AiCommander.TargetType type){
        if (type.getEnum()== AiEnums.Tag.DESIGNATION){
            return (designation.equals(type.getTag()));
        }
        return types.get(0)==type;
    }

    public void setUnit(AiUnitAnalysis enemy){unit = enemy;}
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof AiUnitAnalysis){
            return obj.equals(unit);
        } else
            return super.equals(obj);
    }
}
