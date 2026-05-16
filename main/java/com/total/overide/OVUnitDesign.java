package com.total.overide;


import android.database.Cursor;

import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitDesign;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.Turn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * This class holds the technical details for an Override Unit
 */
public class OVUnitDesign implements IUnitDesign {
    private OVHeader header;
    private final List<OVSegment> segments; //each location on the unit is represented by a single segment
    private final List<IEquipment> equipment; // weapons, ammunition and other equipment are all stored here
    private final List<IWeapon> tics;

    public OVUnitDesign(){
        super();
        header = new OVHeader();
        segments = new ArrayList<>();
        equipment = new ArrayList<>();
        tics = new ArrayList<>();
    }
    public OVUnitDesign(Cursor cur){
        super();
        header = new OVHeader(cur);
        segments = new ArrayList<>();
        equipment = new ArrayList<>();
        tics = new ArrayList<>();
    }
    public OVUnitDesign(List<String> list){
        super();
        ForceList.ForceType forceType = ForceList.ForceType.OV;
        segments = new ArrayList<>();
        equipment = new ArrayList<>();
        tics = new ArrayList<>();
        for (String line : list) {
            String[] parts = line.split(":");
            switch (parts[0]) {
                case "HEADER":
                    header = new OVHeader(parts[1]);
                    forceType = header.getType();
                    break;
                case "SEGMENT":
                    segments.add(OVSegmentInst.newInstance(parts[1],this));
                    break;
                case "EQUIPMENT":
                    // for core equipment (engine/gyro/actuators) state will need to be linked later
                    equipment.add(OVEquipment.newEquipment(parts[1],forceType));
                    break;
                case "TIC":
                    tics.add(OVTic.newTIC(equipment, parts[1]));
                    break;
            }
        }

    }

    @Override
    public IUnitHeader getHeader() {
        return header;
    }

    @Override
    public List<IEquipment> getEquipment() {
        return equipment;
    }

    @Override
    public boolean hasEquipment(OVEquipment.EquipmentType type) {
        boolean hasIt = false;
        for (IEquipment equip : equipment) {
            if (equip.getType() == type) {
                hasIt = true;
                break;
            }
        }
        return hasIt;
    }
    @Override
    public List<IWeapon> getWeapons() {
        if (Game.current != null && !Game.current.isUseTics()){
        // return a list of the individual weapons (OVWeaponInstance)
            List<IWeapon> weaponList = new ArrayList<>();
            for (IEquipment weap : equipment){
                //by using OVWeaponInstance Physical weapons are excluded
                if (weap instanceof OVWeaponInstance){
                    weaponList.add((IWeapon)weap);
                }
            }
            return weaponList;
        } else {
            return tics;
        }
    }

    @Override
    public List<IEquipment> getActivityEnhancers(Turn.Phase phase) {
        List<IEquipment> list = new ArrayList<>();
        for (IEquipment equip : equipment){
            if (equip.isOperational()){
                if (phase == Turn.Phase.MOVE && equip.isMoveModifier()) {
                    list.add(equip);
                }
            }
        }
        return list;
    }

    @Override
    public OVSegment getSegment(OVSegment.OVLocation location) {
        OVSegment ret = null;
        for (Iterator<OVSegment> it = segments.iterator(); it.hasNext() && ret == null;){
            ret = it.next();
            if (!ret.equals(location)){
                ret = null;
            }
        }
        return ret;
    }
    public List<OVSegment> getSegments(){
        return segments;
    }

    /*
     Called to attach all weapons and equipment to the segment that contains it
     */
    public void linkSegments(){
        boolean hasArtemis = false;
        for (int i = 0;i<equipment.size();i++){
            OVSegment.OVLocation loc = equipment.get(i).getLocation();
            // no segment for the body of a vehicle
            if (loc!= OVSegment.OVLocation.BODY)getSegment(loc).addEquipment(equipment.get(i));
            switch (equipment.get(i).getType()) {
                case WEAPON:
                    // find ammo
                    if (equipment.get(i) instanceof OVWeaponInstance)
                        linkAmmo((OVWeaponInstance) equipment.get(i));
                    break;
                case ARTEMISIV:
                    hasArtemis = true;
                    break;
                case ENGINE:
                    ((OVCoreEquipment) equipment.get(i)).setEngine(header.getEngine());
                case GYRO:
                case ACTUATOR:
                    if (equipment.get(i) instanceof OVCoreEquipment) {
                        OVCoreEquipment core = (OVCoreEquipment) equipment.get(i);
                        // if this is an instance then should return the state
                        if (core != null)
                            core.setState(getState());
                    }
                    break;
            }
        }
        if (hasArtemis){
            // if a unit has artemis then it must have it for all MISSILE type weapons, so go
            // through the weapon list and set them all to be artemis enabled
            for (IEquipment weap : getEquipment()){
                if (weap instanceof OVWeaponInstance &&
                        ((OVWeaponInstance)weap).getWeapon().getDmgType() == OVWeapon.WeaponType.MISSILE){
                    // we have a missile weapon so set it to artemis enabled
                    ((OVWeaponInstance)weap).setArtemis(true);
                }
            }
        }

        //set the index values for equipment
        for (OVSegment seg : segments){
            seg.initialiseIndex();
        }
        // sort all equipment by location and then index
        equipment.sort((equip, t1) -> {
            int comp = equip.getLocation().compareTo(t1.getLocation());
            if (comp==0) Boolean.compare(equip.isOperational(),t1.isOperational());
            if (comp==0)
                comp = Integer.compare(equip.getIndex(), t1.getIndex());
            return comp;
        });
    }
    public OVState getState(){return null;}
    private void linkAmmo(OVWeaponInstance weapon){
        for (int i = 0;i<equipment.size();i++){
            if (equipment.get(i).getType() == OVEquipment.EquipmentType.AMMO){
                OVAmmunition ammo = (OVAmmunition) equipment.get(i);
                if (ammo.getWeaponType().getId()==weapon.getWeapon().getId()){
                    weapon.addAmmo(ammo);
                }
            }
        }
    }
    public void updateTic(OVWeaponInstance weapon, int newTic){
        int oldTic = weapon.getTic();
        boolean added = false;
        int deleteID = -1;
        for (int i = 0;i < tics.size();i++){
            if (tics.get(i).getID() == oldTic){
                //remove the weapon
                ((OVTic)tics.get(i)).removeWeapon(weapon.getID());
                if (((OVTic)tics.get(i)).getWeapons().size() < 1){
                    deleteID = i;
                }
            } else if (tics.get(i).getID() == newTic){
                // add the weapon
                added = true;
                ((OVTic)tics.get(i)).addWeapon(weapon);
            }
        }
        if (deleteID >= 0){
            tics.remove(deleteID);
        }
        if (!added){
            OVTic tic = new OVTic(newTic);
            tics.add(tic);
            tic.addWeapon(weapon);
        }
    }

//    public List<IWeapon> getTics() {
//        return tics;
//    }
    public boolean hasIndirectWeapons(){
        boolean indirect = false;
        for (IWeapon weapon : getWeapons()){
            indirect = weapon.isIndirect();
            if (indirect)break;
        }
        return indirect;
    }

    public List<String> getStreamValue(){
        List<String> stream = new ArrayList<>();
        stream.add(header.getStreamValue());
        for (OVSegment seg : segments) {
            stream.add(seg.getStreamValue());
        }
        for (IEquipment equip : equipment){
            stream.add(equip.getStreamValue() + '\n');
        }
        for (IWeapon tic : tics){
            stream.add(tic.getStreamValue());
        }
        return stream;
    }
}
