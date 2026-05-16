package com.total.overiden;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.total.overide.OVDatabaseForce;
import com.total.overide.OVSegment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DatabaseGame extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OverrideGame.db";
    private static final int DATABASE_VERSION = 82;

    public static final String COLUMN_ID = "_id";
    private static final String TABLE_NAME_GAME = "game_game";
    private static final String TABLE_NAME_TURN = "game_turn";
    private static final String TABLE_NAME_UNITTURN = "game_unitturn";
    private static final String TABLE_NAME_TARGET_DATA = "game_target_data";
    private static final String TABLE_NAME_WEAPON = "game_weapon";
    private static final String TABLE_NAME_DAMAGE = "game_damage";
    private static final String TABLE_NAME_DAMAGE_GROUP = "game_damage_grp";
    public static final String TABLE_NAME_CHECK = "game_check";
    public static final String TABLE_NAME_PHYSICAL = "game_physical";
    private static final String COLUMN_GAME_NAME = "game_name";
    private static final String COLUMN_GAME_ACTIVE = "game_active";
    private static final String COLUMN_GAME_FORCE1 = "game_force_one";
    private static final String COLUMN_GAME_FORCE2 = "game_force_two";
    private static final String COLUMN_GAME_FORCE1_TYPE = "game_force_one_type";
    private static final String COLUMN_GAME_FORCE2_TYPE = "game_force_two_type";
    private static final String COLUMN_GAME_SMART_HEAT = "game_smart_heat";
    private static final String COLUMN_GAME_PILOT_DAMAGE = "game_pilot_damage";
    private static final String COLUMN_GAME_USE_TICS = "game_use_tics";
    private static final String COLUMN_GAME_HEXLESS = "game_hexless";
    private static final String COLUMN_GAME_FORCEDWITHDRAWAL = "game_forced_withdrawal";
    private static final String COLUMN_GAME_BALANCEDINIT = "game_balanced_init";
    private static final String COLUMN_GAME_SCENARIO = "game_scenario_key";
    private static final String COLUMN_GAME_EXTECM = "game_extecm";
    private static final String COLUMN_GAME_IMMOBILE = "game_immobile_mod";
    private static final String COLUMN_TURN_NUMBER = "turn_number";
    private static final String COLUMN_TURN_INITIATIVE = "turn_initiative";
    private static final String COLUMN_UNITTURN_UNIT_KEY = "unit_key";
    private static final String COLUMN_UNITTURN_TARGET_COMP = "unit_target_comp";
    private static final String COLUMN_UNITTURN_RESOLVED = "unit_resolved";
    private static final String COLUMN_UNITTURN_EXT_HEAT = "unit_ext_heat";
    private static final String COLUMN_UNITTURN_MOVE_TYPE = "unit_move_type";
    private static final String COLUMN_UNITTURN_MOVE_COMP = "unit_move_comp";
    private static final String COLUMN_UNITTURN_STOOD = "unit_stood";
    private static final String COLUMN_UNITTURN_HEXES = "unit_hexes";
    private static final String COLUMN_UNITTURN_TMM = "unit_tmm";
    private static final String COLUMN_UNITTURN_HASPSR = "unit_haspsr";
    private static final String COLUMN_UNITTURN_HASPHYSPSR = "unit_hasphyspsr";
    private static final String COLUMN_UNITTURN_TAGGED = "unit_tagged";
    private static final String COLUMN_UNITTURN_SPOTTING = "unit_spotting";
    private static final String COLUMN_UNITTURN_RESERVED_PHYS = "unit_reserved";
    private static final String COLUMN_GAME_NUMBER = "target_game";
    private static final String COLUMN_TARGET_UNITTURN_KEY = "target_unitturn_key";  //key of targeted unit
    private static final String COLUMN_TARGET_TARG = "target_targ";  //key of targeted unit
    private static final String COLUMN_TARGET_RANGE = "target_range"; //range in inches/hexes
    private static final String COLUMN_TARGET_PARTIAL = "target_partial"; //target has partial cover
    private static final String COLUMN_TARGET_OTHER = "target_other"; //other to hit mods
    private static final String COLUMN_TARGET_FACING = "target_facing"; //location table for hits
    private static final String COLUMN_WEAPON_LOCATION_TABLE = "target_loctable"; //location table for hits
    private static final String COLUMN_TARGET_FORWARD = "target_forward"; //firing into forward arc
    private static final String COLUMN_TARGET_INDIRECT = "target_indirect";
    private static final String COLUMN_WEAPON_ID = "weapon_id";
    private static final String COLUMN_WEAPON_TOHIT = "weapon_tohit";
    private static final String COLUMN_WEAPON_STATUS = "weapon_status";
    private static final String COLUMN_WEAPON_DICE1 = "weapon_dice_one";
    private static final String COLUMN_WEAPON_DICE2 = "weapon_dice_two";
    private static final String COLUMN_DAMAGE_TARGET = "damage_target";
    private static final String COLUMN_DAMAGE_TYPE = "damage_type";
    private static final String COLUMN_DAMAGE_SHOOTER = "damage_shooter";
    private static final String COLUMN_DAMAGE_WEAPON = "damage_weapon"; // weapon id
    private static final String COLUMN_DAMAGE_APPLIED = "damage_applied"; // damage already applied
    private static final String COLUMN_DAMAGE_CLUSTER = "damage_cluster"; //records cluster dice
    private static final String COLUMN_DAMAGE_INDEX   = "damage_index";
    private static final String COLUMN_DAMAGE_PARENT   = "damage_parent";
    private static final String COLUMN_DAMAGE_PARENT_TYPE   = "damage_parent_type";
    private static final String COLUMN_DAMAGE_DESCRIPTION = "damage_desc"; //records cluster dice
    private static final String COLUMN_DAMAGE_CHECKTYPE = "damage_checktype"; //records cluster dice
    private static final String COLUMN_DAMAGE_EXTHEAT = "damage_extheat"; //records cluster dice
    private static final String COLUMN_DAMAGE_EDGEUSED = "damage_edgeused"; //Has an edged been used on this record
    private static final String COLUMN_GROUP_SEQ = "group_seq";
    private static final String COLUMN_GROUP_DAMAGE = "group_dmg";
    private static final String COLUMN_GROUP_LOCATION = "group_loc";
    private static final String COLUMN_GROUP_LOCATION_CONV = "group_loc_converted";
    private static final String COLUMN_GROUP_CRITCHECK = "group_critcheck";
    public static final String COLUMN_CHECK_TOHIT = "check_tohit";
    public static final String COLUMN_CHECK_ROLLED = "check_rolled";
    public static final String COLUMN_CHECK_PASSED = "check_passed";
    public static final String COLUMN_CHECK_COMPLETE = "check_complete";
    public static final String COLUMN_CHECK_TYPE = "check_type";
    public static final String COLUMN_CHECK_SPECIAL = "check_spec";
    public static final String COLUMN_PHYSICAL_NAME = "physical_name";
    public static final String COLUMN_PHYSICAL_DAMAGE = "physical_damage";
    public static final String COLUMN_PHYSICAL_GROUPING = "physical_grouping";
    public static final String TABLE_NAME_BSPTARGETS = "table_bsptargets";
    public static final String COLUMN_BSPTARGET_ID = "bsptargets_id";
    public static final String COLUMN_BSPTARGET_RANGE = "bsptargets_range";
    public static final String COLUMN_BSPTARGET_DICE = "bsptargets_dice";
    public static final String COLUMN_BSPTARGET_STATUS = "bsptargets_status";
    public static final String TABLE_NAME_BSPSTRIKE = "table_bspstrike";
    public static final String COLUMN_BSPSTRIKE_AVAILABLE = "bspstrike_available";
    public static final String COLUMN_BSPSTRIKE_TARGETED = "bspstrike_targeted";
    public static final String COLUMN_BSPSTRIKE_LANDING = "bspstrike_landing";
    public static final String COLUMN_BSPSTRIKE_TARGET = "bspstrike_target";
    public static final String COLUMN_BSPSTRIKE_TYPE = "bspstrike_type";
    public static final String COLUMN_BSPSTRIKE_NAME = "bspstrike_name";
    public static final String COLUMN_BSPSTRIKE_COST = "bspstrike_cost";
    public static final String COLUMN_BSPSTRIKE_TARGETNO = "bspstrike_targetno";
    public static final String COLUMN_BSPSTRIKE_GRPSIZE = "bspstrike_grpsize";
    public static final String COLUMN_BSPSTRIKE_GROUPS = "bspstrike_groups";
    public static final String COLUMN_BSPSTRIKE_DMGTYPE = "bspstrike_dmgtype";
    public static final String COLUMN_BSPSTRIKE_FOREIGN = "bspstrike_foreign";
    public static final String TABLE_NAME_FORCECARDS = "table_forcecards";
    public static final String COLUMN_FORCECARDS_FORCEID = "forcecards_forceid";
    public static final String COLUMN_FORCECARDS_CARDID = "forcecards_cardid";

    private final Context context;

    public DatabaseGame(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public DatabaseGame(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public DatabaseGame(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_GAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_GAME_NAME + " TEXT, "
                + COLUMN_GAME_ACTIVE + " INTEGER, "
                + COLUMN_GAME_SMART_HEAT + " INTEGER, "
                + COLUMN_GAME_PILOT_DAMAGE + " INTEGER, "
                + COLUMN_GAME_USE_TICS + " INTEGER, "
                + COLUMN_GAME_HEXLESS + " INTEGER, "
                + COLUMN_GAME_FORCEDWITHDRAWAL + " INTEGER, "
                + COLUMN_GAME_BALANCEDINIT + " INTEGER, "
                + COLUMN_GAME_SCENARIO + " INTEGER, "
                + COLUMN_GAME_EXTECM + " INTEGER, "
                + COLUMN_GAME_IMMOBILE + " INTEGER, "
                + COLUMN_GAME_FORCE1 + " INTEGER, "
                + COLUMN_GAME_FORCE1_TYPE + " TEXT, "
                + COLUMN_GAME_FORCE2 + " INTEGER, "
                + COLUMN_GAME_FORCE2_TYPE + " TEXT);";

        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_TURN + " ("
                + COLUMN_ID + " INTEGER, "
                + COLUMN_TURN_NUMBER + " INTEGER, "
                + COLUMN_TURN_INITIATIVE + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_TURN_NUMBER + "));";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_NAME_UNITTURN + " ("
                + COLUMN_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " // game id
                + COLUMN_GAME_NUMBER         + " INTEGER, " // game number
                + COLUMN_TURN_NUMBER         + " INTEGER, "  // turn number
                + COLUMN_UNITTURN_UNIT_KEY   + " INTEGER, " //unit key
                + COLUMN_UNITTURN_TARGET_COMP + " INTEGER, "
                + COLUMN_UNITTURN_RESOLVED   + " INTEGER, "
                + COLUMN_UNITTURN_EXT_HEAT   + " INTEGER, "
                + COLUMN_UNITTURN_MOVE_TYPE  + " TEXT, "
                + COLUMN_UNITTURN_MOVE_COMP  + " INTEGER, "
                + COLUMN_UNITTURN_STOOD      + " INTEGER, "
                + COLUMN_UNITTURN_HEXES      + " INTEGER, "
                + COLUMN_UNITTURN_TMM        + " INTEGER,"
                + COLUMN_UNITTURN_HASPSR     + " INTEGER,"
                + COLUMN_UNITTURN_HASPHYSPSR + " INTEGER,"
                + COLUMN_UNITTURN_SPOTTING   + " INTEGER,"
                + COLUMN_UNITTURN_RESERVED_PHYS + " TEXT, "
                + COLUMN_UNITTURN_TAGGED     + " INTEGER);";

        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_TARGET_DATA + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID                  + " INTEGER, "
                + COLUMN_TARGET_TARG         + " INTEGER, "
                + COLUMN_TARGET_RANGE        + " INTEGER, "
                + COLUMN_TARGET_OTHER        + " INTEGER, "
                + COLUMN_TARGET_PARTIAL      + " INTEGER, "
                + COLUMN_TARGET_FACING       + " TEXT, "
                + COLUMN_TARGET_FORWARD      + " INTEGER, "
                + COLUMN_TARGET_INDIRECT     + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + "));";
        // TODO add an index to the target data table
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_WEAPON + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID + " INTEGER, " // target data id
                + COLUMN_WEAPON_ID + " INTEGER, "
                + COLUMN_WEAPON_TOHIT + " INTEGER, "
                + COLUMN_WEAPON_STATUS + " TEXT, "
                + COLUMN_WEAPON_LOCATION_TABLE + " TEXT, "
                + COLUMN_WEAPON_DICE1 + " INTEGER, "
                + COLUMN_WEAPON_DICE2 + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + ", " + COLUMN_WEAPON_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_DAMAGE + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID + " INTEGER, "
                + COLUMN_DAMAGE_TYPE + " INTEGER, "
                + COLUMN_DAMAGE_WEAPON + " INTEGER, "
                + COLUMN_DAMAGE_SHOOTER + " INTEGER, "
                + COLUMN_DAMAGE_TARGET + " INTEGER, "
                + COLUMN_DAMAGE_APPLIED + " INTEGER, "
                + COLUMN_DAMAGE_CLUSTER + " TEXT, "
                + COLUMN_DAMAGE_INDEX + " INTEGER, "
                + COLUMN_DAMAGE_DESCRIPTION + " TEXT, "
                + COLUMN_DAMAGE_CHECKTYPE + " TEXT, "
                + COLUMN_DAMAGE_EXTHEAT + " INTEGER, "
                + COLUMN_DAMAGE_EDGEUSED + " INTEGER, "
                + COLUMN_DAMAGE_PARENT_TYPE + " TEXT, "
                + COLUMN_DAMAGE_PARENT + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_DAMAGE_GROUP + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID + " INTEGER, " //grouping
                + COLUMN_GROUP_SEQ + " INTEGER, "
                + COLUMN_GROUP_DAMAGE + " INTEGER, "
                + COLUMN_GROUP_LOCATION + " TEXT, "
                + COLUMN_GROUP_LOCATION_CONV + " TEXT, "
                + COLUMN_GROUP_CRITCHECK + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + ", " + COLUMN_GROUP_SEQ + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_CHECK + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID + " INTEGER, " // check key/sequence number
                + COLUMN_CHECK_TOHIT + " INTEGER, "
                + COLUMN_CHECK_ROLLED + " TEXT, "
                + COLUMN_CHECK_PASSED + " INTEGER, "
                + COLUMN_CHECK_COMPLETE + " INTEGER, "
                + COLUMN_CHECK_TYPE + " TEXT, "
                + COLUMN_CHECK_SPECIAL + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_PHYSICAL + " ("
                + COLUMN_TARGET_UNITTURN_KEY + " INTEGER, "
                + COLUMN_ID + " INTEGER, " // check key/sequence number
                + COLUMN_PHYSICAL_NAME + " TEXT, "
                + COLUMN_PHYSICAL_DAMAGE + " INTEGER, "
                + COLUMN_PHYSICAL_GROUPING + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_TARGET_UNITTURN_KEY + ", " + COLUMN_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_BSPSTRIKE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BSPSTRIKE_AVAILABLE + " INTEGER, "
                + COLUMN_BSPSTRIKE_COST + " INTEGER, "
                + COLUMN_BSPSTRIKE_TARGET + " TEXT, "
                + COLUMN_BSPSTRIKE_NAME + " TEXT, "
                + COLUMN_BSPSTRIKE_TYPE + " TEXT, "
                + COLUMN_BSPSTRIKE_DMGTYPE + " TEXT, "
                + COLUMN_BSPSTRIKE_GRPSIZE + " INTEGER, "
                + COLUMN_BSPSTRIKE_GROUPS + " TEXT, "
                + COLUMN_BSPSTRIKE_FOREIGN + " INTEGER, "
                + COLUMN_BSPSTRIKE_LANDING + " INTEGER, "
                + COLUMN_BSPSTRIKE_TARGETED + " TEXT, "
                + COLUMN_BSPSTRIKE_TARGETNO + " INTEGER);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_BSPTARGETS + " (" + COLUMN_ID + " INTEGER, "
                + COLUMN_BSPTARGET_ID + " INTEGER, "
                + COLUMN_BSPTARGET_RANGE + " INTEGER, "
                + COLUMN_BSPTARGET_DICE + " TEXT, "
                + COLUMN_BSPTARGET_STATUS + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_BSPTARGET_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_FORCECARDS + " ("
                + COLUMN_FORCECARDS_FORCEID + " INTEGER, "
                + COLUMN_FORCECARDS_CARDID + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_FORCECARDS_FORCEID + ", " + COLUMN_FORCECARDS_CARDID + "));";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        if (db == null) db = this.getWritableDatabase();
        drop_table(db, TABLE_NAME_GAME);
        drop_table(db, TABLE_NAME_TURN);
        drop_table(db, TABLE_NAME_UNITTURN);
        drop_table(db, TABLE_NAME_TARGET_DATA);
        drop_table(db, TABLE_NAME_WEAPON);
        drop_table(db, TABLE_NAME_DAMAGE);
        drop_table(db, TABLE_NAME_CHECK);
        drop_table(db, TABLE_NAME_DAMAGE_GROUP);
        drop_table(db, TABLE_NAME_PHYSICAL);
        drop_table(db, TABLE_NAME_BSPSTRIKE);
        drop_table(db, TABLE_NAME_BSPTARGETS);
        drop_table(db, TABLE_NAME_FORCECARDS);

        onCreate(db);
    }

    public void addGame(Game game) {
        long rowId;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GAME_NAME, game.getName());
        cv.put(COLUMN_GAME_ACTIVE, game.isActive() ? 1 : 0);
        cv.put(COLUMN_GAME_SMART_HEAT, game.isSmartHeat() ? 1 : 0);
        cv.put(COLUMN_GAME_PILOT_DAMAGE, game.isPilotDamage() ? 1 : 0);
        cv.put(COLUMN_GAME_USE_TICS, game.isUseTics() ? 1 : 0);
        cv.put(COLUMN_GAME_HEXLESS, game.isHexless() ? 1 : 0);
        cv.put(COLUMN_GAME_FORCEDWITHDRAWAL, game.isOvForcedWithdrawal()?1:0);
        cv.put(COLUMN_GAME_BALANCEDINIT, game.isBalanceInitiative()?1:0);
        cv.put(COLUMN_GAME_SCENARIO, game.getScenarioKey());
        cv.put(COLUMN_GAME_EXTECM, game.isExternalECM() ? 1 : 0);
        cv.put(COLUMN_GAME_IMMOBILE,game.getImmobileMod());

        cv.put(COLUMN_GAME_FORCE1, game.getForce(0).getKey());
        cv.put(COLUMN_GAME_FORCE1_TYPE, game.getForceOneType().toString());
        cv.put(COLUMN_GAME_FORCE2, game.getForce(1).getKey());
        cv.put(COLUMN_GAME_FORCE2_TYPE, game.getForceTwoType().toString());
        if (game.getGameKey() >= 0) {
            cv.put(COLUMN_ID, game.getGameKey());
            rowId = db.update(TABLE_NAME_GAME, cv, COLUMN_ID + " = " + game.getGameKey(), null);
        } else {
            rowId = db.insert(TABLE_NAME_GAME, null, cv);
            game.setGameKey((int) rowId);
        }
        // add any cards (will update if existing and add if new)
        addBspStrikeCards(game.getForce(0));
        addBspStrikeCards(game.getForce(1));
        // add the current turn
        if (rowId != -1 && game.isActive()) {
            addTurn(db, game.getThisTurn(), (int) rowId);
        }
    }

    public Game loadGame(int gameID) {
        Game game = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_GAME, new String[]{"*"}, COLUMN_ID + " = " + gameID, null, null, null, null);
        if (cur.moveToNext()) {
            try (OVDatabaseForce forceDB = new OVDatabaseForce(context)) {
                ForceList secondForce, firstForce;
                if (Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE2_TYPE)) == Game.PlayerType.AI) {
                    firstForce = forceDB.getList(getCursorInt(cur, COLUMN_GAME_FORCE1), true);
                    secondForce = forceDB.getAiList(getCursorInt(cur, COLUMN_GAME_FORCE2));
                } else {
                    firstForce = forceDB.getList(getCursorInt(cur, COLUMN_GAME_FORCE1), false);
                    secondForce = forceDB.getList(getCursorInt(cur, COLUMN_GAME_FORCE2), false);
                }
                game = new Game(getCursorString(cur, COLUMN_GAME_NAME), firstForce, secondForce);
            }
            game.setGameKey(gameID);
            game.setActive(getCursorInt(cur, COLUMN_GAME_ACTIVE) > 0);
            game.setSmartHeat(getCursorInt(cur, COLUMN_GAME_SMART_HEAT) > 0);
            game.setPilotDamage(getCursorInt(cur, COLUMN_GAME_PILOT_DAMAGE) > 0);
            game.setUseTics(getCursorInt(cur, COLUMN_GAME_USE_TICS) > 0);
            game.setHexless(getCursorInt(cur, COLUMN_GAME_HEXLESS) > 0);
            game.setOvForcedWithdrawal(getCursorInt(cur, COLUMN_GAME_FORCEDWITHDRAWAL) > 0);
            game.setBalanceInitiative(getCursorInt(cur, COLUMN_GAME_BALANCEDINIT) > 0);
            game.setExternalECM(getCursorInt(cur, COLUMN_GAME_EXTECM) > 0);
            game.generateScenarioData(getCursorInt(cur, COLUMN_GAME_SCENARIO));
            game.setImmobileMod(getCursorInt(cur, COLUMN_GAME_IMMOBILE));
            game.setForceOneType(Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE1_TYPE)));
            game.setForceTwoType(Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE2_TYPE)));
            // check for BSP cards - must happen before the turn load as strikes might be referenced in damage records
            getBSPStrikeCards(game.getForce(0), game);
            getBSPStrikeCards(game.getForce(1), game);
            loadTurn(db, game);
            if (game.getAiForce()!=null)game.getAiForce().initListOnLoad(); // do any setup work for dynamic data not stored in the DB

        }
        assert (game!=null);

        cur.close();
        return game;
    }

    public void loadTurn(SQLiteDatabase db, Game game) {
        Cursor cur = db.query(TABLE_NAME_TURN, new String[]{"*"}, COLUMN_ID + " = " + game.getGameKey(), null, null, null, COLUMN_TURN_NUMBER + " DESC");
        if (cur.moveToFirst()) {

            game.getThisTurn().setTurnNumber(getCursorInt(cur, COLUMN_TURN_NUMBER));
            game.getThisTurn().setInitiative(getCursorInt(cur, COLUMN_TURN_INITIATIVE));
            /*
             * We assume we are loading a turn when first opening an existing game so the unit turn
             * list should be populated with intialised instances
             */
            List<UnitTurn> list = game.getThisTurn().getUnitTurns();
            Cursor unitCur = db.query(TABLE_NAME_UNITTURN, new String[]{"*"},
                    COLUMN_GAME_NUMBER + " = " + game.getGameKey() + " AND " + COLUMN_TURN_NUMBER + " = " + getCursorInt(cur, COLUMN_TURN_NUMBER),
                    null, null, null, null);
            while (unitCur.moveToNext()) {
                int unitKey = getCursorInt(unitCur, COLUMN_UNITTURN_UNIT_KEY);
                IUnitData unit = game.findUnitByKey(unitKey);

                if (unit != null) {
                    UnitTurn unitTurn = unit.getTurn();
                    unitTurn.setKey(getCursorInt(unitCur, COLUMN_ID));
                    unitTurn.setTargetingComplete(getCursorInt(unitCur, COLUMN_UNITTURN_TARGET_COMP) == 1);
                    unitTurn.getMoveData().setHexesMoved(getCursorInt(unitCur, COLUMN_UNITTURN_HEXES));
                    unitTurn.getMoveData().setType(UnitMove.MoveType.valueOf(getCursorString(unitCur, COLUMN_UNITTURN_MOVE_TYPE)), false);
                    unitTurn.getMoveData().setTmm(getCursorInt(unitCur, COLUMN_UNITTURN_TMM));
                    unitTurn.getMoveData().setMoveLocked(getCursorInt(unitCur, COLUMN_UNITTURN_MOVE_COMP) == 1);
                    unitTurn.setTurnResolved(getCursorInt(unitCur, COLUMN_UNITTURN_RESOLVED) == 1);
                    unitTurn.addExternalHeat(getCursorInt(unitCur, COLUMN_UNITTURN_EXT_HEAT));
                    unitTurn.getMoveData().setStood(getCursorInt(unitCur, COLUMN_UNITTURN_STOOD) == 1);
                    unitTurn.setHasDamagePsr(getCursorInt(unitCur, COLUMN_UNITTURN_HASPSR) == 1);
                    unitTurn.setHasPhysDamagePsr(getCursorInt(unitCur, COLUMN_UNITTURN_HASPHYSPSR) == 1);
                    unitTurn.setSpottingThisTurn(getCursorInt(unitCur, COLUMN_UNITTURN_SPOTTING));
                    unitTurn.setTaggedThisTurn(getCursorInt(unitCur, COLUMN_UNITTURN_TAGGED) == 1);
                    unitTurn.setReservePhysicalAttack(PhysicalWeapon.PhysicalWeaponType.valueOf(getCursorString(unitCur,COLUMN_UNITTURN_RESERVED_PHYS)));
                    loadTargetData(db, unitTurn, game);
                    loadCheckRecords(db, unitTurn);
//                    loadPhysicalWeapon(db,unitTurn);
                    list.add(unitTurn); // the turn will already have been added to the unit itself but add it to the list for the overall turn
                }
            }
            // damage records can only be loaded after all target data has been retrieved
            for (UnitTurn unitTurn : game.getThisTurn().getUnitTurns()) {
                loadDamageRecords(db, unitTurn, game);
            }
            unitCur.close();
        }
        cur.close();
    }

    /*
     * If the unit has fallen there could be multiple physical weapons associated
     * with it for this turn, depending on the type parameter we are interested in the
     * fall weapon (900 < ID < 950) or the attack weapon (950 < ID < 1000)
     */
    public IWeapon loadPhysicalWeapon(SQLiteDatabase db, UnitTurn unitTurn, int weaponKey) {
        SQLiteDatabase dbIn = db;
        if (dbIn == null) dbIn = this.getReadableDatabase();
        PhysicalWeapon phys = null;
        Cursor unitCur = dbIn.query(TABLE_NAME_PHYSICAL, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey() + " AND "
                        + COLUMN_ID + " = " + weaponKey,
                null, null, null, null);
        if (unitCur.moveToFirst()) {
//            if (getCursorInt(unitCur,COLUMN_ID)<950 ^ attackType){
//                //this is the wrong type of weapon for what we are looking for, try the next one
//                if (!unitCur.moveToNext()) return null; // no other weapons to try
//            }
            phys = new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.valueOf(getCursorString(unitCur, COLUMN_PHYSICAL_NAME)),
                    getCursorInt(unitCur, COLUMN_PHYSICAL_DAMAGE),
                    PhysicalWeapon.PhysicalHitGrouping.valueOf(getCursorString(unitCur, COLUMN_PHYSICAL_GROUPING)));
            phys.setId(getCursorInt(unitCur, COLUMN_ID));
        }
        unitCur.close();
        return phys;
    }

    public void loadTargetData(SQLiteDatabase db, UnitTurn unitTurn, Game game) {
        Cursor unitCur = db.query(TABLE_NAME_TARGET_DATA, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey(),
                null, null, null, null);
        while (unitCur.moveToNext()) {
            TargetData target = new TargetData(unitTurn.getUnit(), game.findUnitByKey(getCursorInt(unitCur, COLUMN_TARGET_TARG)));
            target.setID(getCursorInt(unitCur, COLUMN_ID));
            target.setRange(getCursorInt(unitCur, COLUMN_TARGET_RANGE));
            target.setOther(getCursorInt(unitCur, COLUMN_TARGET_OTHER));
            target.setPartialCover(getCursorInt(unitCur, COLUMN_TARGET_PARTIAL) == 1);
            target.setFacing(TargetData.LocTable.valueOf(getCursorString(unitCur, COLUMN_TARGET_FACING)));
            target.setForwardArc(getCursorInt(unitCur, COLUMN_TARGET_FORWARD) == 1);
            target.setIndirect(getCursorInt(unitCur, COLUMN_TARGET_INDIRECT) == 1);
            unitTurn.addTarget(target);
            loadTargetWeapons(db, unitTurn.getKey(), target, unitTurn);
        }
        unitCur.close();
    }

    private IEquipment getEquipmentFromKey(IUnitData unit, int key) {
        IEquipment ret = null;
        if (key >= 0) {
            for (IEquipment equip : unit.getEquipment()) {
                if (equip.getID() == key) {
                    ret = equip;
                    break;
                }
            }
        }
        return ret;
    }

    public void loadDamageRecords(SQLiteDatabase db, UnitTurn unitTurn, Game game) {
        String dice;
        Cursor unitCur = db.query(TABLE_NAME_DAMAGE, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey(),
                null, null, null, null);
        while (unitCur.moveToNext()) {
            int type = getCursorInt(unitCur, COLUMN_DAMAGE_TYPE);
            int index = getCursorInt(unitCur, COLUMN_DAMAGE_INDEX);
            // find the parent if there is one
            IChildLink parent = null;
            int parentIndex = getCursorInt(unitCur, COLUMN_DAMAGE_PARENT);
            if (parentIndex>0) {
                switch (IChildLink.ChildType.valueOf(getCursorString(unitCur, COLUMN_DAMAGE_PARENT_TYPE))) {
                    case DAMAGE:
                        for (IDamageRecord rec : unitTurn.getDamageRecords()) {
                            if (rec.getKey() == parentIndex) {
                                parent = rec;
                                break;
                            }
                        }
                        break;
                    case CHECK:
                        for (GenericCheck rec : unitTurn.getTurnChecks()) {
                            if (rec.getKey() == parentIndex) {
                                parent = rec;
                                break;
                            }
                        }
                        break;
                }
            }
            IDamageRecord toAdd = null;
            switch (type) {
                case 1: //general damage record
                    int weaponKey = getCursorInt(unitCur, COLUMN_DAMAGE_WEAPON);
                    IWeapon weapon = null;
                    IUnitData shooter = game.findUnitByKey(getCursorInt(unitCur, COLUMN_DAMAGE_SHOOTER));
                    if (weaponKey >= 4000){
                        weapon = new BspWeapon(Game.findBspStrike(weaponKey-4000,false));
                    } else if (weaponKey >= 950) {
                        // physical weapon so need to retrieve from the unit turn, must have already been loaded
//                    weapon = unitTurn.getPhysicalAttack().getWeapon();
                        if (shooter!=null) //add this check to stop corrupt data causing a catastrophic failure
                            weapon = shooter.getTurn().getPhysicalAttack().getWeapon();
                    } else if (weaponKey >= 900) {
                        // physical weapon fall type so get from db
                        weapon = loadPhysicalWeapon(db, unitTurn, weaponKey);
                    } else {
                        // The shooter must be set at this point, something has gone wrong if it isn't
                        // bail out of this damage record if there is an issue rather than fail catastrophically
                        if (shooter==null) continue;
                        weapon = getWeaponFromKey(shooter, weaponKey);
                    }
                    TargetData target;
                    if (shooter != null) // normal case
                        target = getTargetFromKey(shooter.getTurn(), getCursorInt(unitCur, COLUMN_DAMAGE_TARGET));
                    else // fall damage and ammo explosions
                        target = new TargetData(null, unitTurn.getUnit());
                    DamageRecord record = new DamageRecord(weapon, target);
                    record.setIndex(index);
                    record.markAsSent(); //is always sent if it has been committed to the DB
                    record.setKey(getCursorInt(unitCur, COLUMN_ID));
                    record.setHeatDamage(getCursorInt(unitCur, COLUMN_DAMAGE_EXTHEAT));
                    dice = getCursorString(unitCur, COLUMN_DAMAGE_CLUSTER);
                    if (!dice.isEmpty())
                        record.setClusterDice(new TwoDSix(dice));
                    record.setApplied(getCursorInt(unitCur, COLUMN_DAMAGE_APPLIED) == 1);

                    loadDamageGroup(db, unitTurn, record);
                    toAdd = record;
                    unitTurn.addDamage(record);
                    break;
                case 2: // critical hit
                    TwoDSix critted = null;
                    dice = getCursorString(unitCur, COLUMN_DAMAGE_CLUSTER);
                    if (!dice.isEmpty())
                        critted = new TwoDSix(dice);
                    PilotCheck.PilotCheckType pilot = null;
                    dice = getCursorString(unitCur, COLUMN_DAMAGE_CHECKTYPE);
                    if (!dice.equals("NULL")) {
                        pilot = PilotCheck.PilotCheckType.valueOf(dice);
                    }
                    IEquipment equip = getEquipmentFromKey(unitTurn.getUnit(), getCursorInt(unitCur, COLUMN_DAMAGE_WEAPON));
                    toAdd = new CriticalHit(getCursorString(unitCur, COLUMN_DAMAGE_DESCRIPTION),
                            critted,
                            pilot,
                            equip, parent);
                    toAdd.setApplied(getCursorInt(unitCur, COLUMN_DAMAGE_APPLIED) == 1);
                    toAdd.setIndex(index);
                    toAdd.setEdgeUsed(getCursorInt(unitCur, COLUMN_DAMAGE_EDGEUSED)==1);
                    unitTurn.addDamage(toAdd);
                    break;
                case 3: // damage message
                    DamageMessage msg = new DamageMessage(getCursorString(unitCur, COLUMN_DAMAGE_DESCRIPTION),getCursorInt(unitCur,COLUMN_DAMAGE_INDEX), parent);
                    msg.markAsSent();
                    dice = getCursorString(unitCur, COLUMN_DAMAGE_CLUSTER);
                    if (!dice.isEmpty())
                        msg.setDice(new TwoDSix(dice));
                    msg.setIndex(index);
                    unitTurn.addDamage(msg);
                    toAdd = msg;
                    break;
            }
            if (toAdd!=null && parent!=null){
                toAdd.setParent(parent);
                parent.getChildren().add(toAdd);
            }
        }
        unitCur.close();
    }

    public void loadDamageGroup(SQLiteDatabase db, UnitTurn unitTurn, DamageRecord record) {
        Cursor unitCur = db.query(TABLE_NAME_DAMAGE_GROUP, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey() + " AND "
                        + COLUMN_ID + " = " + record.getKey(),
                null, null, null, null);
        while (unitCur.moveToNext()) {
            DamageRecord.DamageGrouping group = record.addGrouping(getCursorInt(unitCur, COLUMN_GROUP_DAMAGE),
                    new TwoDSix(getCursorString(unitCur, COLUMN_GROUP_LOCATION)));
            try {
                String dbConv = getCursorString(unitCur, COLUMN_GROUP_LOCATION_CONV);
                if (dbConv != null) {
                    group.setConvertedLocation(OVSegment.OVLocation.valueOf(dbConv));
                }
//                dbConv = getCursorString(unitCur,COLUMN_GROUP_CRITCHECK);
//                if (dbConv != null && !dbConv.equals("NULL"))
//                    group.setCritCheck(new TwoDSix(dbConv));
            } catch (IllegalArgumentException e) {
                // just means no conversion has been applied so continue
            }

        }
        unitCur.close();
    }

    public void loadCheckRecords(SQLiteDatabase db, UnitTurn unitTurn) {
        Cursor unitCur = db.query(TABLE_NAME_CHECK, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey(),
                null, null, null, null);
        while (unitCur.moveToNext()) {
            unitTurn.addCheck(GenericCheck.newInstance(unitCur, unitTurn.getUnit()));
        }
        unitCur.close();
    }

    private void loadTargetWeapons(SQLiteDatabase db, long turnKey, TargetData target, UnitTurn unitTurn) {
        Cursor cur = db.query(TABLE_NAME_WEAPON, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + turnKey + " AND " + COLUMN_ID + " = " + target.getID(),
                null, null, null, null);
        while (cur.moveToNext()) {
            int ticKey = getCursorInt(cur, COLUMN_WEAPON_ID);
            TargetWeapon weapon = null;
            //find the weapon associated with the shooting unit we are reading about
            if (ticKey > 900) { //this is a physical attack
                IWeapon phys = loadPhysicalWeapon(db, unitTurn, ticKey);
                if (phys != null) {
                    weapon = new TargetWeapon(target, phys);
                    if (ticKey > 950)
                        unitTurn.setPhysicalAttack(weapon);
                }
            } else {
                for (TargetWeapon targetWeapon : unitTurn.getWeaponList()) {
                    weapon = targetWeapon;
                    if (weapon.getWeapon().getID() == ticKey) {
                        weapon.setTarget(target);
                        break;
                    }
                    weapon = null;
                }
            }

            if (weapon != null) {
                weapon.setLocked(true); // if it is assigned to a target then it is locked
                weapon.setToHit(getCursorInt(cur, COLUMN_WEAPON_TOHIT));
                weapon.setStatus(TargetWeapon.ShotStatus.valueOf(getCursorString(cur, COLUMN_WEAPON_STATUS)));
                String read = getCursorString(cur,COLUMN_WEAPON_LOCATION_TABLE);
                if (read!=null) {
                    TargetData.LocTable locTable = TargetData.LocTable.valueOf(read);
                    weapon.setLocationTable(locTable);
                }
                // might not have dice so check first
                if (getCursorInt(cur, COLUMN_WEAPON_DICE1) >= 0 && getCursorInt(cur, COLUMN_WEAPON_DICE2) >= 0)
                    weapon.setRolled(new TwoDSix(getCursorInt(cur, COLUMN_WEAPON_DICE1), getCursorInt(cur, COLUMN_WEAPON_DICE2)));
                // add links - physical weapon has already been assigned to the physical weapon slot
                // and shouldn't be included in general weapon attacks or it will mess with the
                // shooting phase
                if (ticKey < 900)
                    target.addWeapon(weapon);

            }
        }
        cur.close();
    }

    public void addCheckRecord(SQLiteDatabase dbin, GenericCheck check, @NonNull UnitTurn unitTurn) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        boolean insert = check.getKey() < 0;
        if (!insert) {
            // if this is from an external system the id will be set already even if it is new
            Cursor unitCur = db.query(TABLE_NAME_CHECK, new String[]{"*"},
                    COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey() + " AND " + COLUMN_ID + " = " + check.getKey(),
                    null, null, null, null);
            if (unitCur.getCount() <= 0) insert = true;//nothing in the DB so insert not update
            unitCur.close();
        }
        ContentValues cv = check.setContents();
        long unitKey = unitTurn.getKey();
