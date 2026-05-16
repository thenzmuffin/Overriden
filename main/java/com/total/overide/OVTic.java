package com.total.overide;

import android.content.ContentValues;

import androidx.annotation.Nullable;
import com.total.overiden.DamageMessage;
import com.total.overiden.DamageRecord;
import com.total.overiden.Game;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.TargetData;
import com.total.overiden.TwoDSix;

import java.util.ArrayList;
import java.util.List;

public class OVTic implements IWeapon {
    public static final int cMaxBaseDamage = 5; // 5 * 3
    public static final int cMaxMaxDamage = 14; // 14 * 3
    private final List<OVWeaponInstance> list;
    private final int id;
    private final OVRange range;

    //	private String ticType = null;
    private OVWeapon.WeaponType dmgType = null;
//    private boolean special = false; // for a switchable weapon is the special function on or off
    private WeaponMode mode = WeaponMode.STD; //static method, which firing mode is currently selected
    public OVTic(int key){
        super();
        this.id = key;
        list = new ArrayList<OVWeaponInstance>();
        range = new OVRange();
    }
    public static OVTic newTIC(List<IEquipment> equip, String data){
        // Super inefficient, is there a better way?
        String[] parts = data.split(",");
        OVTic tic = new OVTic(Integer.parseInt(parts[0]));
        for (int i = 1;i< parts.length;i++){
            int id = Integer.parseInt(parts[i]);
            for (IEquipment weapon : equip) {
                if (weapon.getID() == id && weapon instanceof OVWeaponInstance) {
                    tic.addWeapon((OVWeaponInstance) weapon);
                    break;
                }
            }
        }
        return tic;
    }

    public List<OVWeaponInstance> getWeapons(){
        return list;
    }
    @Override
    public int getID() {
        return id;
    }

    @Override
    public boolean isMoveModifier() {
        return false;
    }

    @Override
    public int getSpecial() {
        return 0;
    }

    @Override
    public void setSpecial(int special) {

    }

    @Override
    public int getSpecialTwo() {
        return 0;
    }

    @Override
    public void setSpecialTwo(int special) {

    }

    @Override
    public boolean activateEquipment(IUnitData unit) {
        return false;
    }

    @Override
    public void resolveTurn() {
        //nothing to do here
    }

    @Override
    public void fireWeapon() {

        // for each weapon in the tic we need to expend ammo if it has any
        for (int i = 0; i < list.size();i++) {
            list.get(i).fireWeapon();
        }
    }

    @Override
    public void checkWeaponJam(int rolled) {
        boolean isUltra = dmgType == OVWeapon.WeaponType.ULTRA;
        boolean isRapid = dmgType == OVWeapon.WeaponType.RAPID;
        int jamOn = 0;
        switch (getWeaponMode()){
            case MULTI2:
            case MULTI3:
                jamOn = 2;
                break;
            case MULTI4:
            case MULTI5:
                jamOn = 3;
                break;
            case MULTI6:
                jamOn = 4;
                break;
        }
        // for ULTRA and RAPID the weapon can jam when fired at a higher than normal rate
        if (jamOn > 0) {
            boolean jammed = false;
            if (rolled == 0) {
                // if dice roll not supplied then ask the user whether the jam has occured
                //Custom button text
                Object[] options = {"Jammed",
                        "No Issue"};
//                int n = JOptionPane.showOptionDialog(Frame.getFrames()[0],
//                        "Ultra Weapons Jam on a roll of 2",
//                        "Weapon Jam?",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.QUESTION_MESSAGE,
//                        null,
//                        options,
//                        options[1]);
//                jammed = n == 0;

            } else {
                jammed = rolled <= jamOn;
            }
            if (jammed) {
                // how do we jam the weapon(s)? If multiple rapid fire weapons are in this tic do we jam all of them or just one? At present jam all weapons
                // for ULTRA where the jam can't be cleared we can just mark the weapon as not operational - need to flag a DamageMessage back to the user though
//                Game.findUnit(mechKey).getTurn().addDamage(new DamageMessage("Weapon Jammed :" + getDescription()));
                for (int i = 0;i<list.size();i++) {
              //      if (list.get(i).getWeaponType().getDmgType() == OVWeapon.WeaponType.ULTRA||list.get(i).getWeaponType().getDmgType() == OVWeapon.WeaponType.RAPID) {
                        list.get(i).setJammed(jammed);
              //      }
                }
            }
        }

    }

