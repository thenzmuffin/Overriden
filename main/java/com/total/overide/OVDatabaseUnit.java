package com.total.overide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.total.overiden.ForceList;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitHeader;
import com.total.overiden.IWeapon;
import com.total.overiden.UnitCatalogAdapter;
import com.total.overiden.UnitCatalogGroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class OVDatabaseUnit extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OverrideUnits.db";
    private static final int DATABASE_VERSION = 19;

    public static final String COLUMN_ID = "_id";

    private static final String TABLE_NAME_HEADER = "mech_header";
    private static final String TABLE_NAME_SEGMENT = "mech_segment";
    private static final String TABLE_NAME_WEAPON = "mech_weapon";
    private static final String TABLE_NAME_TIC = "mech_tic";
    private static final String TABLE_NAME_EQUIPMENT = "mech_equipment";
    private static final String COLUMN_HEADER_NAME = "header_name";
    private static final String COLUMN_HEADER_UNIT_TYPE = "header_unit_type";
    private static final String COLUMN_HEADER_VARIANT = "header_type";
    private static final String COLUMN_HEADER_WALK = "header_walk";
    private static final String COLUMN_HEADER_JUMP = "header_jump";
    private static final String COLUMN_HEADER_MASS = "header_mass";
    private static final String COLUMN_HEADER_ENGINE = "header_engine";
    private static final String COLUMN_HEADER_SINKS = "header_sinks";
    public static final String COLUMN_HEADER_TSM = "header_tsm";
    private static final String COLUMN_HEADER_TWSINKS = "header_twsinks";
    private static final String COLUMN_HEADER_SINKS_ARE_DOUBLE = "header_doulbe_sinks";
    private static final String COLUMN_HEADER_ROLE = "header_role";
    private static final String COLUMN_WEAPON_ID = "_id_weapon";
    private static final String COLUMN_WEAPON_KEY = "weapon_key";
    private static final String COLUMN_WEAPON_LOCATION = "weapon_location";
