package com.total.overiden;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class BSPStrike extends BSPStrikeTemplate {
    public class StrikeTarget{
        public int range;
        public IUnitData unit;
        public TwoDSix dice = null;
        public TargetWeapon.ShotStatus status = TargetWeapon.ShotStatus.NOTFIRED;

        StrikeTarget(int range, IUnitData unit){
            this.unit = unit;
            this.range = range;

        }
    }
    private int key = -1;
    private int foreignKey = -1; // This is the key on another device, used when receiving an update over bluetooth
    private boolean available; // has it been used yet
    private boolean targeted; // true if targeted this turn
    private boolean landing; // flag for shots to be resolved this turn
    private String target; // Artillery can target a location on the map, it is described here
    private TwoDSix dice; // used for AE attacks only
    private TwoDSix scatter; // used for AE attacks only
    private final List<StrikeTarget> targetList;
//    public BSPStrike(String name, int targetNumber, int groups, int type){
//        super();
//        available = true;
//        target = "";
//        targeted = false;
//        targetList = new ArrayList<>();
//    }
    public BSPStrike(Cursor cur){
        super(cur);
        key = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_ID);
        available = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_AVAILABLE)==1;
        target = DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_BSPSTRIKE_TARGET);
        targeted = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_TARGETED)==1;
        foreignKey = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_FOREIGN);
        landing = DatabaseGame.getCursorInt(cur,DatabaseGame.COLUMN_BSPSTRIKE_LANDING)==1;
        dice = null; // TODO this should be saved
        scatter = null; // TODO this should be saved
        targetList = new ArrayList<>();
    }
    public BSPStrike(String data){
        super(data);
        String[] parts = data.split(",");
        key = Integer.parseInt(parts[7]);
        available = Boolean.parseBoolean(parts[8]);
        target = parts[9];
        targeted = Boolean.parseBoolean(parts[10]);
        foreignKey = Integer.parseInt(parts[11]);
        landing = Boolean.parseBoolean(parts[12]);
        targetList = new ArrayList<>();
        dice = null; // TODO this should be saved
        scatter = null; // TODO this should be saved
    }
    public BSPStrike(BSPStrikeTemplate template){
        super(template);
        available = true;
        target = "";
        targeted = false;
        landing = false;
        targetList = new ArrayList<>();
        dice = null; // TODO this should be saved
        scatter = null; // TODO this should be saved
    }
    public static int getKey(String[] parts){
        return Integer.parseInt(parts[7]);
    }
    @Override
    public boolean phaseRelevant(Turn.Phase phase){
        boolean rel = available;
        if (rel)rel = super.phaseRelevant(phase);
        switch (phase){
            case COUNTER: // phase for air cover resolution
                break;
            case SHOOT:
            case RESOLVE:
        // in the shooting phase we should display items with the landing flag set
                rel = landing;
                break;
        }
        // any card that has been targeted should be displayed in the shoot phase
        // in the turn resolution all cards that are no longer available should have the
        // targeted flag cleared for the next turn

        return rel;
    }

    public void resolveTurn(){
        boolean update = false;
        // artillery lands next turn from firing so sort out the landing flag
        if (targeted) {
            // targeted is a flag for this turn only and should be cleared in the resolution phase
            targeted = false;
            available = degrade(); //available = false;
            landing = !landing; // if it landed this turn clear it, if it didn't then it should next turn
            update = true;
        } else if (landing) {
            landing = false;
            update = true;
        }
        if (update) {
            try (DatabaseGame db = new DatabaseGame(MainActivity.currentActivity)) {
                // update the database
                db.addBspStrikeCard(this);
            }
        }
    }

    public boolean isTargeted() {
        return targeted;
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
        // everything except artillery lands the same turn it was targeted
        if (targeted && getType()!=BSPStrikeType.ARTILLERY)landing = true;

        if (targeted && getType().isMovePhase()){
            // BSPs used in the movement phase go off the moment they are triggered
            activate("AUTO");
        }
    }
    public void activate(String command){
        switch (command) {
            case "AUTO":
                for (StrikeTarget sTarg : targetList) {
                    // each attack is only resolved once
                    if (sTarg.status!= TargetWeapon.ShotStatus.NOTFIRED)continue;
                    sTarg.dice = new TwoDSix();
                    if (sTarg.dice.getTotal() < getTargetNumber()) {
                        sTarg.status =TargetWeapon.ShotStatus.MISS;
                    } else {
                        sTarg.status =TargetWeapon.ShotStatus.HIT;
                        // now resolve the damage
                        resolveDamage(sTarg);
                    }
                }
                break;
            case "HIT":
                for (StrikeTarget sTarg : targetList) {
                    sTarg.status = TargetWeapon.ShotStatus.HIT;
                    resolveDamage(sTarg);
                }
                break;
            case "MISS":
                for (StrikeTarget sTarg : targetList) {
                    sTarg.status = TargetWeapon.ShotStatus.MISS;
                }
                break;
        }
        try (DatabaseGame db = new DatabaseGame(MainActivity.currentActivity)){
            // strike has been used, make sure the usage is saved to the database
            db.addBspStrikeCard(this);
        }
    }

    private void resolveDamage(StrikeTarget sTarg){
        // the strike is successful, apply damage
        DamageRecord dr = new DamageRecord(new TargetData(null,sTarg.unit));
        dr.setWeapon(new BspWeapon(this));
        int groups = getDamageGroupings(sTarg.range);
        for (int i = 0;i<groups;i++)
            dr.addGrouping(getGroupingSize(sTarg.unit.getHeader().getType()),getLocationTable());
        sTarg.unit.getTurn().addDamage(dr);
        dr.applyDamage(sTarg.unit);
        UpdatePlayerActions.updateWeaponHit( sTarg.unit.getTurn());
        //Using the manual method to transmit damage updates to the other device
        UpdatePlayerActions.updateManualChanges(sTarg.unit);
    }
    public void addNewTarget(IUnitData unit, int range){
        targetList.add(0,new StrikeTarget(range,unit));
    }
    public void addNewTarget(IUnitData unit, int range, String dice, String status){
        StrikeTarget target = new StrikeTarget(range,unit);
        if (!dice.equals("NONE"))target.dice = new TwoDSix(dice);
        target.status = TargetWeapon.ShotStatus.valueOf(status);
        targetList.add(target);
    }
    public void updateTargets(IUnitData unit,int range){
        if (range<0){
            for (int i = 0;i<targetList.size();i++){
                if (targetList.get(i).status== TargetWeapon.ShotStatus.NOTFIRED &&
                        targetList.get(i).unit==unit){
                    targetList.remove(i);
                    break;
                }
            }

        }
        else {
            if (isSingleTarget()){
                targetList.clear();
                addNewTarget(unit,range);
            } else {
                boolean found = false;
                for (StrikeTarget sTarg : targetList) {
                    // if already triggered then skip it (Mainly for minefields)
                    if (sTarg.status != TargetWeapon.ShotStatus.NOTFIRED) continue;
                    if (sTarg.unit == unit) {
                        sTarg.range = range;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addNewTarget(unit, range);
                }
            }
        }
    }
    public int getUnitSelectedRange(IUnitData unit){
        int ret = -1; // value for not selected
        for (StrikeTarget sTarg : targetList) {
            // if already triggered then skip it (Mainly for minefields)
            if (sTarg.status!= TargetWeapon.ShotStatus.NOTFIRED)continue;
            if (sTarg.unit == unit) {
                ret = sTarg.range;
                break;
            }
        }
        return ret;
    }

    public List<StrikeTarget> getTargetList() {
        return targetList;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

//    public boolean isLanding() {
//        return landing;
//    }
//
//    public void setLanding(boolean landing) {
//        this.landing = landing;
//    }

    public void setContent(ContentValues cv){
        super.setContent(cv);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_AVAILABLE,available?1:0);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_TARGET,target);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_TARGETED,targeted?1:0);
        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_LANDING,landing?1:0);

        cv.put(DatabaseGame.COLUMN_BSPSTRIKE_FOREIGN,foreignKey);
    }
    public void updateFromStream(String[] parts){
        super.updateFromStream(parts);
        available = Boolean.parseBoolean(parts[8]);
        target = parts[9];
        targeted = Boolean.parseBoolean(parts[10]);
        foreignKey = Integer.parseInt(parts[11]);
        landing = Boolean.parseBoolean(parts[12]);
    }
    public String getBluetoothStream(){
        String out = "BSPSTRIKE:";
        out += super.getBluetoothStream();
        // reverse key and foreignkey when generating the stream as it is only ever used when sending to the other device
        // does mean the stream should be immediately returned when the other device generates the key
        out += "," + foreignKey + "," + available + "," + target + "," + targeted + "," + key + "," + landing + "\n";
        return out;
    }

    public int getForeignKey() {
        return foreignKey;
    }


    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public TwoDSix getDice() {
        return dice;
    }
    public String getScatterMessage(){
        String ret = "";
        if (dice != null && dice.getTotal() < getTargetNumber()){
            ret = "Scatter by ";
            int margin = (getTargetNumber() - dice.getTotal());
            if (Game.current.isHexless()){
                margin *= 2;
                ret += margin + " inches in direction ";
            } else {
                ret += margin + " hexes in direction ";
            }
            if (scatter!=null) ret += scatter.getTotal();
        }
        return ret;
    }
    @Override
    protected boolean degrade(){
        // Check to see if a mine didn't trigger, as opposed to all other types the mine will only degrade if it successfully hits.
        if (getType()==BSPStrikeType.MINEFIELD){
            if (!targetList.isEmpty() &&
                    targetList.get(0).status== TargetWeapon.ShotStatus.MISS)
                return true; //mines don't degrade if they miss
        }
        return super.degrade();
    }
    public void resolveAreaEffectAttack(){
        dice = new TwoDSix(2, TwoDSix.RollType.TARGET);
        if (dice.getTotal()<getTargetNumber()){
            scatter = new TwoDSix(1, TwoDSix.RollType.LOCATION);
        }
    }

    public void resolveCounter(BSPStrike target){
        int targetNumber;
        // get the target number
        switch (target.getType()){
            case BOMBING:
                if (target.getName().toLowerCase().contains("light")){
                    targetNumber = getDamageGroupings(1);
                } else { // heavy
                    targetNumber = getDamageGroupings(4);
                }
                break;
            case AIRSTRIKE:
                if (target.getName().toLowerCase().contains("light")){
                    targetNumber = getDamageGroupings(0);
                } else { // heavy
                    targetNumber = getDamageGroupings(2);
                }
                break;
            case STRAFING:
                targetNumber = getDamageGroupings(3);
                break;
            default:
                //invalid type to be countered
                return;
        }
        dice = new TwoDSix(2, TwoDSix.RollType.TARGET);
        if (dice.getTotal()>=targetNumber){
            // air cover was successful
            target.available = false;
            target.targeted = false;
        }
    }
}
