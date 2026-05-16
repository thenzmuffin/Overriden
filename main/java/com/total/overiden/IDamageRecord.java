package com.total.overiden;

import java.util.List;


public interface IDamageRecord extends IChildLink{
    void setKey(long key);

//    public void addDamageLine(Fragment p);
    int getTotalDamage();
//    public void toFile(OutputStream pOut);
//    void linkTargets(List<TargetData> targets);

    IWeapon getWeapon();
    TargetData getTarget();
    TwoDSix getClusterDice();
    List<DamageRecord.DamageGrouping> getDamage();
    void applyDamage(IUnitData target);
    boolean isApplied();
    void setApplied(boolean applied);
    List<String> getStreamValue();
    boolean alreadySent();
    void markAsSent();
    void setIndex(int i);
    int getIndex();
    void setEdgeUsed(boolean used);
    boolean isEdgeUsed();

}
