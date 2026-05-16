package com.total.overide;

import android.content.ContentValues;
import android.database.Cursor;

import com.total.overiden.Game;
import com.total.overiden.MainActivity;
import com.total.overiden.Pilot;

public class OVState {
    public enum TurretState{
        OKAY,
        JAMMED,
        JAMCLEARED,
        LOCKED;
    }
    private int id = -1;
    private int externalID = -1;
    private String externalSource = "";
    private int designKey;
    private Pilot pilot;
    private int gyro;
    private int engine;
    private int sensors;
    private int motive = 0; //motive criticals
    private int heatLevel;
    private boolean prone = false;
    private boolean ecmActive = false;
    private boolean active;
    private boolean shutdown;
    private int destroyedHeatSinks=0;
    private int destroyedJets=0;
    private boolean forcedWithdrawal = false;
    private boolean destroyedCrit = false;
    private TurretState turret = TurretState.OKAY;
    private int stunned = 0;

    public OVState(int design){
        super();
        designKey = design;
    }
    public OVState(Cursor cur){
        super();
        setExternalID(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_HEADER_EXTID));
        setExternalSource(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_HEADER_EXTSOURCE));
        setKey(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_ID));
//        setPilot(new Pilot(cur)); // populates from a different table now
        setShutdown(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_SHUTDOWN) == 1);
        setGyro(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_GYRO));
        setEngine(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_ENGINE));
        setEcmActive(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_ECMACTIVE)==1);
        setProne(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_PRONE) == 1);
        setHeat(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_HEAT));
        setDesignKey(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_DESIGN_KEY));
        setActive(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_ACTIVE) == 1);
        setShutdown(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_SHUTDOWN) == 1);
        setForcedWithdrawal(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_FORCEDWITHDRAWAL) == 1);
        setEcmActive(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_ECMACTIVE) == 1);
        setDestroyedHeatSinks(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_DAMAGEDSINKS));
        setDestroyedJets(OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_DAMAGEDJETS));
        destroyedCrit = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_DESTROYEDCRIT)==1;
        stunned = OVDatabaseForce.getCursorInt(cur, OVDatabaseForce.COLUMN_STATE_STUNNED);
        setTurret(TurretState.valueOf(OVDatabaseForce.getCursorString(cur, OVDatabaseForce.COLUMN_STATE_TURRETSTATE)));
