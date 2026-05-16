package com.total.artificial;
import android.database.Cursor;

import androidx.fragment.app.Fragment;

import com.total.overide.OVDatabaseForce;
import com.total.overide.OVHeader;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.Turn;
import java.util.Comparator;
import java.util.List;

public class AiForceList extends ForceList {
//    private final List<ArtificialPilot> pilotList;
    private AiCommander commander;
    public AiForceList(ForceType type) {
        super(type);
//        pilotList = new ArrayList<>();
//        for (int i = 0;i < getCount();i++){
//            pilotList.add(new ArtificialPilot(getUnit(i)));
//        }
        commander = null;
    }

    public AiForceList(List<String> input, String deviceName) {
        super(input, deviceName);
//        pilotList = new ArrayList<>();

    }
    public void configurePilots(){
        try (DatabaseAI dbAi = new DatabaseAI(MainActivity.currentActivity)) {
            Cursor cur = dbAi.loadLiveDeck(this);
            while (cur.moveToNext()) {
                int unitId = OVDatabaseForce.getCursorInt(cur, DatabaseAI.COLUMN_LIVE_UNIT);
                ArtificialPilot pilot = (ArtificialPilot) super.getUnitByKey(unitId);// getPilotForUnit(unitId);
                // we need to set the deck and the current card for the pilot
                pilot.setDeck(OVDatabaseForce.getCursorInt(cur, DatabaseAI.COLUMN_DECK_ID));
                pilot.setCurrentCard(OVDatabaseForce.getCursorInt(cur, DatabaseAI.COLUMN_CARD_ID));
            }
            cur.close();
        }
    }
    private ArtificialPilot getPilotForUnit(int unitId){
        ArtificialPilot ret = null;
        for(int i = 0;i < super.getCount();i++){
            ret = (ArtificialPilot) super.getUnit(i);
//            for (ArtificialPilot pilot : pilotList){
            if (ret.getKey()==unitId){
                break;
            }
            ret = null;
        }
        return ret;
    }
//    public ArtificialPilot getNextUnit(Turn.Phase phase){
//        // get the next card to be played - this includes the unit the card is for
//        if (phase== Turn.Phase.TARGET){
//            // assumes all spotters have a higher priority than all IF units.  might go wrong sometimes?
//            pilotList.sort((t1, t0) -> Integer.compare(t0.getPriority(), t1.getPriority()));
//        } else {
//            pilotList.sort(Comparator.comparingInt(t0 -> t0.getPriority()));
//        }
////        pilotList.sort((t0, t1) -> Integer.compare(t0.getPriority(),t1.getPriority()));
//        ArtificialPilot ret = null;
//        for (ArtificialPilot pilot : pilotList){
//            if (!pilot.phaseComplete(phase)&&pilot.getUnit().isActive()){
//                ret = pilot;
//                break;
//            }
//        }
//        if (ret==null)ret = pilotList.get(0);
//        return ret;
//    }
    public int getNextUnitIndex(Turn.Phase phase){
        // get the next card to be played - this includes the unit the card is for
        if (phase== Turn.Phase.TARGET){
            // assumes all spotters have a higher priority than all IF units.  might go wrong sometimes?
            units.sort((t1, t0) -> Integer.compare(t0.getCompValue(), t1.getCompValue()));
        } else {
            units.sort(Comparator.comparingInt(IUnitData::getCompValue));
        }

        int index = 0;
        for (int i = 0;i<units.size();i++){
            IUnitData ret = units.get(i);
            if (!ret.getTurn().isPhaseComplete(phase)){
                index = i;
                break;
            }
        }
        return index;
    }
    public ArtificialPilot getPilotData(int index){
        return (ArtificialPilot) super.getUnit(index);
    }
    @Override
    public void addUnit(IUnitData add) {

        ArtificialPilot pilot = new ArtificialPilot(add);
        super.addUnit(pilot);
        if (commander!=null)
            pilot.setCommander(commander);
    }

    public void saveLiveDeck(){

        try (DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)){
            for (IUnitData pilot : units){
                db.saveLiveDeck(getKey(),pilot);
            }
        }
    }

    @Override
    public void endPhase(Turn.Phase phase){
        super.endPhase(phase);
//        if (phase== Turn.Phase.RESOLVE){
//            // for the end phase action of the resolve phase the ai needs to determine whether or not a change of commander card is required.
//            // to do this we will need to pop up a dialog box (maybe, sometimes it will be automated)
//            if (commander!=null)
//                commander.checkRules();
//        }
    }

    @Override
    public void startTurn() {
        super.startTurn();
        // for AI at the start of a new turn there are some commander actions that need to be taken
        if (commander!=null){
            commander.startTurn();
            List<AiInstruction> preM = commander.getPreMoves();
            if (!preM.isEmpty()){
                // Commanders additional move instructions exist so add them to all pilots for this turn
                for(IUnitData pilot : units){
                    ((ArtificialPilot)pilot).setPreMoves(preM);
                }
            }
        }

    }

    @Override
    public void endTurn(){
        if (commander!=null)commander.endTurn(unitsDestroyedThisTurn, Game.current.getForce(0).getDestroyed());

        super.endTurn();
    }
    public void initListOnLoad(){
        for(IUnitData pilot : units){
            ((ArtificialPilot)pilot).linkAlly();
        }
    }
    public void setCommander(int id){
        if (commander!=null && commander.getKey()==id)return;
        try (DatabaseAI dbAi = new DatabaseAI(MainActivity.currentActivity)){
            commander = dbAi.loadCommander(id);
            dbAi.loadLiveCommander(this);
        }
        for(IUnitData pilot : units){
            ((ArtificialPilot)pilot).setCommander(commander);
        }
    }
    public AiCommander getCommander(){
        return commander;
    }
    public boolean isPhaseComplete(Turn.Phase phase){

//        if (phase== Turn.Phase.INITIATIVE){
//            // has the commander been updated for this turn
//            if (!commander.isPhaseComplete(phase))return false;
//        } else if (phase== Turn.Phase.RESOLVE){
//
//        }
        return commander.isPhaseComplete(phase) && super.isPhaseComplete(phase);
    }

    public ArtificialPilot getDeckRole(OVHeader.UnitRole role){
        ArtificialPilot ret = null;
        for (IUnitData pilot : units){
            if (pilot.isActive() && ((ArtificialPilot)pilot).getDeck().getDeckRole()==role){
                ret = (ArtificialPilot)pilot;
                break;
            }
        }
        return ret;
    }
}