//        long unitKey = (unitTurn != null) ? unitTurn.getKey() : check.getUnit().getTurn().getKey();
        cv.put(COLUMN_TARGET_UNITTURN_KEY, unitKey);

        if (insert) db.insert(TABLE_NAME_CHECK, null, cv);
        else
            db.update(TABLE_NAME_CHECK, cv, COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurn.getKey()
                    + " AND " + COLUMN_ID + " = " + check.getKey(), null);
    }

    public void addCheckRecords(SQLiteDatabase dbin, UnitTurn unitTurn) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        for (GenericCheck check : unitTurn.getTurnChecks()) {
            addCheckRecord(dbin, check, unitTurn);
        }
        if (dbin == null) db.close();
    }

    private TargetData getTargetFromKey(UnitTurn unit, int targetID) {
        TargetData target = null;
        for (TargetData targetData : unit.getAllTargets()) {
            target = targetData;
            if (target.getID() == targetID) break;
            else target = null;
        }
        return target;
    }

    private IWeapon getWeaponFromKey(IUnitData unit, int weaponID) {
        IWeapon weapon = null;
        for (IWeapon iWeapon : unit.getWeapons()) {
            weapon = iWeapon;
            if (weapon.getID() == weaponID) break;
            else weapon = null;
        }
        return weapon;
    }

    public void addTurn(SQLiteDatabase dbin, Turn turn, int gameKey) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ID, gameKey);
        cv.put(COLUMN_TURN_NUMBER, turn.getTurnNumber());
        cv.put(COLUMN_TURN_INITIATIVE, turn.getInitiative());
        if (db.insert(TABLE_NAME_TURN, null, cv) >= 0) {

            for (UnitTurn unitTurn : turn.getUnitTurns()) {
                addUnitTurn(db, unitTurn);
            }
        }
    }

    public void addUnitTurnHeader(SQLiteDatabase dbin, UnitTurn turn) {
        int gameKey = Game.current.getGameKey();
        int turnNumber = Game.current.getTurnNumber();
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        setUnitTurnValues(cv, turn, gameKey, turnNumber);
        long rowid = turn.getKey();
        if (rowid < 0) {
            rowid = db.insert(TABLE_NAME_UNITTURN, null, cv);
            turn.setKey(rowid);
        } else {
            cv.put(COLUMN_ID, rowid);
            db.update(TABLE_NAME_UNITTURN, cv, COLUMN_ID + " = " + rowid, null);
        }
    }
    public void addUnitTurn(SQLiteDatabase dbin, UnitTurn turn) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        addUnitTurnHeader(db,turn);
