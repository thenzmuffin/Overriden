package com.total.artificial;

import android.util.ArraySet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.total.overide.OVHeader;
import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;
import com.total.overiden.ForceList;
import com.total.overiden.Game;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IUnitData;
import com.total.overiden.MainActivity;
import com.total.overiden.TargetData;
import com.total.overiden.TargetWeapon;
import com.total.overiden.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ArtificialPilot extends AiUnitAnalysis {
    public static class TurnTarget{
        // class to store the currently designated targets
//        private String designation;
        private final AiUnitAnalysis unit;
        private AiEnums.LOS hasLos;

        public TurnTarget(IUnitData unit){
            super();
            this.unit = (unit instanceof AiUnitAnalysis)?(AiUnitAnalysis)unit:null;
            hasLos = AiEnums.LOS.UNKNOWN;
        }
        void setHasLos(AiEnums.LOS has){hasLos = has;}
        public AiEnums.LOS getHasLos(){return hasLos;}

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean ret = (obj instanceof TurnTarget);
            if (ret){
                ret = ((TurnTarget) obj).unit.getKey() == unit.getKey();
            } else if (obj instanceof IUnitData){
                ret = ((IUnitData) obj).getKey() == unit.getKey();
            }
            return ret;
        }

        public AiUnitAnalysis getUnit() {
            return unit;
        }

    }
//    private final IUnitData unit;
    private ArtificialDeck deck;
//    private int currentCard;
    private AiUnitCard currentCard;