//    private static final String COLUMN_WEAPON_TIC_ID = "weapon_tic_id";
    private static final String COLUMN_TIC_ID = "_id_tic";
    private static final String COLUMN_TIC_WEAPON = "tic_wepaon_id";
    public static final String COLUMN_EQUIP_ID = "equip_id";
    public static final String COLUMN_EQUIP_TYPE = "equip_type";
    public static final String COLUMN_EQUIP_LINK = "equip_link";
    public static final String COLUMN_EQUIP_SPEC1 = "equip_spec1";
    public static final String COLUMN_EQUIP_SPEC2 = "equip_spec2";
    public static final String COLUMN_EQUIP_NAME = "equip_name";
    public static final String COLUMN_EQUIP_LOCATION = "equip_location";
    public static final String COLUMN_EQUIP_SLOTS = "equip_slots";
    private static final String COLUMN_SEG_LOCATION = "seg_location";
    private static final String COLUMN_SEG_ARMOUR = "seg_armour";
    private static final String COLUMN_SEG_ARMOURTYPE = "seg_armourtype";
    private static final String COLUMN_SEG_REAR = "seg_rear";
    private static final String COLUMN_SEG_STRUCTURE = "seg_structure";


    private final Context context;

    public OVDatabaseUnit(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //TODO do we really need the context here?
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_HEADER + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
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
                + COLUMN_HEADER_TSM + " INTEGER, "
                + COLUMN_HEADER_ROLE  + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_WEAPON + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_WEAPON_ID + " INTEGER, "
                + COLUMN_WEAPON_KEY + " INTEGER, "
                + COLUMN_WEAPON_LOCATION + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_WEAPON_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_TIC + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_TIC_ID + " INTEGER, "
                + COLUMN_TIC_WEAPON + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_TIC_ID + ", " + COLUMN_TIC_WEAPON + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_EQUIPMENT + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_EQUIP_ID + " INTEGER, "
                + COLUMN_EQUIP_NAME + " TEXT, "
                + COLUMN_EQUIP_LOCATION + " TEXT, "
                + COLUMN_EQUIP_SLOTS + " INTEGER, "
                + COLUMN_EQUIP_TYPE + " TEXT, "
                + COLUMN_EQUIP_LINK + " INTEGER, "
                + COLUMN_EQUIP_SPEC1 + " INTEGER, "
                + COLUMN_EQUIP_SPEC2 + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_EQUIP_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_SEGMENT + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_SEG_LOCATION + " TEXT, "
                + COLUMN_SEG_ARMOURTYPE + " TEXT, "
                + COLUMN_SEG_ARMOUR + " INTEGER, "
                + COLUMN_SEG_REAR + " INTEGER, "
                + COLUMN_SEG_STRUCTURE + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_SEG_LOCATION + "));";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        if (db == null) db = this.getWritableDatabase();
        drop_table(db, TABLE_NAME_HEADER);
        drop_table(db, TABLE_NAME_WEAPON);
        drop_table(db, TABLE_NAME_EQUIPMENT);
        drop_table(db, TABLE_NAME_SEGMENT);
        drop_table(db, TABLE_NAME_TIC);

        onCreate(db);
    }

    private void drop_table(SQLiteDatabase db, String tab_name) {
        String query = "DROP TABLE IF EXISTS " + tab_name;
        db.execSQL(query);
    }

    public void addUnit(OVUnitDesign design) {
        // should add a pop up for the attempt to add when a record already exists with that name and variant number
        if (checkExists(design.getHeader().getName(),design.getHeader().getVariant())>=0)return;
        addHeader(design.getHeader());
        for (OVSegment ovSegment : design.getSegments()) {
            addSegment(ovSegment, design.getHeader().getKey());
        }
        for (IEquipment equip : design.getEquipment()) {
            if (equip instanceof OVWeaponInstance) {
                addWeapon((OVWeaponInstance) equip, design.getHeader().getKey());
            } else {
                addEquipment(equip, design.getHeader().getKey());
            }
        }
        for (IWeapon tic : design.getWeapons()) {
            addTic((OVTic) tic, design.getHeader().getKey());
        }

    }

    public List<UnitCatalogGroupAdapter.CatalogGroupEntry> getGroupCatalog(UnitCatalogGroupAdapter.CatalogGroup type, String chassis) {
        List<UnitCatalogGroupAdapter.CatalogGroupEntry> list = new ArrayList<>();
//    SQLiteDatabase db = this.getReadableDatabase();
        switch (type) {
            case ROLE:
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Ambusher"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Brawler"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Juggernaut"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Missile Boat"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Scout"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Skirmisher"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Sniper"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "Striker"));
                break;
            case MASS:
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "20 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "25 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "30 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "35 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "40 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "45 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "50 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "55 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "60 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "65 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "70 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "75 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "80 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "85 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "90 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "95 ton"));
                list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(type, "100 ton"));
                break;
            case CHASSIS:
                // get all the chassis with the current group
                list = getCatalog(chassis);
                break;
            case UNIT:
                break;
        }

        return list;
    }
    public boolean groupChassisHasChildren(UnitCatalogGroupAdapter.CatalogGroup type, String chassis){
        return !getCatalog(chassis).isEmpty();
    }
    private List<UnitCatalogGroupAdapter.CatalogGroupEntry> getCatalog(String role){
        List<UnitCatalogGroupAdapter.CatalogGroupEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        OVHeader.UnitRole unitRole = null;
        int mass = 0;
        try {
            mass = Integer.parseInt(role.substring(0, 2));
            if (mass==10)mass = 100;
        } catch (NumberFormatException nf){
            // just carry on
        }
        Cursor cur;
        if (mass>0){
            cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_MASS + " = " + mass, null, null, null, COLUMN_HEADER_NAME);

        } else {
            switch (role.toLowerCase()) {
                case "ambusher":
                    unitRole = OVHeader.UnitRole.AMBUSHER;
                    break;
                case "brawler":
                    unitRole = OVHeader.UnitRole.BRAWLER;
                    break;
                case "juggernaut":
                    unitRole = OVHeader.UnitRole.JUGGERNAUT;
                    break;
                case "missile boat":
                    unitRole = OVHeader.UnitRole.MISSILEBOAT;
                    break;
                case "scout":
                    unitRole = OVHeader.UnitRole.SCOUT;
                    break;
                case "skirmisher":
                    unitRole = OVHeader.UnitRole.SKIRMISHER;
                    break;
                case "sniper":
                    unitRole = OVHeader.UnitRole.SNIPER;
                    break;
                case "striker":
                default:
                    unitRole = OVHeader.UnitRole.STRIKER;
                    break;
            }

            cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_ROLE + " = ?", new String[]{unitRole.toString()}, null, null, COLUMN_HEADER_NAME);
        }
        String chassis = "";
        while (cur.moveToNext()){
            if (chassis.equals(getCursorString(cur,COLUMN_HEADER_NAME)))continue;
            chassis = getCursorString(cur,COLUMN_HEADER_NAME);
            list.add(new UnitCatalogGroupAdapter.CatalogGroupEntry(UnitCatalogGroupAdapter.CatalogGroup.CHASSIS,chassis));
        }
        cur.close();
        return list;
    }
    public List<UnitCatalogAdapter.CatalogEntry> getCatalogByChassis(String chassis){
        List<UnitCatalogAdapter.CatalogEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_NAME + " = ?", new String[]{chassis}, null, null, null);
        while (cur.moveToNext()){
            list.add(new UnitCatalogAdapter.CatalogEntry(getCursorInt(cur,COLUMN_ID),getCursorString(cur,COLUMN_HEADER_NAME),getCursorString(cur,COLUMN_HEADER_VARIANT)));
        }
        cur.close();
