package com.total.overiden;

import android.content.ContentValues;
import android.graphics.Paint;

import com.total.overide.OVAmmunition;
import com.total.overide.OVEquipment;
import com.total.overide.OVRange;
import com.total.overide.OVSegment;
import com.total.overide.OVWeapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhysicalWeapon implements IWeapon{
    public enum PhysicalWeaponType {
        NONE("None",0),
        PUNCH("Punch",0),
        KICK("Kick",0),
        PUSH("Push",5),
        CHARGE("Charge",5),
        DFA("DFA",5),
        FALL("Fall",5),
        HATCHET("Hatchet",0),
        AMMO("Ammo Explosion",0),
        WEAPON("Weapon Explosion",0);
        private final String description;
        private final int cluster;
        PhysicalWeaponType(String desc,int clus){
            description = desc;
            cluster = clus;
        }
        public String toDisplay(){return description;}
    }
    public enum PhysicalHitGrouping {
        TOP, // punch hit location table
        FULL, // full hit location table
        BOTTOM; // kick location table
    }
    private final String name;
    private final PhysicalWeaponType type;
    private final int damage;
//    private OVSegment.OVLocation location = null;
    private int id;
    private final PhysicalHitGrouping grouping;
    public PhysicalWeapon(PhysicalWeaponType type, int damage, PhysicalHitGrouping grouping){
        super();
        this.type = type;
        this.name = type.description;
        this.damage = damage;
        this.grouping = grouping;
        id = -1;
    }
    public PhysicalWeapon(String name){
        super();
        String[] data = name.split(",");
        id = Integer.parseInt(data[0]);
        this.type = PhysicalWeaponType.valueOf(data[1]);
        this.name = type.description;
        this.damage = Integer.parseInt(data[2]);
        this.grouping = PhysicalHitGrouping.valueOf(data[3]);
    }
    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean isMoveModifier() {
        return false;
    }

    @Override
    public int getSpecial() {
        return 0;
    }

    public PhysicalWeaponType getPhysType() {
        return type;
    }
    @Override
    public void setSpecial(int special) {

    }

    @Override
    public int getSpecialTwo() {
        return 0;
    }

    @Override
    public void setSpecialTwo(int special) {

    }

    @Override
    public boolean activateEquipment(IUnitData unit) {
        return false;
    }

    @Override
    public void resolveTurn() {
        // nothing to do here
    }

    /*
     * setID is used when loading a record from the database
     */
    public void setId(int id) {
        this.id = id;
    }
    /*
     * generateId is used as physicalWeapon objects have an ID associated
     * with the type of physical attack
     */
    public void generateId() {
        switch (type){
            case FALL: id = 910; break;
            case AMMO: id = 920; break;
            case WEAPON: id = 930; break;
            case HATCHET: id = 970; break;
            case CHARGE: id = 980; break;
            case DFA: id = 985; break;
            case PUSH: id = 990; break;
            case PUNCH: id = 995; break;
            case KICK: id = 975; break;
            default: id = 999; break;
        }
    }
    @Override
    public int getHeat() {
        return 0; // physical attacks don't generate heat
    }

    @Override
    public void fireWeapon() {
// nothing required, physical attacks never have ammo
    }

    @Override
    public void checkWeaponJam(int rolled) {

    }

    @Override
    public boolean isJammed() {
        return false;
    }

    @Override
    public void hit(TargetData target, TargetData.LocTable table) {
        hit(target,damage,table);
    }

    public void hit(TargetData target, int totalDmg, TargetData.LocTable table) {
        DamageRecord dmg = new DamageRecord(this, target);
        if (type.cluster==0)
            dmg.addGrouping(totalDmg, table);
        else {
            // damage done in clusters
            while (totalDmg > 0){
                dmg.addGrouping(Math.min(totalDmg,type.cluster), table);
                totalDmg -= type.cluster;
            }
        }
        target.getTarget().getTurn().addDamage(dmg);
        dmg.applyDamage(target.getTarget());

        // some special attacks have other updates required
        int legs = 0;
        int split = 1;
        PilotCheck shooterCheck = null;
        switch(type){
            case DFA:
                // damage to attacker
                if (target.getShooter().getHeader().getType()== ForceList.ForceType.TW) {
                    legs = Math.floorDiv(target.getShooter().getHeader().getMass(), 5);
                    split = 5;
                }else {
                    legs = Math.floorDiv(target.getShooter().getHeader().getMass() + 10, 15);
                    split = 2;
                }
                shooterCheck = new PilotCheck(target.getShooter(), PilotCheck.PilotCheckType.DFAATTEMPT,null);
                target.getTarget().getTurn().getTurnChecks().add(new PilotCheck(target.getTarget(), PilotCheck.PilotCheckType.DFAD,dmg));
                break;
            case CHARGE:
                if (target.getShooter().getHeader().getType()== ForceList.ForceType.TW) {
                    legs = Math.floorDiv(target.getShooter().getHeader().getMass(), 10);
                    split = 5;
                }else {
                    legs = Math.floorDiv(target.getShooter().getHeader().getMass() + 25, 30);
                    split = 2;
                }
                shooterCheck = new PilotCheck(target.getShooter(), PilotCheck.PilotCheckType.CHARGER,null);
                target.getTarget().getTurn().getTurnChecks().add(new PilotCheck(target.getTarget(), PilotCheck.PilotCheckType.CHARGED,dmg));
                break;
            case KICK:
                target.getTarget().getTurn().getTurnChecks().add(new PilotCheck(target.getTarget(), PilotCheck.PilotCheckType.KICKED,dmg));
                break;
        }
        if (legs>0) {
            // self inflicted damage from charge/dfa
            dmg = new DamageRecord(this,  new TargetData(null, target.getShooter()));
            while (legs > 0) {
                dmg.addGrouping(Math.min(legs, split), type==PhysicalWeaponType.DFA? TargetData.LocTable.KICK:TargetData.LocTable.FULL);
                legs -= split;
            }
            target.getShooter().getTurn().addDamage(dmg);
            dmg.applyDamage(target.getShooter());

            //shooterCheck should always be created in this scenario
            shooterCheck.setParent(dmg);
            target.getShooter().getTurn().getTurnChecks().add(shooterCheck);
        }
    }

    public void missedAttack(TargetData target){
        switch (type){
            case DFA:
                DamageRecord dmg = new DamageRecord(this, new TargetData(null, target.getShooter()));
                target.getShooter().addFallDamage(dmg, 3); // failed DFA falls 2 levels(lvl 0 +2)
//            target.getShooter().getTurn().addDamage(dmg);
                dmg.applyDamage(target.getShooter());
                break;
            case KICK:
                if (target.getShooter().getHeader().getType() == ForceList.ForceType.TW) {
                    target.getShooter().getTurn().getTurnChecks().add(new PilotCheck(target.getShooter(),
                            PilotCheck.PilotCheckType.MISSEDKICK, null));

                }
                break;
        }
    }

    @Override
    public WeaponMode getWeaponMode() {
        return WeaponMode.STD;
    }

    @Override
    public boolean setWeaponMode(WeaponMode mode) {
        return mode == WeaponMode.STD;
    }

    @Override
    public List<WeaponMode> getAvailableModes() {
        List<WeaponMode> modeList = new ArrayList<>();
        modeList.add(WeaponMode.STD);
        return modeList;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OVSegment.OVLocation getLocation() {

        return OVSegment.OVLocation.RIGHTARM;
    }


    @Override
    public OVEquipment.EquipmentType getType() {
        return OVEquipment.EquipmentType.WEAPON;
    }

    @Override
    public void setStatus(boolean operational) {

    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {

    }

    @Override
    public void reverseCrit() {

    }

    @Override
    public boolean isOperational() {
        return true;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getRangeMod(int range) {
        int mod = range== OVRange.pb?0:20;
        if (Game.current!=null && !Game.current.isGameOV()) {
            // adjust for attack type
            switch (type) {
                case HATCHET:
                    mod -= 1;
                break;
                case KICK:
                    mod -= 2;
                    break;
                case PUSH:
                    mod -= 1;
                    break;
            }
        }
        return mod;
    }

    @Override
    public int getHeatDamage() {
        return 0;
    }

    @Override
    public DamageType getDamageType() {
        return null;
    }

    @Override
    public OVWeapon.WeaponType getWeaponType() {
        return null;
    }

    @Override
    public String getLocationText() {
            return "Physical";
    }

    @Override
    public boolean isMultiMode() {
        return false;
    }

    @Override
    public List<OVAmmunition> getAmmo() {
        return Collections.emptyList();
    }

    @Override
    public boolean isIndirect() {
        return false;
    }

    @Override
    public String getStreamValue() {
        return "PHYSICALWEAPON:"+id+","+type.toString()+","+damage+","+grouping+"\n";
    }

    @Override
    public void updateFromStream(String[] data) {
        // shouldn't ever use this for a physical weapon (what about melee weapons?)
    }

    @Override
    public boolean alreadySent() {
        return false;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public void setIndex(int ind) {

    }

    @Override
    public void markAsSent() {

    }

    @Override
    public int getCritSlots() {
        return 0;
    }

    @Override
    public void setDatabase(ContentValues cv) {

    }

    @Override
    public String getDamageText() {
        return Integer.toString(getDamage());
    }

    @Override
    public boolean hasArtemis() {
        return false;
    }

    @Override
    public void setArtemis(boolean art) {

    }

    @Override
    public MainActivity.Sounds getSoundEffect() {
        return MainActivity.Sounds.BOOM;
    }

    @Override
    public int getClusterDamage() {
        return 0;
    }

    @Override
    public int compareTo(IEquipment iEquipment) {
        return 0;
    }

    public int getDamage() {
        return damage;
    }

    public PhysicalHitGrouping getGrouping() {
        return grouping;
    }

    public List<OptionButton.OptionButtonChoice> getHitLocationTables(){
        List<OptionButton.OptionButtonChoice> list = new ArrayList<>();
        Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
        gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.Green, null));
        switch (type){
            case PUNCH:
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.PUNCH.toString(),gen,TargetData.LocTable.PUNCH.toFile()));
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.DodgerBlue, null));
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.KICK.toString(),gen,TargetData.LocTable.KICK.toFile())); //if 1 level below target will punch the legs
                break;
            case KICK:
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.KICK.toString(),gen,TargetData.LocTable.KICK.toFile()));
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.DodgerBlue, null));
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.PUNCH.toString(),gen,TargetData.LocTable.PUNCH.toFile())); //if 1 level above target will kick the body
                break;
            case HATCHET:
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.FULL.toString(),gen,TargetData.LocTable.FULL.toFile()));
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.DodgerBlue, null));
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.PUNCH.toString(),gen,TargetData.LocTable.PUNCH.toFile())); //if 1 level above target will hatchet the body
                gen = new Paint(Paint.ANTI_ALIAS_FLAG);
                gen.setColor(MainActivity.currentActivity.getResources().getColor(R.color.IndianRed, null));
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.KICK.toString(),gen,TargetData.LocTable.KICK.toFile())); //if 1 level below target will hatchet the legs
                break;
            default:
                list.add(new OptionButton.OptionButtonChoice(TargetData.LocTable.FULL.toString(),gen,TargetData.LocTable.FULL.toFile()));
                break;

        }
        return list;
    }
}