//        ContentValues cv = new ContentValues();
//        setUnitTurnValues(cv, turn, gameKey, turnNumber);
        long rowid = turn.getKey();
//        if (rowid < 0) {
//            rowid = db.insert(TABLE_NAME_UNITTURN, null, cv);
//            turn.setKey(rowid);
//        } else {
//            cv.put(COLUMN_ID, rowid);
//            db.update(TABLE_NAME_UNITTURN, cv, COLUMN_ID + " = " + rowid, null);
//        }
        // physical weapon must be saved before the targetdata or it will not have initialised the
        // ID field causing errors - the related target weapon record needs the id
//        if (turn.getPhysicalAttack()!=null){
//            addPhysicalWeapon(db,turn.getPhysicalAttack().getWeapon(),rowid);
//        }
        int count = 1;
        // make sure we aren't duplicating keys by accident
        for (TargetData data : turn.getAllTargets()) {
            if (data.getID() >= count) count = data.getID() + 1;
        }
        boolean firstSave;
        // don't use iterator as we get concurrent modification errors
//        for (TargetData data : turn.getAllTargets()) {
        TargetData data;
        for (int i = 0;i < turn.getAllTargets().size();i++) {
            data = turn.getAllTargets().get(i);
            firstSave = false;
            if (data.getID() < 0) {
                //this means we haven't saved this target before
                data.setID(count++);
                firstSave = true;
            }
            addTargetData(db, data, rowid, firstSave);
        }
        if (turn.getPhysicalAttack() != null) {
            //we have a physical attack so add to the DB, Target data SHOULD already be saved
            addTargetWeapon(db, turn.getPhysicalAttack(), rowid, turn.getPhysicalAttack().getTargetData().getID());
        }
        addCheckRecords(db, turn);
        addDamageRecords(db, turn.getDamageRecords(), rowid /*turnNumber*/);

    }

    public void addPhysicalWeapon(SQLiteDatabase dbin, IWeapon physical, long unitturnID) {
        if (!(physical instanceof PhysicalWeapon)) {
            System.out.println("Called DB Update for PhysicalWeapon with IWeapon that is not Physical");
            return;
        }
        PhysicalWeapon physicalWeapon = (PhysicalWeapon) physical;
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TARGET_UNITTURN_KEY, unitturnID);
        cv.put(COLUMN_PHYSICAL_NAME, physicalWeapon.getPhysType().toString());
        cv.put(COLUMN_PHYSICAL_DAMAGE, physicalWeapon.getDamage());
        cv.put(COLUMN_PHYSICAL_GROUPING, physicalWeapon.getGrouping().toString());
        if (physical.getID() < 0) { // hasn't been written to the DB yet
            physicalWeapon.generateId();
            cv.put(COLUMN_ID, physicalWeapon.getID());
            db.insert(TABLE_NAME_PHYSICAL, null, cv);
        } else {
            // if we have received the physical weapon from another device it will have an ID even
            // though it doesn't yet exist in the DB so check whether we want an insert or update
            Cursor unitCur = db.query(TABLE_NAME_PHYSICAL, new String[]{"*"},
                    COLUMN_TARGET_UNITTURN_KEY + " = " + unitturnID + " AND "
                            + COLUMN_ID + " = " + physicalWeapon.getID(),
                    null, null, null, null);
            cv.put(COLUMN_ID, physicalWeapon.getID());
            if (unitCur.moveToNext()) {
                db.update(TABLE_NAME_PHYSICAL, cv, COLUMN_TARGET_UNITTURN_KEY + " = " + unitturnID + " AND "
                        + COLUMN_ID + " = " + physicalWeapon.getID(), null);
            } else {
                db.insert(TABLE_NAME_PHYSICAL, null, cv);
            }
            unitCur.close();
        }
    }
    private int getDamageType(IDamageRecord rec) {
        if (rec instanceof DamageRecord) {
            return 1;
        } else if (rec instanceof CriticalHit) {
            return 2;
        } else if (rec instanceof DamageMessage) {
            return 3;
        }
        return 0;
    }
    public void addDamageRecords(SQLiteDatabase dbin, List<IDamageRecord> damageList, long unitturnID) {
        long nextKey = 1;
        SQLiteDatabase db = dbin;
//        IDamageRecord damage = null;
        if (db == null) db = this.getWritableDatabase();
        // determine the next available key before we start
        for (IDamageRecord damage : damageList) {
            nextKey = Math.max(damage.getKey() + 1, nextKey);
        }
        for (IDamageRecord damage : damageList) {
//            damage = iDamageRecord;
            ContentValues cv = new ContentValues();
            boolean newRecord = false;
            cv.put(COLUMN_DAMAGE_TYPE, getDamageType(damage));
            cv.put(COLUMN_DAMAGE_INDEX,damage.getIndex());
            cv.put(COLUMN_TARGET_UNITTURN_KEY, unitturnID);
            if (damage.getKey() <= 0) {
                damage.setKey(nextKey++);
                newRecord = true;
            }
            cv.put(COLUMN_ID, damage.getKey());
            int id = -1;
            if (damage instanceof DamageRecord) {
                IWeapon weapon = damage.getWeapon();
                if (weapon != null) id = weapon.getID();
            } else if (damage instanceof CriticalHit) {
                id = ((CriticalHit) damage).getEquipmentKey();
            }
            cv.put(COLUMN_DAMAGE_WEAPON, (id >= 0) ? id : 999); // in case a physical weapon hasn't been updated
            id = -1;
            TargetData target = damage.getTarget();
            if (target != null) id = target.getID();
            cv.put(COLUMN_DAMAGE_TARGET, id);
            id = -1;
            if (target != null) {
                IUnitData shooter = target.getShooter();
                if (shooter != null)
                    id = shooter.getKey();
            }
            cv.put(COLUMN_DAMAGE_SHOOTER, id);
            id = damage.isApplied() ? 1 : 0;
            cv.put(COLUMN_DAMAGE_APPLIED, id);
            cv.put(COLUMN_DAMAGE_EDGEUSED,damage.isEdgeUsed()?1:0);
            TwoDSix dice = damage.getClusterDice();
            String out = "";
            if (dice != null) {
                out = dice.toString();
//                out = dice.getRollType().toString();
//                for (int i = 1; i <= dice.getNumberOfDice(); i++) {
//                    out += ",";
//                    out += dice.getDice(i);
//                }
            }
            cv.put(COLUMN_DAMAGE_CLUSTER, out);

            out = "";
            if (damage instanceof DamageMessage) out = ((DamageMessage) damage).getDescription();
            cv.put(COLUMN_DAMAGE_DESCRIPTION, out);
            id = 0;
            if (damage instanceof DamageRecord) id = ((DamageRecord) damage).getHeatDamage();
            cv.put(COLUMN_DAMAGE_EXTHEAT, id);

            out = "NULL";
            if (damage instanceof CriticalHit) {
                PilotCheck.PilotCheckType checkType = ((CriticalHit) damage).getCheck();
                if (checkType != null) out = checkType.toString();
            }
            cv.put(COLUMN_DAMAGE_CHECKTYPE, out);

            // set parent ref
            if (damage.getParent()!=null){
                cv.put(COLUMN_DAMAGE_PARENT_TYPE,damage.getParent().getRecordType().toString());
                cv.put(COLUMN_DAMAGE_PARENT,damage.getParent().getKey());
            } else
                cv.put(COLUMN_DAMAGE_PARENT,-1);

            if (newRecord) {
                long rowid = db.insert(TABLE_NAME_DAMAGE, null, cv);
                if (rowid >= 0) {
                    int count = 1;
                    for (Iterator<DamageRecord.DamageGrouping> it2 = damage.getDamage().iterator(); it2.hasNext(); count++) {
                        addDamageGrouping(db, it2.next(), unitturnID, damage.getKey(), count);
                    }
                } else {
                    System.out.println("INSERT of TABLE DAMAGE failed to add record PANIC PANIC id: " + damage.getKey());
                }
            } else {
                // if updating a damage record we don't need to update groupings as these don't change after being created
                int retCode = db.update(TABLE_NAME_DAMAGE, cv,
                        COLUMN_TARGET_UNITTURN_KEY + " = " + unitturnID + " AND " +
                                COLUMN_ID + " = " + damage.getKey(), null);
                if (retCode <= 0) {
                    System.out.println("UPDATE of TABLE DAMAGE failed to match an existing record PANIC PANIC id: " + damage.getKey());
                }
            }
        }
    }
    public void addDamageGrouping(SQLiteDatabase dbin, DamageRecord.DamageGrouping damage, long unitturnID, long damageKey, int seqno) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TARGET_UNITTURN_KEY, unitturnID);
        cv.put(COLUMN_ID, damageKey);
        cv.put(COLUMN_GROUP_SEQ, seqno);
        cv.put(COLUMN_GROUP_DAMAGE, damage.damage);
        String out = "";
        if (damage.getLocation() != null) {
            out = damage.getLocation().toString();
//            for (int i = 1; i <= damage.location.getNumberOfDice(); i++) {
//                out += ",";
//                out += damage.location.getDice(i);
//            }
        }
        cv.put(COLUMN_GROUP_LOCATION, out);

        out = "NULL";