    @Override
    public boolean isJammed() {
        return false;
    }

    @Override
    public OVSegment.OVLocation getLocation() {
        // Tics can have more than one location??
        OVSegment.OVLocation ret = null;
        if (!list.isEmpty()){
            ret = list.get(0).getLocation();
        }
        return ret;
    }

    public String getLocationText() {
        StringBuilder desc = new StringBuilder();
        String loc;
        for (int i = 0; i < list.size();i++) {
            loc = list.get(i).getLocation().getShortName();
            if (!desc.toString().contains(loc)) {
                if (i > 0) desc.append(", ");
                desc.append(loc);
            }
        }
        return desc.toString();
    }

    @Override
    public OVEquipment.EquipmentType getType() {
        return OVEquipment.EquipmentType.WEAPON;
    }

    @Override
    public void setStatus(boolean operational) {

    }

    @Override
    public void reverseCrit() {

    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {

    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getDamage() {
        int damage = 0;
        for(OVWeaponInstance inst : list){
            damage += inst.getDamage();
        }
        return damage;
    }

    public void recalculateTic() {
        range.pointBlankR = range.shortR = range.mediumR = range.longR = range.extremeR = -10;
        dmgType = null;
        for (int i = 0;i < list.size();i++){
            updateRange(list.get(i).getWeapon().getRange());
            if (dmgType == null || dmgType == OVWeapon.WeaponType.STANDARD) dmgType = list.get(i).getWeapon().getDmgType();

        }
    }
    public void addWeapon(OVWeaponInstance weapon) {
        OVRange rng = weapon.getWeapon().getRange();
        list.add(weapon);
        weapon.setTic(this.id);
        if (list.size() == 1){
            range.pointBlankR = rng.pointBlankR;
            range.shortR = rng.shortR;
            range.mediumR = rng.mediumR;
            range.longR = rng.longR;
            range.extremeR = rng.extremeR;

            mode = weapon.getWeaponMode();
        } else {
            updateRange(rng);
        }
        //		ticType = weapon.getWeaponType().getTicGroup();
        OVWeapon.WeaponType damageType = weapon.getWeapon().getDmgType();
        // STANDARD and CLUSTER weapons can be mixed, need to add logic for Ultra
        if (dmgType == null || dmgType == OVWeapon.WeaponType.STANDARD) dmgType = damageType;
    }

    public void removeWeapon(int weaponID){
        for (int i = 0;i < list.size();i++) {
            if (list.get(i).getID() == weaponID){
                list.remove(i);
                break;
            }
        }
        recalculateTic();
    }

    private void updateRange(OVRange rng){
        if (rng.pointBlankR > range.pointBlankR) {
            range.pointBlankR = rng.pointBlankR;
        }
        if (rng.shortR > range.shortR) {
            range.shortR = rng.shortR;
        }
        if (rng.mediumR > range.mediumR) {
            range.mediumR = rng.mediumR;
        }
        if (rng.longR > range.longR) {
            range.longR = rng.longR;
        }
        if (rng.extremeR > range.extremeR) {
            range.extremeR = rng.extremeR;
        }
    }

    public int getBaseDamage(){
        int dmg = 0;
        for (int i = 0; i <list.size();i++) {
            // exclude weapons that aren't operational
//            if (list.get(i).isOperational()) {
                dmg += list.get(i).getWeapon().getDamage();
//            }
        }
        return dmg;
    }
    public int getMaxDamage(){
        int dmg = 0;
        for (int i = 0; i < list.size();i++) {
            // exclude weapons that aren't operational
//            if (list.get(i).isOperational()) {
                dmg += list.get(i).getWeapon().getDamageMax();
//            }
        }
        return Math.floorDiv(dmg+2, 3);
    }

    public int getHeatDamage(){
        int dmg = 0;
        for (int i = 0; i < list.size();i++) {
            // exclude weapons that aren't operational
            if (list.get(i).isOperational()) {
                dmg += list.get(i).getHeatDamage();
            }
        }
        if (dmg > 0 && dmg <= 7) dmg = 1; // if any heat damage is inflicted there is aminimum of 1 Heat applied
        else dmg = Math.floorDiv(dmg+2, 5); // otherwise round to the nearest group of 5.
        return dmg;
    }
    public int size() {
        return list.size();
    }

    public String getDamageText() {
        int base = getBaseDamage();
        String temp;
        int max = getMaxDamage();
        int dice = 0;
        if (max > base) {
            temp = Integer.toString( base );
            for (int i = 0; i < list.size();i++) {
                // exclude weapons that aren't operational
                dice+= (list.get(i).getWeapon().getNoOfDice());
            }
            String diceType = "";
            switch (dmgType) {
                case MISSILE:
                    diceType = "+M";
                    break;
                case CLUSTER:
                    diceType = "+C";
                    break;
                case ULTRA: // this won't work for ultra
                    diceType = "+2R";
                    break;
                default:
            }
            temp += diceType + dice + " (" + max + ")";
        } else {
            temp = Integer.toString( max );
        }
        if (getHeatDamage()>0){
            temp += "+" + getHeatDamage() + "H";
        }
        return temp;
    }

    @Override
    public boolean hasArtemis() {
        return false;
    }

    @Override
    public void setArtemis(boolean art) {

    }

    @Override
    public MainActivity.Sounds getSoundEffect() {

        return getWeapons().get(0).getSoundEffect();
    }

    @Override
    public int getClusterDamage() {
        return 0;
    }

    public int getRangeMod(int range) {
        int calc = this.range.getRange(range);
        // The only weapons that use cluster mode are LB-X (other cluster weapons use the cluster
        // damage type but will have standard ammo e.g. HAG40 LB-X firing cluster get -1 to hit
        if (mode==WeaponMode.CLUS) calc--;
        return calc;
    }

    @Override
    public DamageType getDamageType() {
        return null;
    }

    @Override
    public boolean isMultiMode() {
        // what about where weapons have special ammo? (e.g. amour piercing)
        return dmgType == OVWeapon.WeaponType.ULTRA || dmgType == OVWeapon.WeaponType.RAPID ||
                dmgType == OVWeapon.WeaponType.CLUSTER;
    }

    public String getName() {
        int count = 2;
        String desc = "";
        String name;
        String temp = null;
        for (int i =0;i < list.size();i++) {
            name = list.get(i).getWeaponName();
            if (desc.contains(name)) {
                int start = desc.indexOf(name);
                temp = "";
                if (start > 0) {
                    int back = 0;
                    if (desc.charAt(start - 1) == 'x') {
                        count = Integer.parseInt(desc.substring(start-2, start-1));
                        back = 2;
                        count++;
                    }
                    temp = desc.substring(0, start - back);
                }
                temp += count + "x";
                count = 2;
                temp += desc.substring(start);
                desc = temp;
            } else {
                desc += name;
            }
        }
        return desc;
    }

    private void damageWithHeadTransfer(DamageRecord dmg, int pips) {
        DamageRecord.DamageGrouping group = dmg.addGrouping(pips);
        // head shot: in this case deal 2 damage to the head and everything else to the torso
        // only if there is more than one weapon in the tic
        if (pips > 2 && group.getConvertedLocation() == OVSegment.OVLocation.HEAD &&
            getWeapons().size()>1) {
            group.damage = 2;
            dmg.addGrouping(pips-2, new TwoDSix(3,4));
//            group.location.setDice(3,4);
        }
    }

    @Override
    public void hit(TargetData unit, TargetData.LocTable table) {
        //		Random ran = new Random();
        //		int roll = 0;
        int damageTotal = getMaxDamage();
        int damageBase = getBaseDamage();
        hit(unit, damageBase,damageTotal);
    }

    public void hit(TargetData unit, int baseDmg,int totalDmg) {
        int damageTotal = totalDmg;
        int damageBase = baseDmg;
        int mDamage = 0;
        TwoDSix mDice = null;
        int dice = 0;
        boolean artemis = false;
        DamageRecord dmg = new DamageRecord(this, unit);
        switch (dmgType) {
            case MISSILE:
                // missile type can only be grouped with other missile types
                // how many M dice

                artemis = list.get(0).hasArtemis();
                for (int i = 0; i < list.size();i++) {
                    // exclude weapons that aren't operational
                    dice+= (list.get(i).getWeapon().getNoOfDice());
                }
                mDice = new TwoDSix(dice, TwoDSix.RollType.MDICE);
                dmg.setClusterDice(mDice);

                // check for Narc modifier

//                if (getWeaponMode()==WeaponMode.SRMNARC ||
//                    getWeaponMode()==WeaponMode.LRMNARC) {
                if (getWeaponMode()==WeaponMode.NARC) {
                    boolean narc = false;
//                    in the event the missiles are narc equipped then we need to check for NARC pods on the target
                    for (IEquipment equip : unit.getTarget().getEquipment()) {
                        if (!equip.isOperational()) continue;
                        if (equip.getType() == OVEquipment.EquipmentType.NARC && equip.getSpecial() == 0) {
                            narc = true;
                        }
                    }

                    if (narc) {
                        int reroll = 0;
                        for (int i = 1; i <= dice; i++) {
                            if (mDice.getDice(i) > 3) reroll++;
                        }
                        mDice.addDice(reroll);
                    }
                }

//                damageWithHeadTransfer(dmg, damageBase);
                // roll M Dice, add a group for base damage and then every 2 damage after that
                mDamage = damageBase;
                if (artemis) mDice.setmTarget(4);
                for (int i = 1;i <= dice;i++) {

                    int num = mDice.getDice(i);
                    if (num <= 3) mDamage += num;
                    if (artemis && num == 4) mDamage += 3 ;
                }
                mDamage = Math.min(mDamage, damageTotal);
                if (!(getWeaponMode()==WeaponMode.INFERNO)) {
                    if (mDamage > 1)
                        mDamage -= unit.getTarget().getTurn().triggerDefensiveWeapon(mDamage);
                    if (mDamage <= 0) mDamage = 1;
                    while (mDamage > 0) {
                        if (mDamage >= 2) {
                            dmg.addGrouping(2);
                            mDamage -= 2;
                        } else {
                            dmg.addGrouping(1);
                            mDamage -= 1;
                        }
                    }
                } else {
                    // Inferno missiles
                    dmg.setHeatDamage(mDamage);
                    // is there a problem with having no groups?
                }
                break;
            case CLUSTER:
                if (getWeaponMode()==WeaponMode.CLUS) { // we can have selected special munitions or not
                    // calculate the number of c dice
                    // deal damage for base damage and then every point of c damage
                    for (int i = 0; i < list.size();i++) {
                        // exclude weapons that aren't operational
                        dice+= (list.get(i).getWeapon().getNoOfDice());
                    }
                    mDice = new TwoDSix(dice, TwoDSix.RollType.CDICE);
                    dmg.setClusterDice(mDice);
                    damageWithHeadTransfer(dmg, damageBase);
                    // roll C Dice, add a group for base damage and then every 2 damage after that
                    for (int i = 1;i <= dice;i++) {

                        int num = mDice.getDice(i);
                        if (num > 2) {
                            dmg.addGrouping(1);
                        }
                    }
                    break;
                }
            case PULSE:
            case STANDARD:
                // add max damage to a single group UNLESS the tic has multiple weapons and the location is the head
                damageWithHeadTransfer(dmg, damageTotal);
                if (getHeatDamage()>0)dmg.setHeatDamage(getHeatDamage());
                break;
            case STREAK:
                //weapon has hit so we need to expend the ammo
                for (int i = 0;i<list.size();i++) {
                    // all weapons in this TIC must be STREAK
                    if (!list.get(i).expendAmmo()) {
                        //this means there wasn't any ammo left so it didn't shoot
                        damageTotal -= list.get(i).getWeapon().getDamageMax();
                        if (damageTotal < damageBase)damageBase = damageTotal;
                    }
                }
                if (damageTotal > 1) damageTotal -= unit.getTarget().getTurn().triggerDefensiveWeapon(mDamage);
//                damageWithHeadTransfer(dmg, damageBase);
                mDamage = list.get(0).getWeapon().getDamageC();
                while (damageTotal > 0) {
                        dmg.addGrouping(Math.min(damageTotal,mDamage));
                    damageTotal -=mDamage;
                }
                break;
            case ULTRA:
            case RAPID: //RAC 5 and 2
                damageWithHeadTransfer(dmg, damageTotal);
                if (getWeaponMode()!=WeaponMode.STD) {// double firing an ultra weapon
                    //find any ultra weapons and add their damage as a secondary hit.
                    for (OVWeaponInstance inst : list) {
//                        for (int i = 0; i < list.size();i++) {
                        if (inst.isOperational() && (inst.getWeapon().getDmgType() == OVWeapon.WeaponType.ULTRA ||
                                inst.getWeapon().getDmgType() == OVWeapon.WeaponType.RAPID)){
                            // roll the cluster
                            mDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
                            dmg.setClusterDice(mDice);
                            int clusters = OVWeapon.getNumberOfHits(mDice.getTotal(),getWeaponMode().getRounds());
                            for (int i = 1;i<clusters;i++) //start at 1 because one has already auto hit
                            { // will need cluster for RAC - should we add it here as well?
                                dmg.addGrouping(inst.getWeapon().getDamage());
                            }
                        }
                    }
                }
                // ultra and RAC is like cluster except every cluster does more than 1 point of damage (potentially, ultra ac 2 does only 1 damage per cluster)
                // note that a critical can occur here to jam the weapon, for RAC the jam can also be cleared
                break;
            case DEFENCE:
                // Shouldn't be shot, this includes AMS
                break;
            case SPECIAL:
                // special includes weapons such as the narc
                // if it is TAG then mark the target as tagged
                for (OVWeaponInstance checkWeapon : getWeapons()){
                    switch (checkWeapon.getWeapon().getName()){
                        case "TAG":
                        case "LTAG":
                            unit.getTarget().getTurn().addDamage(new DamageMessage("Unit has been TAGed"));
                            unit.getTarget().getTurn().setTaggedThisTurn(true);
                            break;
                        case "NARC":
                            OVSegment.OVLocation narcLoc = unit.getTarget().convertLocation((new TwoDSix()).getTotal(),unit.getFacing(), TargetData.LocTable.FULL, false,null);
                            OVEquipment narcPod = new OVEquipment(OVEquipment.EquipmentType.NARC,0,narcLoc,"NARC Pod");
                            narcPod.setSpecial(1); // can we use the special field to capture the turn the pod is attached?
                            unit.getTarget().getEquipment().add(narcPod);
                            unit.getTarget().getTurn().addDamage(new DamageMessage("NARC Pod Attached-" + narcLoc.toString()));
                            break;
                    }
                }
                // for special type don't add the general damagerecord
                return;
        }
        unit.getTarget().getTurn().addDamage(dmg);
        dmg.applyDamage(unit.getTarget());
    }

//    @Override
//    public void setSpecial(boolean on) {
//        this.special = on;
//    }

    @Override
    public WeaponMode getWeaponMode() {
        return mode;
    }

    @Override
    public boolean setWeaponMode(WeaponMode mode) {
        boolean ret = true;
        for (OVWeaponInstance inst : list){
            ret = inst.setWeaponMode(mode);
            if (!ret) break;
        }
        if (ret)
            this.mode = mode;
        else {
            // reset to the previous mode
            for (OVWeaponInstance inst : list){
                ret = inst.setWeaponMode(this.mode);
                if (!ret) break;
            }
        }

        return ret;
    }

    @Override
    public List<WeaponMode> getAvailableModes() {
        List<WeaponMode> ret = new ArrayList<>();
        for (OVWeaponInstance inst : list){
            inst.getAvailableModesSet(ret);
        }
        return ret;
    }

    @Override
    public int getHeat() {
        int heat = 0;
        for (int i = 0; i < list.size();i++) {
            // the heat stored here is from TW
            heat += list.get(i).getHeat();
        }
        // adding two then getting the floor of dividing by 5 gets us the correct result
        // e.g. 3 heat goes to 1 (3+2)/5 == 1, 6 heat goes to 1 also ( (6+2) / 5 == 1)
        // so note that sometimes combining weapons increases the net heat (2xslas == 1 heat) and sometimes it decreases the heat (2xmlas == 1 heat)
        //		heat = Math.floorDiv(heat+2, 5);
        if (Game.current!=null && !Game.current.isSmartHeat()) heat = Math.floorDiv(heat+2, 5);
        return heat;
    }

//    public boolean hasBothAmmo() {
//        boolean ret = true;
//        for (int i = 0; i < list.size() && ret;i++) {
//            // looking for a switchable weapon, does it have both ammo types?
//            if (list.get(i).getWeaponType().switchableWeapon()) {
//                ret = list.get(i).checkSwitchAmmo();
//            }
//
//        }
//        return ret;
//    }

    public OVWeapon.WeaponType getWeaponType() {
        return dmgType;
    }
    @Override
    public boolean isOperational() {
        boolean active = true;
        for (int i = 0;i<list.size() && active;i++) {
//            active = active || list.get(i).isOperational();
            active = list.get(i).isOperational();
        }
        return active;
    }
    public boolean canAddWeapon(OVWeapon weap) {
        int base = weap.getDamage();
        int max = weap.getDamageMax();
        for (int i = 0; i < list.size();i++) {
            base += list.get(i).getWeapon().getDamage();
            max += list.get(i).getWeapon().getDamageMax();
        }
        max = Math.floorDiv(max+2, 3);
        boolean can = max <= cMaxBaseDamage;
        if (base < max) {
            can = max <= cMaxMaxDamage && base <= cMaxBaseDamage;
        }
        return can;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean match = false;
        if (obj instanceof OVTic){
            match = id == ((OVTic) obj).id;
        }
        return match;
    }

    @Override
    public int compareTo(IEquipment iEquipment) {
        return 0;
    }

    @Override
    public List<OVAmmunition> getAmmo() {
        List<OVAmmunition> ammo = new ArrayList<>();
        for (OVWeaponInstance inst : list) {
            if (inst.isOperational()){
                ammo.addAll(inst.getAmmo());
            }
        }
        return ammo;
    }
    public boolean isIndirect(){
        boolean indirect = true;
        for (OVWeaponInstance inst : getWeapons()){
            indirect = inst.isIndirect();
            if (!indirect)break;
        }
        return indirect;
    }

    @Override
    public String getStreamValue() {
        StringBuilder stream = new StringBuilder("TIC:");
        boolean first = true;
        stream.append(Integer.toString(id));
        for (OVWeaponInstance inst: getWeapons()){
            stream.append(",");
            stream.append(inst.getID());
        }
        stream.append('\n');
        return stream.toString();

    }

    @Override
    public void updateFromStream(String[] data) {

    }

    @Override
    public boolean alreadySent() {
        return false;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public void setIndex(int ind) {

    }

    @Override
    public void markAsSent() {

    }

    @Override
    public int getCritSlots() {
        return 0;
    }

    @Override
    public void setDatabase(ContentValues cv) {

    }
}
