package com.total.overiden;

import android.database.Cursor;

public class HeatCheck extends GenericCheck{
    public enum HeatCheckType {
        SHUTDOWN("Avoid Shutdown"),
        AMMO("Avoid Ammo Explosion");
        private final String description;
        HeatCheckType(String desc){
            description = desc;
        }

    }
    private final HeatCheckType type;
//    private int toHit;
//    private TwoDSix rolledNumber = null;
//    private boolean passed = false;
//    private boolean complete = false;
//    private IUnitData unit;
    public HeatCheck(IUnitData unit, HeatCheckType type, int passOn) {
        super(unit, passOn, null);
        this.type = type;

    }

    public HeatCheck(IUnitData unit, Cursor cur) {
        super(unit, cur);
        type = HeatCheckType.valueOf(DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_CHECK_SPECIAL));
    }
    @Override
    public String getDescription() {
        return type.description;
    }

    @Override
    public void setSuccess(boolean passed) {
        super.setSuccess(passed);

        // set the unit status as shutdown
        switch (type) {
            case SHUTDOWN:
                getUnit().getState().setShutdown(!passed);
                break;
            case AMMO:
                if (!passed) {
                    //TODO: determine the ammo which exploded
                    int location = 0;
                    //TODO: should be the ammo which causes most damage in one round, regardless of how many rounds are left
                    int damage = 100;
                    //TODO: explode the ammo
//                    (new AmmoCrit(damage)).actionCrit(unit, location);
                }

                break;
        }
    }
    public String getSpecial(){
        return type.toString();
    }
    public CheckType getCheckType(){
        return CheckType.HEAT;
    }
    public String getStreamValue(){

        return super.getStreamValue() + "," + type.toString() + "\n";
    }
    public String getTypeDescription(){
        return type.description;
    }
    public String calculateTargetNumberTooltip(){
        return type.description + " on " + getToHit() + " for heat level " +
                getUnit().getState().getHeat();
    }
    @Override
    public void reverseCrit(UnitTurn turn) {
        super.reverseCrit(turn);
        // heat checks only ever happen at the end of the turn and are never a child node
        if (type==HeatCheckType.SHUTDOWN && !isPassed() && isComplete()){
            getUnit().getState().setShutdown(false);
        }
    }
}
