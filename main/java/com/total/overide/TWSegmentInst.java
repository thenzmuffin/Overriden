package com.total.overide;

import com.total.overiden.CriticalHit;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.TargetData;
import com.total.overiden.TwoDSix;

import java.util.Random;

public class TWSegmentInst extends OVSegmentInst{

    public TWSegmentInst(OVLocation location, int armour, int structure, int rear, ArmourType type){
        super(location, armour, structure, rear, type);
    }
    public TWSegmentInst(String data){
        super(data);
    }
    @Override
    public void checkForCrit(IUnitData unit,CriticalHit crit, IDamageRecord parent) {

            // determine if a crit has occurred
            TwoDSix check = new TwoDSix();
            if (check.getTotal()>=8) {
                int noOfCrits = check.getTotal()>=10?check.getTotal()>=12?3:2:1;
                // special case on a 12 a limb will immediately be blown off
                if (noOfCrits==3 && (getLocation()==OVLocation.LEFTLEG ||
                        getLocation()==OVLocation.RIGHTLEG ||
                        getLocation()==OVLocation.RIGHTARM ||
                        getLocation()==OVLocation.LEFTARM)){
                    // limb is blown off
                    destroyLocation(unit, parent);
                    unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-Limb Destroyed", check, null, null, parent));
                    return;
                }
                applyNumberOfCrits(unit,check, noOfCrits, parent);
            } else {
                unit.getTurn().addDamage(new CriticalHit(getLocation().name() + "-No Crit", check, null, null, parent));
            }
        }

        private void applyNumberOfCrits(IUnitData unit, TwoDSix check, int no, IDamageRecord parent){
            IDamageRecord out;
            for(int i=0;i<no;i++) {
                out = null;
                //reorder the equipment to make sure all non-critable equipment is at the bottom of the list
                sortEquipment();
                //calculate how many valid crit locations are still present
                int totalCritSlots = getRemainingCritSlots();
                // randomly determine the hit location
                if (totalCritSlots>0){
                    int loc = (new Random()).nextInt(totalCritSlots);
                    for (IEquipment equip : getEquipment()){
                        if (equip.isOperational()){
                            loc -=equip.getCritSlots();
                            if (loc <0){
                                out = new CriticalHit(getLocation().name() + "- " + equip.getName() + " destroyed", check, null, equip, parent);
                                unit.getTurn().addDamage(out);
                                out.applyDamage(unit);
                                break;
                            }
                        }
                    }
                } else {
                    // we need to roll this to the next location inwards on the mech,
                    // typically this will only happen from side torso to centre torso
                    OVLocation newLocation = unit.transferDestroyedLocations(getLocation(), TargetData.LocTable.FRONT);
                    ((TWSegmentInst)unit.getSegment(newLocation)).applyNumberOfCrits(unit,check, no, parent);
                }
//            if (out!=null)out.applyDamage(unit);
            }

        }

}
