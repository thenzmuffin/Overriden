package com.total.overide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.total.artificial.AiCommander;
import com.total.artificial.AiForceList;
import com.total.artificial.AiUnitAnalysis;
import com.total.overiden.ForceCatalogAdapter;
import com.total.overiden.ForceList;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IUnitDesign;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.Pilot;

import java.util.ArrayList;
import java.util.List;

public class OVDatabaseForce  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OverrideForce.db";
    private static final int DATABASE_VERSION = 51;

    public static final String COLUMN_ID = "_id";
    public static final String TABLE_NAME_FORCE = "mech_force";
    public static final String TABLE_NAME_FORCE_ITEM = "mech_force_item";
    public static final String COLUMN_FORCE_NAME = "force_name";
    public static final String COLUMN_FORCE_INUSE = "force_inuse";
    public static final String COLUMN_FORCE_EXTID = "force_extid";
    public static final String COLUMN_FORCE_PARENT = "force_parent";
    public static final String COLUMN_FORCE_EXTSOURCE = "force_devname";
    public static final String COLUMN_FORCE_LISTTYPE = "force_list_type";
    public static final String COLUMN_FORCE_COMMANDER = "force_list_commander";
    public static final String COLUMN_FITEM_UNIT = "item_unit";
    public static final String TABLE_NAME_HEADER = "mech_header";
    public static final String TABLE_NAME_PILOT = "mech_pilot";
    public static final String TABLE_NAME_SEGMENT = "mech_segment";
    public static final String TABLE_NAME_WEAPON = "mech_weapon";
    public static final String TABLE_NAME_TIC = "mech_tic";
    public static final String TABLE_NAME_EQUIPMENT = "mech_equipment";
    public static final String COLUMN_HEADER_NAME = "header_name";
    public static final String COLUMN_HEADER_UNIT_TYPE = "header_unit_type";
    public static final String COLUMN_HEADER_EXTID = "header_extid";
    public static final String COLUMN_HEADER_EXTSOURCE = "header_devname";
    public static final String COLUMN_HEADER_VARIANT = "header_type";
    public static final String COLUMN_HEADER_WALK = "header_walk";
    public static final String COLUMN_HEADER_JUMP = "header_jump";
    public static final String COLUMN_HEADER_MASS = "header_mass";
    public static final String COLUMN_HEADER_ENGINE = "header_engine";
    public static final String COLUMN_HEADER_SINKS = "header_sinks";
    public static final String COLUMN_HEADER_TWSINKS = "header_twsinks";
    public static final String COLUMN_HEADER_SINKS_ARE_DOUBLE = "header_doulbe_sinks";
    public static final String COLUMN_HEADER_ROLE = "header_role";
    public static final String COLUMN_HEADER_TYPE = "header_sheet_type";
    public static final String COLUMN_HEADER_TSM = "header_tsm";
    public static final String COLUMN_WEAPON_ID = "_id_weapon";
    public static final String COLUMN_WEAPON_KEY = "weapon_key";
    public static final String COLUMN_WEAPON_STATE = "weapon_state";
    public static final String COLUMN_WEAPON_MODE = "weapon_mode";
    public static final String COLUMN_WEAPON_LOCATION = "weapon_location";
    //    public static final String COLUMN_WEAPON_TIC_ID = "weapon_tic_id";
    public static final String COLUMN_TIC_ID = "_id_tic";
    public static final String COLUMN_TIC_WEAPON = "tic_wepaon_id";
    public static final String COLUMN_SEG_LOCATION = "seg_location";
    public static final String COLUMN_SEG_ARMOUR = "seg_armour";
    public static final String COLUMN_SEG_ARMOURTYPE = "seg_armourtype";

    public static final String COLUMN_SEG_REAR = "seg_rear";
    public static final String COLUMN_SEG_STRUCTURE = "seg_structure";
    public static final String COLUMN_SEG_ARMOUR_DMG = "seg_armour_dmg";
    public static final String COLUMN_SEG_REAR_DMG = "seg_rear_dmg";
    public static final String COLUMN_SEG_STRUCTURE_DMG = "seg_structure_dmg";
    public static final String COLUMN_SEG_ARMOUR_TURN_DMG = "seg_armour_turn_dmg";
    public static final String COLUMN_SEG_REAR_TURN_DMG = "seg_rear_turn_dmg";
    public static final String COLUMN_SEG_STRUCTURE_TURN_DMG = "seg_structure_turn_dmg";
    public static final String COLUMN_SEG_STABILISER = "seg_stabiliser";
    public static final String COLUMN_SEG_ACTIVE = "seg_active";
    public static final String COLUMN_STATE_PILOT_NAME = "state_pilot_name";
    public static final String COLUMN_STATE_GUNNERY = "state_gunnery";
    public static final String COLUMN_STATE_PILOTING = "state_piloting";
    public static final String COLUMN_STATE_INJURIES = "state_injuries";
    public static final String COLUMN_STATE_CONSCIOUS = "state_conscious";
    public static final String COLUMN_STATE_SHUTDOWN = "state_shutdown";
    public static final String COLUMN_STATE_GYRO = "state_gyro";
    public static final String COLUMN_STATE_ENGINE = "state_engine";
    public static final String COLUMN_STATE_ECMACTIVE = "header_ecmactive";
    public static final String COLUMN_STATE_DESIGN_KEY = "state_design_key";
    public static final String COLUMN_STATE_ACTIVE= "state_active";
    public static final String COLUMN_STATE_PRONE = "header_prone";
    public static final String COLUMN_STATE_HEAT = "state_heat_level";
    public static final String COLUMN_STATE_DAMAGEDSINKS = "state_damaged_sinks";
    public static final String COLUMN_STATE_DAMAGEDJETS = "state_damaged_jets";
    public static final String COLUMN_STATE_FORCEDWITHDRAWAL = "state_forced_withdrawal";
    public static final String COLUMN_STATE_DESTROYEDCRIT = "state_destroyed_crit";
    public static final String COLUMN_STATE_TURRETSTATE = "state_turret_state";
    public static final String COLUMN_STATE_STUNNED = "state_crew_stunned";
    public static final String COLUMN_EQUIP_INST_STATE = "equip_inst_state";
    public static final String COLUMN_EQUIP_INST_SPEC1 = "equip_inst_spec1";
    public static final String COLUMN_PILOT_NAMED = "pilot_named";
    public static final String COLUMN_PILOT_EXPERIENCE = "pilot_experience";
    public static final String COLUMN_PILOT_EDGE_TOKENS = "pilot_edge_tokens";
    public static final String COLUMN_PILOT_EDGE_SKILLS = "pilot_edge_skills";

    public OVDatabaseForce(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_FORCE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FORCE_NAME + " TEXT, "
                + COLUMN_FORCE_EXTID + " INTEGER, "
                + COLUMN_FORCE_EXTSOURCE + " TEXT, "
                + COLUMN_FORCE_LISTTYPE + " TEXT, "
                + COLUMN_FORCE_PARENT + " INTEGER, "
                + COLUMN_FORCE_COMMANDER + " INTEGER, "
                + COLUMN_FORCE_INUSE + " INTEGER);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_FORCE_ITEM + " ("
                + COLUMN_ID + " INTEGER, "
                + COLUMN_FITEM_UNIT + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_FITEM_UNIT + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_HEADER + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HEADER_EXTID + " INTEGER, "
                + COLUMN_HEADER_EXTSOURCE + " TEXT, "
                + COLUMN_HEADER_NAME + " TEXT, "
                + COLUMN_HEADER_UNIT_TYPE + " TEXT, "
                + COLUMN_HEADER_VARIANT + " TEXT, "
                + COLUMN_HEADER_WALK + " INTEGER, "
                + COLUMN_HEADER_JUMP + " INTEGER, "
                + COLUMN_HEADER_MASS + " INTEGER, "
                + COLUMN_HEADER_ENGINE + " TEXT, "
                + COLUMN_HEADER_SINKS + " INTEGER, "
                + COLUMN_HEADER_SINKS_ARE_DOUBLE + " INTEGER, "
                + COLUMN_HEADER_TWSINKS + " INTEGER, "
                + COLUMN_HEADER_ROLE + " TEXT, "
                + COLUMN_HEADER_TYPE + " TEXT, "
                + COLUMN_HEADER_TSM + " INTEGER, "
                + COLUMN_STATE_SHUTDOWN + " INTEGER, "
                + COLUMN_STATE_GYRO + " INTEGER, "
                + COLUMN_STATE_ENGINE + " INTEGER, "
                + COLUMN_STATE_ECMACTIVE + " INTEGER, "
                + COLUMN_STATE_PRONE  + " INTEGER, "
                + COLUMN_STATE_HEAT   + " INTEGER, "
                + COLUMN_STATE_DAMAGEDSINKS     + " INTEGER, "
                + COLUMN_STATE_DAMAGEDJETS     + " INTEGER, "
                + COLUMN_STATE_FORCEDWITHDRAWAL + " INTEGER, "
                + COLUMN_STATE_DESTROYEDCRIT + " INTEGER, "
                + COLUMN_STATE_TURRETSTATE + " TEXT, "
                + COLUMN_STATE_STUNNED + " INTEGER, "
                + COLUMN_STATE_DESIGN_KEY + " INTEGER, "
                + COLUMN_STATE_ACTIVE + " INTEGER);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_PILOT + " (" + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_STATE_PILOT_NAME + " TEXT, "
                + COLUMN_STATE_GUNNERY + " INTEGER, "
                + COLUMN_STATE_PILOTING + " INTEGER, "
                + COLUMN_STATE_INJURIES + " INTEGER, "
                + COLUMN_STATE_CONSCIOUS + " INTEGER, "
                + COLUMN_PILOT_NAMED + " INTEGER, "
                + COLUMN_PILOT_EXPERIENCE + " INTEGER, "
                + COLUMN_PILOT_EDGE_TOKENS + " INTEGER, "
                + COLUMN_PILOT_EDGE_SKILLS + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_WEAPON + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_WEAPON_ID + " INTEGER, "
                + COLUMN_WEAPON_KEY + " INTEGER, "
                + COLUMN_WEAPON_LOCATION + " TEXT, "
                + COLUMN_WEAPON_STATE + " INTEGER, "
                + COLUMN_WEAPON_MODE + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_WEAPON_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_TIC + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_TIC_ID + " INTEGER, "
                + COLUMN_TIC_WEAPON + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_TIC_ID + ", " + COLUMN_TIC_WEAPON + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_EQUIPMENT + " (" + COLUMN_ID + " INTEGER, "
                + OVDatabaseUnit.COLUMN_EQUIP_ID + " INTEGER, "
                + OVDatabaseUnit.COLUMN_EQUIP_NAME + " TEXT, "
                + OVDatabaseUnit.COLUMN_EQUIP_LOCATION + " TEXT, "
                + OVDatabaseUnit.COLUMN_EQUIP_SLOTS + " INTEGER, "
                + OVDatabaseUnit.COLUMN_EQUIP_TYPE + " TEXT, "
                + OVDatabaseUnit.COLUMN_EQUIP_LINK + " INTEGER, "
                + OVDatabaseUnit.COLUMN_EQUIP_SPEC1 + " INTEGER, "
                + OVDatabaseUnit.COLUMN_EQUIP_SPEC2 + " TEXT, "
                + COLUMN_EQUIP_INST_STATE + " INTEGER, "
                + COLUMN_EQUIP_INST_SPEC1 + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + OVDatabaseUnit.COLUMN_EQUIP_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SEGMENT + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_SEG_LOCATION + " TEXT, "
                + COLUMN_SEG_ARMOURTYPE + " TEXT, "
                + COLUMN_SEG_ARMOUR + " INTEGER, "
                + COLUMN_SEG_REAR + " INTEGER, "
                + COLUMN_SEG_STRUCTURE + " INTEGER, "
                + COLUMN_SEG_ARMOUR_TURN_DMG + " INTEGER, "
                + COLUMN_SEG_REAR_TURN_DMG + " INTEGER, "
                + COLUMN_SEG_STRUCTURE_TURN_DMG + " INTEGER, "
                + COLUMN_SEG_ARMOUR_DMG + " INTEGER, "
                + COLUMN_SEG_REAR_DMG + " INTEGER, "
                + COLUMN_SEG_STRUCTURE_DMG + " INTEGER, "
                + COLUMN_SEG_STABILISER + " INTEGER, "
                + COLUMN_SEG_ACTIVE +  " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_SEG_LOCATION + "));";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        SQLiteDatabase dbLocal = db;
        if (dbLocal == null) dbLocal = this.getWritableDatabase();
        drop_table(dbLocal, TABLE_NAME_FORCE);
        drop_table(dbLocal, TABLE_NAME_FORCE_ITEM);
        drop_table(dbLocal, TABLE_NAME_HEADER);
        drop_table(dbLocal, TABLE_NAME_PILOT);
        drop_table(dbLocal, TABLE_NAME_WEAPON);
        drop_table(dbLocal, TABLE_NAME_EQUIPMENT);
        drop_table(dbLocal, TABLE_NAME_SEGMENT);
        drop_table(dbLocal, TABLE_NAME_TIC);
        onCreate(dbLocal);
        if (db == null)dbLocal.close();
    }
    private void drop_table(SQLiteDatabase db, String tab_name) {
        String query = "DROP TABLE IF EXISTS " + tab_name;
        db.execSQL(query);
    }

    public void addHeader(ForceList force) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FORCE_NAME, force.getName());
        cv.put(COLUMN_FORCE_EXTID, force.getExternalKey());
        cv.put(COLUMN_FORCE_EXTSOURCE, force.getExternalSource());
        cv.put(COLUMN_FORCE_INUSE, force.isInUse()?1:0);
        cv.put(COLUMN_FORCE_PARENT,force.getParentKey());
        cv.put(COLUMN_FORCE_LISTTYPE, force.getType().toString());
        if (force instanceof AiForceList) {
            AiCommander comm = ((AiForceList) force).getCommander();
            if (comm!=null)cv.put(COLUMN_FORCE_COMMANDER, comm.getKey());
        }
        if (force.getKey() >= 0){
            cv.put(COLUMN_ID,force.getKey());
            db.update(TABLE_NAME_FORCE,cv,COLUMN_ID + " = " + force.getKey(),null);
        } else {
            long rowid = db.insert(TABLE_NAME_FORCE, null, cv);
            force.setKey((int) rowid);
        }
    }

    public void updateItems(ForceList force){
        if (!getHeader(force)){
            addHeader(force);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete(TABLE_NAME_FORCE_ITEM,COLUMN_ID + " = " + force.getKey(),null);
        cv.put(COLUMN_ID,force.getKey());
        for (int i = 0;i < force.getCount();i++){
            cv.put(COLUMN_FITEM_UNIT,force.getUnit(i).getKey());
            db.insert(TABLE_NAME_FORCE_ITEM,null,cv);
        }
//        db.close();
    }
    public void updateAllUnits(ForceList force){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
        for (int i = 0;i < force.getCount();i++){
            updateUnitData( force.getUnit(i));
        }

    }

    public ForceList getList(int key, boolean forAiGame){
        ForceList fl = new ForceList(ForceList.ForceType.OV);
        fl.setKey(key);
        getHeader(fl);
        getUnits(fl, forAiGame);
        getSubLists(fl, forAiGame);
        return fl;
    }

    public AiForceList getAiList(int key){
        // even though this is an AiList we shouldn't use the forAiGame flag since the
        // underlying forcelist constructor will take care of the wrapper
        AiForceList fl = new AiForceList(ForceList.ForceType.OV);
        fl.setKey(key);
        getHeader(fl);
        getUnits(fl, false);
        getSubLists(fl, false);
        fl.configurePilots();
        return fl;
    }
    private void getSubLists(ForceList superList, boolean forAiGame){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_FORCE, new String[]{"*"}, COLUMN_FORCE_PARENT + " = " + superList.getKey(), null, null, null, null);
        while (cur.moveToNext()){
            ForceList fl = new ForceList(superList.getType());
            fl.setKey(getCursorInt(cur,COLUMN_ID));
            fl.setName(getCursorString(cur,COLUMN_FORCE_NAME));
            fl.setExternalKey(getCursorInt(cur,COLUMN_FORCE_EXTID));
            fl.setExternalSource(getCursorString(cur,COLUMN_FORCE_EXTSOURCE));
            fl.setInUse(getCursorInt(cur,COLUMN_FORCE_INUSE) == 1);
            fl.setParentKey(getCursorInt(cur,COLUMN_FORCE_PARENT));
            fl.setType(ForceList.ForceType.valueOf(getCursorString(cur,COLUMN_FORCE_LISTTYPE)));
            getUnits(fl, forAiGame);
            getSubLists(fl, forAiGame);
            superList.addSubList(fl);
        }
        cur.close();
    }
    public boolean getHeader(ForceList fl){
        boolean ret = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_FORCE, new String[]{"*"}, COLUMN_ID + " = " + fl.getKey(), null, null, null, null);
        if (cur.getCount() > 0){
            cur.moveToFirst();
            fl.setName(getCursorString(cur,COLUMN_FORCE_NAME));
            fl.setExternalKey(getCursorInt(cur,COLUMN_FORCE_EXTID));
            fl.setExternalSource(getCursorString(cur,COLUMN_FORCE_EXTSOURCE));
            fl.setInUse(getCursorInt(cur,COLUMN_FORCE_INUSE) == 1);
            fl.setParentKey(getCursorInt(cur,COLUMN_FORCE_PARENT));
            fl.setType(ForceList.ForceType.valueOf(getCursorString(cur,COLUMN_FORCE_LISTTYPE)));
            if (fl instanceof AiForceList)
                ((AiForceList) fl).setCommander(getCursorInt(cur,COLUMN_FORCE_COMMANDER));
            ret = true;
        }
        cur.close();
