package com.total.overiden;

import android.database.Cursor;

public class ConsciousnessCheck extends GenericCheck {

    public ConsciousnessCheck( IUnitData unit, int toHitNumber, IChildLink parent) {
        super(unit,toHitNumber>0?toHitNumber:getToHit(unit.getState().getPilot().getInjuries()),parent);
    }
    public ConsciousnessCheck(IUnitData unit, Cursor cur) {
        super(unit, cur);
    }
    @Override
    public String getDescription() {

        return "Consciousness Check";
    }
    @Override
    public void setSuccess(boolean passed) {
        super.setSuccess(passed);
        if (passed && !getUnit().getPilot().isConscious())getUnit().getPilot().setConscious(true);
        // no automatic fall, but auto fail for any subsequent pilot checks
        // pilot consciousness is now set in the end phase section to allow the current phase
        // activities to be completed
        // getUnit().getPilot().setConscious(passed);
    }

    private static int getToHit(int injuries) {
        int check = 0;
        switch (injuries) {
            case 5:
                check = 1;
            case 4:
                check += 3;
            case 3:
                check += 2;
            case 2:
                check += 2;
            case 1:
                check += 3;
            default:
        }
        return check;
    }
    public CheckType getCheckType(){
        return CheckType.CONSCIOUS;
    }
    public String getStreamValue(){

        return super.getStreamValue() + "\n"; // just required to put the end of line character in
    }
    public String calculateTargetNumberTooltip(){
        String tip = "Conciousness Check : " + getToHit(getUnit().getPilot().getInjuries()) + "\n";
        tip += "Pass Number for " + getUnit().getPilot().getInjuries() + " Injuries";
        return tip;
    }

    public String getTypeDescription(){
        return "Consciousness";
    }

    @Override
    public void reverseCrit(UnitTurn turn) {
        super.reverseCrit(turn);
        // if there is no parent then this check is in the end phase to see if a pilot wakes up
        if (getParent()!= null)
            if (!isPassed() && isComplete())getUnit().getPilot().setConscious(true);
        else
            if (isPassed() && isComplete())getUnit().getPilot().setConscious(false);
    }
}
