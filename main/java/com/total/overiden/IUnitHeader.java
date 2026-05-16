package com.total.overiden;

import com.total.overide.OVHeader;
import com.total.overide.OVSegment;

public interface IUnitHeader {
    enum UnitType{
        NONE, //Used for spinner creation
        MECH,
        BA,
        TANK,
        HOVER,
        WHEEL,
        VTOL,
        BUILDING;
        public static UnitType parse(String input){
            UnitType out = null;
            switch (input.strip().toLowerCase()){
                case "wheeled":
                    out = WHEEL;
                    break;
                case "hover":
                    out = HOVER;
                    break;
                case "tracked":
                    out = TANK;
                    break;
                case "building":
                    out = BUILDING;
                    break;
            }
            return out;
        }
    }
    UnitType getUnitType();
    void setUnitType(UnitType type);
    int getKey();  // unique identifier for type of mech (numeric)
    void setKey(int num);  // set the identifier
    String getName();
    void setName(String name);
    String getVariant();
    int getWalk();
    int getRun();
    boolean canJump();
    int getJump();
    int getMass();
    OVHeader.EngineType getEngine();
    OVHeader.UnitRole getRole();
    void setArmourType(OVSegment.ArmourType type);
    OVSegment.ArmourType getArmourType();
    int getHeatSinks();
    int getTwHeatSinks();
    boolean isDoubleHeatSinks();
    ForceList.ForceType getType();
    void setType(ForceList.ForceType type);
    boolean isTsm();
    void setTsm(boolean tsm);
}