//        db.close();
        return ret;
    }
    public void getUnits(ForceList fl, boolean forAiGame){
//        OVDatabaseUnit dbUnit = new OVDatabaseUnit(ct);
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_FORCE_ITEM, new String[]{"*"}, COLUMN_ID + " = " + fl.getKey(), null, null, null, null);
        while (cur.moveToNext()) {
            IUnitData data = getUnitData(getCursorInt(cur, COLUMN_FITEM_UNIT), forAiGame);
            if (data != null) {
                // if we are playing an AI based game then the player force should use the unitanalysis wrapper
                IUnitData ret;
                if (forAiGame) ret = new AiUnitAnalysis(data);
                else ret = data;
                fl.addUnit(ret);
            }

        }
        cur.close();
//        db.close();

    }

    public List<ForceCatalogAdapter.CatalogEntry> getCatalog(boolean includeInUse, ForceList.ForceType fType){
        List<ForceCatalogAdapter.CatalogEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // dynamically build the where clause
        String selection = COLUMN_FORCE_PARENT + " = -1";
        if (!includeInUse){
            selection += " AND ";
            selection += COLUMN_FORCE_INUSE + " = 0";
        }
        if (fType != null) {
            selection += " AND ";
            selection += COLUMN_FORCE_LISTTYPE + " = '" + fType.name() + "'";
        }
        Cursor cur = db.query(TABLE_NAME_FORCE, new String[]{"*"}, selection, null, null, null, null);
        while (cur.moveToNext()){
            String external = "";

            if (getCursorInt(cur, COLUMN_FORCE_EXTID) >= 0)
                external = "(From device " + getCursorString(cur, COLUMN_FORCE_EXTSOURCE) + ")";
            list.add(new ForceCatalogAdapter.CatalogEntry(getCursorInt(cur, COLUMN_ID),
                    getCursorString(cur, COLUMN_FORCE_NAME) + external,
                    getCursorInt(cur, COLUMN_FORCE_INUSE) == 1, ForceList.ForceType.valueOf(getCursorString(cur, COLUMN_FORCE_LISTTYPE))));
        }
        cur.close();
        return list;
    }

    public static int getCursorInt(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getInt(index);
    }

    public static String getCursorString(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getString(index);
    }

    public void createExternalForce(ForceList force) {
        // first try and find the force in the DB in case it already exists and set the local key
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.query(TABLE_NAME_FORCE, new String[]{"*"}, COLUMN_FORCE_EXTID + " = " + force.getExternalKey() + " AND "
                                                                   + COLUMN_FORCE_EXTSOURCE + " = ?", new String[] {force.getExternalSource()}, null, null, null);
        if (cur.moveToFirst()){
            // set the existing key and this should cause an update rather than an add
            force.setKey(getCursorInt(cur,COLUMN_ID));
            // We are refreshing the list, we should delete all the units
        }
        cur.close();
        addHeader(force);

        // add all the units in the force into the database
        for (IUnitData unit : force.getAllUnits()){
            // does the unit already exist?
            Cursor unitCur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_EXTID + " = " + unit.getState().getExternalID() + " AND "
                    + COLUMN_HEADER_EXTSOURCE + " = ?", new String[] {unit.getState().getExternalSource()}, null, null, null);
            if (unitCur.moveToFirst()){
                unit.getState().setKey(getCursorInt(unitCur,COLUMN_ID));
            }
            unitCur.close();
            addUnit(unit);
        }
        // update the item list for the force
        updateItems(force);
