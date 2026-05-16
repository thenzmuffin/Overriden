package com.total.scenario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.total.artificial.AiForceList;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVDatabaseUnit;
import com.total.overiden.ForceList;
import com.total.overiden.Scenario;
import com.total.overiden.ScenarioWaypoint;
import com.total.overiden.ScenarioWaypointTrigger;

import java.util.ArrayList;
import java.util.List;

public class ScenarioDB extends SQLiteOpenHelper {
    public static class ScenCat{
        public String name;
        public long key;
        public ScenCat(String name, long key){
            this.name = name;
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
    private static final String DATABASE_NAME = "Scenario.db";
    private static final int DATABASE_VERSION = 16;

    public static final String COLUMN_ID = "_id";

    private static final String TABLE_NAME_TERRAIN = "terrain";
    private static final String COLUMN_TERRAIN_NAME = "terrain_name";
    private static final String COLUMN_TERRAIN_ID = "terrain_id";
    private static final String COLUMN_TERRAIN_SHAPE = "terrain_shape";
    private static final String TABLE_NAME_MAP = "map";
    private static final String COLUMN_MAP_ID = "map_id";
    private static final String COLUMN_MAP_NAME = "map_name";
    private static final String COLUMN_MAP_WIDTH = "map_width";
    private static final String COLUMN_MAP_LENGTH = "map_length";
    private static final String TABLE_NAME_SCEN_TERRAIN = "scenario_terrain";
    private static final String COLUMN_SCENTERR_TYPE = "scenterr_type";
    private static final String COLUMN_SCENTERR_ANGLE = "scenterr_angle";
    private static final String COLUMN_SCENTERR_X = "scenterr_x";
    private static final String COLUMN_SCENTERR_Y = "scenterr_y";
    private static final String TABLE_NAME_SCENARIO = "scenario";
    public static final String COLUMN_SCENARIO_KEY = "scenario_key";
    public static final String COLUMN_SCENARIO_LINK_ID = "scenario_link";
    public static final String COLUMN_SCENARIO_NAME = "scenario_name";
    public static final String COLUMN_SCENARIO_FORCE_ONE_NAME = "scenario_name_one";
    public static final String COLUMN_SCENARIO_FORCE_TWO_NAME = "scenario_name_two";
    public static final String COLUMN_SCENARIO_AI_GAME = "scenario_ai_game";
    public static final String COLUMN_SCENARIO_AI_COMMANDER = "scenario_ai_commander";
    public static final String COLUMN_SCENARIO_FLAVOUR = "scenario_flavour";
    public static final String COLUMN_SCENARIO_SETUP = "scenario_setup";
    public static final String COLUMN_SCENARIO_ATTACKER = "scenario_attacker";
    public static final String COLUMN_SCENARIO_DEFENDER = "scenario_defender";
    private static final String TABLE_NAME_SCENARIO_UNIT = "scenario_unit";
    public static final String COLUMN_UNIT_ID = "unit_id";
    public static final String COLUMN_UNIT_RESERVE = "unit_reserve";
    public static final String COLUMN_UNIT_NAME = "unit_name";
    public static final String COLUMN_UNIT_VARIANT = "unit_variant";
    public static final String COLUMN_UNIT_GUNNERY = "unit_gunnery";
    public static final String COLUMN_UNIT_PILOTING = "unit_piloting";
    public static final String COLUMN_UNIT_PILOT_NAME = "unit_pilot_name";
    public static final String COLUMN_UNIT_FORCE = "unit_force";
    public static final String COLUMN_UNIT_START_TYPE = "unit_start";
    public static final String TABLE_NAME_SCENARIO_WAYPOINT = "waypoints";
    public static final String TABLE_NAME_SCENARIO_TRIGGER = "trigger_table";
    public static final String COLUMN_TRIGGER_EVENT = "trigger_event";
    public static final String COLUMN_TRIGGER_ACTIVATIONS = "trigger_activations";
    public static final String COLUMN_TRIGGER_CHECK = "trigger_check";
    public static final String COLUMN_TRIGGER_KEY = "trigger_key";
    public static final String COLUMN_WAY_EVENT = "way_event";
    public static final String COLUMN_WAY_TYPE = "way_type";
    public static final String COLUMN_WAY_CHECK = "way_check";
    public static final String COLUMN_WAY_FLAVOUR = "way_flavour";
    public static final String COLUMN_WAY_PARAM_ONE = "way_param_one";
    public static final String COLUMN_WAY_PARAM_TWO = "way_param_two";
    public static final String COLUMN_WAY_PARAM_CHAR = "way_param_char";
    private static final String TABLE_NAME_SCENARIO_INST = "scenario_inst";
    public static final String COLUMN_INST_KEY = "inst_key";
    public static final String COLUMN_INST_WAYPOINTS = "inst_waypoints";
    private final Context context;

    public ScenarioDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //TODO do we really need the context here?
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_TERRAIN + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TERRAIN_NAME + " TEXT, "
                + COLUMN_TERRAIN_SHAPE + " INTEGER, "
                + COLUMN_TERRAIN_ID + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_MAP + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MAP_NAME + " TEXT, "
                + COLUMN_MAP_LENGTH + " INTEGER, "
                + COLUMN_MAP_WIDTH + " INTEGER);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCEN_TERRAIN + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MAP_ID + " INTEGER, "
                + COLUMN_SCENTERR_TYPE + " TEXT, "
                + COLUMN_SCENTERR_ANGLE + " DOUBLE, "
                + COLUMN_SCENTERR_X + " FLOAT, "
                + COLUMN_SCENTERR_Y + " FLOAT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCENARIO + " (" + COLUMN_SCENARIO_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SCENARIO_LINK_ID + " INTEGER, "
                + COLUMN_SCENARIO_NAME + " TEXT, "
                + COLUMN_SCENARIO_FORCE_ONE_NAME + " TEXT, "
                + COLUMN_SCENARIO_FORCE_TWO_NAME + " TEXT, "
                + COLUMN_SCENARIO_SETUP + " TEXT, "
                + COLUMN_SCENARIO_ATTACKER + " TEXT, "
                + COLUMN_SCENARIO_DEFENDER + " TEXT, "
                + COLUMN_SCENARIO_AI_GAME +  " INTEGER, "
                + COLUMN_SCENARIO_AI_COMMANDER +  " INTEGER, "
                + COLUMN_SCENARIO_FLAVOUR + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCENARIO_UNIT + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_SCENARIO_KEY + " INTEGER, "
                + COLUMN_UNIT_RESERVE + " INTEGER, "
                + COLUMN_UNIT_ID + " INTEGER, "
                + COLUMN_UNIT_NAME + " TEXT, "
                + COLUMN_UNIT_VARIANT + " TEXT, "
                + COLUMN_UNIT_GUNNERY + " INTEGER, "
                + COLUMN_UNIT_PILOTING + " INTEGER, "
                + COLUMN_UNIT_PILOT_NAME + " TEXT, "
                + COLUMN_UNIT_FORCE + " INTEGER, "
                + COLUMN_UNIT_START_TYPE + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_SCENARIO_KEY + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCENARIO_TRIGGER + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_SCENARIO_KEY + " INTEGER, "
                + COLUMN_TRIGGER_EVENT + " TEXT, "
                + COLUMN_TRIGGER_CHECK + " INTEGER, "
                + COLUMN_TRIGGER_ACTIVATIONS + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_SCENARIO_KEY + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCENARIO_WAYPOINT + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_SCENARIO_KEY + " INTEGER, "
                + COLUMN_TRIGGER_KEY + " INTEGER, "
                + COLUMN_WAY_EVENT + " TEXT, "
                + COLUMN_WAY_TYPE + " TEXT, "
                + COLUMN_WAY_CHECK + " INTEGER, "
                + COLUMN_WAY_PARAM_ONE + " INTEGER, "
                + COLUMN_WAY_PARAM_TWO + " INTEGER, "
                + COLUMN_WAY_PARAM_CHAR + " TEXT, "
                + COLUMN_WAY_FLAVOUR + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_SCENARIO_KEY + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SCENARIO_INST + " (" + COLUMN_INST_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SCENARIO_KEY + " INTEGER, "
                + COLUMN_INST_WAYPOINTS + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        if (db == null) db = this.getWritableDatabase();
        drop_table(db, TABLE_NAME_TERRAIN);
        drop_table(db, TABLE_NAME_MAP);
        drop_table(db, TABLE_NAME_SCEN_TERRAIN);
        drop_table(db, TABLE_NAME_SCENARIO);
        drop_table(db, TABLE_NAME_SCENARIO_UNIT);
        drop_table(db, TABLE_NAME_SCENARIO_WAYPOINT);

        onCreate(db);
    }

    private void drop_table(SQLiteDatabase db, String tab_name) {
        String query = "DROP TABLE IF EXISTS " + tab_name;
        db.execSQL(query);
    }

    public void saveScenario(Scenario scenario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        scenario.updateContent(cv);
        if (scenario.getKey()>=0){
            db.update(TABLE_NAME_SCENARIO,cv,COLUMN_ID + " = " + scenario.getKey(),null);
        } else {
            long result = db.insert(TABLE_NAME_SCENARIO, null, cv);
            scenario.setKey(result);
        }
        saveScenarioUnits(db,scenario);
        saveScenarioWaypointTriggers(db,scenario);
        if (scenario.getInstanceKey()>=0){
            // save the instance data too
            saveScenarioInstance(scenario);
        }
    }
    public void saveScenarioInstance(Scenario scenario){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        scenario.updateInstanceContent(cv);
        if (scenario.getInstanceKey()>=0){
            db.update(TABLE_NAME_SCENARIO_INST,cv,COLUMN_INST_KEY + " = " + scenario.getInstanceKey(),null);
        } else {
            long result = db.insert(TABLE_NAME_SCENARIO_INST, null, cv);
            scenario.setInstanceKey(result);
        }
    }

    private void saveScenarioUnits(SQLiteDatabase db, Scenario scenario) {
//        ContentValues cv = new ContentValues();
        saveUnits(db, scenario, scenario.getReserveUnits(),1);
        saveUnits(db, scenario, scenario.getOpForUnits(),0);
    }

    private void saveUnits(SQLiteDatabase db, Scenario scenario, List<Scenario.ReserveUnit> list, int reserve) {
        ContentValues cv = new ContentValues();
        for (Scenario.ReserveUnit unit : list) {
            cv.clear();
            cv.put(COLUMN_UNIT_RESERVE, reserve);
            unit.updateContent(cv);
            cv.put(COLUMN_SCENARIO_KEY, scenario.getKey());
            db.insertWithOnConflict(TABLE_NAME_SCENARIO_UNIT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
    private void saveScenarioWaypointTriggers(SQLiteDatabase db, Scenario scenario){
        ContentValues cv = new ContentValues();

        for (ScenarioWaypointTrigger trigger : scenario.getWaypoints()) {
            cv.clear();
            trigger.updateContent(cv);
            cv.put(COLUMN_SCENARIO_KEY,scenario.getKey());
            db.insertWithOnConflict(TABLE_NAME_SCENARIO_TRIGGER, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            saveScenarioWaypoints(db,scenario.getKey(),trigger);
        }
    }
    private void saveScenarioWaypoints(SQLiteDatabase db, long scenKey,ScenarioWaypointTrigger trigger){
        ContentValues cv = new ContentValues();

        for (ScenarioWaypoint waypoint : trigger.getWaypoints()) {
            cv.clear();
            waypoint.updateContent(cv);
            cv.put(COLUMN_SCENARIO_KEY,scenKey);
            cv.put(COLUMN_TRIGGER_KEY,trigger.getKey());
            db.insertWithOnConflict(TABLE_NAME_SCENARIO_WAYPOINT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public Scenario loadScenario(long key){
        Scenario scen = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_SCENARIO, new String[]{"*"}, COLUMN_SCENARIO_KEY + " = ?", new String[]{Long.toString(key)}, null, null, null);
        if (cur.moveToFirst()){
            scen = new Scenario(cur);
            Cursor unitCur = db.query(TABLE_NAME_SCENARIO_UNIT, new String[]{"*"}, COLUMN_SCENARIO_KEY + " = ?", new String[]{Long.toString(key)}, null, null, null);
            while (unitCur.moveToNext()){
                if (getCursorInt(unitCur,COLUMN_UNIT_RESERVE)==1){
                    scen.getReserveUnits().add(new Scenario.ReserveUnit(unitCur));
                } else {
                    scen.getOpForUnits().add(new Scenario.ReserveUnit(unitCur));
                }
            }
            unitCur.close();
            unitCur = db.query(TABLE_NAME_SCENARIO_TRIGGER, new String[]{"*"}, COLUMN_SCENARIO_KEY + " = ?", new String[]{Long.toString(key)}, null, null, null);
            while (unitCur.moveToNext()){
                ScenarioWaypointTrigger trigger = new ScenarioWaypointTrigger(unitCur);
                scen.getWaypoints().add(trigger);
                getTriggerWaypoints(db,key,trigger);
            }
            unitCur.close();
        }
        cur.close();
        return scen;

    }
    private void getTriggerWaypoints(SQLiteDatabase db,long key,ScenarioWaypointTrigger trigger){
        Cursor unitCur = db.query(TABLE_NAME_SCENARIO_WAYPOINT, new String[]{"*"}, COLUMN_SCENARIO_KEY + " = ? AND " + COLUMN_TRIGGER_KEY + " = ?", new String[]{Long.toString(key),Long.toString(trigger.getKey())}, null, null, null);
        while (unitCur.moveToNext()){
            trigger.getWaypoints().add(new ScenarioWaypoint(unitCur));
        }
        unitCur.close();
    }
    public void addTerrain(Terrain terrain) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_TERRAIN_NAME, terrain.getName());
        cv.put(COLUMN_TERRAIN_SHAPE, terrain.isShape()?1:0);
        cv.put(COLUMN_TERRAIN_ID, terrain.getId());
        long result = db.insert(TABLE_NAME_TERRAIN, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Mech Design DB Insert Failed -Terrain", Toast.LENGTH_SHORT).show();
        }    else terrain.setKey(result);
    }
    public List<Terrain> getCatalog( ){
        List<Terrain> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_TERRAIN, new String[]{"*"}, null, null, null, null, null);
        while (cur.moveToNext()){
            list.add(new Terrain(getCursorInt(cur,COLUMN_ID),
                    getCursorString(cur,COLUMN_TERRAIN_NAME),
                    getCursorString(cur,COLUMN_TERRAIN_ID),
                    getCursorInt(cur,COLUMN_TERRAIN_SHAPE)==1));
        }
        cur.close();
        return list;
    }
    public List<ScenCat> getScenarioCatalog(boolean addBlankEntry){
        List<ScenCat> list = new ArrayList<>();
        if (addBlankEntry){
            list.add(new ScenCat("None", -1));
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_SCENARIO, new String[]{"*"}, null, null, null, null, null);
        while (cur.moveToNext()) {
            list.add(new ScenCat(getCursorString(cur, COLUMN_SCENARIO_NAME), getCursorInt(cur, COLUMN_SCENARIO_KEY)));
        }
        return list;
    }
    public Terrain getTerrain(String id){
        Terrain list = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_TERRAIN, new String[]{"*"},COLUMN_TERRAIN_ID + " = ?"  , new String[]{id}, null, null, null);
        while (cur.moveToNext()){
            list = new Terrain(getCursorInt(cur,COLUMN_ID),
                    getCursorString(cur,COLUMN_TERRAIN_NAME),
                    getCursorString(cur,COLUMN_TERRAIN_ID),
                    getCursorInt(cur,COLUMN_TERRAIN_SHAPE)==1);
        }
        cur.close();
        return list;
    }
    public int getCursorInt(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getInt(index);
    }

    public String getCursorString(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getString(index);
    }

    public float getCursorFloat(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getFloat(index);
    }
    public double getCursorDouble(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getDouble(index);
    }

    public void saveMap(ScenarioMap map){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_MAP_NAME, map.getName());
        cv.put(COLUMN_MAP_LENGTH, map.getLength());
        cv.put(COLUMN_MAP_WIDTH, map.getWidth());
        if (map.getKey()>=0){
            cv.put(COLUMN_ID, map.getKey());
            db.update(TABLE_NAME_MAP,cv,COLUMN_ID + " = " + map.getKey(),null);
        } else {
            long result = db.insert(TABLE_NAME_MAP, null, cv);
            if (result != -1) {
                map.setKey(result);
            }
        }
        saveMapItems(map, db);
    }

    private void saveMapItems(ScenarioMap map, SQLiteDatabase db){
        ContentValues cv = new ContentValues();
        for (ScenarioTerrain terrain : map.getTerrain()){
            cv.clear();
            cv.put(COLUMN_MAP_ID, map.getKey());
            cv.put(COLUMN_SCENTERR_TYPE, terrain.getType().getId());
            cv.put(COLUMN_SCENTERR_X, terrain.getY());
            cv.put(COLUMN_SCENTERR_Y, terrain.getY());
            cv.put(COLUMN_SCENTERR_ANGLE, terrain.getAngle());

            if (terrain.getKey()>=0){
                cv.put(COLUMN_ID, terrain.getKey());
                db.update(TABLE_NAME_SCEN_TERRAIN,cv,COLUMN_ID + " = " + terrain.getKey(),null);
            } else {
                long result = db.insert(TABLE_NAME_SCEN_TERRAIN, null, cv);
                if (result != -1) {
                    terrain.setKey(result);
                }
            }
        }
    }

    public ScenarioMap loadMap(long id){
        ScenarioMap map = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_MAP, new String[]{"*"},COLUMN_ID + " = " + id  , null, null, null, null);
        if (cur.moveToFirst()){
            map = new ScenarioMap(getCursorInt(cur,COLUMN_ID),
                    getCursorInt(cur,COLUMN_MAP_WIDTH),
                    getCursorInt(cur,COLUMN_MAP_LENGTH));
        }
        cur.close();
        if (map!=null)loadMapItems(map,db);
        return map;
    }
    private void loadMapItems(ScenarioMap map, SQLiteDatabase db){
        Cursor cur = db.query(TABLE_NAME_SCEN_TERRAIN, new String[]{"*"},COLUMN_MAP_ID + " = " + map.getKey()  , null, null, null, null);
        while (cur.moveToNext()){
            map.addTerrain(new ScenarioTerrain(getTerrain(getCursorString(cur,COLUMN_SCENTERR_TYPE)),
                                               getCursorDouble(cur,COLUMN_SCENTERR_ANGLE),
                                               getCursorFloat(cur,COLUMN_SCENTERR_X),
                                               getCursorFloat(cur,COLUMN_SCENTERR_Y)));
        }
    }
    public void deleteScenario(long key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_SCENARIO,COLUMN_SCENARIO_KEY + " = " + key,null);
        db.delete(TABLE_NAME_SCENARIO_UNIT,COLUMN_SCENARIO_KEY + " = " + key,null);
        db.delete(TABLE_NAME_SCENARIO_WAYPOINT,COLUMN_SCENARIO_KEY + " = " + key,null);
    }
    public int generateOpForForceList(ScenCat cat, ForceList.ForceType type){
        SQLiteDatabase db = this.getReadableDatabase();
        // create AI forcelist header
        AiForceList list = new AiForceList(type);
        Cursor headerCur = db.query(TABLE_NAME_SCENARIO, new String[]{"*"},COLUMN_SCENARIO_KEY + " = " + cat.key, null, null, null, null);
        if (headerCur.moveToFirst()){
            list.setCommander(getCursorInt(headerCur,COLUMN_SCENARIO_AI_COMMANDER));
            list.setName(cat.name + " : Op For");
        }
        Cursor cur = db.query(TABLE_NAME_SCENARIO_UNIT, new String[]{"*"},COLUMN_SCENARIO_KEY + " = " + cat.key + " AND " + COLUMN_UNIT_RESERVE + " = 0", null, null, null, null);

        try (OVDatabaseForce dbForce = new OVDatabaseForce(context);
             OVDatabaseUnit dbUnit = new OVDatabaseUnit(context)){
            dbForce.addHeader(list);

            while (cur.moveToNext()) {
                // create a force from this selection set
                int key = getCursorInt(cur,COLUMN_UNIT_ID);
                if (key == -1)
                    dbUnit.checkExists(getCursorString(cur,COLUMN_UNIT_NAME),getCursorString(cur,COLUMN_UNIT_VARIANT));
                if (key >= 0){
                    dbForce.addDesignToForce(list,dbUnit.getUnitDesign(key,type));
                } else {
                    // Couldn't find one of the units so throw an exception

                }
            }
        }
        return list.getKey();
    }
}
