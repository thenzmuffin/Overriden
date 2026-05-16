package com.total.artificial;

import androidx.annotation.NonNull;

import com.total.overide.OVRange;
import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;
import com.total.overide.OVTic;
import com.total.overide.OVWeaponInstance;
import com.total.overide.TWWeaponInstance;
import com.total.overiden.ForceList;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.UnitMove;

public class AiUnitAnalysis extends AiUnitDataProxy{
    /* This class is used to determine optimum ranges for a unit
     * i.e. what range should the unit try and get to, should it close or hit from
     * distance, does it want to be at short, medium or long range?
     * the calculation determines average damage done at each range (using modifiers for the
     * range mod on each weapon) then multiplying the total by a factor for each range bracket
     * to reflect the likelihood of the enemy delivering successful damage back
     */
    private class Modifier{
        public int rangePen;
        public int modifier;
        public Modifier(int penalty, int mod){
            rangePen = penalty;
            modifier = mod;
        }
    }
    private final Modifier[] mods = {new Modifier(-2,17),new Modifier(-1,14),
            new Modifier(0,10),new Modifier(1,6),new Modifier(2,4),
            new Modifier(3,2),new Modifier(4,1)};
    private final float[] rangeMod = {1, 1.5f, 2.5f, 3, 3};
    private float pointBlank = 0;
    private float shortRange = 0;
    private float mediumRange = 0;
    private float longRange = 0;
    private float extremeRange = 0;
    private int bestRange = 0;
    private int strength = 0;
//    private final IUnitData unit;
    public AiUnitAnalysis(IUnitData unit){
        super(unit);
//        this.unit = unit;
        findOptimumRanges();
    }
    public void findOptimumRanges(){
        // determine the current optimum range for this unit
        for (IWeapon weapon : getWeapons()){
            if (weapon.isOperational()){
                pointBlank +=getCalculatedDamageValue(OVRange.pb,weapon);
                shortRange +=getCalculatedDamageValue(OVRange.sh,weapon);
                mediumRange +=getCalculatedDamageValue(OVRange.me,weapon);
                longRange +=getCalculatedDamageValue(OVRange.lo,weapon);
                extremeRange +=getCalculatedDamageValue(OVRange.ex,weapon);
            }
        }
        float max = Math.max(pointBlank,Math.max(Math.max(shortRange,mediumRange),Math.max(longRange,extremeRange)));
        if (pointBlank==max)bestRange = OVRange.pb;
        else if (shortRange==max)bestRange = OVRange.sh;
        else if (mediumRange==max)bestRange = OVRange.me;
        else if (longRange==max)bestRange = OVRange.lo;
        else if (extremeRange==max)bestRange = OVRange.ex;
        OVSegment seg = getCoreSegment();
        if (seg!=null)
            strength = seg.getArmour() + seg.getStructure();

    }
    private String getRangeLabel(int range){
        switch(range){
            case OVRange.pb:
                return "Point Blank";
            case OVRange.sh:
                return "Short Range";
            case OVRange.me:
                return "Medium Range";
            case OVRange.lo:
                return "Long Range";
            case OVRange.ex:
                return "Extreme Range";
        }
        return "Unknown";
    }
    public String getOptimumRangeToken(){
        return getRangeLabel(determineBestRange(0));
    }
    public String getMaxRange(){
        return getRangeLabel(determineBestRange(2));
    }
    public String getMinRange(){
        int range;
        float value = getRangeValue(bestRange) * 0.6f;
        if (pointBlank>value) range = OVRange.pb;
        else if (shortRange>value) range = OVRange.sh;
        else if (mediumRange>value) range = OVRange.me;
        else if (longRange>value) range = OVRange.lo;
        else range = OVRange.ex;
        return getRangeLabel(range);
    }
    private float getRangeValue(int range){
        switch (range){
            case OVRange.pb:
                return pointBlank;
            case OVRange.sh:
                return shortRange;
            case OVRange.me:
                return mediumRange;
            case OVRange.lo:
                return longRange;
            case OVRange.ex:
                return extremeRange;
        }
        return 0f;
    }
    private int getWeaponDamage(IWeapon weapon){
        int damage = 0;
        if (weapon instanceof OVTic){
            for (IWeapon weap : ((OVTic) weapon).getWeapons()){
                damage += getWeaponDamage(weap);
            }
        } else if (weapon instanceof OVWeaponInstance){
            damage = ((OVWeaponInstance) weapon).getWeapon().getDamageMax();
        }
        return damage;
    }
    private int determineBestRange(int max){
        int extremeR = 0, longR = 0, mediumR = 0, shortR = 0, pointB = 0;
        for (IWeapon weapon : getWeapons()){
            if (weapon.getRangeMod(OVRange.ex)<=max){
                extremeR += getWeaponDamage(weapon);
            } else if (weapon.getRangeMod(OVRange.lo)<=max){
                longR += getWeaponDamage(weapon);
            } else if (weapon.getRangeMod(OVRange.me)<=max){
                mediumR += getWeaponDamage(weapon);
            } else if (weapon.getRangeMod(OVRange.sh)<=max){
                shortR += getWeaponDamage(weapon);
            } else if (weapon.getRangeMod(OVRange.pb)<=max){
                pointB += getWeaponDamage(weapon);
            }
        }
        int largest = Math.max(Math.max(extremeR, longR), Math.max(mediumR,Math.max(shortR,pointB)));
        if (largest==extremeR)return OVRange.ex;
        if (largest==longR)return OVRange.lo;
        if (largest==mediumR)return OVRange.me;
        if (largest==shortR)return OVRange.sh;
        return OVRange.pb;
    }

