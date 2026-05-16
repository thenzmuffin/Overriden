package com.total.artificial;

import com.total.overide.OVHeader;
import com.total.overiden.IUnitData;

import java.util.ArrayList;
import java.util.List;

/* Header record for the AI, groups together a set of instructions
 *
 */
public class ArtificialHeader {
    private int priority;
    private OVHeader.UnitRole role;
    private IUnitData previousTarget;
    private List<AiInstruction> moves;
    public ArtificialHeader(){
        super();
        priority = 999;
        role = OVHeader.UnitRole.BRAWLER;
        previousTarget = null;
        moves = new ArrayList<>();
    }
}