//    private final AiUnitAnalysis analysis;
    private final Set<TurnTarget> targets;

    private final Set<AiTargetChoice> targetAnalysis; //specially designated targets (closest)
    private AiCommander commander;

    private ArtificialPilot linkedAlly = null;//this allows two allied units to work together e.g. IF missile boat and a spotter

    private AiEnums.Tactic localTactic = AiEnums.Tactic.NONE;

    private final List<AiInstruction> preMoves;
    private IUnitData lastTarget = null;
    // the selectedTargets list contains all valid targets within the valid range bracket
    private List<IUnitData> selectedTargets;

    private int priorityModifier; //initially used by commander adjustments
    private final Set<TargetData> whoShotMe;
    public ArtificialPilot(IUnitData unit){
        super(unit);
//        this.unit = unit;
        deck = null;
//        currentCard = -1;
        currentCard = null;
        targetAnalysis = new ArraySet<>();
        targets = new ArraySet<>();
        whoShotMe = new ArraySet<>();
        preMoves = new ArrayList<>();
        commander = null;
        selectedTargets = new ArrayList<>();
    }

    public void setDeck(int deckId){
        try (DatabaseAI dbAi = new DatabaseAI(MainActivity.currentActivity)) {
            deck = dbAi.loadDeck(deckId);
            deck.setPilot(this);
        }
    }

    public ArtificialDeck getDeck(){
        return deck;
    }

    public AiUnitCard drawNextCard(){
        Random ran = new Random();
        currentCard = deck.getCards().get(ran.nextInt(deck.getCardCount()));
        currentCard.setPilot(this);
        updateDatabase();
        return currentCard;
    }
    private void updateDatabase(){
        if (Game.current!=null) {
            try (DatabaseAI db = new DatabaseAI(MainActivity.currentActivity)) {
                db.saveLiveDeck(Game.current.getForce(1).getKey(), this);
            }
        }
    }

    public void setCurrentCard(int cardId) {
//        this.currentCard = currentCard;
        if (cardId<0) currentCard = null;
        else currentCard = deck.getCard(cardId);

        if (currentCard !=null) currentCard.setPilot(this);
        updateDatabase();
    }

    public AiUnitCard getCurrentCard(){
        if (currentCard ==null)
            drawNextCard();
        return currentCard;
    }

    public int getCurrentCardIndex(){
        return currentCard ==null?-1: currentCard.getId();
    }

    public void endTurn(){
        super.endTurn();
        if (!isActive())return;

        priorityModifier = 0; // reset for the new turn, initiative phase will call the commander card to update
        // clear the targets set for this turn
        for (TurnTarget targ : targets){
            targ.hasLos = AiEnums.LOS.UNKNOWN;
        }
        //determine if there are any overrides for the tactics
        if (getHeader().getType()== ForceList.ForceType.OV){
            if (!checkSegment(OVSegment.OVLocation.TORSO))checkSegment(OVSegment.OVLocation.FRONT);
        } else {
            if (checkSegment(OVSegment.OVLocation.CENTRETORSO)) {
                checkSegment(OVSegment.OVLocation.LEFTTORSO);
                checkSegment(OVSegment.OVLocation.RIGHTTORSO);
            } else {
                checkSegment(OVSegment.OVLocation.FRONT);
            }
        }

        //preserve the target from this turn
        TargetData targetData = getTurn().getPrimaryTarget();
        if (targetData!=null)lastTarget = targetData.getTarget();
        else lastTarget = null;

        // who shot at me this turn?
        whoShotMe.clear();
        for (IDamageRecord record : getTurn().getDamageRecords()){
            TargetData data = record.getTarget();
            if (data!=null) {
                if (data.getShooter() != null) {
                    whoShotMe.add(data);
                }
            }
        }

        linkAlly();
        currentCard.endTurn();
        // reset any premoves set last turn by the commander
        preMoves.clear();
    }
    public void linkAlly(){
        // am I a spotter or an IF unit?
        if (deck.getDeckRole()== OVHeader.UnitRole.SPOTTER)
            setLinkedAlly(Game.current.getAiForce().getDeckRole(OVHeader.UnitRole.IF_MISSILEBOAT));
        else if (deck.getDeckRole()== OVHeader.UnitRole.IF_MISSILEBOAT){
            // relink with the spotter/if units
            setLinkedAlly(Game.current.getAiForce().getDeckRole(OVHeader.UnitRole.SPOTTER));
        }
    }
    private boolean checkSegment(OVSegment.OVLocation loc){
        OVSegmentInst inst = ((OVSegmentInst)getSegment(loc));
        if (inst!=null) {
            if (inst.getArmourTurnDmg() <= 0)
                localTactic = AiEnums.Tactic.DEFENSIVE;
            return true;
        }
        return false;
    }

    public void updateTargetLos(AiUnitAnalysis unit, boolean hasLos) {
        if (unit!=null) {
            // find the existing record and update hasLOS field
            for (TurnTarget targ : targets) {
                if (targ.equals(unit)) {
                    targ.setHasLos(hasLos ? AiEnums.LOS.HASLOS : AiEnums.LOS.NOLOS);
                    break;
                }
            }
        }
    }

    public void setCommander(@NonNull AiCommander commander) {
        if (this.commander==null || this.commander.getKey() != commander.getKey()) {
            this.commander = commander;
            // get the list of targets for tracking
            if (Game.current!=null) {
                for (IUnitData unit : Game.current.getForce(0).getAllUnits()) {
                    targets.add(new TurnTarget( unit));
                }
            }
        }
    }

    public AiCommander getCommander(){
        return commander;
    }

    public AiEnums.Tactic getCurrentTactic(){
        // if the local tactic is set then it overrides what the commander is sending
        return (localTactic!= AiEnums.Tactic.NONE)?localTactic:commander.getCurrentTactic();
    }

    public IUnitData getLastTarget(){return lastTarget;}
    public Set<TargetData> getWhoShotMe(){return whoShotMe;}
    private static class TargetRank{
        private final IUnitData unit;
        private final float rank;
        public TargetRank(IUnitData unit, float rank){
            this.rank = rank;
            this.unit = unit;
        }
        public float getRank(){return rank;}
    }
    public void applyTargetRules(){
        // reduces the current selecttargets as far as possible towards a single target
        for ( AiCommander.TargetType type : currentCard.getTargetRules()){
            // if there is 1 or 0 targets in the list then we have found what we wanted
            if (selectedTargets.size()<=1)break;
            // this reduces the targetlist down to the top matched targets
            getTopRankedTargets(type);
        }
    }
    public void getTopRankedTargets(AiCommander.TargetType rule){
        //generate a list of targets that top the supplied target rule
        List<IUnitData> ret = this.getTargetList();

        if (rule.getEnum() == AiEnums.Tag.SPOTTER) {
            // what if we have already added the designated unit?
            for (IUnitData spot : Game.current.getForce(1).getAllUnits()){
                if (((ArtificialPilot)spot).deck.getDeckRole()== OVHeader.UnitRole.SPOTTER){
                    // do we need to break out of the loop after the first spotter is found or should we leave all in?
                    ret.add(spot);
                }
            }
        } else {
            // from the list of valid targets
            List<TargetRank> ranked = new ArrayList<>();
            for (IUnitData unit : this.getTargetList()) {
                ranked.add(new TargetRank(unit, ((AiUnitAnalysis) unit).getValue(rule)));
            }
            // sort them by the rule type
            if (rule.getEnum().isHigh())
                ranked.sort((t0, t1) -> Float.compare(t0.getRank(), t1.getRank()));
            else
                ranked.sort((t1, t0) -> Float.compare(t0.getRank(), t1.getRank()));

            float rankNo = -1;
            //start with an empty list
            ret.clear();
            for (TargetRank rank : ranked) {
                if (rankNo==-1 ||rankNo==rank.rank) {
                    ret.add(rank.unit);
                    rankNo = rank.rank;
                }else if (rankNo!=rank.rank)break;
            }
        }
        //for a targettype that designates a specific unit we add them to the list
//        if (rule.getEnum() == AiCommander.Tag.DESIGNATION) {
//            // what if we have already added the designated unit?
//            AiUnitAnalysis got = commander.getDesignatedTarget(rule);
//            for (TurnTarget targ : targets) {
//                if (targ.unit.getKey() == got.getKey()) {
//                    if(targ.hasLos!=LOS.NOLOS)ret.add(targ);
//                }
//            }
//
//        } else
//        if (rule.getEnum() == AiCommander.Tag.SPOTTER) {
//            // what if we have already added the designated unit?
//            for (IUnitData spot : Game.current.getForce(1).getAllUnits()){
//                if (((ArtificialPilot)spot).deck.getDeckRole()== OVHeader.UnitRole.SPOTTER){
//                    // do we need to break out of the loop after the first spotter is found or should we leave all in?
//                    ret.add(new TurnTarget(spot));
//                }
//            }
//        } else {
//            // we need a list of the units that are remaining to be sorted
//            List<TargetRank> list = new ArrayList<>();
//            for (TurnTarget unit : targets) {
//                if (unit.hasLos!=LOS.NOLOS)
//                    list.add(new TargetRank(unit, unit.unit.getValue(rule)));
//            }
//            if (rule.getEnum().isHigh())
//                list.sort((t0, t1) -> Float.compare(t0.getRank(), t1.getRank()));
//            else
//                list.sort((t1, t0) -> Float.compare(t0.getRank(), t1.getRank()));
//            if (!list.isEmpty()) {
//                float value = list.get(0).getRank();
//                for (TargetRank rank : list) {
//                    if (value!= rank.getRank()){
//                        break;
//                    }
//                    ret.add(rank.unit);
//                }
//            }
//        }
//        return ret;
    }
    public boolean moreTargets(){
        boolean ret = false;
        for (TurnTarget turn : targets){
            if (turn.hasLos!=AiEnums.LOS.NOLOS){
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void orderWeapons(){
        List<TargetWeapon> weapons = getTurn().getWeaponList();
        weapons.sort(this::sortWeapons);
    }
    private int sortWeapons(TargetWeapon first, TargetWeapon second){
        int ret = 0, index = 0;
        while (ret==0 && currentCard.getWeaponPriorityList().size()>index){
            switch (currentCard.getWeaponPriorityList().get(index)) {
                case CRIT_SEEKER:
                    // put cluster weapons first (and set them to cluster mode where appropriate)
                    ret = Integer.compare(1,2);
                    break;
                case TO_HIT:
                    ret = Integer.compare(first.getToHit(), second.getToHit());
                    break;
                case DAMAGE:
                    ret = Integer.compare(second.getWeapon().getDamage(), first.getWeapon().getDamage());
                    break;
                case LOW_HEAT:
                    ret = Integer.compare(first.getWeapon().getHeat(), second.getWeapon().getHeat());
                    break;
                case HEAT_RATIO:
                    float firstRatio = ((float)first.getWeapon().getDamage()) / first.getWeapon().getHeat();
                    float secondRatio = ((float)second.getWeapon().getDamage()) / second.getWeapon().getHeat();
                    ret = Float.compare(secondRatio,firstRatio);
                    break;
            }
            index++;
        }

        return ret;
    }
    public void lockInWeapons(){
        int threshold;
        List<TargetWeapon> weapons = getTurn().getWeaponList();
        TargetData primary = getTurn().getPrimaryTarget();
        if (primary==null)return;
        int heat = getAdjustedHeat();
        for (TargetWeapon weapon : weapons){
            // if it can't hit then skip this weapon
            if (weapon.getToHit()>12)continue;
            threshold = currentCard.getHeatThreshold(weapon);
            // does it meet the heat threshold
            int weaponHeat = weapon.getWeapon().getHeat();
            if ((heat + weaponHeat)<threshold) {
                weapon.setLocked(true);
                if (weapon.getTargetData() != null)
                    weapon.getTargetData().updateWeapon(weapon);
                heat += weaponHeat;
            }
        }
    }

    public void addDesignatedTarget(String designation,AiUnitAnalysis enemy){
        for (AiTargetChoice comm : targetAnalysis){
            if (designation.equalsIgnoreCase(comm.getDesignation())){
                comm.setUnit(enemy);
                return;
            }
        }
        targetAnalysis.add(new AiTargetChoice(designation,enemy));
    }
    public AiUnitAnalysis getDesignatedTarget(AiCommander.TargetType type){
        AiUnitAnalysis ret = null;
        switch (type.getEnum()) {
            case DESIGNATION:
                for (AiTargetChoice comm : targetAnalysis) {
                    if (type.getTag().equalsIgnoreCase(comm.getDesignation())) {
                        ret = comm.getUnit();
                    }
                }
                break;
            case SHOT_ME:
                IUnitData biggest = null;
                for (TargetData unit : whoShotMe) {
                    if (biggest == null) biggest = unit.getShooter();
                    else {
                        if (unit.getShooter().getHeader().getMass() > biggest.getHeader().getMass())
                            biggest = unit.getShooter();
                    }
                }
                if (biggest != null) {
                    ret = (AiUnitAnalysis) biggest;
                }
                break;
            case SHOT_BY_ALLY:
                // return the heaviest unit shot by an ally this turn that we can either
                // see or don't know if we can see
                // who has already registered their shots?
                IUnitData prime = null;
                AiForceList list = Game.current.getAiForce();

                for (int i = 0;i < list.getCount();i++){
                    if (list.getUnit(i).getTurn().isTargetingComplete()){
                        TargetData data;
                        if ((data = list.getUnit(i).getTurn().getPrimaryTarget())!=null){
                            if (prime ==null || prime.getHeader().getMass() < data.getTarget().getHeader().getMass()) {
                                TurnTarget turn = getTargetById(data.getTarget().getKey());
                                if (turn.hasLos!=AiEnums.LOS.NOLOS)
                                    prime = data.getTarget();
                            }
                        }
                    }
                }
                if (prime != null) {
                    ret = (ArtificialPilot)prime;
                }
                break;
            case REAR_SHOT:
                IUnitData backShot = null;
                for (TargetData unit : whoShotMe) {
                    if (unit.getFacing()!= TargetData.LocTable.REAR)continue;
                    if (backShot == null) backShot = unit.getShooter();
                    else {
                        if (unit.getShooter().getHeader().getMass() > backShot.getHeader().getMass())
                            backShot = unit.getShooter();
                    }
                }
                if (backShot != null) {
                    ret = (ArtificialPilot)backShot;
                }
                break;
        }
        if (ret == null) {
            ret = commander.getDesignatedTarget(type);
        }
        return ret;
    }

    @Override
    public int getCompValue() {
        return getCurrentCard().getPriority() + priorityModifier;
    }

    public String getRange(String designation){
        String out;
        switch (designation){
            case "BEST":
                //get the optimum range for this unit
                out = getOptimumRangeToken();
                break;
            case "MIN":
                //get the minimum range for effective fire from this unit
                out = getMinRange();
                break;
            case "MAX":
                // return the maximum range for effective fire from this unit
                out = getMaxRange();
                break;
            default:
                out = "Unknown";
        }
        return out;
    }
    public TurnTarget getTargetById(int id){
        for (TurnTarget turn : targets){
            if (turn.unit.getKey()==id)return turn;
        }
        return null;
    }
    public void setLinkedAlly(ArtificialPilot linkedAlly) {
        if (linkedAlly==null || !linkedAlly.isActive())this.linkedAlly = null;
        else this.linkedAlly = linkedAlly;
    }

    public ArtificialPilot getLinkedAlly(){
        if (linkedAlly != null &&!linkedAlly.isActive())linkedAlly = null;
        return linkedAlly;
    }

    private final List<TargetData> ifTargets = new ArrayList<>();
    public List<TargetData> getSpottingForTargetting(IUnitData target){
        // we always recalculate in case the target is changing
        ifTargets.clear();
        // return a list of units that this pilot is spotting for as TargetData
        if (target!=null && linkedAlly!=null && deck.getDeckRole()== OVHeader.UnitRole.SPOTTER){
            //only need to compile the list if a target is supplied, if there is a linked
            // Ally and this unit is marked as a spotter.
            // find any units that this pilot is spotting for (can't just use the linked Ally as there might be more than one)
            for (IUnitData uni : Game.current.getForce(1).getAllUnits()){
                ArtificialPilot ally = (ArtificialPilot) uni;
                if (ally.deck.getDeckRole()== OVHeader.UnitRole.IF_MISSILEBOAT &&
                        ally.getLinkedAlly()!= null &&
                        ally.getLinkedAlly().getKey()== getKey()){
                    ally.getTurn().generateTargetData(Game.current.getForce(0));
                    // found one!
                    for (TargetData targ : ally.getTurn().getAllTargets()){
                        if (targ.getTarget().getKey()==target.getKey()){ ifTargets.add(targ);break;}
                    }
                }
            }

        }
        return ifTargets;
    }
    public List<TargetData> getIfTargets(){return ifTargets;}

    public int getPriorityModifier() {
        return priorityModifier;
    }

    public void setPreMoves(List<AiInstruction> instructions) {
        for (AiInstruction next : instructions) {
            preMoves.add(new AiInstruction(next));
        }
    }
    public AiInstruction getNextPreMove(int index){
        for (AiInstruction next : preMoves){
            if (next.getIndex()==index)
                return next;
        }
        return null;
    }
    public boolean getPreMoves(List<AiInstruction> onScreen){
        // set the premoves to the screen list, allowing choice selection or display
        // if this method returns true it means all instructions in this list have been resolved
        if (preMoves.isEmpty())return true;
        // preMoves always start at 0
        AiInstruction instruction = preMoves.get(0);

        instruction.setPilot(this);
        onScreen.add(0,instruction);
        while (instruction.autoResolve()) {
//            if (instruction.getSelected() < 0) {
//                // this is an auto resolved and it isn't yet resolved then action it now.
//                instruction.autoResolve();
//            }
            instruction = findInstructionById(instruction.getSelectedChoice().getNextInstruction());
            if (instruction != null) {
                instruction.setPilot(this);
                onScreen.add(0,instruction);
            } else break;
        }
        if (instruction==null)return false;
        return instruction.getSelected() >= 0;
    }
    private AiInstruction findInstructionById(int id){
        for (AiInstruction move : preMoves){
            if (move.getIndex()==id){
//                if (move.isAuto() && !move.isResolved()){
                move.autoResolve();
//                }
                return move;
            }
        }
        return null;
    }
    public void setPriorityModifier(int priorityModifier) {
        this.priorityModifier = priorityModifier;
    }
    public void setTargetList(List<IUnitData> selected){
        selectedTargets.clear();
        selectedTargets.addAll(selected);
    }
    public void updateTargetList(){
        // this method checks if the target has been selected for this turn and resets the target list if required
        if (selectedTargets.isEmpty() && !getTurn().isTargetingComplete()){
            selectedTargets.addAll(Game.current.getForce(0).getAllUnits());
        }
    }
    public List<IUnitData> getTargetList() {
        return selectedTargets;
    }

    @Override
    public boolean endPhase(Turn.Phase phase) {
        boolean stillActive = super.endPhase(phase);
        if (stillActive && phase== Turn.Phase.MOVE){
            // clear the selected targets and reset to the full list for selection
            selectedTargets.clear();
            selectedTargets.addAll(Game.current.getForce(0).getAllUnits());
        }
        return stillActive;
    }

}
