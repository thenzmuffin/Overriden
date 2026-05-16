package com.total.overiden;

import com.total.overide.OVEquipment;
import com.total.overide.OVSegment;

import java.util.List;

public interface IUnitDesign extends IForceItem {
    IUnitHeader getHeader();
    List<IEquipment> getEquipment();
    boolean hasEquipment(OVEquipment.EquipmentType type);
    List<IWeapon> getWeapons();
    // Retrieve any equipment that can be activated to change movement profile
    List<IEquipment> getActivityEnhancers(Turn.Phase phase);
    OVSegment getSegment(OVSegment.OVLocation location);
    List<OVSegment> getSegments();
    boolean hasIndirectWeapons();
}
