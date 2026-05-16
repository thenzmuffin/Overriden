package com.total.overiden;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BSPStrikeTemplate {
    public enum BSPStrikeType {
        ARTILLERY("Artillery Support",false,0,R.drawable.grey_frame),
        MINEFIELD("Minefield Support", true,1,R.drawable.brown_frame),
        BOMBING("Offensive Aerospace Support",false, 0,R.drawable.red_frame),
        AIRSTRIKE("Offensive Aerospace Support",false,1,R.drawable.red_frame),
        STRAFING("Offensive Aerospace Support",false,2,R.drawable.red_frame),
        BLUFF("Offensive Aerospace Support",false,-1,R.drawable.red_frame),
        AIRCOVER("Defensive Aerospace Support", false,1,R.drawable.green_frame);
        private final String description;
        private final boolean movePhase; // true - target in move phase, false target in targeting phase
        private final int noOfTargets; //0 - area effect, 1 - single target, 2 - multiple targets, -1 no targets
        private final int colour;
        BSPStrikeType(String desc, boolean move, int no, int col){
            movePhase = move;
            noOfTargets = no;
            description = desc;
            colour = col;
        }
        public boolean isMovePhase(){return movePhase;}
        public int getColour(){return colour;}
        public int getNoOfTargets() {
            return noOfTargets;
        }

        @NonNull
        public String toString(){return description;}
    }
    private static class DamageGroup{
        int range;
        int groups;
        DamageGroup(int range, int groups){
            this.range = range;
            this.groups = groups;
        }
    }
    private final BSPStrikeType type;
    private final String name;
    private final int cost;
    private final int targetNumber;
    private final int groupingSize;
    private final List<DamageGroup> damage;
    private final String damageType;

    public BSPStrikeTemplate(String data){
        String[] parts = data.split(",");
        name = parts[0];
        cost = Integer.parseInt(parts[1]);
        targetNumber = Integer.parseInt(parts[2]);
        groupingSize = Integer.parseInt(parts[3]);
        damageType = parts[5];
        type = BSPStrikeType.valueOf(parts[6]);
        damage = new ArrayList<>();
        setDamageBrackets(parts[4]);
    }
    public BSPStrikeTemplate(Cursor cur){
        super();
        name = DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_BSPSTRIKE_NAME);
        cost = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_COST);
        targetNumber = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_TARGETNO);
        groupingSize = DatabaseGame.getCursorInt(cur, DatabaseGame.COLUMN_BSPSTRIKE_GRPSIZE);
        damageType = DatabaseGame.getCursorString(cur, DatabaseGame.COLUMN_BSPSTRIKE_DMGTYPE);
        type = BSPStrikeType.valueOf( DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_BSPSTRIKE_TYPE));
        damage = new ArrayList<>();
        setDamageBrackets(DatabaseGame.getCursorString(cur, DatabaseGame.COLUMN_BSPSTRIKE_GROUPS));
    }
    private void setDamageBrackets(String dmg){
        String[] parts = dmg.split("/");
        for (int i = 0;i<parts.length;i++){
            damage.add(new DamageGroup(i, Integer.parseInt(parts[i])));
        }
    }
    public BSPStrikeTemplate(BSPStrikeTemplate template){
        super();
        name = template.name;
        cost = template.cost;
        targetNumber = template.targetNumber;
        groupingSize = template.groupingSize;
        damageType = template.damageType;
        type = template.type;
        damage = new ArrayList<>();
        for (DamageGroup grp : template.damage){
            damage.add(new DamageGroup(grp.range, grp.groups));
        }
    }
    public String getName(){
        return name;
    }

    public BSPStrikeType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public int getGroupingSize(ForceList.ForceType type) {
        if (type== ForceList.ForceType.OV)
            return Math.floorDiv(groupingSize+2,3);
        return groupingSize;
    }

    protected TargetData.LocTable getLocationTable() {
        switch (type) {
            case MINEFIELD:
                return TargetData.LocTable.KICK;
            default:
                return TargetData.LocTable.FULL;
        }
    }
    protected boolean degrade(){
        // degrades damage done, returns true if the BSP is still available
        if (type == BSPStrikeType.MINEFIELD) {
            for (DamageGroup grp : damage) {
                if (grp.groups > 0) grp.groups -= 1;
            }
            return damage.get(0).groups>0;
        }
        return false;
    }
    public String getDamageType() {
        return damageType;
    }
    public String getDVG(){
        StringBuilder out = new StringBuilder();
        for (DamageGroup grp : damage){
            if (out.length()>0)out.append("/");
            out.append(grp.groups);
        }
        return out.toString();
    }
    public boolean phaseRelevant(Turn.Phase phase){
        boolean rel = false;
        switch(phase){
            case SETUP:
                rel = true;
                break;
            case MOVE:
                rel = type.isMovePhase();
                break;
            case TARGET:
                rel = !type.isMovePhase();
                break;
            case COUNTER:
                rel = type == BSPStrikeType.AIRCOVER;
                break;
        }
        return rel;
    }
    public int getDamageGroupings(int range){
        for (DamageGroup grp : damage){
            if (grp.range==range)return grp.groups;
        }
        return 0;
    }
    public List<OptionButton.OptionButtonChoice> getTargetOptions(){
        List<OptionButton.OptionButtonChoice> list = new ArrayList<>();
        Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
        gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.Green, null));
        list.add(new OptionButton.OptionButtonChoice("Not Targeted",gen, Integer.toString(-1)));
        switch (damageType){
            case "AE":
                // get the range
                for (DamageGroup grp : damage){
                    gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                    gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.Red, null));
                    list.add(new OptionButton.OptionButtonChoice( "Range:    " + grp.range,gen,Integer.toString(grp.range)));
                }
                break;
            default:
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.Red, null));
                list.add(new OptionButton.OptionButtonChoice( "Targeted",gen, Integer.toString(0)));
        }
        return list;
    }
    public boolean isSingleTarget(){
        return type==BSPStrikeType.MINEFIELD || type==BSPStrikeType.AIRSTRIKE || type==BSPStrikeType.BOMBING;
    }
    public void setContent(ContentValues cv){
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_COST,cost);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_NAME,name);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_DMGTYPE,damageType);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_GRPSIZE,groupingSize);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_TARGETNO,targetNumber);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_TYPE,type.name());
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_GROUPS,getDVG());
    }
    public void updateFromStream(String[] parts){
        // not sure anything is required here, these should come from the template,
        // however for minefields the damage groupings do get updated...
    }
    public String getBluetoothStream(){
        StringBuilder out = new StringBuilder();
        out.append(name).append(",").append(cost).append(",").append(targetNumber).append(",");
        out.append(groupingSize).append(",");
        boolean first = true;
        for (DamageGroup grp : damage){
            if (first){out.append("/");first = false;}
            out.append(grp.groups);
        }
        out.append(",").append(damageType).append(",").append(type.name());
        return out.toString();
    }
    public boolean rollThisPhase(Turn.Phase phase){
        boolean ret;
        switch (type){
            case MINEFIELD:
                ret = (phase == Turn.Phase.MOVE);
                break;
            case AIRCOVER:
                ret = (phase == Turn.Phase.TARGET);
                break;
            default:
                ret = (phase == Turn.Phase.SHOOT);
                break;
        }
        return ret;
    }

}