//        designKey = design;
    }
    public OVState(String line, String deviceName){
        super();
        String[] parts = line.split(",");
        // this is always coming through a bluetooth connection so it is the external ID not the
        // local ID we are receiving - the source field should also be populated
        externalID = Integer.parseInt(parts[0]);
        externalSource = deviceName;

        designKey = Integer.parseInt(parts[1]);
        // TODO add named pilot details to Bluetooth streams
        pilot = new Pilot(parts[7],Integer.parseInt(parts[8]),Integer.parseInt(parts[9]), id);

        updateState(parts);
//        gyro = Integer.parseInt(parts[2]);
//        engine = Integer.parseInt(parts[3]);
//        motive = Integer.parseInt(parts[4]); //motive criticals
//        heatLevel = Integer.parseInt(parts[5]);
//        prone = Boolean.parseBoolean(parts[6]);
//        pilot = new Pilot(parts[7],Integer.parseInt(parts[8]),Integer.parseInt(parts[9]));
//        pilot.addInjury(Integer.parseInt(parts[10]));
//        pilot.setConscious(Boolean.parseBoolean(parts[11]));
//        active = Boolean.parseBoolean(parts[12]);
//        shutdown = Boolean.parseBoolean(parts[13]);
//        forcedWithdrawal = Boolean.parseBoolean(parts[14]);
//        ecmActive = Boolean.parseBoolean(parts[15]);
//        destroyedHeatSinks = Integer.parseInt(parts[16]);
//        destroyedJets = Integer.parseInt(parts[17]);
//        destroyedCrit = Boolean.parseBoolean(parts[18]);
//        turret = TurretState.valueOf(parts[19]);
//        stunned = Integer.parseInt(parts[20]);
    }
    public int getDesignKey(){
        return designKey;
    }
    public void setDesignKey(int design){
        designKey = design;
    }
    public Pilot getPilot(){
        return pilot;
    }
    public void setPilot(Pilot pilot){
        this.pilot = pilot;
    }
    public void setGyro(int gyro){
        this.gyro = gyro;
    }
    public int getGyro(){return gyro;}
    public void setEngine(int engine){
        this.engine = engine;
    }
    public int getEngine(){return engine;}
    public void setKey(int key){
        id = key;
    }
    public int getKey(){
        return id;
    }
    public void resolveTurn(){
        // called at the end of a turn
        if (stunned>0)stunned--;
    }
    public int getMotive() {
        return motive;
    }

    public void setMotive(int motive) {
        this.motive = motive;
    }

    public int getSensors() {
        return sensors;
    }

    public void setSensors(int sensors) {
        this.sensors = sensors;
    }

    public int getHeat(){return heatLevel;}
    public void setHeat(int heat){
        heatLevel = heat;
    }

    public int getHeatShootingPenalty() {
        int penalty = 0;
        if (Game.current.isSmartHeat()){
            if (heatLevel > 23)penalty = 4;
            else if (heatLevel > 16)penalty = 3;
            else if (heatLevel > 12)penalty = 2;
            else if (heatLevel > 7)penalty = 1;
        } else {
            penalty = heatLevel >= 2 ? 1 : 0;
        }
        return penalty;
    }
    public int getHeatMovementPenalty(){
        int penalty = 0;
        if (Game.current.isSmartHeat()){
            if (heatLevel > 24)penalty = 5;
            else if (heatLevel > 19)penalty = 4;
            else if (heatLevel > 14)penalty = 3;
            else if (heatLevel > 9)penalty = 2;
            else if (heatLevel > 4)penalty = 1;
        } else {
            penalty = heatLevel >= 1 ? 2 : 0;
        }
        return penalty;
    }
    public int getHeatShutdownCheck(){
        int ret = 0;
        if (Game.current.isSmartHeat()){
            if (heatLevel > 29) ret = 13;
            else if (heatLevel > 25) ret = 10;
            else if (heatLevel > 21) ret = 8;
            else if (heatLevel > 17) ret = 6;
            else if (heatLevel > 13) ret = 4;

        } else if (heatLevel >= 3) ret = 8;
        return ret;
    }
    public int getHeatAmmoCheck(){
        int ret = 0;
        if (Game.current.isSmartHeat()){
            if (heatLevel > 27) ret = 8;
            else if (heatLevel > 22) ret = 6;
            else if (heatLevel > 18) ret = 4;
        } else if (heatLevel >= 4) ret = 8;
        return ret;
    }
    public boolean isProne() {
        return prone;
    }

    public void setProne(boolean prone) {
        this.prone = prone;
    }

    public String getStreamValue(){
        String stream = "STATE:";

        stream += Integer.toString(id);
        stream += "," + designKey;
        stream += "," + gyro;
        stream += "," + engine;
        stream += "," + motive;
        stream += "," + heatLevel;
        stream += "," + prone;
        stream += "," + pilot.getPilotName();
        stream += "," + pilot.getGunnery();
        stream += "," + pilot.getPilotSkill();
        stream += "," + pilot.getInjuries();
        stream += "," + pilot.isConscious();
        stream += "," + active;
        stream += "," + shutdown;
        stream += "," + forcedWithdrawal;
        stream += "," + ecmActive;
        stream += "," + destroyedHeatSinks;
        stream += "," + destroyedJets;
        stream += "," + destroyedCrit;
        stream += "," + turret.name();
        stream += "," + stunned + '\n';
        return stream;
    }

    public int getExternalID() {
        return externalID;
    }

    public void setExternalID(int externalID) {
        this.externalID = externalID;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }
    public void updateState(String[] parts){
        if (id != Integer.parseInt(parts[0]))System.out.println("Mismatch in Unit ID during State update");
        gyro = Integer.parseInt(parts[2]);
        engine = Integer.parseInt(parts[3]);
        motive = Integer.parseInt(parts[4]); //motive criticals
        heatLevel = Integer.parseInt(parts[5]);
        prone = Boolean.parseBoolean(parts[6]);
        pilot.setInjuries(Integer.parseInt(parts[10]));
        pilot.setConscious(Boolean.parseBoolean(parts[11]));
        active = Boolean.parseBoolean(parts[12]);
        shutdown = Boolean.parseBoolean(parts[13]);
        boolean withdrOld = forcedWithdrawal;
        forcedWithdrawal = Boolean.parseBoolean(parts[14]);
        ecmActive = Boolean.parseBoolean(parts[15]);
        destroyedHeatSinks = Integer.parseInt(parts[16]);
        destroyedJets = Integer.parseInt(parts[17]);
        destroyedCrit = Boolean.parseBoolean(parts[18]);
        turret = TurretState.valueOf(parts[19]);
        stunned = Integer.parseInt(parts[20]);
        if (Game.current.isSoundEffects()){
            if (!withdrOld && forcedWithdrawal)
                MainActivity.currentActivity.playSound(MainActivity.Sounds.WITHDRAW);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public boolean isEcmActive() {
        return ecmActive;
    }

    public void setEcmActive(boolean ecmActive) {
        this.ecmActive = ecmActive;
    }

    public boolean isForcedWithdrawal() {
        return forcedWithdrawal;
    }

    public void setForcedWithdrawal(boolean forcedWithdrawal) {
        this.forcedWithdrawal = forcedWithdrawal;
    }
    public int getTsmBonus(){
        int bonus = 0;
        if (Game.current.isSmartHeat()){
            if (heatLevel>9)bonus = 2;
        }else {
            if (heatLevel>=2) bonus = 2;
        }
        return bonus;
    }

    public int getDestroyedJets() {
        return destroyedJets;
    }

    public void setDestroyedJets(int destroyedJets) {
        this.destroyedJets = destroyedJets;
    }

    public int getDestroyedHeatSinks() {
        return destroyedHeatSinks;
    }

    public void setDestroyedHeatSinks(int destroyedHeatSinks) {
        this.destroyedHeatSinks = destroyedHeatSinks;
    }

    public boolean isDestroyedCrit() {
        return destroyedCrit;
    }

    public void setDestroyedCrit(boolean destroyedCrit) {
        this.destroyedCrit = destroyedCrit;
    }

    public TurretState getTurret() {
        return turret;
    }

    public void setTurret(TurretState turret) {
        if (turret==TurretState.JAMMED && (this.turret==TurretState.JAMMED ||
                this.turret==TurretState.LOCKED ||
                this.turret==TurretState.JAMCLEARED))
            this.turret = TurretState.LOCKED;
        else
            this.turret = turret;
    }

    public boolean isStunned() {
        return stunned>0;
    }

    public void setStunned(boolean stunned) {
        if (stunned){
            // if crew was not previously stunned then add 2 so it will last until next turn
            if (this.stunned==0)this.stunned+=2;
            else this.stunned++;
        } else {
            if (this.stunned>0)this.stunned--;
        }
    }

    public void setContentValues(ContentValues cv){
//        cv.put(OVDatabaseForce.COLUMN_STATE_PILOT_NAME, pilot.getPilotName());
//        cv.put(OVDatabaseForce.COLUMN_STATE_GUNNERY, pilot.getGunnery());
//        cv.put(OVDatabaseForce.COLUMN_STATE_PILOTING, pilot.getPilotSkill());
//        cv.put(OVDatabaseForce.COLUMN_STATE_INJURIES, pilot.getInjuries());
//        cv.put(OVDatabaseForce.COLUMN_STATE_CONSCIOUS, pilot.isConscious() ? 1 : 0);
        cv.put(OVDatabaseForce.COLUMN_STATE_SHUTDOWN, shutdown ? 1 : 0);
        cv.put(OVDatabaseForce.COLUMN_STATE_GYRO, getGyro());
        cv.put(OVDatabaseForce.COLUMN_STATE_ENGINE, getEngine());
        cv.put(OVDatabaseForce.COLUMN_STATE_ECMACTIVE,ecmActive ? 1 : 0);
        cv.put(OVDatabaseForce.COLUMN_STATE_PRONE, prone?1:0);
        cv.put(OVDatabaseForce.COLUMN_STATE_HEAT, heatLevel);
        cv.put(OVDatabaseForce.COLUMN_STATE_DAMAGEDSINKS,destroyedHeatSinks);
        cv.put(OVDatabaseForce.COLUMN_STATE_DAMAGEDJETS,destroyedJets);
        cv.put(OVDatabaseForce.COLUMN_STATE_FORCEDWITHDRAWAL,forcedWithdrawal?1:0);
        cv.put(OVDatabaseForce.COLUMN_STATE_DESIGN_KEY, designKey);
        cv.put(OVDatabaseForce.COLUMN_STATE_ACTIVE,active?1:0);
        cv.put(OVDatabaseForce.COLUMN_STATE_DESTROYEDCRIT,destroyedCrit?1:0);
        cv.put(OVDatabaseForce.COLUMN_STATE_TURRETSTATE,turret.name());
        cv.put(OVDatabaseForce.COLUMN_STATE_STUNNED,stunned);
    }
}