//        db.close();
        return list;
    }
    public List<UnitCatalogAdapter.CatalogEntry> getCatalog(String name, IUnitHeader.UnitType type){
        List<UnitCatalogAdapter.CatalogEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "";
        String[] args = null;
        if (name!=null && !name.isEmpty()){
            selection += COLUMN_HEADER_NAME + " LIKE ?"; //+ name + "'";
            args = new String[1];
            args[0] = "%" + name + "%";
        }
        if (type!=null && type != IUnitHeader.UnitType.NONE){
            if (!selection.isEmpty()) selection += " AND ";
            selection += COLUMN_HEADER_UNIT_TYPE + " = '" + type.name() + "'";
        }
        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, selection, args, null, null, null);
        while (cur.moveToNext()){
            list.add(new UnitCatalogAdapter.CatalogEntry(getCursorInt(cur,COLUMN_ID),getCursorString(cur,COLUMN_HEADER_NAME),getCursorString(cur,COLUMN_HEADER_VARIANT)));
        }
        cur.close();
        return list;
    }
    public void addHeader(IUnitHeader mech) {
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
        cv.put(COLUMN_HEADER_TSM, mech.isTsm()?1:0);
        long rowid = db.insert(TABLE_NAME_HEADER, null, cv);
        if (rowid > -1) {
            mech.setKey((int) rowid);
        }
    }
    public void addWeapon(OVWeaponInstance weapon, int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_WEAPON_ID, weapon.getID());
        cv.put(COLUMN_WEAPON_KEY, weapon.getWeapon().getId());
        cv.put(COLUMN_WEAPON_LOCATION, weapon.getLocation().toString());
