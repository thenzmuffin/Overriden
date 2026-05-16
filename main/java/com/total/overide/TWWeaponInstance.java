package com.total.overide;

import com.total.overiden.DamageMessage;
import com.total.overiden.DamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.TargetData;
import com.total.overiden.TwoDSix;

public class TWWeaponInstance extends OVWeaponInstance{
    public TWWeaponInstance(String[] parts, int id, OVWeapon weapon) {
        super(parts, id, weapon);
    }

    public TWWeaponInstance(String[] parts) {
        super(parts);
    }

    public TWWeaponInstance(OVSegment.OVLocation location, int id, OVWeapon weapon, boolean state, WeaponMode mode) {
        super(location, id, weapon, state, mode);
    }
    @Override
    public void hit(TargetData targetData, TargetData.LocTable table) {
        IUnitData target = targetData.getTarget(); // for efficiency and readability
        int damageTotal = getWeapon().getDamageMax();
        TwoDSix mDice = null;
        boolean artemis = false;
        DamageRecord dmg = new DamageRecord(this, targetData);
        switch (getWeapon().getDmgType()) {
            case MISSILE:
                boolean narc = false;
                // note that artemis has no effect on indirect shots
                artemis = !targetData.isIndirect() && hasArtemis() && getWeaponMode() == WeaponMode.ARTIV;

                // cluster roll
                mDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);//use location as there is no pass value
                dmg.setClusterDice(mDice);

                // check for Narc modifier (i.e. missiles are Narc equipped)
                // Narc doesn't give a bonus for indirect fire so bypass in that case
                // (other benefits of NARC in IF are dealt with elsewhere)
//                if (!targetData.isIndirect() && (getWeaponMode() == WeaponMode.LRMNARC ||
//                        getWeaponMode() == WeaponMode.SRMNARC) && !(targetData.getEcmBubble()>1)) {
                    if (!targetData.isIndirect() && getWeaponMode() == WeaponMode.NARC
                            && !(targetData.getEcmBubble()>1)) {
//                    in the event the missiles are narc equipped then we need to check for NARC pods on the target
                    narc = target.isNarced(); // do this here to avoid processing when narc missiles aren't being used
                }

                // roll M Dice, add a group for base damage and then every 2 damage after that
                int clusterRoll = mDice.getTotal();
                // adjust for artemis and Narc
                if (artemis || narc) clusterRoll = Math.min(12,clusterRoll+2);
                // adjust for AMS
                clusterRoll -= target.getTurn().triggerDefensiveWeapon(getWeapon().getTwClusterTable());
                if (clusterRoll<2)clusterRoll = 2;

                assignClusteredDamage(OVWeapon.getNumberOfHits(clusterRoll, getWeapon().getTwClusterTable()),dmg);
                break;
            case CLUSTER:
                if (getWeaponMode() == WeaponMode.CLUS) { // we can have selected special munitions or not
                    // get the number of hits from the scatter shot
                    mDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
                    dmg.setClusterDice(mDice);
                    assignClusteredDamage(OVWeapon.getNumberOfHits(mDice.getTotal(), getWeapon().getTwClusterTable()),dmg);
                    break;
                }
            case PULSE:
            case STANDARD:
                // add max damage to a single group
                dmg.addGrouping(damageTotal);
                break;
            case STREAK:
                //weapon has hit so we need to expend the ammo
                // all weapons in this TIC must be STREAK
                if (!expendAmmo()) {
                    //this means there wasn't any ammo left so it didn't shoot
                    return;
                }
                int noOfHits = getWeapon().getTwClusterTable(); //STREAK auto hits with all,
                if (target.getTurn().triggerDefensiveWeapon(damageTotal)>0) { //unless AMS messes with it!
                    noOfHits = OVWeapon.getNumberOfHits(7, getWeapon().getTwClusterTable());
                }
                assignClusteredDamage(noOfHits,dmg);

                break;
            case ULTRA:
            case RAPID: //RAC 5 and 2
                dmg.addGrouping(damageTotal);
                if (getWeaponMode() != WeaponMode.STD) {// double firing an ultra weapon
                    // roll the cluster - using cluster tables not C dice
                    mDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
                    dmg.setClusterDice(mDice);
                    int clusters = OVWeapon.getNumberOfHits(mDice.getTotal(), getWeaponMode().getRounds());
                    for (int i = 1; i < clusters; i++) //start at 1 because one has already auto hit
                    { // works for RAC and Ultra as getNumberOfHits uses the appropriate cluster table
                        dmg.addGrouping(getWeapon().getDamage());// returns the base damage which in this case is correct
                    }
                }
                // ultra and RAC is like cluster except every cluster does more than 1 point of damage (potentially, ultra ac 2 does only 1 damage per cluster)
                // note that a critical can occur here to jam the weapon, for RAC the jam can also be cleared
                break;
            case DEFENCE:
                // Shouldn't be shot, this includes AMS
                break;
            case SPECIAL:
                // special includes weapons such as the narc
                // if it is TAG then mark the target as tagged
                switch (getWeapon().getName()) {
                    case "TAG":
                    case "LTAG":
                        target.getTurn().addDamage(new DamageMessage("Unit has been TAGed"));
                        target.getTurn().setTaggedThisTurn(true);
                        break;
                    case "NARC":
                        OVSegment.OVLocation narcLoc = target.convertLocation((new TwoDSix()).getTotal(), targetData.getFacing(), table, false, null);
                        if (target.getTurn().triggerDefensiveWeapon(damageTotal)>0){
                            mDice = new TwoDSix(1, TwoDSix.RollType.MDICE);
                            if (mDice.getTotal()<=3){
                                target.getTurn().addDamage(new DamageMessage("NARC Pod Destroyed by AMS",mDice));
                                break;
                            } else {
                                target.getTurn().addDamage(new DamageMessage("AMS failed to Hit the NARC Pod",mDice));
                            }
                        }
                        OVEquipment narcPod = new OVEquipment(OVEquipment.EquipmentType.NARC, 0, narcLoc, "NARC Pod");
                        narcPod.setSpecial(1); // can we use the special field to capture the turn the pod is attached?
                        target.getEquipment().add(narcPod);//add to both the general list
                        target.getSegment(narcLoc).getEquipment().add(narcPod); // and the location list
                        target.getTurn().addDamage(new DamageMessage("NARC Pod Attached-" + narcLoc.toString()));
                        break;
                }
                // for special type don't add the general damagerecord
                return;
        }
        target.getTurn().addDamage(dmg);
        dmg.applyDamage(target);
    }
    @Override
    public String getName() {
        return getWeaponName(); // super getname will add the tic number in front
    }
    protected void assignClusteredDamage(int noOfHits,DamageRecord dmg){
        while (noOfHits > 0) {
            if (noOfHits >= getWeapon().getTwClusterGroup()) {
                dmg.addGrouping(getWeapon().getTwClusterGroup() * getWeapon().getTwClusterDamage());
                noOfHits -= getWeapon().getTwClusterGroup();
            } else {
                dmg.addGrouping(noOfHits * getWeapon().getTwClusterDamage());
                noOfHits = 0;
            }
        }
    }
    @Override
    public int getClusterDamage() {
        return getWeapon().getTwClusterGroup() * getWeapon().getTwClusterDamage();
    }
}
