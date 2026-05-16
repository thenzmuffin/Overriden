package com.total.overiden;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericCheck implements IAutoCheck{
    private static int nextKey = 1;
    private IChildLink parent;
    private final List<IChildLink> children = new ArrayList<>();

    @Override
    public void setParent(IChildLink parent) {
        this.parent = parent;
        if (parent!=null)parent.getChildren().add(this);
    }

    @Override
    public IChildLink getParent() {
        return parent;
    }

    @Override
    public List<IChildLink> getChildren() {
        return children;
    }

    public enum CheckType{
        CONSCIOUS,
        PILOT,
        HEAT;
    }
    private int key;
    private int toHit;
    private TwoDSix rolledNumber = null;
    private boolean passed = false;
    private boolean complete = false;
    private final IUnitData unit;
    private boolean sent = false; // if playing via bluetooth has it been sent yet?

    public static GenericCheck newInstance(Cursor cur, IUnitData unit){
        GenericCheck ret = null;

        switch (CheckType.valueOf(DatabaseGame.getCursorString(cur,DatabaseGame.COLUMN_CHECK_TYPE))){
            case CONSCIOUS:
                ret = new ConsciousnessCheck(unit, cur);
                break;
            case HEAT:
                ret = new HeatCheck(unit, cur);
                break;
            case PILOT:
                ret = new PilotCheck(unit, cur);
        }
        ret.markAsSent(); // this is called when generating from the DB so it has already been sent
        return ret;
    }
    public static GenericCheck newInstance(String[] parts, IUnitData unit, IChildLink parent){
        GenericCheck ret = null;

        switch (CheckType.valueOf(parts[5])){
            case CONSCIOUS:
                ret = new ConsciousnessCheck(unit, Integer.parseInt(parts[1]), parent);
                break;
            case HEAT:
                ret = new HeatCheck(unit, HeatCheck.HeatCheckType.valueOf(parts[6]),Integer.parseInt(parts[1]));
                break;
            case PILOT:
                ret = new PilotCheck(unit, PilotCheck.PilotCheckType.valueOf(parts[6]), parent);
                ((PilotCheck)ret).setLevels(Integer.parseInt(parts[7]));
                break;
        }
        ret.key = Integer.parseInt(parts[0]);
        if (ret.key>=nextKey)nextKey = ret.key + 1; // update our counter
        if (parts[2].length() > 2) ret.rolledNumber = new TwoDSix(parts[2].replace("|",","));
        ret.passed = Boolean.parseBoolean(parts[3]);
        ret.complete = Boolean.parseBoolean(parts[4]);
        ret.markAsSent(); // this is called when generating from the DB so it has already been sent
        return ret;
    }

    public GenericCheck(IUnitData unit, int passOn, IChildLink parent){
        super();
        key = -1;
        this.unit = unit;
        this.toHit = passOn;
        setParent(parent);
    }
    public GenericCheck(IUnitData unit, Cursor cur){
        super();
        this.unit = unit;
        key = DatabaseGame.getCursorInt(cur, DatabaseGame.COLUMN_ID);
        if (key >= nextKey)nextKey = key + 1;
        this.toHit = DatabaseGame.getCursorInt(cur, DatabaseGame.COLUMN_CHECK_TOHIT);
        String dice = DatabaseGame.getCursorString(cur, DatabaseGame.COLUMN_CHECK_ROLLED);
        if (dice != null && dice.length() > 2) {
            this.rolledNumber = new TwoDSix(dice);
        }
        this.passed = DatabaseGame.getCursorInt(cur, DatabaseGame.COLUMN_CHECK_PASSED)==1;
        this.complete = DatabaseGame.getCursorInt(cur, DatabaseGame.COLUMN_CHECK_COMPLETE)==1;
    }
    @Override
    public String getDescription() {
        return "";
    }
    public CheckType getCheckType(){
        return null;
    }
    @Override
    public void setSuccess(boolean passed) {
        complete = true;
        this.passed = passed;
        sent = false; // mark as not sent so the update goes
    }

    @Override
    public int getToHit() {
        return toHit;
    }
    public void setToHit(int mod){
        if (!complete) {
            toHit = mod;
            sent = false;
        }
    }

    public long getKey() {
        return key;
    }

    public int generateKey(){
        if (key < 0)
            key = nextKey++;
        return (int)getKey();
    }

    @Override
    public TargetWeapon.ShotStatus getStatus() {
        TargetWeapon.ShotStatus ret;
        if (!complete) {
            ret = TargetWeapon.ShotStatus.NOTFIRED;
        } else {
            ret = passed?TargetWeapon.ShotStatus.HIT:TargetWeapon.ShotStatus.MISS;
        }
        return ret;
    }

    @Override
    public void setRoll(TwoDSix rolled) {
        this.rolledNumber = rolled;
        setSuccess(rolledNumber.getTotal()>=toHit);
    }

    @Override
    public TwoDSix getRoll() {
        return rolledNumber;
    }
    public IUnitData getUnit(){return unit;}

    public ContentValues setContents( ){
        ContentValues cv = new ContentValues();
        cv.put(DatabaseGame.COLUMN_ID, generateKey()); // generateKey method will get the existing key or generate one if required
        cv.put(DatabaseGame.COLUMN_CHECK_TOHIT, toHit);
        if (rolledNumber != null)
            cv.put(DatabaseGame.COLUMN_CHECK_ROLLED, rolledNumber.toString());
        cv.put(DatabaseGame.COLUMN_CHECK_PASSED, passed ? 1 : 0);
        cv.put(DatabaseGame.COLUMN_CHECK_COMPLETE, complete ? 1 : 0);
        cv.put(DatabaseGame.COLUMN_CHECK_TYPE, getCheckType().toString());
        cv.put(DatabaseGame.COLUMN_CHECK_SPECIAL, getSpecial());
        return cv;
    }
    public String getSpecial(){
        return "";
    }
    public String calculateTargetNumberTooltip(){
        return "";
    }
    public String getStreamValue(){
        String stream = "GENERICCHECK:" + key + "," + toHit + ",";
        String dice= "";
        if (rolledNumber!=null)
            dice = rolledNumber.toString().replace(",","|");
        stream += dice + "," + passed + "," + complete + ","; // don't need the unit
        stream += getCheckType().toString();
        return stream;
    }
    public boolean alreadySent(){
        return sent;
    }
    public void markAsSent(){sent = true;}
    public String getTypeDescription(){
        return "Generic";
    }
    public void updateFromString(String[] parts){
        if (key!= Integer.parseInt(parts[0])) return; // throw an error too?
        if (parts[2].length()>2)rolledNumber = new TwoDSix(parts[2].replace("|",","));
        passed = Boolean.parseBoolean(parts[3]);
        complete = Boolean.parseBoolean(parts[4]);
        // check type shouldn't change once created
    }
    @Override
    public ChildType getRecordType() {
        return ChildType.CHECK;
    }

    public void reverseCrit(UnitTurn turn) {
        // When it is a child component of a damage tree a completed check cannot be reversed
        // but here we are relying on this check having been completed already
        // reverse all children
        for (IChildLink child : children){
            child.reverseCrit(turn);
        }
        children.clear();
        turn.getTurnChecks().remove(this);
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean isComplete() {
        return complete;
    }
}
