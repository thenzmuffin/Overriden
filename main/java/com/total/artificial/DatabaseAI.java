package com.total.artificial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.total.overide.OVDatabaseForce;
import com.total.overiden.IUnitData;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAI extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OverrideAI.db";
    private static final int DATABASE_VERSION = 34;
    public static final String TABLE_NAME_LIVE_COMMANDER = "ai_live_commander";
    public static final String COLUMN_LIVE_TACTIC = "ai_live_tactic";
    public static final String TABLE_NAME_LIVE_DECK = "ai_live_deck";
    public static final String COLUMN_LIVE_LIST = "ai_live_list";
    public static final String COLUMN_LIVE_UNIT = "ai_live_unit";
    public static final String COLUMN_LIVE_COMMANDER = "ai_live_commander";
    public static final String COLUMN_CARD_ID = "ai_card_id";
    public static final String TABLE_NAME_DECK = "ai_deck";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DECK_ID = "ai_deck_id";
    public static final String COLUMN_DECK_NAME = "ai_deck_name";
    public static final String COLUMN_DECK_ROLE = "ai_deck_role";
    public static final String TABLE_NAME_CARD = "ai_card";
    public static final String COLUMN_CARD_PRIORITY = "ai_card_priority";
    public static final String COLUMN_CARD_PRIORITY_MOD = "ai_card_priority_mod";
    public static final String COLUMN_CARD_PRIORITY_TYPE = "ai_card_priority_type";
    public static final String COLUMN_CARD_HEAT_DEF = "ai_card_heat_def";
    public static final String COLUMN_CARD_HEAT_CRIT = "ai_card_heat_crit";
    public static final String COLUMN_CARD_HEAT_KILL = "ai_card_heat_kill";
    public static final String COLUMN_CARD_HEAT_DEATH = "ai_card_heat_death";
    public static final String COLUMN_CARD_WEAPON = "ai_card_weapon";
    public static final String COLUMN_CARD_TARGETS = "ai_card_targets";
    public static final String COLUMN_CARD_TACTICS = "ai_card_tactics";
    public static final String COLUMN_CARD_BRACKETS = "ai_card_brackets";
    public static final String TABLE_NAME_MOVE_INSTRUCTION = "ai_move";
    public static final String COLUMN_MOVE_ID = "ai_move_id";
    public static final String COLUMN_MOVE_TYPE = "ai_move_type";
    public static final String COLUMN_MOVE_DESC = "ai_move_desc";
    public static final String COLUMN_MOVE_AUTO_RULE = "ai_move_rule";
    public static final String COLUMN_MOVE_AUTO_PARAM = "ai_move_param";
    public static final String COLUMN_MOVE_DESIGNATION = "ai_move_desig";
    public static final String COLUMN_MOVE_QUALIFIERS = "ai_move_quals";
    public static final String TABLE_NAME_MOVE_RESULT = "ai_move_result";
    public static final String COLUMN_RESULT_ID = "ai_result_id";
    public static final String COLUMN_RESULT_LABEL = "ai_result_label";
    public static final String COLUMN_RESULT_NEXT = "ai_result_next";
    public static final String TABLE_NAME_INSTRUCTION_SET = "ai_set";
    public static final String TABLE_NAME_COMMANDER = "ai_commander";
    public static final String COLUMN_COMMANDER_DESC = "ai_commander_description";
    public static final String TABLE_NAME_COMM_CARD = "ai_comm_card";
    public static final String COLUMN_COMM_CARD_ID = "ai_comm_card_id";
    public static final String TABLE_NAME_TARGET = "ai_target";
    public static final String COLUMN_COMMANDER_ID = "ai_commander_id";
    public static final String COLUMN_TARGET_DESIGNATION = "ai_target_desig";
    public static final String COLUMN_TARGET_TYPE = "ai_target_type";
    public static final String TABLE_NAME_ORDERS = "ai_orders";
    public static final String COLUMN_ORDERS_TYPE = "ai_orders_type";
    public static final String COLUMN_ORDERS_MOD = "ai_orders_mod";
    public static final String COLUMN_ORDERS_CHOICE = "ai_orders_choice";
    public static final String TABLE_NAME_CHANGE_UP = "ai_change_up";
    public static final String COLUMN_COMM_CHANGE_TYPE = "ai_comm_change_type";
    public static final String COLUMN_COMM_CHANGE_NEXT = "ai_comm_change_next";
