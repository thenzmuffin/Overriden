package com.total.artificial;

import com.total.overiden.R;

public class AiEnums {
    public enum WeaponPriority{
        TO_HIT,
        DAMAGE,
        CRIT_SEEKER,
        LOW_HEAT,
        HEAT_RATIO
    }
    public enum PriorityModType{
        SHOT_BY,     // unit that did most damage to this unit last turn has not moved yet
        SHOT_BY_ALL,
        REAR_SHOT,   // units that shot me in my back
        PREV_TARGET;   // torso location has lost all armour
    }
    public enum LOS {
        UNKNOWN,
        HASLOS,
        NOLOS;
    }
    public enum MovedStatus {
        MOVED, //Moved
        NOT_MOVED, //Not Moved
        ALL //All
    }
    public enum Tactic {
        NONE(R.drawable.agressive),
        AGGRESSIVE(R.drawable.agressive),
        BALANCED(R.drawable.balanced),
        CAUTIOUS(R.drawable.cautious),
        DEFENSIVE(R.drawable.defensive);
        private final int imageId;
        Tactic(int id){imageId = id;}
        public int getImageId(){return imageId;}
    }
    public enum ResolutionAction {
        NONE(false),
        HAS_LOS(false), // can the unit get LOS to the target
        UNIT(false), //select one unit from a list of enemy units
        UNIT_LIST(false), // select 0..n units from a list of enemy units
        LIST(false), // select from a list of results
        CAN_HIT_REAR(false), // Can we target the rear arc of this unit?
        HAS_LINK(true), //is either a spotter or using a spotter
        WAS_SHOT(true), // was shot last turn
        TARGET_RULE(true), // apply a rule to a previously selected set of units (from UNIT_LIST)
        UNIT_DESTROYED(true), // intended as a tactic rule - what % of friendly force is dead
        UNIT_MOVED(true), // has specified target already moved?
        ANALYSIS(true); // apply a rule defined in the parameters
        private final boolean auto;
        ResolutionAction(boolean auto){
            this.auto = auto;
        }
        boolean isAuto(){return auto;}
    }
    public enum Tag{
        NONE(true), //placeholder
        SPOTTER(true), // used for allies, is a spotter
        DESIGNATION(true), // target by tag - determined by the commander
        BV_HIGH(true), // either BV or PV value - not currently stored
        BV_LOW(false), // either BV or PV value - not currently stored
        CAT_HIGH(true), // unit category (Light,Medium,Heavy,Assault)
        CAT_LOW(false), // unit category (Light,Medium,Heavy,Assault)
        MASS_HIGH(true), // unit weight
        MASS_LOW(false), // unit weight
        SPEED_HIGH(true), //running/jumping value (which ever is highest)
        SPEED_LOW(false), //running/jumping value (which ever is highest)
        TMM_HIGH(true), //TMM for current adjusted walking value
        TMM_LOW(false), //TMM for current adjusted walking value
        HEALTH_HIGH(true), //current armour+structure
        HEALTH_LOW(false), //current armour+structure
        DAMAGE_HIGH(true), //Damage Taken armour+structure
        DAMAGE_LOW(false), //Damage Taken armour+structure
        HAS_CORE_ARMOUR(true), // remaining armour in core segment
        HAVE_REAR_SHOT(true), // do they have a rear shot on any target?
        STRENGTH_HIGH(true), //original armour+structure
        STRENGTH_LOW(false), //original armour+structure
        THREAT_ORIG(true), //Highest single bracket damage rating at start of game
        THREAT_NOW(true), //Highest single bracket damage rating
        MIN_RANGE(false), //not a target but used to update a question with the attacking units minimum viable range
        BEST_RANGE(false), //not a target but used to update a question with the attacking units optimum range
        MAX_RANGE(false), //not a target but used to update a question with the attacking units maximum viable range
        ENEMY_SPRINT(false),
        ENEMY_RANGE(false), // enemies most effective range
        SHOT_BY_ALLY(true), //"Already shot by an ally this turn
        SHOT_ME(true),//"Biggest unit to shot me last turn"
        REAR_SHOT(true), //"biggest unit who shot me in the back last turn"
        FRONT(false), //unit closest to own home edge
        BACK(false); //unit closest to enemy home edge
        private final boolean high;
        Tag(boolean high){
            this.high = high;
        }
        public boolean isHigh(){
            //returns true if this type is for a high value
            return high;
        }
    }
    public enum AiCommanderChangeType{
        OBJECTIVE,
        LOST_MORE,
        LOST_FEWER,
        FAST_ENEMY_CLOSE,
        ENEMY_CLOSE,
        ENEMY_BEHIND;
    }
    public enum CommInst {
        INIT_MOD, // apply a modifier to the initiative roll for the AI
        MOVE_FIRST, // apply a priority modifier to ensure a chosen unit moves first
        MOVE_LAST, // apply a priority modifier to ensure a chosen unit moves last
        MOVE_INSTR; // priority move instructions (inserted before pilot card instructions)

    }
}
