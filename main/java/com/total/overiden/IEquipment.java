package com.total.overiden;

import android.content.ContentValues;

import com.total.overide.OVEquipment;
import com.total.overide.OVSegment;

public interface IEquipment extends Comparable<IEquipment>{
    public String getName();
    public OVSegment.OVLocation getLocation();
    public OVEquipment.EquipmentType getType();

    public void setStatus(boolean operational);
    void applyCrit(IUnitData unit, IDamageRecord parent);
    void reverseCrit();
    boolean isOperational();
    int getHealth(); //returns the number of damage pips this equipment can take, normally one
    int getDamage(); // the number of pips of damage taken
    int getID();
    boolean isMoveModifier();
    int getSpecial();
    void setSpecial(int special);
    int getSpecialTwo();
    void setSpecialTwo(int special);
    boolean activateEquipment(IUnitData unit);
    void resolveTurn();
    String getStreamValue();
    void updateFromStream(String[] data);
    boolean alreadySent();
    int getIndex();
    void setIndex(int ind);
    void markAsSent();
    int getCritSlots();
    void setDatabase(ContentValues cv);
}