    private float getCalculatedDamageValue(int range,IWeapon weapon){
        float damage = 0;

        float avgDmg = 0;
        // average damage here is a simple average between base and max damage
        if (weapon instanceof OVTic) {
            avgDmg = (((OVTic) weapon).getBaseDamage() + ((OVTic) weapon).getMaxDamage()) / 2f;
        } else if (weapon instanceof OVWeaponInstance) {
            avgDmg = (((OVWeaponInstance) weapon).getWeapon().getDamageMax() + ((OVWeaponInstance) weapon).getWeapon().getDamage()) / 2f;
        }
        // Multiply value by a range mod reflecting increased damage received at shorter ranges
        damage += getRangeMod(weapon.getRangeMod(range)) * avgDmg * rangeMod[range - 1];

        return damage;
    }
    private int getRangeMod(int ovMod){
        if (ovMod>4)return 0;
        if (ovMod<-2)ovMod = -2;
        for (Modifier modif : mods){
            if (modif.rangePen==ovMod){
                return modif.modifier;
            }
        }
        return 1;
    }

    public float getValue(AiCommander.TargetType type){
        float ret = 0f;
        OVSegmentInst seg;
        switch (type.getEnum()){
            case BV_LOW:
            case BV_HIGH:
                // get the adjusted walking speed and convert to TMM
                ret = 1;
                break;
            case CAT_LOW:
            case CAT_HIGH:
                // get the adjusted walking speed and convert to TMM
                int mass = getHeader().getMass();
                if (mass >= 80)ret = 4;
                else if(mass>=60)ret = 3;
                else if (mass>=40)ret = 2;
                else if (mass >=5)ret = 1; // allow for no category (infantry)
                break;
            case MASS_LOW:
            case MASS_HIGH:
                // get the adjusted walking speed and convert to TMM
                ret = getHeader().getMass();
                break;
            case TMM_LOW:
            case TMM_HIGH:
                // get the adjusted walking speed and convert to TMM
                ret = UnitMove.convertDistanceToTMM(this.getAdjustedMovement(UnitMove.MoveType.WALK));
                break;
            case SPEED_LOW:
            case SPEED_HIGH:
                ret = Math.max(getAdjustedMovement(UnitMove.MoveType.RUN),
                        getAdjustedMovement(UnitMove.MoveType.JUMP));
                break;
            case THREAT_ORIG:
            case THREAT_NOW:
                ret = Math.max(Math.max(extremeRange,longRange),Math.max(shortRange,mediumRange));
                break;
            case HEALTH_HIGH:
            case HEALTH_LOW:
                seg = getCoreSegment();
                if (seg!=null)
                    ret = seg.getArmourTurnDmg() + seg.getStructureTurnDmg();
                break;
            case DAMAGE_HIGH:
            case DAMAGE_LOW:
                seg = getCoreSegment();
                if (seg!=null)
                    ret = strength - (seg.getArmourTurnDmg() + seg.getStructureTurnDmg());
                break;
            case HAS_CORE_ARMOUR:
                seg = getCoreSegment();
                if (seg!=null)
                    ret = seg.getArmourTurnDmg();
                break;
            case STRENGTH_LOW:
            case STRENGTH_HIGH:
                ret = strength;
                break;
        }
        return ret;
    }

//    public IUnitData getUnit(){return unit;}

    @NonNull
    @Override
    public String toString() {
        return getHeader().getName() + " : " + getPilot().getPilotName();
    }
}