//        db.close();
    }
    public long addDesignToForce(ForceList force, OVUnitDesign design){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        //create a new unit instance
        long key = addUnit(design);
        // add the unit to the supplied force list
        cv.put(COLUMN_ID,force.getKey());
        cv.put(COLUMN_FITEM_UNIT, key);
        db.insert(TABLE_NAME_FORCE_ITEM, null, cv);
//        db.close();
        return key;
    }
//    public void addGroupToForce(ForceList superList, ForceList subList){
//
//    }
    public long addUnit(IUnitDesign design) {
        /*
         * Take the supplied unit design and create a new entry in the force database for it
         */
        OVState state = null;
        if (design instanceof IUnitData) state = ((IUnitData)design).getState();
        long key = addUnitHeader(design.getHeader(),state);
        for (OVSegment ovSegment : design.getSegments()) {
            addSegment(ovSegment, key);
        }
        for (IEquipment equip : design.getEquipment()) {
            if (equip instanceof OVWeaponInstance) {
                addWeapon((OVWeaponInstance) equip, key);
            } else {
                addEquipment(equip, key);
            }
        }
        for (IWeapon tic : design.getWeapons()) {
            if (tic instanceof OVTic)
                addTic((OVTic) tic, key);
        }
        return key;
    }
    public long addUnitHeader(IUnitHeader mech, OVState state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_HEADER_NAME, mech.getName());
        cv.put(COLUMN_HEADER_UNIT_TYPE, mech.getUnitType().toString());
        cv.put(COLUMN_HEADER_VARIANT, mech.getVariant());
        cv.put(COLUMN_HEADER_WALK, mech.getWalk());
        cv.put(COLUMN_HEADER_JUMP, mech.getJump());
        cv.put(COLUMN_HEADER_MASS, mech.getMass());
        cv.put(COLUMN_HEADER_ENGINE, "" + mech.getEngine());
        cv.put(COLUMN_HEADER_SINKS, mech.getHeatSinks());
        cv.put(COLUMN_HEADER_TWSINKS, mech.getTwHeatSinks());
        cv.put(COLUMN_HEADER_SINKS_ARE_DOUBLE, mech.isDoubleHeatSinks());
        cv.put(COLUMN_HEADER_ROLE, mech.getRole().toString());
        cv.put(COLUMN_HEADER_TYPE, mech.getType().toString());
        cv.put(COLUMN_HEADER_TSM, mech.isTsm()?1:0);
        if (state != null) {
            cv.put(COLUMN_HEADER_EXTID, state.getExternalID());
            cv.put(COLUMN_HEADER_EXTSOURCE, state.getExternalSource());
            state.setContentValues(cv);
        } else {
            cv.put(COLUMN_HEADER_EXTID, -1);
            cv.put(COLUMN_HEADER_EXTSOURCE, "");
            cv.put(COLUMN_STATE_SHUTDOWN, 0);
            cv.put(COLUMN_STATE_GYRO, 0);
            cv.put(COLUMN_STATE_ENGINE, 0);
            cv.put(COLUMN_STATE_ECMACTIVE, 0);
            cv.put(COLUMN_STATE_HEAT, 0);
            cv.put(COLUMN_STATE_DAMAGEDSINKS,0);
            cv.put(COLUMN_STATE_DAMAGEDJETS,0);
            cv.put(COLUMN_STATE_FORCEDWITHDRAWAL, 0);
            cv.put(COLUMN_STATE_TURRETSTATE, OVState.TurretState.OKAY.name());
            cv.put(COLUMN_STATE_STUNNED, 0);
            cv.put(COLUMN_STATE_DESTROYEDCRIT,0);
            cv.put(COLUMN_STATE_DESIGN_KEY, mech.getKey());
            cv.put(COLUMN_STATE_ACTIVE,1);
        }
        long ret;
        if (state != null && state.getKey()>=0){// if the state doesn't exist we are creating from a design

            db.update(TABLE_NAME_HEADER, cv, COLUMN_ID + " = " + state.getKey(), null);
            ret = state.getKey();
        } else {
            ret = db.insert(TABLE_NAME_HEADER, null, cv);
            if (state !=null)state.setKey((int)ret);
        }
        // update pilot
        updatePilot(db,(state!=null)?state.getPilot():null,ret);
        return ret;
    }
    public void updatePilot(SQLiteDatabase dbIn, Pilot pilot, long id){
        SQLiteDatabase db = dbIn==null?this.getWritableDatabase():dbIn;
        ContentValues cv = new ContentValues();
        if (pilot!=null){
            pilot.setContentValues(cv);
        } else {
            cv.put(COLUMN_STATE_PILOT_NAME, "Bob Ivanovich");
            cv.put(COLUMN_STATE_GUNNERY, 4);
            cv.put(COLUMN_STATE_PILOTING, 5);
            cv.put(COLUMN_STATE_INJURIES, 0);
            cv.put(COLUMN_STATE_CONSCIOUS, 1);
            cv.put(COLUMN_PILOT_NAMED, 0);
            cv.put(COLUMN_PILOT_EXPERIENCE, 150);
            cv.put(COLUMN_PILOT_EDGE_TOKENS, 1);
            cv.put(COLUMN_PILOT_EDGE_SKILLS, "");
        }
        cv.put(COLUMN_ID, id);
        db.insertWithOnConflict(TABLE_NAME_PILOT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public void addSegment(OVSegment segment, long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_SEG_LOCATION, segment.getLocation().toString());
        cv.put(COLUMN_SEG_ARMOUR, segment.getArmour());
        cv.put(COLUMN_SEG_ARMOURTYPE, segment.getArmourType().toString());
        cv.put(COLUMN_SEG_REAR, segment.getArmourRear());
        cv.put(COLUMN_SEG_STRUCTURE, segment.getStructure());
        if (segment instanceof TWVehicleSegment)
            cv.put(COLUMN_SEG_STABILISER, ((TWVehicleSegment) segment).isStabiliserDamaged()?1:0);
        else
            cv.put(COLUMN_SEG_STABILISER,0);
                    // use overiden method to either default full values (for design) or current state(for instance)
        segment.storeDamage(cv);
        db.insertWithOnConflict(TABLE_NAME_SEGMENT,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateAmmunition(List<OVAmmunition> list, long unitKey) {
        for (OVEquipment equip : list) {
            addEquipment(equip, unitKey);
        }
    }
    public void addEquipment(IEquipment equip, long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        equip.setDatabase(cv);
        if (equip.getType()== OVEquipment.EquipmentType.AMMO) {
            cv.put(COLUMN_EQUIP_INST_SPEC1, "" + (((OVAmmunition)equip).getRemaining()));

        } else if (equip.getType()== OVEquipment.EquipmentType.MASC) {
            // is the MASC unit activated
            cv.put(OVDatabaseUnit.COLUMN_EQUIP_SPEC1, "" + equip.getSpecial());
            // how many turns in a row has it been activated
            cv.put(COLUMN_EQUIP_INST_SPEC1, "" + equip.getSpecialTwo());
        }
        cv.put(COLUMN_EQUIP_INST_STATE, equip.isOperational()?1:0);

        db.insertWithOnConflict(TABLE_NAME_EQUIPMENT,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public void addWeapon(OVWeaponInstance weapon, long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_WEAPON_ID, weapon.getID());
        cv.put(COLUMN_WEAPON_KEY, weapon.getWeapon().getId());
        cv.put(COLUMN_WEAPON_LOCATION, weapon.getLocation().toString());
        cv.put(COLUMN_WEAPON_STATE,weapon.isOperational()?1:0);
        cv.put(COLUMN_WEAPON_MODE,weapon.getWeaponMode().toString());
        db.insertWithOnConflict(TABLE_NAME_WEAPON,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public void addTic(OVTic tic, long rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_TIC_ID, tic.getID());
        // clear any existing tic configuration
        db.delete(TABLE_NAME_TIC,COLUMN_ID + " = " + rowid  + " AND " + COLUMN_TIC_ID + " = " + tic.getID(),null);
        for (OVWeaponInstance ovWeaponInstance : tic.getWeapons()) {
            cv.put(COLUMN_TIC_WEAPON, ovWeaponInstance.getID());
            long result = db.insert(TABLE_NAME_TIC, null, cv);
            if (result == -1) {
//                db.close();
                return;
            }
        }
//        db.close();
    }

    public void updateUnitData(IUnitData unit){
        addUnitHeader(unit.getHeader(),unit.getState());

        for (OVSegment ovSegment : unit.getSegments()) {
            addSegment(ovSegment, unit.getKey());
        }
        for (IEquipment iEquipment : unit.getEquipment()) {
            if (iEquipment instanceof OVWeaponInstance)
                addWeapon((OVWeaponInstance) iEquipment, unit.getState().getKey());
            else addEquipment(iEquipment, unit.getKey());
        }

    }

    public UnitData getUnitData(long key, boolean forAiGame){
        UnitData data = getState((int) key);

        getUnitDesignFromHeader(data);

        return data;
    }
    private void getUnitDesignFromHeader(UnitData design) {
        getSegments(design);
        getEquipment(design.getEquipment(), design.getHeader());
        getWeapons(design, design.getState().getKey());
        getTics(design);
        design.linkSegments();
    }
    public void getSegments(UnitData design) {
        List<OVSegment> segments = design.getSegments();
        int rowID = design.getState().getKey();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_SEGMENT, new String[]{"*"}, COLUMN_ID + " = " + rowID, null, null, null, null);

        while (cur.moveToNext()) {
            OVSegmentInst segment;
            segment = OVSegmentInst.newInstance(OVSegment.OVLocation.valueOf(getCursorString(cur, COLUMN_SEG_LOCATION)),
                    getCursorInt(cur, COLUMN_SEG_ARMOUR),
                    getCursorInt(cur, COLUMN_SEG_STRUCTURE),
                    getCursorInt(cur, COLUMN_SEG_REAR),
                    OVSegment.ArmourType.valueOf(getCursorString(cur, COLUMN_SEG_ARMOURTYPE)),design);
            if (getCursorInt(cur, COLUMN_SEG_STABILISER)==1){
                // must be a vehicle
                if (segment instanceof TWVehicleSegment)
                    ((TWVehicleSegment) segment).setStabiliserDamaged(true);
            }
            segment.setSavedDamage(cur);
            segments.add(segment);
        }
        cur.close();
//        db.close();
    }
    public void getEquipment(List<IEquipment> equip, IUnitHeader header) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_EQUIPMENT, new String[]{"*"}, COLUMN_ID + " = " + header.getKey(), null, null, null, null);
        while (cur.moveToNext()) {
            IEquipment newEquip = null;
            OVEquipment.EquipmentType type = OVEquipment.EquipmentType.valueOf(getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_TYPE));
            switch (type) {
                case AMMO:
                    newEquip = new OVAmmunition(cur, null);
                    ((OVAmmunition)newEquip).setRemaining(getCursorInt(cur,COLUMN_EQUIP_INST_SPEC1));
                    newEquip.markAsSent();//otherwise it will be double sent in a bluetooth game
                    break;
                case GYRO:
                    newEquip = new OVCoreEquipment(OVEquipment.EquipmentType.GYRO,
                            header.getType()== ForceList.ForceType.OV? OVSegment.OVLocation.TORSO: OVSegment.OVLocation.CENTRETORSO
                            ,getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID));
                    ((OVCoreEquipment)newEquip).setId(getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID));
                    break;
                case ENGINE:
                    newEquip = new OVCoreEquipment(header.getEngine(),getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID),
                            header.getType()== ForceList.ForceType.OV? OVSegment.OVLocation.TORSO: OVSegment.OVLocation.CENTRETORSO);
                    ((OVCoreEquipment)newEquip).setId(getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID));
                    break;
                case ACTUATOR:
                    if (header.getType()== ForceList.ForceType.OV) {
                        newEquip = new OVCoreEquipment(OVEquipment.EquipmentType.ACTUATOR,
                                OVSegment.OVLocation.valueOf(getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_LOCATION)),
                                getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID));
                    } else {
                        // TW mechs use a special actuator class
                        newEquip = new TWActuator(OVSegment.OVLocation.valueOf(getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_LOCATION)),
                                getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID), getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_SPEC1));
                    }
                    break;
                case SENSORS:
                case LIFESUPPORT:
                case COCKPIT:
                case HEATSINK:
                case JUMPJET:
                    // not needed for OV mechs
                    if (header.getType()== ForceList.ForceType.OV) continue;
                    newEquip = new OVCoreEquipment(type, OVSegment.OVLocation.valueOf(getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_LOCATION)), getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID));
                    break;
                default:
                    newEquip = new OVEquipment(type,
                            getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_ID),
                            OVSegment.OVLocation.valueOf(getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_LOCATION)),
                            getCursorString(cur, OVDatabaseUnit.COLUMN_EQUIP_NAME));
                    if (type == OVEquipment.EquipmentType.MASC){
                        newEquip.setSpecial(getCursorInt(cur,OVDatabaseUnit.COLUMN_EQUIP_SPEC1));
                        newEquip.setSpecialTwo(getCursorInt(cur,COLUMN_EQUIP_INST_SPEC1));
                    }
            }
            newEquip.setStatus(getCursorInt(cur,COLUMN_EQUIP_INST_STATE)==1);
            equip.add(newEquip);
        }
        cur.close();
    }
    public void getWeapons(UnitData unit, int rowID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_WEAPON,
                new String[]{"*"},
                COLUMN_ID + " = " + rowID,
                null,
                null,
                null,
                null);
        while (cur.moveToNext()) {
            unit.addWeapon(cur);
        }
        cur.close();
    }
    /*
    must only call the getTics method after the weapons have been added to the unit
     */
    public void getTics(OVUnitDesign design) {
        int ticID = -1;
        int newTic = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_TIC, new String[]{"*"},
                COLUMN_ID + " = " + design.getHeader().getKey(),
                null, null, null, null);

        OVTic tic = null;
        while (cur.moveToNext()) {
            newTic = getCursorInt(cur, COLUMN_TIC_ID);
            if (ticID != newTic) {
                tic = new OVTic(newTic);
                design.getWeapons().add(tic);
                ticID = newTic;
            }
            int weapon_key = getCursorInt(cur, COLUMN_TIC_WEAPON);
            //find the weapon to be linked
            for (IEquipment weap : design.getEquipment()) {
                if (weap.getType() != OVEquipment.EquipmentType.WEAPON) continue;
                if ( weap.getID() == weapon_key) {
                    if (tic != null) tic.addWeapon((OVWeaponInstance) weap);
                    break;
                }
            }

        }
        cur.close();
