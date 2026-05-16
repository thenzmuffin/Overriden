package com.total.overide;

import android.database.Cursor;

import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.IUnitHeader;

import java.util.Locale;

public class OVHeader implements IUnitHeader {
    public enum EngineType {
        STANDARD,
        ISXL,
        CLANXL,
        ISXXL,
        CLANXXL,
        ICE;
    }
    private UnitType unitType;
    public enum UnitRole {
        AMBUSHER,
        BRAWLER,
        JUGGERNAUT,
        MISSILEBOAT,
        SCOUT,
        SKIRMISHER,
        STRIKER,
        SNIPER,
        SPOTTER,
        IF_MISSILEBOAT;
    }
    private ForceList.ForceType type;
    private int designKey;
    private String name;
    private String variant;
    private int mass;
    private EngineType engine;
    private int walk;
    private int jump;
    private int heatSinks;
    private boolean doubleHeatSinks = false;
    private int twHeatSinks;
    private UnitRole role = null;
    private boolean tsm = false; // mech has triple strength myomer
    private OVSegment.ArmourType armourType = OVSegment.ArmourType.STANDARD;
    public OVHeader(){
        super();
        unitType = UnitType.MECH;
    }
    public OVHeader(Cursor cur){
        super();
        setKey(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_ID));
        setName(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_HEADER_NAME));
        setUnitType(IUnitHeader.UnitType.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_HEADER_UNIT_TYPE)));
        setWalk(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_WALK));
        setVariant(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_HEADER_VARIANT));
        setJump(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_JUMP));
        setMass(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_MASS));
        setEngine(OVHeader.EngineType.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_HEADER_ENGINE)));
        setHeatSinks(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_SINKS));
        setDoubleHeatSinks(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_SINKS_ARE_DOUBLE)==1);
        setTwHeatSinks(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_TWSINKS));
        setRole(OVHeader.UnitRole.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_HEADER_ROLE)));
        setType(ForceList.ForceType.valueOf(OVDatabaseForce.getCursorString(cur,OVDatabaseForce.COLUMN_HEADER_TYPE)));
        setTsm(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_TSM)==1);
    }
    public OVHeader(String data){
        super();
        String[] parts = data.split(",");
        designKey  = -1;
        name       = parts[0];
        variant    = parts[1];
        mass       = Integer.parseInt(parts[2]);
        engine     = EngineType.valueOf(parts[3]);
        walk       = Integer.parseInt(parts[4]);;
        jump       = Integer.parseInt(parts[5]);;
        heatSinks  = Integer.parseInt(parts[6]);;
        role       = UnitRole.valueOf(parts[7]);
        armourType = OVSegment.ArmourType.valueOf(parts[8]);
        type       = ForceList.ForceType.valueOf(parts[9]);
        tsm        = Boolean.parseBoolean(parts[10]);
        unitType   = UnitType.valueOf(parts[11]);
    }

    @Override
    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public void setUnitType(UnitType type) {
        unitType = type;
    }

    public int getKey() {
        return designKey;
    }

    public void setKey(int designKey) {
        this.designKey = designKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeatSinks() {
        int sinking;
        if (Game.current == null || !Game.current.isSmartHeat()){
            sinking = heatSinks;
        } else {
            sinking = twHeatSinks;
            if (doubleHeatSinks)sinking *= 2;
        }
        return sinking;
    }

    public void setHeatSinks(int heatSinks) {
        this.heatSinks = heatSinks;
    }

    public boolean isDoubleHeatSinks() {
        return doubleHeatSinks;
    }

    public void setDoubleHeatSinks(boolean doubleHeatSinks) {
        this.doubleHeatSinks = doubleHeatSinks;
    }

    public int getTwHeatSinks() {
        return twHeatSinks;
    }

    public void setTwHeatSinks(int twHeatSinks) {
        this.twHeatSinks = twHeatSinks;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public OVSegment.ArmourType getArmourType() {
        return armourType;
    }

    public void setArmourType(OVSegment.ArmourType armourType) {
        this.armourType = armourType;
    }

    public int getWalk() {
        return walk;
    }

    @Override
    public int getRun() {
        int run = (int)Math.ceil(walk * 1.5);//TODO: armour type can affect this!!!!
        return run;
    }

    public void setWalk(int walk) {
        this.walk = walk;
    }

    public int getJump() {
        return jump;
    }

    public void setJump(int jump) {
        this.jump = jump;
    }
    public boolean canJump(){
        return jump > 0;
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public EngineType getEngine() {
        return engine;
    }

    public void setEngine(EngineType engine) {
        this.engine = engine;
    }

    public UnitRole getRole() {
        return role;
    }

    public void setRole(UnitRole role) {
        this.role = role;
    }

    public void parseRole(String name){
        switch (name.toLowerCase(Locale.ENGLISH)){
            case "ambusher":
                role = UnitRole.AMBUSHER;
                break;
            case "striker":
                role = UnitRole.STRIKER;
                break;
            case "sniper":
                role = UnitRole.SNIPER;
                break;
            case "scout":
                role = UnitRole.SCOUT;
                break;
            case "juggernaut":
                role = UnitRole.JUGGERNAUT;
                break;
            case "missile boat":
                role = UnitRole.MISSILEBOAT;
                break;
            case "skirmisher":
                role = UnitRole.SKIRMISHER;
                break;
            case "brawler":
                role = UnitRole.BRAWLER;
                break;
        }
    }
    public String getStreamValue(){
        String stream = "HEADER:" + name + "," + variant;
        stream += "," + mass;
        stream += "," + engine.toString();
        stream += "," + walk;
        stream += "," + jump;
        stream += "," + heatSinks;
        stream += "," + role.toString();
        stream += "," + armourType.toString();
        stream += "," + type.toString();
        stream += "," + tsm;
        stream += "," + unitType.toString() + '\n';
        return stream;
    }

    public ForceList.ForceType getType() {
        return type;
    }

    public void setType(ForceList.ForceType type) {
        this.type = type;
    }

    public boolean isTsm() {
        return tsm;
    }

    public void setTsm(boolean tsm) {
        this.tsm = tsm;
    }
}
