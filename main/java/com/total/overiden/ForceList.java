package com.total.overiden;

import androidx.fragment.app.Fragment;

import com.total.artificial.AiForceList;
import com.total.artificial.ArtificialPilot;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVUnitData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * contains a list of units composing a single force
 */
public class ForceList implements IForceItem{
    public enum ForceType {
        OV,
        TW
    }

    private int key = -1;
    private int externalKey = -1;
    private String externalSource = null;
    private int parentKey = -1;
    protected final List<IUnitData> units;
    protected final List<IUnitData> unitsDestroyedThisTurn = new ArrayList<>();;
    private List<ForceList> subLists;
    private final List<BSPStrike> bspStrikes;
    private BSPPhaseStatus bspStatus;
    private String name;
    private ForceType type;
    private boolean expanded = true;
    private boolean inUse = false;

    private int initiativeModifier = 0;

    public ForceList(ForceType type) {
        super();
        units = new ArrayList<>();
        subLists = new ArrayList<>();
        name = "Default";
        this.type = type;
        bspStrikes = new ArrayList<>();
        bspStatus = new BSPPhaseStatus(false);
    }

    public ForceList(List<String> input, String deviceName) {
        super();
        units = new ArrayList<>();
        externalSource = deviceName;
        bspStrikes = new ArrayList<>();
        bspStatus = new BSPPhaseStatus(false);

        boolean collect = false;
        boolean breakOut = false;
        List<String> subList = new ArrayList<>();
        int count = 0;
        for(String line : input){
            count++;
            // sometimes the first line read will be the start tag, sometimes that has already been read before calling the constructor
            String[] parts = line.split(":");
            switch (parts[0]){
                case "FORCELIST":
                    name = parts[1];
                    break;
                case "FORCEID":
                    externalKey = Integer.parseInt(parts[1]);
                    break;
                case "FORCETYPE":
                    type = ForceType.valueOf(parts[1]);
                    break;
                case "STARTOVMECH":
                case "STARTTWMECH":
                    collect = true;
                    break;
                case "ENDOVMECH":
                case "ENDTWMECH":
                    collect = false;
                    if (!subList.isEmpty()) {
                        IUnitData gen = OVUnitData.newInstance(subList, deviceName);
                        // if this is an Ai list then create a pilot to wrap the unit
                        if (this instanceof AiForceList)
                            gen = new ArtificialPilot(gen);
                        units.add(gen);
                    }
                    subList.clear();
                    break;
                case "STARTSUBLIST":
                    subLists.add(new ForceList(input.subList(count,input.size()),deviceName));
                    break;
                case "ENDFORCELIST":
                    // break out of loop
                    breakOut = true;
                    break;
            }
            if (collect){
                subList.add(line);
            }
            if (breakOut) break;
        }
    }
    public List<String> getStreamValue(){
        List<String> stream = new ArrayList<>();
        stream.add("FORCELIST:" + name + '\n');
        stream.add("FORCEID:" + key + '\n');
        stream.add("FORCETYPE:" + type.toString() + '\n');
        for (IUnitData unit : units){
            stream.addAll(unit.getStreamValue());
        }
        // add any sublists
        for (ForceList sub : subLists){
            stream.addAll(sub.getStreamValue());
        }
        stream.add("ENDFORCELIST:" + name + '\n');

        return stream;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public void addUnit(IUnitData add) {
        units.add(add);
    }

    public void addSubList(ForceList sub){
        subLists.add(sub);
    }

    public int getCount() {
        return units.size();
    }

    public void reorderUnits() {
        // put destroyed units at the bottom of the list
        units.sort((unitData, t1) -> Boolean.compare(!unitData.isActive(),!t1.isActive()));
        for (ForceList sub : subLists)sub.reorderUnits();
    }
    public int getActiveCount(){
        int count = 0;
        for (IUnitData unit : units) {
            if (unit.isActive()) count++;
        }
        for (ForceList sub : subLists){
            count += sub.getActiveCount();
        }
        return count;
    }
    public int getForceItemIndent(int index){
        /* called from Force creation screens so not in game and no current phase */
        int ret = -1;
        if (index < units.size()) {
            ret = 0;
        } else {
            index -= units.size();
            for (ForceList sub : subLists){
                if (index==0) return 0;
                index--;
                if (sub.expanded) {
                    ret = sub.getForceItemIndent(index) + 1;
                    index -= sub.getDisplayCount(Turn.Phase.SETUP);
                }
                if (index < 0)break;
            }
        }
        return ret;
    }
    public IForceItem getForceItem(int index, Turn.Phase phase){
        // gets the item specified from the list ignoring collapsed subLists
        IForceItem ret = null;
        if (index < getDisplayCount(phase)) {
            return getActiveUnit(index, phase);
        } else {
            index -= units.size();
            for (ForceList sub : subLists){
                if (index==0) return sub;
                index--;
                if (sub.expanded) {
                    ret = sub.getForceItem(index, phase);
                    index -= sub.getDisplayCount(phase);
                }
                if (index < 0)break;
            }
        }
        return ret;
    }
    public int getDisplayCount(Turn.Phase phase){
        if (!expanded)return 0;
        int ret = 0;
        for (IUnitData data : units) {
            if (data.isActiveForPhase(phase)) ret++;
        }
        for (ForceList sub : subLists){
            ret++;
            ret+=sub.getDisplayCount(phase);
        }
        return ret;
    }
    public IUnitData getUnit(int index) {
        IUnitData ret = null;
        if (index < units.size()) {
            ret = units.get(index);
        }
        return ret;
    }
    public IUnitData getActiveUnit(int index, Turn.Phase phase) {
        /* An active unit is defined (in this context) as a unit that is relevant for this phase
         */
        IUnitData ret = null;
        int count = index;
        if (index < this.getDisplayCount(phase)) {
            for (Iterator<IUnitData> it = units.iterator();count >= 0 && it.hasNext();){
                ret = it.next();
                if (ret.isActiveForPhase(phase))count--;
            }
            if (count >= 0) ret = null;
        } else {
            count -= this.getDisplayCount(phase);
            for (ForceList sub : subLists){
                if (count < sub.getDisplayCount(phase)){
                    ret = sub.getActiveUnit(count, phase);
                    break;
                } else count -= sub.getDisplayCount(phase);
            }
        }
        return ret;
    }

    public IUnitData getUnitByKey(int key) {
        IUnitData ret = null;
        for (int i = 0; i < units.size(); i++) {
			if(units.get(i).getKey() == key) {
				ret = units.get(i);
				break;
			}
        }
        if (ret==null){
            for (ForceList sub : subLists) {
                ret = sub.getUnitByKey(key);
                if (ret!=null)break;
            }
        }
        return ret;
    }
    public IUnitData getUnitByExtKey(int key) {
        IUnitData ret = null;
        if (key<0){
            System.out.println("*************PANIC PANIC **** RETRIEVING -1 VALUE FOR EXTERNAL ID *****************");
        }
        for (int i = 0; i < units.size(); i++) {
            if(units.get(i).getState().getExternalID() == key) {
                ret = units.get(i);
                break;
            }
        }
        if (ret==null){
            for (ForceList sub : subLists) {
                ret = sub.getUnitByExtKey(key);
                if (ret!=null)break;
            }
        }
        return ret;
    }

    public void addUnitTurns(Turn turn) {
        for (IUnitData unitData : units ){
			unitData.resetTurn();
            turn.addUnit(unitData);
        }
        for (ForceList sub : subLists) sub.addUnitTurns(turn);
    }
//    public void generateTargetLists(ForceList opFor){
//        //this is now done JIT
////        for (IUnitData unitData : units ){
////            unitData.getTurn().generateTargetData(opFor);
////        }
//    }

    public List<IUnitData> getAllUnits() {
        return units;
    }

    public boolean isPhaseComplete(Turn.Phase phase) {
        boolean bol = true;
        for (int i = 0; i < units.size(); i++) {
            if (!units.get(i).getTurn().isPhaseComplete(phase)) {
                bol = false;
            }
        }
        for (ForceList sub : subLists) bol = bol && sub.isPhaseComplete(phase);
        bol = bol && bspStatus.isPhaseComplete(phase);
        return bol;
    }
    public boolean getBspStatus(Turn.Phase phase){
        boolean complete = true;
        for (BSPStrike strike : getBspStrikes(phase)) {
            switch (phase) {
                case MOVE:
                case SHOOT:
                    if (strike.isTargeted()) {
                        for (BSPStrike.StrikeTarget target : strike.getTargetList()) {
                            if (target.status == TargetWeapon.ShotStatus.NOTFIRED) {
                                complete = false;
                                break;
                            }
                        }
                    }
                    break;
            }
            if (!complete)break;
        }
        return complete;
    }
//    public BSPPhaseStatus getBspStatus(Turn.Phase phase){
//        return bspStatus;
//    }

    public ForceType getType() {
        return type;
    }

    public void setType(ForceType type) {
        this.type = type;
    }

    public void removeUnit(int index) {
        if (index < units.size()) {
            units.remove(index);
            try (OVDatabaseForce dbForce = new OVDatabaseForce(MainActivity.currentActivity)) {
                dbForce.updateItems(this);
            }
        } else {
            int count = index - units.size();
            int subIndex = 0;
            for (ForceList sub : subLists){
                if (count==0){
                    try (OVDatabaseForce dbForce = new OVDatabaseForce(MainActivity.currentActivity)) {
                        dbForce.deleteForceList(sub.getKey());
                    }
                    subLists.remove(subIndex);
                    break;
                }
                count--; //move past sub list header line
                if (count < sub.getDisplayCount(Turn.Phase.SETUP)){
                    sub.removeUnit(count);
                    break;
                } else {
                    count -= sub.getDisplayCount(Turn.Phase.SETUP);
                }
                subIndex++;
            }
        }
    }

    public void startTurn() {
        // This method carries out all actions required when a new turn is created.
        // This should only occur when:
        // a) an old turn ends and a new one is created
        // b) the first turn of a game is generated

        // no action for a standard force list.
    }
    public void endTurn(){
        for (IUnitData unit : units){
            unit.endTurn();
        }
        for (ForceList sub : subLists)sub.endTurn();
        for (BSPStrike strike : bspStrikes){
            // clear card status for next turn
            strike.resolveTurn();
        }
        bspStatus = new BSPPhaseStatus(!bspStrikes.isEmpty());
        unitsDestroyedThisTurn.clear();
    }
    public List<IUnitData> getDestroyed(){
        return unitsDestroyedThisTurn;
    }
    public void endPhase(Turn.Phase phase) {
        boolean ecmCheck = false;
        for (IUnitData unit : units){
            if (!unit.isActive())continue; // don't process units that are already marked as dead
            if (unit.endPhase(phase)) {
                if (phase == Turn.Phase.RESOLVE && !ecmCheck)
                    ecmCheck = unit.getState().isEcmActive();
            } else {
                // capture the fact that this unit was destroyed in this turn
                // this is used for AI command updates mainly
                unitsDestroyedThisTurn.add(unit);
            }
        }
        for (ForceList sub : subLists)sub.endPhase(phase);
        if (ecmCheck)Game.current.setUnitECMActive(true);

        // at the end of the initiative phase clear any mods that were applied for this turn
        // might make the display a little odd looking but stops mods being accidentally
        // carried over since they aren't stored on the turn itself.
        if (phase== Turn.Phase.INITIATIVE)
            initiativeModifier = 0;
    }
    public int getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(int externalKey) {
        this.externalKey = externalKey;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }

    public List<BSPStrike> getBspStrikes(Turn.Phase phase) {
        if (phase== Turn.Phase.SETUP)
            // if we return the actual list it can be directly updated in the setup screen
            return bspStrikes;
        else {
            List<BSPStrike> retList = new ArrayList<>();
            for (BSPStrike str : bspStrikes){
                if (str.phaseRelevant(phase))retList.add(str);
            }
            if (retList.isEmpty())bspStatus.setPhaseComplete(phase, true);
            return retList;
        }
    }

    public int getParentKey() {
        return parentKey;
    }

    public void setParentKey(int parentKey) {
        this.parentKey = parentKey;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    public int addGroup(int place, String name){
        int ret = -1;
        if (place < units.size()){
            ForceList toAdd = new ForceList(type);
            toAdd.setName(name);
            toAdd.setParentKey(this.key);
            subLists.add(toAdd);
            ret = getDisplayCount(Turn.Phase.SETUP) - 1;
            // create a new instance of the design and add to the force list
            try (OVDatabaseForce dbForce = new OVDatabaseForce(MainActivity.currentActivity)) {
                dbForce.addHeader(toAdd);
            }
        } else {
            int count = place - units.size();
            ret = units.size();
            for (ForceList sub : subLists){
                ret++;
                count--;//get rid of the header line for this group
                if (count <= sub.getDisplayCount(Turn.Phase.SETUP)){
                    ret += sub.addGroup(count, name);
                    break;
                } else {
                    count -= sub.getDisplayCount(Turn.Phase.SETUP);
                    ret += sub.getDisplayCount(Turn.Phase.SETUP);
                }
            }
        }
        return ret;
    }

    public ForceList getSelectedGroup(int index){
        ForceList ret = null;
        if (index < units.size()){
            ret = this;
        } else {
            int count = index - units.size();
            for (ForceList sub : subLists){
                if (count==0){
                    ret = sub;
                    break;
                } else if (count < sub.getDisplayCount(Turn.Phase.SETUP)){
                    ret = sub.getSelectedGroup(count - 1);
                    break;
                } else {
                    count -= sub.getDisplayCount(Turn.Phase.SETUP);
                }
            }
        }
        return ret;
    }

    public int getIndexByKey(int key, Turn.Phase phase){
        /* return the display index of the matching key for this phase
         * if the specified key is not displayed in this phase then return -1
         * if the key isn't found then return -1
         */
        int index = -1;
        int tracker = 0;
        for(IUnitData unit : units){
            if (unit.getKey()==key) {index = tracker;break;}
            tracker++;
        }
        if (index<0){
            for (ForceList sub : subLists){
                tracker++;
                if (sub.expanded){
                    int subIndex = sub.getIndexByKey(key, phase);
                    if (subIndex>0){
                        index = tracker+subIndex;
                        break;
                    } else tracker += sub.getDisplayCount(phase);
                }
            }
        }
        return index;
    }

    public int getInitiativeModifier() {
        return initiativeModifier;
    }

    public void setInitiativeModifier(int initiativeModifier) {
        this.initiativeModifier = initiativeModifier;
    }
}
