package com.total.overide;

import android.content.ContentValues;

import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;

import java.util.Random;

public class TWActuator extends OVCoreEquipment{
    private boolean top = false; //shoulder or hip
    private boolean upper = false;
    private boolean lower = false;
    private boolean startedWithLower = false;
    private boolean bottom = false; //hand or foot
    private boolean startedWithBottom = false; //hand or foot


    public TWActuator(OVSegment.OVLocation location, int key, int special) {
        super(OVEquipment.EquipmentType.ACTUATOR, location, key);
        setSpecial(special);
    }

    public TWActuator(String[] parts) {
        super(OVEquipment.EquipmentType.ACTUATOR, parts);
// special should get set inside the super constructor (OVEquipment level)
    }

    @Override
    public void setSpecial(int binary) {
        super.setSpecial(binary);
        if (binary >= 32){
            startedWithBottom = true;
            binary-=32;
        }
        if (binary >=16) {
            startedWithLower = true;
            binary -= 16;
        }
        if (binary >= 8){
            top = true;
            binary-=8;
        }
        if (binary >=4) {
            upper = true;
            binary -= 4;
        }
        if (binary >= 2){
            lower = true;
            binary -= 2;
        }
        if (binary==1)bottom = true;
    }

    @Override
    public int getCritSlots() {
        int slots = 0;
        if (top)slots++;
        if (upper)slots++;
        if (lower)slots++;
        if (bottom)slots++;
        return slots;
    }
    @Override
    protected int numberOfPips(){
        // return the number of crits slots (health pips) the actuator started with
        int health = 4;
        if (!startedWithBottom)health--;
        if (!startedWithLower)health--;
        return health;
    }

    @Override
    public void reverseCrit() {
        // TODO how do we know which one was critted???
        // For now just reverse the first one found
        if (!top)setTop(true);
        else if (!upper)setUpper(true);
        else if (!lower)setLower(true);
        else setBottom(true);
    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
        int left = getCritSlots();
        if (left>0) {//should have already been checked but just in case!
            int loc = (new Random()).nextInt(left);
            if (top){
                if (loc==0){
                    setTop(false);
                    return;
                }
                else loc--;
            }
            if (upper){
                if (loc==0){
                    setUpper(false);
                    return;
                }
                else loc--;
            }
            if (lower){
                if (loc==0){
                    setLower(false);
                    return;
                }
                else loc--;
            }
            if (bottom)setBottom(false);//only one left so just crit it

        }
    }
    public int getShootingMod(){
        if (!top)return 4;
        int mod = 0;
        if (!upper) mod = 1;
        if (!lower && startedWithLower)mod++;
        return mod;
    }
    public int getMovementMod(int initialWalk){
        int outWalk = initialWalk;
        if (!top){
            return Math.floorDiv(outWalk+1,2);
        }
        //TODO TW rules say that a hip crit wipes any impact from previous crits but
        // subsequent ones will cause additional impact
        if (!upper)outWalk--;
        if (!lower)outWalk--;
        if (!bottom)outWalk--;
        return outWalk;
    }
    public int getPSRMods(){
        if (!top){
            return 2;
        }
        int mod = 0;
        if (!upper)mod++;
        if (!lower)mod++;
        if (!bottom)mod++;
        return mod;
    }

    public void setTop(boolean top) {
        //when changing the value of any of the actuators update the special key which stores it in the DB
        if (this.top!=top) {
            this.top = top;
            setSpecial(getSpecial() + (top ? 8 : -8));
        }
    }

    public void setUpper(boolean upper) {
        //when changing the value of any of the actuators update the special key which stores it in the DB
        if (this.upper != upper) {
            this.upper = upper;
            setSpecial(getSpecial() + (upper ? 4 : -4));
        }
    }

    public void setLower(boolean lower) {
        //when changing the value of any of the actuators update the special key which stores it in the DB
        if (this.lower != lower) {
            this.lower = lower;
            setSpecial(getSpecial() + (lower ? 2 : -2));
        }
        if (lower && !startedWithLower){
            startedWithLower=true;
            setSpecial(getSpecial() + 16);
        }
    }

    public void setBottom(boolean bottom) {
        //when changing the value of any of the actuators update the special key which stores it in the DB
        if (this.bottom != bottom) {
            this.bottom = bottom;
            setSpecial(getSpecial() + (bottom ? 1 : -1));
        }
        if (bottom && !startedWithBottom){
            startedWithBottom=true;
            setSpecial(getSpecial() + 32);
        }
    }
    @Override
    public void setDatabase(ContentValues cv) {
        super.setDatabase(cv);
        cv.put(OVDatabaseUnit.COLUMN_EQUIP_SPEC1, getSpecial());
    }
    @Override
    public int getDamage(){
        int damage = 0;
        if (!top)damage++;
        if (!upper)damage++;
        if (startedWithLower && !lower)damage++;
        if (startedWithBottom && !bottom)damage++;
        return damage;
    }

    public boolean isTop(){return top;}
}