//        if (damage.getCritCheck() != null)
//            out = damage.getCritCheck().toString();
        cv.put(COLUMN_GROUP_CRITCHECK, out);
        if (damage.getConvertedLocation() != null) {
            cv.put(COLUMN_GROUP_LOCATION_CONV, damage.getConvertedLocation().toString());
        }
//        cv.put(COLUMN_GROUP_LOCATION,damage.convertedLocation.toString());
        db.insert(TABLE_NAME_DAMAGE_GROUP, null, cv);
    }
    public void addTargetData(SQLiteDatabase dbin, TargetData target, long id, boolean firstSave) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = getUnitTurnValues(target, id);
        long rowid;
        if (!firstSave) {
            rowid = db.update(TABLE_NAME_TARGET_DATA, cv, COLUMN_TARGET_UNITTURN_KEY + " = " + id + " AND " + COLUMN_ID + " = " + target.getID(), null);
        } else {
            rowid = db.insert(TABLE_NAME_TARGET_DATA, null, cv);
        }
        if (rowid >= 0) {
            for (TargetWeapon targetWeapon : target.getWeapons()) {
                addTargetWeapon(db, targetWeapon, id, target.getID());
            }
        }
    }
    @NonNull
    private static ContentValues getUnitTurnValues(TargetData target, long id) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TARGET_UNITTURN_KEY, id);
        cv.put(COLUMN_ID, target.getID());
        cv.put(COLUMN_TARGET_TARG, target.getTarget().getKey());
        cv.put(COLUMN_TARGET_RANGE, target.getRange());
        cv.put(COLUMN_TARGET_OTHER, target.getOther());
        cv.put(COLUMN_TARGET_PARTIAL, target.isPartialCover() ? 1 : 0);
        cv.put(COLUMN_TARGET_FACING, target.getFacing().toFile());
        cv.put(COLUMN_TARGET_FORWARD, target.isForwardArc() ? 1 : 0);
        cv.put(COLUMN_TARGET_INDIRECT, target.isIndirect() ? 1 : 0);
        return cv;
    }
    public void addTargetWeapon(SQLiteDatabase dbin, TargetWeapon weapon, long unitturnID, long targetID) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (weapon.getWeapon().getID() >= 900 || weapon.getWeapon().getID() < 0) { // this is a physical attack so we need to store weapon details
            addPhysicalWeapon(db, weapon.getWeapon(), unitturnID);
        }
        cv.put(COLUMN_TARGET_UNITTURN_KEY, unitturnID);
        cv.put(COLUMN_ID, targetID);
        cv.put(COLUMN_WEAPON_ID, weapon.getWeapon().getID());
        cv.put(COLUMN_WEAPON_TOHIT, weapon.getToHit());
        cv.put(COLUMN_WEAPON_STATUS, weapon.getStatus().toString());
        cv.put(COLUMN_WEAPON_LOCATION_TABLE, weapon.getLocationTable().toFile());

        if (weapon.getRolled() != null) {
            cv.put(COLUMN_WEAPON_DICE1, weapon.getRolled().getDice(1));
            cv.put(COLUMN_WEAPON_DICE2, weapon.getRolled().getDice(2));
        } else {
            cv.put(COLUMN_WEAPON_DICE1, -1);
            cv.put(COLUMN_WEAPON_DICE2, -1);
        }

        //does it already exist or do we need to create it?
        Cursor cur = db.query(TABLE_NAME_WEAPON,
                new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = ?" + " AND " + COLUMN_ID + " = ?"
                        + " AND " + COLUMN_WEAPON_ID + " = ?",
                new String[]{Long.toString(unitturnID),
                        Integer.toString(weapon.getTargetData().getID()),
                        Integer.toString(weapon.getWeapon().getID())}, null, null, null);
        if (cur.getCount() > 0) {
            db.update(TABLE_NAME_WEAPON, cv, COLUMN_TARGET_UNITTURN_KEY + " = " + unitturnID
                    + " AND " + COLUMN_ID + " = " + weapon.getTargetData().getID()
                    + " AND " + COLUMN_WEAPON_ID + " = " + weapon.getWeapon().getID(), null);
        } else {
            db.insert(TABLE_NAME_WEAPON, null, cv);
        }
        cur.close();


    }
    private void drop_table(SQLiteDatabase db, String tab_name) {
        String query = "DROP TABLE IF EXISTS " + tab_name;
        db.execSQL(query);
    }
    public static int getCursorInt(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getInt(index);
    }
    public static String getCursorString(Cursor cur, String columnName) {
        int index = cur.getColumnIndex(columnName);
        return cur.getString(index);
    }
    public List<GameListAdapter.GameCatalogEntry> getGameCatalog(boolean onlyCurrent) {
        List<GameListAdapter.GameCatalogEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = null;
        if (onlyCurrent) {
            selection = COLUMN_GAME_ACTIVE + " = 1";
        }
        Cursor cur = db.query(TABLE_NAME_GAME, new String[]{"*"}, selection, null, null, null, null);
        while (cur.moveToNext()) {
            GameListAdapter.GameCatalogEntry entry
                    = new GameListAdapter.GameCatalogEntry(getCursorInt(cur, COLUMN_ID),
                    getCursorString(cur, COLUMN_GAME_NAME));
            list.add(entry);
        }
        cur.close();
        return list;
    }
    public void addTargetingReverted(IUnitData unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_TARGET_DATA, COLUMN_TARGET_UNITTURN_KEY + " = " + unit.getTurn().getKey(), null);
        db.delete(TABLE_NAME_WEAPON, COLUMN_TARGET_UNITTURN_KEY + " = " + unit.getTurn().getKey(), null);
        // update the targeting complete flag on unitturn record
        updateMovePhase(unit.getTurn(), db);
    }
    public void addTargetingComplete(IUnitData unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = 1;
        for (TargetData data : unit.getTurn().getAllTargets()) {
            if (data.getID() >= count) count = data.getID() + 1;
        }
        for (TargetData data : unit.getTurn().getAllTargets()) {
            if (data.hasWeapons()) {
                boolean firstSave = false;

                if (data.getID() < 0) {
                    data.setID(count++);
                    firstSave = true;
                }
                addTargetData(db, data, unit.getTurn().getKey(), firstSave);
            }
        }
        updateMovePhase(unit.getTurn(), db);
    }
    /*
     * addPhysicalTarget: called when a physical attack is locked in (resolved)
     */
    public void addPhysicalTarget(IUnitData unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        TargetWeapon weapon = unit.getTurn().getPhysicalAttack();
        TargetData data = weapon.getTargetData();

        addTargetData(db, data, unit.getTurn().getKey(), !targetDataExists(db, unit.getTurn().getKey(), data.getID()));
        // just need to add the target weapon to the existing targetdata
        addTargetWeapon(db, weapon, unit.getTurn().getKey(), data.getID());

        if (weapon.getStatus() == TargetWeapon.ShotStatus.HIT) {
            //update the target in case there is a fall added (kicked a leg into destruction for example)
            for (IDamageRecord dmg : unit.getTurn().getDamageRecords()) {
                if (dmg.getWeapon() instanceof PhysicalWeapon && dmg.getWeapon().getID() < 0) {
                    // send the attached weapon
                    addPhysicalWeapon(null, dmg.getWeapon(), unit.getTurn().getKey());
                }
            }
        }
    }

    private boolean targetDataExists(SQLiteDatabase db, long unitTurnKey, int dataKey) {
        Cursor unitCur = db.query(TABLE_NAME_TARGET_DATA, new String[]{"*"},
                COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurnKey + " AND " + COLUMN_ID + " = " + dataKey,
                null, null, null, null);
        boolean exists = unitCur.moveToFirst();
        unitCur.close();
        return exists;
    }

    private void setUnitTurnValues(ContentValues cv, UnitTurn turn, int gameKey, int turnNumber) {
        cv.put(COLUMN_GAME_NUMBER, gameKey);
        cv.put(COLUMN_TURN_NUMBER, turnNumber);
        cv.put(COLUMN_UNITTURN_UNIT_KEY, turn.getUnit().getKey());
        cv.put(COLUMN_UNITTURN_TARGET_COMP, turn.isTargetingComplete() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_RESOLVED, turn.turnComplete() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_EXT_HEAT, turn.getExternalHeat());
        cv.put(COLUMN_UNITTURN_MOVE_TYPE, turn.getMove().toString());
        cv.put(COLUMN_UNITTURN_MOVE_COMP, turn.getMoveData().isMoveLocked() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_STOOD, turn.getMoveData().isStood() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_HASPSR, turn.isHasDamagePsr() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_HASPHYSPSR, turn.isHasPhysDamagePsr() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_SPOTTING, turn.getSpottingThisTurn());
        cv.put(COLUMN_UNITTURN_TAGGED, turn.isTaggedThisTurn() ? 1 : 0);
        cv.put(COLUMN_UNITTURN_HEXES, turn.getMoveData().getHexesMoved());
        cv.put(COLUMN_UNITTURN_TMM, turn.getMoveData().getTmm());
        cv.put(COLUMN_UNITTURN_RESERVED_PHYS, turn.getReservePhysicalAttack().toString());
    }

    public void updateMovePhase(UnitTurn turn, SQLiteDatabase dbin) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        Cursor unitCur = db.query(TABLE_NAME_UNITTURN, new String[]{"*"},
                COLUMN_ID + " = " + turn.getKey(),
                null, null, null, null);

        if (unitCur.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, getCursorInt(unitCur, COLUMN_ID));
            setUnitTurnValues(cv, turn, getCursorInt(unitCur, COLUMN_GAME_NUMBER), getCursorInt(unitCur, COLUMN_TURN_NUMBER));

            db.update(TABLE_NAME_UNITTURN, cv, COLUMN_ID + " = " + getCursorInt(unitCur, COLUMN_ID), null);
        }
        unitCur.close();
    }

    public void updateTurn(SQLiteDatabase dbin, Game game) {
        SQLiteDatabase db = dbin;
        if (db == null) db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ID, game.getGameKey());
        cv.put(COLUMN_TURN_NUMBER, game.getThisTurn().getTurnNumber());
        cv.put(COLUMN_TURN_INITIATIVE, game.getThisTurn().getInitiative());
        db.update(TABLE_NAME_TURN, cv, COLUMN_ID + " = " + game.getGameKey() + " AND "
                + COLUMN_TURN_NUMBER + " = " + game.getThisTurn().getTurnNumber(), null);


    }

    public void updateTargetWeapon(TargetWeapon weapon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long unitTurnKey = weapon.getTargetData().getShooter().getTurn().getKey();

        // check for physical weapon first as the save will update the ID if it is the first time
        if (weapon.getWeapon().getID() >= 900 || weapon.getWeapon().getID() < 0) { // this is a physical attack so we need to store weapon details
            addPhysicalWeapon(db, weapon.getWeapon(), unitTurnKey);
        }

        cv.put(COLUMN_TARGET_UNITTURN_KEY, unitTurnKey);
        cv.put(COLUMN_ID, weapon.getTargetData().getID());
        cv.put(COLUMN_WEAPON_ID, weapon.getWeapon().getID());
        cv.put(COLUMN_WEAPON_TOHIT, weapon.getToHit());
        cv.put(COLUMN_WEAPON_STATUS, weapon.getStatus().toString());
        if (weapon.getRolled() != null) {
            cv.put(COLUMN_WEAPON_DICE1, weapon.getRolled().getDice(1));
            cv.put(COLUMN_WEAPON_DICE2, weapon.getRolled().getDice(2));
        } else {
            cv.put(COLUMN_WEAPON_DICE1, -1);
            cv.put(COLUMN_WEAPON_DICE2, -1);
        }
        int ret = db.update(TABLE_NAME_WEAPON, cv, COLUMN_TARGET_UNITTURN_KEY + " = " + unitTurnKey
                + " AND " + COLUMN_ID + " = " + weapon.getTargetData().getID()
                + " AND " + COLUMN_WEAPON_ID + " = " + weapon.getWeapon().getID(), null);
        if (ret == 0) // no rows updated - try an insert
            db.insert(TABLE_NAME_WEAPON, null, cv);

    }

    public Game deleteGame(int gameID) {
        Game game = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_GAME, new String[]{"*"}, COLUMN_ID + " = " + gameID, null, null, null, null);
        if (cur.moveToNext()) {
            try (OVDatabaseForce forceDB = new OVDatabaseForce(context)) {
                ForceList secondForce;
                if (Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE2_TYPE)) == Game.PlayerType.AI) {
                    secondForce = forceDB.getAiList(getCursorInt(cur, COLUMN_GAME_FORCE2));
                } else {
                    secondForce = forceDB.getList(getCursorInt(cur, COLUMN_GAME_FORCE2),false);
                }
                // don't need to worry about the AiGame flag since we are just deleting the lists not playing
                game = new Game(getCursorString(cur, COLUMN_GAME_NAME), forceDB.getList(getCursorInt(cur, COLUMN_GAME_FORCE1),false), secondForce);
            }
            game.setGameKey(gameID);
            game.setActive(false);
            game.setForceOneType(Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE1_TYPE)));
            game.setForceTwoType(Game.PlayerType.valueOf(getCursorString(cur, COLUMN_GAME_FORCE2_TYPE)));
            loadTurn(db, game);
            addGame(game);
        }
        cur.close();
        return game;
    }

    public UnitTurn loadSingleUnitTurn(Game game, IUnitData unit, int turnNumber) {
        UnitTurn unitTurn = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor unitCur = db.query(TABLE_NAME_UNITTURN, new String[]{"*"},
                COLUMN_GAME_NUMBER + " = " + game.getGameKey() + " AND "
                        + COLUMN_TURN_NUMBER + " = " + turnNumber + " AND "
                        + COLUMN_UNITTURN_UNIT_KEY + " = " + unit.getKey(),
                null, null, null, null);
        if (unitCur.moveToFirst()) {

            unitTurn = new UnitTurn(unit);
            unitTurn.setKey(getCursorInt(unitCur, COLUMN_ID));
            unitTurn.setTargetingComplete(getCursorInt(unitCur, COLUMN_UNITTURN_TARGET_COMP) == 1);
            unitTurn.getMoveData().setHexesMoved(getCursorInt(unitCur, COLUMN_UNITTURN_HEXES));
            unitTurn.getMoveData().setType(UnitMove.MoveType.valueOf(getCursorString(unitCur, COLUMN_UNITTURN_MOVE_TYPE)), false);
            unitTurn.getMoveData().setTmm(getCursorInt(unitCur, COLUMN_UNITTURN_TMM));
            unitTurn.getMoveData().setMoveLocked(getCursorInt(unitCur, COLUMN_UNITTURN_MOVE_COMP) == 1);
            unitTurn.setTurnResolved(getCursorInt(unitCur, COLUMN_UNITTURN_RESOLVED) == 1);
            unitTurn.addExternalHeat(getCursorInt(unitCur, COLUMN_UNITTURN_EXT_HEAT));
            unitTurn.getMoveData().setStood(getCursorInt(unitCur, COLUMN_UNITTURN_STOOD) == 1);
            unitTurn.setHasDamagePsr(getCursorInt(unitCur, COLUMN_UNITTURN_HASPSR) == 1);
            unitTurn.setHasPhysDamagePsr(getCursorInt(unitCur, COLUMN_UNITTURN_HASPHYSPSR) == 1);
            unitTurn.setTaggedThisTurn(getCursorInt(unitCur, COLUMN_UNITTURN_SPOTTING) == 1);
            unitTurn.setTaggedThisTurn(getCursorInt(unitCur, COLUMN_UNITTURN_TAGGED) == 1);
            unitTurn.setReservePhysicalAttack(PhysicalWeapon.PhysicalWeaponType.valueOf(getCursorString(unitCur, COLUMN_UNITTURN_RESERVED_PHYS)));
            loadTargetData(db, unitTurn, game);
            loadCheckRecords(db, unitTurn);
//                    loadPhysicalWeapon(db,unitTurn);
            loadDamageRecords(db, unitTurn, game);
        }
        // damage records can only be loaded after all target data has been retrieved

        unitCur.close();
        return unitTurn;
    }
    public void getBSPStrikeCards(ForceList force, Game game){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor unitCur = db.query(TABLE_NAME_FORCECARDS, new String[]{"*"},
                COLUMN_FORCECARDS_FORCEID + " = " + force.getKey(),
                null, null, null, null);
        while (unitCur.moveToNext()){
            force.getBspStrikes(Turn.Phase.SETUP).add(getBSPStrikeCard(getCursorInt(unitCur,COLUMN_FORCECARDS_CARDID), game));
        }
    }
    public BSPStrike getBSPStrikeCard(int key, Game game){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor unitCur = db.query(TABLE_NAME_BSPSTRIKE, new String[]{"*"},
                COLUMN_ID + " = " + key,
                null, null, null, null);
        if (!unitCur.moveToFirst())return null;
        BSPStrike strike = new BSPStrike(unitCur);
        Cursor targetCur = db.query(TABLE_NAME_BSPTARGETS, new String[]{"*"},
                COLUMN_ID + " = " + key,
                null, null, null, null);
        while (targetCur.moveToNext()){
            // forces should already be loaded
            strike.addNewTarget(game.findUnitByKey(getCursorInt(targetCur,COLUMN_BSPTARGET_ID)),
                    getCursorInt(targetCur,COLUMN_BSPTARGET_RANGE),
                    getCursorString(targetCur,COLUMN_BSPTARGET_DICE),
                    getCursorString(targetCur,COLUMN_BSPTARGET_STATUS));
        }
        return strike;
    }
    public void addBspStrikeCards(ForceList force) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete(TABLE_NAME_FORCECARDS, COLUMN_FORCECARDS_FORCEID + " = " + force.getKey(), null);
        for (BSPStrike card : force.getBspStrikes(Turn.Phase.SETUP)) {
            addBspStrikeCard(card);
            cv.put(COLUMN_FORCECARDS_FORCEID, force.getKey());
            cv.put(COLUMN_FORCECARDS_CARDID,card.getKey());
            db.insert(TABLE_NAME_FORCECARDS, null, cv);
        }
    }
    public void addBspStrikeCard(BSPStrike card){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        card.setContent(cv);
        if (card.getKey() >= 0) {
            cv.put(DatabaseGame.COLUMN_ID,card.getKey());
            db.update(TABLE_NAME_BSPSTRIKE, cv, COLUMN_ID + " = " + card.getKey(), null);
        } else {
            long key = db.insert(TABLE_NAME_BSPSTRIKE, null, cv);
            if (key>=0)card.setKey((int)key);
        }
        db.delete(TABLE_NAME_BSPTARGETS,COLUMN_ID + " = " + card.getKey(),null);
        for (BSPStrike.StrikeTarget target : card.getTargetList()){
            ContentValues cvTarg = new ContentValues();
            cvTarg.put(COLUMN_ID,card.getKey());
            cvTarg.put(COLUMN_BSPTARGET_ID,target.unit.getKey());
            cvTarg.put(COLUMN_BSPTARGET_RANGE,target.range);
            if (target.dice!=null)
                cvTarg.put(COLUMN_BSPTARGET_DICE,target.dice.toString());
            else
                cvTarg.put(COLUMN_BSPTARGET_DICE,"NONE");
            cvTarg.put(COLUMN_BSPTARGET_STATUS,target.status.toString());
            // always an add because we delete all related entries before the save
            db.insert(TABLE_NAME_BSPTARGETS, null, cvTarg);
        }
    }
}