//        cv.put(COLUMN_WEAPON_TIC_ID, weapon.getTic());
        long result = db.insert(TABLE_NAME_WEAPON, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Mech Design DB Insert Failed - Weapon", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    public void addTic(OVTic tic, int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_TIC_ID, tic.getID());
        for (OVWeaponInstance ovWeaponInstance : tic.getWeapons()) {
            cv.put(COLUMN_TIC_WEAPON, ovWeaponInstance.getID());
            long result = db.insert(TABLE_NAME_TIC, null, cv);
            if (result == -1) {
                Toast.makeText(context, "Mech Design DB Insert Failed - TIC", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    public void addSegment(OVSegment segment, int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        cv.put(COLUMN_SEG_LOCATION, "" + segment.getLocation());
        cv.put(COLUMN_SEG_ARMOUR, segment.getArmour());
        cv.put(COLUMN_SEG_ARMOURTYPE, segment.getArmourType().toString());
        cv.put(COLUMN_SEG_REAR, segment.getArmourRear());
        cv.put(COLUMN_SEG_STRUCTURE, segment.getStructure());
        long result = db.insert(TABLE_NAME_SEGMENT, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Mech Design DB Insert Failed - Segment", Toast.LENGTH_SHORT).show();
        }
    }
    public void addEquipment(IEquipment equip, int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_ID, rowid);
        equip.setDatabase(cv);
        long result = db.insert(TABLE_NAME_EQUIPMENT, null, cv);
        if (result == -1) {
            result = db.update(TABLE_NAME_EQUIPMENT, cv,
                    COLUMN_ID + " = " + rowid + " AND " + COLUMN_EQUIP_ID + " = " + equip.getID(), null);
        }
        if (result == -1) {
            Toast.makeText(context, "Mech Design DB Insert Failed - Equipment", Toast.LENGTH_SHORT).show();
        }
    }
    public OVUnitDesign getUnitDesign(int key, ForceList.ForceType fType) {
        OVUnitDesign ret = new OVUnitDesign();
        if (!getUnitDesign(key, ret, fType)) ret = null;
        return ret;
    }
    public boolean getUnitDesign(int key, OVUnitDesign design, ForceList.ForceType fType) {
        if (getHeaderByID(key, (OVHeader) design.getHeader())) {
            design.getHeader().setType(fType);
            getUnitDesignFromHeader(design, fType);
            return true;
        }
        return false;
    }
    private void getUnitDesignFromHeader(OVUnitDesign design, ForceList.ForceType fType) {
        getSegments(design , fType);
        getEquipment(design.getEquipment(), design.getHeader(), fType);
        getWeapons(design.getEquipment(), design.getHeader().getKey(),fType);
        getTics(design);
        design.linkSegments();
    }
    public OVHeader getHeader(String type, OVHeader head) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_VARIANT + " = '" + type + "'", null, null, null, null);
        if (cur.getCount() != 1) {
            cur.close();
            return null;
        }
        cur.moveToFirst();
        getHeaderFromCursor(cur, head);
        cur.close();
        return head;
    }
    public int checkExists(String name, String variant) {
        // return the unit design key or -1 if nothing is found
        SQLiteDatabase db = this.getReadableDatabase();
        int ret = -1;
        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"}, COLUMN_HEADER_VARIANT + " = ? AND " + COLUMN_HEADER_NAME + " = ?", new String[]{variant,name}, null, null, null);
        if (cur.moveToFirst()){
            ret = getCursorInt(cur,COLUMN_ID);
        }
        cur.close();
        return ret;
    }
    public boolean getHeaderByID(int type, OVHeader head) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_HEADER, new String[]{"*"},
                COLUMN_ID + " = " + type, null, null, null, null);
        if(cur.moveToFirst())
            getHeaderFromCursor(cur, head);
        cur.close();
        return true;
    }
    private void getHeaderFromCursor(Cursor cur, OVHeader mech) {
        // should this be passed in? Already created in the unit constructor
        mech.setKey(getCursorInt(cur, COLUMN_ID));
        mech.setName(getCursorString(cur, COLUMN_HEADER_NAME));
        mech.setUnitType(IUnitHeader.UnitType.valueOf(getCursorString(cur, COLUMN_HEADER_UNIT_TYPE)));
        mech.setWalk(getCursorInt(cur, COLUMN_HEADER_WALK));
        mech.setVariant(getCursorString(cur, COLUMN_HEADER_VARIANT));
        mech.setJump(getCursorInt(cur, COLUMN_HEADER_JUMP));
        mech.setMass(getCursorInt(cur, COLUMN_HEADER_MASS));
        mech.setEngine(OVHeader.EngineType.valueOf(getCursorString(cur, COLUMN_HEADER_ENGINE)));
        mech.setHeatSinks(getCursorInt(cur, COLUMN_HEADER_SINKS));
        mech.setTwHeatSinks(getCursorInt(cur, COLUMN_HEADER_TWSINKS));
        mech.setDoubleHeatSinks(getCursorInt(cur, COLUMN_HEADER_SINKS_ARE_DOUBLE)==1);
        mech.setRole(OVHeader.UnitRole.valueOf(getCursorString(cur, COLUMN_HEADER_ROLE)));
        mech.setTsm(getCursorInt(cur, COLUMN_HEADER_TSM)==1);
        //Default the Type to OV - it will be overwritten later if needed
        mech.setType(ForceList.ForceType.OV);
    }
    public void getSegments(OVUnitDesign unit, ForceList.ForceType fType) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_SEGMENT, new String[]{"*"}, COLUMN_ID + " = " + unit.getHeader().getKey(), null, null, null, null);
        int torsoArm = 0;
        int torsoStr = 0;
        int torsoRear = 0;
        OVSegment.ArmourType armourType = null;
        while (cur.moveToNext()) {
            int arm, str, rear;

            armourType = OVSegment.ArmourType.valueOf(getCursorString(cur,COLUMN_SEG_ARMOURTYPE));
            OVSegment.OVLocation thisLoc = OVSegment.OVLocation.valueOf(getCursorString(cur, COLUMN_SEG_LOCATION));
            arm = getCursorInt(cur, COLUMN_SEG_ARMOUR);
            str = getCursorInt(cur, COLUMN_SEG_STRUCTURE);
            rear = getCursorInt(cur, COLUMN_SEG_REAR);
            if (fType== ForceList.ForceType.OV){
                // for OV we need some logic to convert armour values and merge the torso locations
                if (thisLoc== OVSegment.OVLocation.CENTRETORSO ||
                    thisLoc== OVSegment.OVLocation.LEFTTORSO ||
                    thisLoc== OVSegment.OVLocation.RIGHTTORSO){
                    torsoArm += arm;
                    if (thisLoc== OVSegment.OVLocation.CENTRETORSO)torsoStr += str; // stucture is dictated by ct only
                    torsoRear += getCursorInt(cur, COLUMN_SEG_REAR);
                    continue; // we will handle the torso at the end
                } else if (thisLoc== OVSegment.OVLocation.HEAD){
                    // head has a special conversion rate
                    arm = OVMtfReader.getHeadArmour(arm);
                    str = 1;
                }else{
                    if (unit.getHeader().getUnitType() == IUnitHeader.UnitType.MECH) {
                        arm = Math.floorDiv(arm + 1, 3);
                    } else {
                        arm = Math.max(1,Math.floorDiv(arm + 2, 4));
                    }
                    str = Math.max(1,Math.floorDiv(str + 1, 3));
                }
            }
            if (unit instanceof UnitData){
                unit.getSegments().add(OVSegmentInst.newInstance(thisLoc,arm,str,rear,armourType,unit));
            } else {
                unit.getSegments().add(new OVSegment(thisLoc,arm,str,rear,armourType));
            }
        }
        // for an OV unit that has a torso (mechs only) combine torso locations into a single segment.
        if (fType== ForceList.ForceType.OV && torsoStr>0){
            if (unit instanceof UnitData){
                unit.getSegments().add(OVSegmentInst.newInstance(OVSegment.OVLocation.TORSO,
                        Math.floorDiv(torsoArm + 3, 6),
                        Math.floorDiv(torsoStr + 1, 3),
                        Math.floorDiv(torsoRear + 3, 6),
                        armourType,unit));
            } else {
                unit.getSegments().add(new OVSegment(OVSegment.OVLocation.TORSO,
                        Math.floorDiv(torsoArm + 3, 6),
                        Math.floorDiv(torsoStr + 1, 3),
                        Math.floorDiv(torsoRear + 3, 6),
                        armourType));
            }
        }
        cur.close();
    }
    public void getEquipment(List<IEquipment> equip, IUnitHeader header, ForceList.ForceType fType) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_EQUIPMENT, new String[]{"*"}, COLUMN_ID + " = " + header.getKey(), null, null, null, null);

        while (cur.moveToNext()) {
            OVSegment.OVLocation thisLoc = convertTWtoOVLocation(fType,
                    OVSegment.OVLocation.valueOf(getCursorString(cur, COLUMN_EQUIP_LOCATION)));
            OVEquipment newEquip = null;
            OVEquipment.EquipmentType type = OVEquipment.EquipmentType.valueOf(getCursorString(cur, COLUMN_EQUIP_TYPE));
            switch (type) {
                case AMMO:
                    newEquip = new OVAmmunition(cur, fType);
                    break;
                case GYRO:
                    newEquip = new OVCoreEquipment(OVEquipment.EquipmentType.GYRO,
                            thisLoc, getCursorInt(cur, COLUMN_EQUIP_ID));
                    newEquip.setId(getCursorInt(cur, COLUMN_EQUIP_ID));
                    break;
                case ENGINE:
                    // OV units can ignore engines coming from side torsos
                    if (fType== ForceList.ForceType.OV &&
                            OVSegment.OVLocation.valueOf(getCursorString(cur, COLUMN_EQUIP_LOCATION))!= OVSegment.OVLocation.CENTRETORSO)continue;
                    newEquip = new OVCoreEquipment(header.getEngine(),getCursorInt(cur, COLUMN_EQUIP_ID),thisLoc);
                    newEquip.setId(getCursorInt(cur, COLUMN_EQUIP_ID));
                    break;
                case ACTUATOR:
                    if (fType== ForceList.ForceType.OV){
                        // OV mechs don't need arm actuators tracked
                        if (thisLoc== OVSegment.OVLocation.LEFTARM ||thisLoc== OVSegment.OVLocation.RIGHTARM) continue;
                        newEquip = new OVCoreEquipment(OVEquipment.EquipmentType.ACTUATOR,
                                thisLoc, getCursorInt(cur, COLUMN_EQUIP_ID));
                    } else { //Creating TW actuators
                        newEquip = new TWActuator(thisLoc,
                                getCursorInt(cur, COLUMN_EQUIP_ID),
                                getCursorInt(cur, OVDatabaseUnit.COLUMN_EQUIP_SPEC1));
                    }
                    break;
                case SENSORS:
                case LIFESUPPORT:
                case COCKPIT:
                case HEATSINK:
                case JUMPJET:
                    // not needed for OV mechs
                    if (fType == ForceList.ForceType.OV) continue;
                    newEquip = new OVCoreEquipment(type, thisLoc, getCursorInt(cur, COLUMN_EQUIP_ID));
                    break;
                default:
                    newEquip = new OVEquipment(type,
                            getCursorInt(cur, COLUMN_EQUIP_ID),
                            thisLoc,
                            getCursorString(cur, COLUMN_EQUIP_NAME));
            }
            newEquip.setCritSlots(getCursorInt(cur, COLUMN_EQUIP_SLOTS));
            equip.add(newEquip);
        }
        cur.close();
    }
    public void updateDesign(OVUnitDesign design){
        SQLiteDatabase db = this.getWritableDatabase();

        // at present the only possible updates are to the weapon assignments
        // first remove the current db entries
//        db.delete(TABLE_NAME_WEAPON, COLUMN_ID + "=" + design.getHeader().getKey(), null);
        db.delete(TABLE_NAME_TIC, COLUMN_ID + "=" + design.getHeader().getKey(), null);
        // now add the updated versions back in
//        for (IEquipment equip : design.getEquipment()) {
//            if (equip instanceof OVWeaponInstance) {
//                addWeapon((OVWeaponInstance) equip, design.getHeader().getKey());
//            }
//        }
        for (IWeapon tic : design.getWeapons()) {
            addTic((OVTic) tic, design.getHeader().getKey());
        }
    }
    public void getWeapons(List<IEquipment> equip, int rowID, ForceList.ForceType fType) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur = db.query(TABLE_NAME_WEAPON,
                              new String[]{"*"},
                     COLUMN_ID + " = " + rowID,
                  null,
                      null,
                       null,
                      null);
        if (cur.getCount() < 1) {
            cur.close();
            return;
        }
        while (cur.moveToNext()) {
            OVSegment.OVLocation thisLoc = convertTWtoOVLocation(fType,
                    OVSegment.OVLocation.valueOf(getCursorString(cur, COLUMN_WEAPON_LOCATION)));
            equip.add(new OVWeaponInstance(thisLoc,getCursorInt(cur, COLUMN_WEAPON_ID),
                                           OVMtfReader.findOVWeaponByID(getCursorInt(cur, COLUMN_WEAPON_KEY)),true, null)
            );
        }
        cur.close();
    }
    public static OVSegment.OVLocation convertTWtoOVLocation(ForceList.ForceType fType, OVSegment.OVLocation start) {
        if (fType == ForceList.ForceType.OV) {
            //convert the location to OV torso
            if (start == OVSegment.OVLocation.CENTRETORSO ||
                    start == OVSegment.OVLocation.LEFTTORSO ||
                    start == OVSegment.OVLocation.RIGHTTORSO) return OVSegment.OVLocation.TORSO;
            else if (start == OVSegment.OVLocation.CTREAR ||
                    start == OVSegment.OVLocation.LTREAR ||
                    start == OVSegment.OVLocation.RTREAR) return OVSegment.OVLocation.REAR;
        }
        return start; // no change
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
        if (cur.getCount() < 1) {
            cur.close();
            return;
        }
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
                if (weap.getID() == weapon_key) {
                    if(tic!=null)tic.addWeapon((OVWeaponInstance) weap);
                    break;
                }
            }

        }
        cur.close();
    }
    public void deleteUnit(int key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_HEADER,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_WEAPON,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_TIC,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_EQUIPMENT,COLUMN_ID + " = " + key,null);
        db.delete(TABLE_NAME_SEGMENT,COLUMN_ID + " = " + key,null);
    }

    public int getCursorInt(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getInt(index);
    }

    public String getCursorString(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getString(index);
    }

}
