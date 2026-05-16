package com.total.overiden;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArraySet;

import com.total.overide.OVDatabaseForce;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Pilot {
    private static final Integer[] edgeCost = new Integer[]{0,0,60,120,200,300,420,560,720,900,1100};
    private static final Integer[] skillsCost = new Integer[]{0,60,180,360,600,900};
    private static final Integer[] pilotingCost = new Integer[]{1600,850,475,225,100,0};
    private static final Integer[] gunneryCost = new Integer[]{2550,1425,675,300,0};
    public enum EdgeTrigger{
        REROLL_HIT,
        REROLL_LOCATION,
        REROLL_CRIT,
        BOOST_SHOT,
        MOVE_CHECK
    }
    public enum EdgeSkill {
        NONE,
        NIMBLE,
        JUMPING_JACK,
        SPEED_DEMON,
        MARKSMAN,
        MELEE_SPECIALIST,
        FORWARD_OBSERVER,
        BULWARK,
        COOLANT_FLUSH,
        PATIENT,
        CAUTIOUS,
        PROTECTOR,
        ASSASSIN,
        SHOOTING_BUMP, // This is the default skill for improving shot accuracy
        CRITICAL // This is the default skill for saving crits
    }
    public enum PilotingMods {
        COMMANDER(1),
        DRIVERCRIT(2),
        MINORDMG(1),
        MODERATEDMG(2),
        HEAVYDMG(3);
        private final int mod;
        PilotingMods(int mod){
            this.mod = mod;
        }
    }
    private int id;
    private int gunnery;
    private int pilotSkill;
    private String pilotName;
    private int injuries;
    private final Set<PilotingMods> pilotingMods;
    private boolean conscious;

    private boolean namedPilot; // is the pilot named in a campaign force
    private int experience; // earned SP
    private int gunneryXP;
    private int pilotingXP;
    private int edgePointsXP;
    private int edgeSkillsXP;
    private int edge;
    private final List<EdgeSkill> skills = new ArrayList<>();
    public Pilot(Cursor cur){
        super();
        id           = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_ID);
        pilotName    = OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_STATE_PILOT_NAME);
        gunnery      = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_GUNNERY);
        pilotSkill   = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_PILOTING);
        injuries     = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_INJURIES);
        conscious    = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_CONSCIOUS) == 1;
        pilotingMods = new ArraySet<>();
        namedPilot = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_PILOT_NAMED) == 1;
        experience = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_PILOT_EXPERIENCE); // earned SP

        edge = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_PILOT_EDGE_TOKENS);
        String data = OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_PILOT_EDGE_SKILLS);
        for (String skill : data.split(":")){
            if (skill.isEmpty())continue;
            skills.add(EdgeSkill.valueOf(skill));
        }
        gunneryXP = gunneryCost[gunnery];
        pilotingXP = pilotingCost[pilotSkill];
        edgePointsXP = edgeCost[edge];
        edgeSkillsXP = skillsCost[skills.size()];
    }
    public Pilot(String name, int gun, int pilot, int id) {
        super();
        this.id = id;
        gunnery = gun;
        pilotName = name;
        pilotSkill = pilot;
        injuries = 0;
        conscious = true;
        pilotingMods = new ArraySet<>();
        namedPilot = false;
        gunneryXP = pilotingXP = edgePointsXP = edgeSkillsXP = 0;
        edge = 1;
        experience = 150; //starting XP
    }
    public Pilot(String data) {
        super();
        pilotingMods = new ArraySet<>();
        String[] parts = data.split(",");
        if (parts[0].equals("PILOT")) {
            gunnery = Integer.parseInt(parts[1]);
            pilotSkill = Integer.parseInt(parts[2]);
            pilotName = parts[3];
            injuries = Integer.parseInt(parts[4]);
            conscious = Boolean.parseBoolean(parts[5]);
            namedPilot = Boolean.getBoolean(parts[6]);
            edge = Integer.parseInt(parts[7]);
            experience = Integer.parseInt(parts[8]); //starting XP
            gunneryXP = gunneryCost[gunnery];
            pilotingXP = pilotingCost[pilotSkill];
            edgePointsXP = edgeCost[edge];
            edgeSkillsXP = skillsCost[skills.size()];
        } else {
            gunnery = 4;
            pilotName = "Default";
            pilotSkill = 5;
            injuries = 0;
            conscious = true;
            namedPilot = false;
            gunneryXP = pilotingXP = edgePointsXP = edgeSkillsXP = 0;
            edge = 1;
            experience = 150; //starting XP
        }
    }
    public int getGunnery() {
        return gunnery;
    }
    public String getGunneryDisplay() {
        int skillMod;
        if (Game.current !=null && Game.current.isPilotDamage()){
            skillMod = Math.floorDiv(injuries,2);
        } else {
            skillMod = injuries;
        }
        String gun = Integer.toString(gunnery);
        if (skillMod > 0) gun += " + " + skillMod;
        //    if (Game.current != null) gun += Game.current.getGunneryInjuryMod(injuries);
        return gun;
    }

    public void setGunnery(int gunnery) {
        this.gunnery = gunnery;
    }

    public int getPilotSkill() {
        return pilotSkill;
    }
    public String getPilotSkillDisplay() {
        int skillMod;
        if (Game.current !=null && Game.current.isPilotDamage()){
            skillMod = Math.floorDiv(injuries+1,2);
        } else {
            skillMod = injuries;
        }
        skillMod += getPilotingMods();
        String pil = Integer.toString(pilotSkill);
        if (skillMod > 0) pil += " + " + skillMod;
        //    if (Game.current != null) pil += Game.current.getPilotingInjuryMod(injuries);
        return pil;
    }

    public void setPilotSkill(int pilotSkill) {
        this.pilotSkill = pilotSkill;
    }

    public String getPilotName() {
        return pilotName;
    }

    public void setPilotName(String pilotName) {
        this.pilotName = pilotName;
    }
    public void addInjury(int no) {
        injuries += no;
    }
    public int getInjuries() {
        return injuries;
    }
    public int getPilotSkillMod(){
        int mod = injuries;
        if (Game.current!=null && Game.current.isPilotDamage()){
            mod = Math.floorDiv(mod+1,2);
        }
        mod+=getPilotingMods();
        return mod;
    }

    public int getGunnerySkillMod() {
        int mod = injuries;
        if (Game.current != null && Game.current.isPilotDamage()) {
            mod = Math.floorDiv(mod, 2);
        }
        if (pilotingMods.contains(PilotingMods.COMMANDER))
            mod++; //+1 for commander hit
        return mod;
    }

    public void setInjuries(int injuries) {
        this.injuries = injuries;
    }

    public void setConscious(boolean conscious) {
        this.conscious = conscious;
    }

    public boolean isConscious() {
        return conscious;
    }

    public int getPilotingMods() {
        int mods = 0;
        for (PilotingMods pilot : pilotingMods){
            mods += pilot.mod;
        }
        return mods;
    }

    public Set<PilotingMods> getPilotingModsSet() {
        return pilotingMods;
    }

    public boolean setPilotingMods(PilotingMods pilotingMod) {
        if (this.pilotingMods.contains(pilotingMod))
            return false;
        else
            this.pilotingMods.add(pilotingMod);
        return true;
    }
    public void setContentValues(ContentValues cv) {
        cv.put(OVDatabaseForce.COLUMN_STATE_PILOT_NAME, pilotName);
        cv.put(OVDatabaseForce.COLUMN_STATE_GUNNERY, gunnery);
        cv.put(OVDatabaseForce.COLUMN_STATE_PILOTING, pilotSkill);
        cv.put(OVDatabaseForce.COLUMN_STATE_INJURIES, injuries);
        cv.put(OVDatabaseForce.COLUMN_STATE_CONSCIOUS, conscious ? 1 : 0);

        cv.put(OVDatabaseForce.COLUMN_PILOT_NAMED, namedPilot ? 1 : 0);
        cv.put(OVDatabaseForce.COLUMN_PILOT_EXPERIENCE, experience);
        cv.put(OVDatabaseForce.COLUMN_PILOT_EDGE_TOKENS, edge);
        StringBuilder skillList = new StringBuilder();
        for (EdgeSkill skill: skills){
            if (skillList.length()>0)
                skillList.append(":");
            skillList.append(skill.toString());
        }
        cv.put(OVDatabaseForce.COLUMN_PILOT_EDGE_SKILLS, skillList.toString());
    }

    public EdgeSkill hasEdge(UnitTurn turn, EdgeTrigger trigger, Turn.Phase phase, boolean rear){
        EdgeSkill ret = EdgeSkill.NONE;
        if (edge>0) { // can only use a skill if edge points remain
            switch (trigger) {
                case REROLL_HIT:
                    if (phase == Turn.Phase.PHYSICAL && skills.contains(EdgeSkill.MELEE_SPECIALIST))
                        ret = EdgeSkill.MELEE_SPECIALIST;
                    else if (phase == Turn.Phase.SHOOT && turn.getMove() == UnitMove.MoveType.STILL
                            && skills.contains(EdgeSkill.PATIENT))
                        ret = EdgeSkill.PATIENT;
                    break;
                case REROLL_LOCATION:
                    if (skills.contains(EdgeSkill.ASSASSIN) && rear)
                        ret = EdgeSkill.ASSASSIN;
                    break;
                case BOOST_SHOT:
                    ret = EdgeSkill.SHOOTING_BUMP;
                    break;
                case REROLL_CRIT:
                    // This is a default skill which is always available to named pilots
                    ret = EdgeSkill.CRITICAL;
                    break;
                case MOVE_CHECK:
                    // for move_check the rear flag is true if current tmm is more than 2
                    if (skills.contains(EdgeSkill.NIMBLE))
                        if (!rear || edge>1)ret = EdgeSkill.NIMBLE;
                    break;
            }
        }
        return ret;
    }
    public int spendEdge(EdgeSkill skill, int param){
        // spend a point of edge using the skill provided
        // param is sometimes needed where the number of edge points required for the skill is
        // dependent on a separate value. e.g. Nimble with starting tmm of 3+
        int spent = 1;
        this.edge--;
        switch (skill){
            case NIMBLE:
                if (param>2){
                    edge--; //second edge required for higher TMM
                    spent++;
                }
                break;
        }
        return spent;
    }
    public int reverseEdge(EdgeSkill skill, int param){
        // spend a point of edge using the skill provided
        // param is sometimes needed where the number of edge points required for the skill is
        // dependent on a separate value. e.g. Nimble with starting tmm of 3+
        int spent = 1;
        this.edge++;
        switch (skill){
            case NIMBLE:
                if (param>3){
                    edge++; //second edge required for higher TMM
                    spent++;
                }
                break;
        }
        return spent;
    }

    public boolean isNamedPilot() {
        return namedPilot;
    }
    public void setNamedPilot(boolean namedPilot) {
        this.namedPilot = namedPilot;
    }

    public int getExperience() {
        return experience;
    }
    public int getUnusedExperience() {
        return experience - gunneryXP - pilotingXP - edgePointsXP - edgeSkillsXP;
    }
    public int getGunneryXP() {
        return gunneryXP;
    }

    public int getPilotingXP() {
        return pilotingXP;
    }

    public int getEdgePointsXP() {
        return edgePointsXP;
    }

    public int getEdgeSkillsXP() {
        return edgeSkillsXP;
    }

    public int getEdge() {
        return edge;
    }
    public boolean adjustEdgeTokens(int adjust){
        if (adjust + edge < edgeCost.length){
            int cost = edgeCost[adjust + edge] - edgeCost[edge] ;
            if (cost <= getUnusedExperience()){
                edge += adjust;
                edgePointsXP = edgeCost[edge];
                return true;
            }
        }
        return false;
    }
    public boolean addEdgeSkill(){
        if (skills.size() + 1 < skillsCost.length){
            // throw dialog to add a new skill
            return (skillsCost[skills.size() + 1] - edgeSkillsXP) <= getUnusedExperience();
        }
        return false;
    }
    public void addSkill(EdgeSkill skill){
        skills.add(skill);
        edgeSkillsXP = skillsCost[skills.size()];
    }
    public boolean adjustGunnery(){
        boolean ret = adjustSkill(gunnery,gunneryCost);
        if (ret){
            gunnery--;
            gunneryXP = gunneryCost[gunnery];
        }
        return ret;
    }
    public boolean adjustPiloting(){
        boolean ret = adjustSkill(pilotSkill,pilotingCost);
        if (ret){
            pilotSkill--;
            pilotingXP = pilotingCost[pilotSkill];
        }
        return ret;
    }
    private boolean adjustSkill(int skill, Integer[] rate){
        if (skill - 1 >= 0){
            int cost = rate[skill - 1] - rate[skill] ;
            return cost <= getUnusedExperience();
        }
        return false;
    }
    public List<EdgeSkill> getSkills() {
        return skills;
    }

    public int getId() {
        return id;
    }
    public int getTokenCost(){
        if (edge+1>=edgeCost.length) return 100000; // just passing back an impossible value as no further steps exist
        return edgeCost[edge+1] - edgeCost[edge];
    }
    public int getSkillCost(){
        if (skills.size()+1>=skillsCost.length) return 100000; // just passing back an impossible value as no further steps exist
        return skillsCost[skills.size()+1] - skillsCost[skills.size()];
    }
    public int getGunneryCost(){
        if (gunnery-1<0) return 100000; // just passing back an impossible value as no further steps exist
        return gunneryCost[gunnery-1] - gunneryCost[gunnery];
    }
    public int getPilotingCost(){
        if (pilotSkill-1<0) return 100000; // just passing back an impossible value as no further steps exist
        return pilotingCost[pilotSkill-1] - pilotingCost[pilotSkill];
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }
}