//    public class CommanderItem{
//        public String name;
//        public int key;
//        public CommanderItem(String name, int key){
//            super();
//            this.name = name;
//            this.key = key;
//        }
//
//        @NonNull
//        @Override
//        public String toString() {
//            return name;
//        }
//    }
//    public static final String COLUMN_SET_ID = "ai_set_id";
    public DatabaseAI(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_LIVE_COMMANDER + " ("
                + COLUMN_LIVE_LIST + " INTEGER PRIMARY KEY, " //force list id
                + COLUMN_LIVE_COMMANDER + " INTEGER, "
                + COLUMN_LIVE_TACTIC + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_LIVE_DECK + " ("
                + COLUMN_LIVE_LIST + " INTEGER, " //force list id
                + COLUMN_LIVE_UNIT + " INTEGER, " //unit key
                + COLUMN_DECK_ID + " INTEGER, " //deck being used
                + COLUMN_CARD_ID + " INTEGER, " //currently selected card
                + COLUMN_LIVE_COMMANDER + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_LIVE_LIST + ", " + COLUMN_LIVE_UNIT + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_DECK + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DECK_NAME + " TEXT, "
                + COLUMN_DECK_ROLE + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_CARD + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DECK_ID + " INTEGER, "
                + COLUMN_CARD_PRIORITY + " INTEGER, "
                + COLUMN_CARD_PRIORITY_MOD + " INTEGER, "
                + COLUMN_CARD_PRIORITY_TYPE + " TEXT, "
                + COLUMN_CARD_HEAT_DEF + " INTEGER, "
                + COLUMN_CARD_HEAT_CRIT + " INTEGER, "
                + COLUMN_CARD_HEAT_KILL + " INTEGER, "
                + COLUMN_CARD_HEAT_DEATH + " INTEGER, "
                + COLUMN_CARD_WEAPON + " TEXT, "
                + COLUMN_CARD_TACTICS + " TEXT, "
                + COLUMN_CARD_BRACKETS + " TEXT, "
                + COLUMN_CARD_TARGETS + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_COMMANDER + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_COMMANDER_DESC + " TEXT);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_MOVE_INSTRUCTION + " ("
                + COLUMN_CARD_ID + " INTEGER, "
                + COLUMN_MOVE_ID + " INTEGER, "
                + COLUMN_MOVE_TYPE + " TEXT, "
                + COLUMN_MOVE_DESC + " TEXT, "
                + COLUMN_MOVE_AUTO_RULE + " INTEGER, "
                + COLUMN_MOVE_AUTO_PARAM + " INTEGER, "
                + COLUMN_MOVE_DESIGNATION + " TEXT, "
                + COLUMN_MOVE_QUALIFIERS + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_CARD_ID + ", " + COLUMN_MOVE_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_MOVE_RESULT + " ("
                + COLUMN_CARD_ID + " INTEGER, "
                + COLUMN_MOVE_ID + " INTEGER, "
                + COLUMN_RESULT_ID + " INTEGER, "
                + COLUMN_RESULT_LABEL + " TEXT, "
                + COLUMN_RESULT_NEXT + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_CARD_ID + ", " + COLUMN_MOVE_ID + ", " + COLUMN_RESULT_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_COMM_CARD + " ("
                + COLUMN_COMMANDER_ID + " INTEGER, "
                + COLUMN_ID + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_COMMANDER_ID + ", " + COLUMN_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_TARGET + " ("
                + COLUMN_COMMANDER_ID + " INTEGER, "
                + COLUMN_COMM_CARD_ID + " INTEGER, "
                + COLUMN_TARGET_DESIGNATION + " TEXT, "
                + COLUMN_TARGET_TYPE + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_COMMANDER_ID + ", " + COLUMN_COMM_CARD_ID + ", " + COLUMN_TARGET_DESIGNATION + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_ORDERS + " ("
                + COLUMN_COMMANDER_ID + " INTEGER, "
                + COLUMN_COMM_CARD_ID + " INTEGER, "
                + COLUMN_ID + " INTEGER, "
                + COLUMN_ORDERS_MOD + " INTEGER, "
                + COLUMN_ORDERS_TYPE + " TEXT, "
                + COLUMN_ORDERS_CHOICE + " TEXT, "
                + "PRIMARY KEY (" + COLUMN_COMMANDER_ID + ", " + COLUMN_COMM_CARD_ID + ", " + COLUMN_ID + "));";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_NAME_CHANGE_UP + " ("
                + COLUMN_COMMANDER_ID + " INTEGER, "
                + COLUMN_COMM_CARD_ID + " INTEGER, "
                + COLUMN_ID + " INTEGER, "
                + COLUMN_COMM_CHANGE_TYPE + " TEXT, "
                + COLUMN_COMM_CHANGE_NEXT + " INTEGER, "
                + "PRIMARY KEY (" + COLUMN_COMMANDER_ID + ", " + COLUMN_COMM_CARD_ID + ", " + COLUMN_ID +  "));";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        SQLiteDatabase dbLocal = db;
        if (dbLocal == null) dbLocal = this.getWritableDatabase();
        drop_table(dbLocal, TABLE_NAME_LIVE_COMMANDER);
        drop_table(dbLocal, TABLE_NAME_LIVE_DECK);
        drop_table(dbLocal, TABLE_NAME_DECK);
        drop_table(dbLocal, TABLE_NAME_CARD);
        drop_table(dbLocal, TABLE_NAME_MOVE_INSTRUCTION);
        drop_table(dbLocal, TABLE_NAME_MOVE_RESULT);
        drop_table(dbLocal, TABLE_NAME_COMMANDER);
        drop_table(dbLocal, TABLE_NAME_COMM_CARD);
        drop_table(dbLocal, TABLE_NAME_TARGET);
        drop_table(dbLocal, TABLE_NAME_ORDERS);
        drop_table(dbLocal, TABLE_NAME_CHANGE_UP);
        drop_table(dbLocal, TABLE_NAME_INSTRUCTION_SET);
        onCreate(dbLocal);
    }
    private void drop_table(SQLiteDatabase db, String tab_name) {
        String query = "DROP TABLE IF EXISTS " + tab_name;
        db.execSQL(query);
    }
    public void loadLiveCommander(AiForceList list){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_LIVE_COMMANDER, new String[]{"*"}, COLUMN_LIVE_LIST + " = " + list.getKey(), null, null, null, null);
        if (cur.moveToFirst()) {
            list.getCommander().setCurrentTactic(AiEnums.Tactic.valueOf(OVDatabaseForce.getCursorString(cur,COLUMN_LIVE_TACTIC)));
        }
        cur.close();
    }
    public void saveLiveCommander(AiForceList list){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LIVE_LIST,list.getKey());
        cv.put(COLUMN_LIVE_COMMANDER, list.getCommander().getKey());
        cv.put(COLUMN_LIVE_TACTIC,list.getCommander().getCurrentTactic().name());
        db.insertWithOnConflict(TABLE_NAME_LIVE_COMMANDER, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public AiCommander loadCommander(int id){
        AiCommander ret = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_COMMANDER, new String[]{"*"}, COLUMN_ID + " = " + id, null, null, null, null);
        if (cur.moveToFirst()) {
            ret = new AiCommander(cur);
            loadCommanderCard(db,ret);
            loadCommanderTactics(db,ret);
//            loadCommanderTargets(db,ret);
//            loadCommandOrders(db,ret.getCommanderInstructions(),ret.getKey());
        }
        cur.close();
        return ret;
    }
    private void loadCommanderCard(SQLiteDatabase db, AiCommander comm){
        Cursor cur = db.query(TABLE_NAME_COMM_CARD, new String[]{"*"}, COLUMN_COMMANDER_ID + " = " + comm.getKey(), null, null, null, null);
        while (cur.moveToNext()) {
            AiCommanderCard card = new AiCommanderCard(cur);
            comm.addCard(card);
            loadCommanderInstructions(db,card);
            loadCommanderTargets(db,card);
            loadCommandOrders(db,card);
            loadCommanderChange(db,card);
        }
    }
    private void loadCommanderChange(SQLiteDatabase db, AiCommanderCard card){
        Cursor cur = db.query(TABLE_NAME_CHANGE_UP, new String[]{"*"}, COLUMN_COMMANDER_ID + " = " + card.getKey() + " AND " + COLUMN_COMM_CARD_ID + " = " + card.getKey(), null, null, null, null);
        while (cur.moveToNext()) {
            AiCommanderChangeRule change = new AiCommanderChangeRule(cur);
            card.getChangeUp().add(change);
        }
    }
    private void loadCommandOrders(SQLiteDatabase db, AiCommanderCard card){
        Cursor cur = db.query(TABLE_NAME_ORDERS, new String[]{"*"}, COLUMN_COMMANDER_ID + " = " + card.getParentKey() + " AND " + COLUMN_COMM_CARD_ID + " = " + card.getKey(), null, null, null, null);
        while (cur.moveToNext()){
            card.getCommanderInstructions().add(new AiCommanderInstructions(cur));
        }
        cur.close();
    }
    private void saveCommandOrders(SQLiteDatabase db, AiCommanderCard card){
        ContentValues cv = new ContentValues();
        int id = 0;
        for (AiCommanderInstructions inst : card.getCommanderInstructions()){
            id++;
            cv.put(COLUMN_COMMANDER_ID,card.getParentKey());
            cv.put(COLUMN_COMM_CARD_ID,card.getKey());
            cv.put(COLUMN_ID,id);
            cv.put(COLUMN_ORDERS_TYPE, inst.toString());
            cv.put(COLUMN_ORDERS_MOD, inst.getModifier());
            AiTargetChoice choice = inst.getChoice();
            if (choice!= null)
                cv.put(COLUMN_ORDERS_TYPE, choice.toString());
            db.insertWithOnConflict(TABLE_NAME_ORDERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
    private void saveCommandCardChange(SQLiteDatabase db, AiCommanderCard card){
        ContentValues cv = new ContentValues();
        for (AiCommanderChangeRule rule : card.getChangeUp()){
            cv.put(COLUMN_COMMANDER_ID,card.getParentKey());
            cv.put(COLUMN_COMM_CARD_ID,card.getKey());
            cv.put(COLUMN_ID,rule.getKey());
            cv.put(COLUMN_COMM_CHANGE_NEXT, rule.getNewCard());
            cv.put(COLUMN_COMM_CHANGE_TYPE, rule.getRule().toString());
            db.insertWithOnConflict(TABLE_NAME_CHANGE_UP, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
    public void saveCommander(AiCommander commander) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COMMANDER_DESC, commander.getName());
        if (commander.getKey() >= 0) {
            cv.put(COLUMN_ID, commander.getKey());
            db.update(TABLE_NAME_COMMANDER, cv, COLUMN_ID + " = " + commander.getKey(), null);
        } else {
            long key = db.insert(TABLE_NAME_COMMANDER, null, cv);
            if (key > 0) commander.setKey((int) key);
        }
        saveInstructions(db,commander.getKey() + 10000,commander.getTacticAnalysis());
        for (AiCommanderCard card : commander.getCards()){
            saveCommanderCard(db, card);
        }
    }
    private void saveCommanderCard(SQLiteDatabase db, AiCommanderCard card){
        int generated = card.getParentKey()*100 + card.getKey() + 1000000;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COMMANDER_ID, card.getParentKey());
        cv.put(COLUMN_ID, card.getKey());
        db.insertWithOnConflict(TABLE_NAME_COMM_CARD, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        saveInstructions(db,generated,card.getPreMoves());
        saveCommanderTargets(db,card);
        saveCommandOrders(db, card);
        saveCommandCardChange(db,card);
    }
    public void loadCommanderTactics(SQLiteDatabase dbin, AiCommander commander){
        SQLiteDatabase db = dbin==null?this.getReadableDatabase():dbin;
        loadMoveSet(db, commander.getKey() + 10000,commander.getTacticAnalysis());
//        loadMoveSet(db, commander.getKey() + 1000000,commander.getPreMoves());
    }
    public void loadCommanderInstructions(SQLiteDatabase dbin, AiCommanderCard card){
        SQLiteDatabase db = dbin==null?this.getReadableDatabase():dbin;
//        loadMoveSet(db, commander.getKey() + 10000,commander.getTacticAnalysis());
        int generated = card.getParentKey()*100 + card.getKey() + 1000000;
        loadMoveSet(db, generated,card.getPreMoves());
    }
    public void loadCommanderTargets(SQLiteDatabase dbin, AiCommanderCard card){
        SQLiteDatabase db = dbin==null?this.getReadableDatabase():dbin;
        Cursor cur = db.query(TABLE_NAME_TARGET, new String[]{"*"}, COLUMN_COMMANDER_ID + " = " + card.getParentKey() + " AND " + COLUMN_COMM_CARD_ID + " = " + card.getKey(), null, COLUMN_TARGET_DESIGNATION, null, null);
        AiTargetChoice choice;
        while (cur.moveToNext()){
            choice = new AiTargetChoice(cur);
            card.getTargetAnalysis().add(choice);
        }
    }
    private void saveCommanderTargets(SQLiteDatabase db, AiCommanderCard card){
        ContentValues cv = new ContentValues();
        for (AiTargetChoice choice : card.getTargetAnalysis()) {
            cv.put(COLUMN_COMMANDER_ID, card.getParentKey());
            cv.put(COLUMN_COMM_CARD_ID, card.getKey());
            cv.put(COLUMN_TARGET_DESIGNATION, choice.getDesignation());
            cv.put(COLUMN_TARGET_TYPE, choice.toTargetTypeString());
            db.insertWithOnConflict(TABLE_NAME_TARGET, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
    public List<ListItem> getCommanderList(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<ListItem> list = new ArrayList<>();
        Cursor cur = db.query(TABLE_NAME_COMMANDER, new String[]{"*"}, null, null, null, null, null);
        if (cur.moveToFirst()){
            list.add(new ListItem(OVDatabaseForce.getCursorString(cur,COLUMN_COMMANDER_DESC),OVDatabaseForce.getCursorInt(cur,COLUMN_ID)));
        }
        cur.close();
        return list;
    }
    public Cursor loadLiveDeck(AiForceList list){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME_LIVE_DECK, new String[]{"*"}, COLUMN_LIVE_LIST + " = " + list.getKey(), null, null, null, null);
    }
    public ArtificialDeck loadDeck(int deckId){
        ArtificialDeck deck = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_DECK, new String[]{"*"}, COLUMN_ID + " = " + deckId, null, null, null, null);
        if (cur.moveToFirst()){
            deck = new ArtificialDeck(cur);
            loadCardDeck(db,deck);
        }
        cur.close();
        return deck;
    }
    private void loadCardDeck(SQLiteDatabase db,ArtificialDeck deck){
        Cursor cur = db.query(TABLE_NAME_CARD, new String[]{"*"}, COLUMN_DECK_ID + " = " + deck.getId(), null, null, null, null);
        while (cur.moveToNext()){
            AiUnitCard card = new AiUnitCard(cur);
            deck.getCards().add(card);
            loadMoveSet(db,card.getId(),card.getMainMoves());
        }
    }
    private void loadMoveSet(SQLiteDatabase db, int id, List<AiInstruction> list){
        Cursor cur = db.query(TABLE_NAME_MOVE_INSTRUCTION, new String[]{"*"}, COLUMN_CARD_ID + " = " + id, null, null, null, null);
        while (cur.moveToNext()){
            AiInstruction move = AiInstruction.newInstance(cur);
            list.add(move);
            loadMoveResults(db, id, move);
        }
        cur.close();
    }
    private void loadMoveResults(SQLiteDatabase db, int id, AiInstruction move ){
        Cursor cur = db.query(TABLE_NAME_MOVE_RESULT, new String[]{"*"}, COLUMN_CARD_ID + " = " + id + " AND " + COLUMN_MOVE_ID + " = " + move.getIndex(), null, null, null, null);
        while (cur.moveToNext()) {
            move.addResult(OVDatabaseForce.getCursorString(cur,COLUMN_RESULT_LABEL),
                    OVDatabaseForce.getCursorInt(cur,COLUMN_RESULT_NEXT));
        }
        cur.close();
    }

    public void saveLiveDeck(int listId, IUnitData pilot){
        ArtificialPilot list = (ArtificialPilot) pilot;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LIVE_LIST, listId);
        cv.put(COLUMN_DECK_ID,list.getDeck().getId());
        cv.put(COLUMN_LIVE_UNIT,list.getKey());
        cv.put(COLUMN_LIVE_COMMANDER,list.getKey());
        cv.put(COLUMN_CARD_ID,list.getCurrentCardIndex());
        db.insertWithOnConflict(TABLE_NAME_LIVE_DECK, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public void saveDeck(ArtificialDeck deck){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DECK_NAME,deck.getDeckName());
        cv.put(COLUMN_DECK_ROLE,deck.getDeckRole().name());
        if (deck.getId()>=0){
            cv.put(COLUMN_ID,deck.getId());
            db.update(TABLE_NAME_DECK,cv,COLUMN_ID + " = " + deck.getId(),null);
        } else {
            long rowid = db.insert(TABLE_NAME_DECK, null, cv);
            deck.setId((int) rowid);
        }
        for (AiUnitCard card : deck.getCards()){
            saveCard(db,card,deck.getId());
        }
    }
    public void saveCard(SQLiteDatabase dbin, AiUnitCard card, int deckId){
        SQLiteDatabase db = dbin==null?this.getWritableDatabase():dbin;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DECK_ID,deckId);
        cv.put(COLUMN_CARD_PRIORITY,card.getPriority());
        cv.put(COLUMN_CARD_PRIORITY_MOD,card.getPriorityMod());
        cv.put(COLUMN_CARD_PRIORITY_TYPE,card.getPriorityModType());
        cv.put(COLUMN_CARD_HEAT_DEF,card.getDefaultHeat());
        cv.put(COLUMN_CARD_HEAT_CRIT,card.getCritHeat());
        cv.put(COLUMN_CARD_HEAT_KILL,card.getKillHeat());
        cv.put(COLUMN_CARD_HEAT_DEATH,card.getNearDeath());
        cv.put(COLUMN_CARD_WEAPON,card.getWeaponFormatted());
        cv.put(COLUMN_CARD_BRACKETS,card.getBracketsFormatted());
        cv.put(COLUMN_CARD_TARGETS,card.getTargetsFormatted());
        cv.put(COLUMN_CARD_TACTICS,card.getTacticsFormatted());

        if (card.getId()>=0){
            cv.put(COLUMN_ID,card.getId());
            db.update(TABLE_NAME_CARD,cv,COLUMN_ID + " = " + card.getId(),null);
        } else {
            long rowid = db.insert(TABLE_NAME_CARD, null, cv);
            card.setId((int) rowid);
        }
        saveInstructions(db,card.getId(),card.getMainMoves());
    }
    public void saveInstructions(SQLiteDatabase dbin,int id, List<AiInstruction> list){
        SQLiteDatabase db = dbin==null?this.getWritableDatabase():dbin;
        ContentValues cv = new ContentValues();
        for (AiInstruction move : list) {
            cv.put(COLUMN_CARD_ID, id);
            move.setContent(cv);

            db.insertWithOnConflict(TABLE_NAME_MOVE_INSTRUCTION, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            saveResults(db,id,move.getIndex(), move.getHolders());
        }
    }
    public void saveResults(SQLiteDatabase dbin,int setId,int moveId, List<AiInstruction.MoveChoice> list){
        SQLiteDatabase db = dbin==null?this.getWritableDatabase():dbin;
        ContentValues cv = new ContentValues();
        int count = 1;
        for (AiInstruction.MoveChoice choice : list) {
            cv.put(COLUMN_CARD_ID, setId);
            cv.put(COLUMN_MOVE_ID, moveId);
            cv.put(COLUMN_RESULT_ID, count++);
            cv.put(COLUMN_RESULT_LABEL, choice.toString());
            cv.put(COLUMN_RESULT_NEXT, choice.getNextInstruction());
            db.insertWithOnConflict(TABLE_NAME_MOVE_RESULT, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

//    private int generateInstructionSet(SQLiteDatabase db, String name){
//        ContentValues cv = new ContentValues();
//        cv.put(COLUMN_SET_DESC,name);
//        long rowid = db.insert(TABLE_NAME_INSTRUCTION_SET, null, cv);
//        return ((int) rowid);
//    }
    public static class ListItem {
        private final String name;
        public int index;
        public ListItem(String name, int key){
            super();
            this.name = name;
            this.index = key;
        }
        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
    public List<ListItem> getDeckList(){
        List<ListItem> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_NAME_DECK, new String[]{"*"}, null, null, null, null, null);
        while (cur.moveToNext()){
            String desc = OVDatabaseForce.getCursorString(cur,COLUMN_DECK_ROLE) + " : " + OVDatabaseForce.getCursorString(cur,COLUMN_DECK_NAME);
            ret.add(new ListItem(desc, OVDatabaseForce.getCursorInt(cur, COLUMN_ID)));
        }
        cur.close();
        return ret;
    }

    public void deleteDeck(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_DECK,COLUMN_ID + " = " + id,null);
        db.delete(TABLE_NAME_LIVE_DECK,COLUMN_DECK_ID + " = " + id,null);
        Cursor cur = db.query(TABLE_NAME_CARD, new String[]{"*"}, COLUMN_DECK_ID + " = " + id, null, null, null, null);
        while (cur.moveToNext()){
            int main = OVDatabaseForce.getCursorInt(cur,COLUMN_ID);
            db.delete(TABLE_NAME_MOVE_INSTRUCTION,COLUMN_CARD_ID + " = " + main,null);
            db.delete(TABLE_NAME_MOVE_RESULT,COLUMN_CARD_ID + " = " + main,null);
        }
        cur.close();
        db.delete(TABLE_NAME_CARD,COLUMN_DECK_ID + " = " + id,null);
    }
}