//        db.close();
    }
    public UnitData getState(int key){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_ID + " = " + key, null, null, null, null);
        UnitData unit = null;
        if (cur.moveToFirst()) {
            unit = UnitData.newInstance(cur);
            Cursor curPilot = db.query(TABLE_NAME_PILOT, new String[]{"*"}, COLUMN_ID + " = " + key, null, null, null, null);
            if (unit !=null && curPilot.moveToFirst()) {
                unit.getState().setPilot(new Pilot(curPilot));
            }
        }
        cur.close();
        return unit;
    }

    public void setInUse(int key) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.query(TABLE_NAME_FORCE, new String[]{"*"}, COLUMN_ID + " = " + key, null, null, null, null);
        if (cur.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_FORCE_NAME, getCursorString(cur, COLUMN_FORCE_NAME));
            cv.put(COLUMN_FORCE_EXTID, getCursorInt(cur, COLUMN_FORCE_EXTID));
            cv.put(COLUMN_FORCE_EXTSOURCE, getCursorString(cur, COLUMN_FORCE_EXTSOURCE));
            cv.put(COLUMN_FORCE_LISTTYPE, getCursorString(cur, COLUMN_FORCE_LISTTYPE));
            cv.put(COLUMN_FORCE_COMMANDER, getCursorString(cur, COLUMN_FORCE_COMMANDER));
            cv.put(COLUMN_FORCE_INUSE, 0);
            cv.put(COLUMN_ID, getCursorInt(cur, COLUMN_ID));
            db.update(TABLE_NAME_FORCE, cv, COLUMN_ID + " = " + getCursorInt(cur, COLUMN_ID), null);
        }
        cur.close();
    }

    public void deleteForceList(int key){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.query(TABLE_NAME_FORCE_ITEM, new String[]{"*"}, COLUMN_ID + " = " + key, null, null, null, null);
        while (cur.moveToNext()){
                deleteUnit(db,getCursorInt(cur,COLUMN_FITEM_UNIT));
        }
        cur.close();
        db.delete(TABLE_NAME_FORCE_ITEM,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_FORCE,COLUMN_ID + " = " + key,null);
//        db.close();
    }
    private void deleteUnit(SQLiteDatabase db, int key){
        db.delete(TABLE_NAME_HEADER,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_PILOT,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_WEAPON,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_EQUIPMENT,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_SEGMENT,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_TIC,COLUMN_ID + " = " + key,null);
    }
}
