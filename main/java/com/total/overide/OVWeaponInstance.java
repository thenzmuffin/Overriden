package com.total.overide;

import com.total.overiden.DamageMessage;
import com.total.overiden.DamageRecord;
import com.total.overiden.Game;
import com.total.overiden.IDamageRecord;
import com.total.overiden.IEquipment;
import com.total.overiden.IUnitData;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.PhysicalWeapon;
import com.total.overiden.TargetData;
import com.total.overiden.TwoDSix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OVWeaponInstance extends OVEquipment implements IWeapon {
    private final OVWeapon weaponType;
    private boolean jammed;
    private boolean artemis;
    private int ticId;
    private WeaponMode mode;

    private final List<OVAmmunition> ammo;

    public OVWeaponInstance(String[] parts, int id, OVWeapon weapon) {
        super(EquipmentType.WEAPON, id, OVMtfReader.parseLocation(parts[1]), weapon.getName());
        weaponType = weapon;
        artemis = false;
        ticId = -1; //not yet assigned
//        setStatus(true); //defaults to true so not needed
        jammed = false;
        ammo = new ArrayList<>();
        mode = weapon.getDefaultMode();
    }

    public OVWeaponInstance(String[] parts) {
        super(EquipmentType.WEAPON, parts);
        weaponType = OVMtfReader.findOVWeaponByID(Integer.parseInt(parts[8]));

        jammed = Boolean.parseBoolean(parts[9]);
        artemis = Boolean.parseBoolean(parts[10]);
        ticId = -1; // will get assigned when generating tics
        mode = WeaponMode.valueOf(parts[12]);
        ammo = new ArrayList<>();
    }

    public OVWeaponInstance(OVSegment.OVLocation location, int id, OVWeapon weapon, boolean state, WeaponMode mode) {
        super(EquipmentType.WEAPON, id, location, weapon.getName());
        weaponType = weapon;
        artemis = false;
//        ticId = ticid; //not yet assigned
        if (!state) {
            setStatus(false); // defaults to true so not needed
            setSent(true); //doesn't need to be synced here
        }
        jammed = false;
        ammo = new ArrayList<>();
        if (mode==null) this.mode = weapon.getDefaultMode();
        else this.mode = mode;
    }

    public void fireWeapon() {
        // allow for streak which only uses ammo if it hits
        if (!ammo.isEmpty() && weaponType.getDmgType() != OVWeapon.WeaponType.STREAK) {
            expendAmmo();
        }
    }

    public boolean expendAmmo() {
        boolean ret = false;
        boolean ammoLeft = false;
        for (int i = 0; i < ammo.size(); i++) {
            if (!ret) ret = ammo.get(i).fire(getWeaponMode());
            if (!ammoLeft) ammoLeft = ammo.get(i).hasAmmo();
            if (ret&&ammoLeft)break;
        }
        if (!ammoLeft) {
            setStatus(false);
        }
        return ret;
    }

    public OVWeapon.WeaponType getWeaponType() {
        return weaponType.getDmgType();
    }

    @Override
    public String getLocationText() {
        return getLocation().getShortName();
    }

    public int getHeat() {
        return weaponType.getHeat() * mode.getRounds();
    }

    @Override
    public void applyCrit(IUnitData unit, IDamageRecord parent) {
        setStatus(false);
        if (weaponType.isExplosiveWeapon() > 0) {
            int damage = Math.floorDiv(weaponType.isExplosiveWeapon(), 3); //have to div by 3 for OV values
            DamageRecord dr = new DamageRecord(null, new TargetData(null, unit));
            dr.setParent(parent);
            dr.addGrouping(damage, getLocation());
            dr.setWeapon(new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.WEAPON, damage, PhysicalWeapon.PhysicalHitGrouping.FULL));
            unit.getTurn().addDamage(dr);
        }
    }

    @Override
    public void checkWeaponJam(int rolled) {

    }

    @Override
    public void hit(TargetData targetData, TargetData.LocTable table) {
        IUnitData target = targetData.getTarget(); // for efficiency and readability
        int damageTotal = weaponType.getDamageMax();
        int damageBase = weaponType.getDamage();
        int mDamage = 0;
        TwoDSix mDice = null;
        int dice = 0;
        boolean artemis = false;
        DamageRecord dmg = new DamageRecord(this, targetData);
        switch (weaponType.getDmgType()) {
            case MISSILE:
                artemis = !targetData.isIndirect() && hasArtemis() && getWeaponMode()==WeaponMode.ARTIV && !(targetData.getEcmBubble()>0);

                // how many M dice
                mDice = new TwoDSix(weaponType.getNoOfDice(), TwoDSix.RollType.MDICE);
                dmg.setClusterDice(mDice);

                // check for Narc modifier (i.e. missiles are Narc equipped)
                // if target is in an ecm bubble narc has no effect
//                if (!targetData.isIndirect() && (getWeaponMode()==WeaponMode.LRMNARC ||
//                        getWeaponMode()==WeaponMode.SRMNARC) && !(targetData.getEcmBubble()>1)) {
                if (!targetData.isIndirect() && getWeaponMode()==WeaponMode.NARC &&
                        !(targetData.getEcmBubble()>1)) {
                    boolean narc = target.isNarced();
//                  target has been narced so adjust cluster hits

                    if (narc) {
                        int reroll = 0;
                        for (int i = 1; i <= weaponType.getNoOfDice(); i++) {
                            if (mDice.getDice(i) > 3) reroll++;
                        }
                        mDice.addDice(reroll);
                    }
                }

                // roll M Dice, add a group for base damage and then every 2 damage after that
                mDamage = damageBase;
                if (artemis) mDice.setmTarget(4);
                for (int i = 1;i <= mDice.getNumberOfDice();i++) {

                    int num = mDice.getDice(i);
                    if (num <= 3) mDamage += num;
                    if (artemis && num == 4) mDamage += 3 ;
                }
                mDamage = Math.min(mDamage, damageTotal);
                if (mDamage > 1) mDamage -= target.getTurn().triggerDefensiveWeapon(mDamage);
                if (mDamage <=0) mDamage = 1; // shouldn't be needed but doesn't hurt
                while (mDamage > 0) {
                    if (mDamage >= weaponType.getDamageC()) {
                        dmg.addGrouping(weaponType.getDamageC());
                        mDamage -= weaponType.getDamageC();
                    } else {
                        dmg.addGrouping(mDamage);
                        mDamage = 0;
                    }
                }
                break;
            case CLUSTER:
                if (getWeaponMode()==WeaponMode.CLUS) { // we can have selected special munitions or not
                    // calculate the number of c dice
                    // deal damage for base damage and then every point of c damage
                    dice = weaponType.getNoOfDice();
                    mDice = new TwoDSix(dice, TwoDSix.RollType.CDICE);
                    dmg.setClusterDice(mDice);
                    dmg.addGrouping(damageBase);
                    // roll C Dice, add a group for base damage and then every 2 damage after that
                    for (int i = 1;i <= dice;i++) {

                        int num = mDice.getDice(i);
                        if (num > 2) {
                            dmg.addGrouping(weaponType.getDamageC());//weapons like HAG have fewer dice and larger damage chunks
                        }
                    }
                    break;
                }
            case PULSE:
            case STANDARD:
                // add max damage to a single group
                dmg.addGrouping(damageTotal);
                break;
            case STREAK:
                //weapon has hit so we need to expend the ammo
                // all weapons in this TIC must be STREAK
                if (!expendAmmo()) {
                    //this means there wasn't any ammo left so it didn't shoot
                    return;
                }
                // check for AMS
                mDamage = damageTotal;
                if (mDamage > 1) mDamage -= target.getTurn().triggerDefensiveWeapon(damageTotal);
//                dmg.addGrouping(damageBase);
//                mDamage = damageTotal - damageBase;
                while (mDamage > 0) {
                    if (mDamage >= weaponType.getDamageC()) {
                        dmg.addGrouping(weaponType.getDamageC());
                        mDamage -= weaponType.getDamageC();
                    } else {
                        dmg.addGrouping(mDamage);
                        mDamage = 0;
                    }
                }
                break;
            case ULTRA:
            case RAPID: //RAC 5 and 2
                dmg.addGrouping(damageTotal);
                if (getWeaponMode()!=WeaponMode.STD) {// double firing an ultra weapon
                    // roll the cluster - using cluster tables not C dice
                    mDice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
                    dmg.setClusterDice(mDice);
                    int clusters = OVWeapon.getNumberOfHits(mDice.getTotal(), getWeaponMode().getRounds());
                    for (int i = 1; i < clusters; i++) //start at 1 because one has already auto hit
                    { // works for RAC and Ultra as getNumberOfHits uses the appropriate cluster table
                        dmg.addGrouping(weaponType.getDamage());// returns the base damage which in this case is correct
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
                switch (weaponType.getName()) {
                    case "TAG":
                    case "LTAG":
                        target.getTurn().addDamage(new DamageMessage("Unit has been TAGed"));
                        target.getTurn().setTaggedThisTurn(true);
                        break;
                    case "NARC":
                        OVSegment.OVLocation narcLoc = target.convertLocation((new TwoDSix()).getTotal(), targetData.getFacing(), TargetData.LocTable.FULL, false,null);
                        OVEquipment narcPod = new OVEquipment(OVEquipment.EquipmentType.NARC, 0, narcLoc, "NARC Pod");
                        narcPod.setSpecial(1); // can we use the special field to capture the turn the pod is attached?
                        target.getEquipment().add(narcPod);
                        target.getTurn().addDamage(new DamageMessage("NARC Pod Attached-" + narcLoc.toString()));
                        break;
                }
                // for special type don't add the general damagerecord
                return;
        }
        target.getTurn().addDamage(dmg);
        dmg.applyDamage(target);
    }

    @Override
    public WeaponMode getWeaponMode() {
        return mode;
    }

    @Override
    public boolean setWeaponMode(WeaponMode mode) {
        this.mode = mode;
        setSent(false);
        return true;
    }

    @Override
    public int getRangeMod(int range) {
        int mod;
        if (Game.current.isHexless()) {
            mod = weaponType.getRange().getRange(range);
        } else {
            // Hex based game
            mod = weaponType.getTwRangeMod(range);
            // for a hexed game we need to add on special modifiers for specific weapons:
            // pulse laser -2
            // ATM -1
            // MRM +1
            mod += weaponType.getTargetingMod();

        }
        // this works for LB-X, are there weapons marked as cluster that aren't -1 to hit?
        if (mode == WeaponMode.CLUS) mod--;
        return mod;
    }

    @Override
    public int getHeatDamage() {
        return weaponType.getHeatDamage();
    }

    @Override
    public DamageType getDamageType() {
        return weaponType.getDamageType();
    }

    @Override
    public boolean isMultiMode() {
        return weaponType.getDmgType() == OVWeapon.WeaponType.ULTRA ||
                weaponType.getDmgType() == OVWeapon.WeaponType.RAPID ||
                weaponType.getDmgType() == OVWeapon.WeaponType.CLUSTER;
    }

    public boolean isOperational() {
        boolean hasAmmo = !weaponType.hasAmmo();
        for (OVAmmunition set : ammo) {
            if (set.isOperational() && set.getRemaining() > 0) {
                hasAmmo = true;
                break;
            }
        }
        return super.isOperational() && !jammed && hasAmmo;
    }

    public void setJammed(boolean jammed) {
        if (this.jammed != jammed) setSent(false);
        this.jammed = jammed;
    }

    public boolean isJammed() {
        return jammed;
    }

    public String getWeaponName() {
        String weaponName = weaponType.getName();
        if (getLocation() == OVSegment.OVLocation.REAR) weaponName += "(R)";
        if (artemis) weaponName +="(AIV)";
        // Allow the display of remaining ammo to be configured
        if (!ammo.isEmpty() && (Game.current == null || Game.current.isDisplayAmmo())) {
            weaponName += "(";
            int rem = 0;
            WeaponMode ty = null;
            for (OVAmmunition am : ammo) {
                if (ty != am.getAmmoType()) {
                    if (ty!=null)weaponName += rem + ty.getSuffix() + ",";
                    ty = am.getAmmoType();
                    rem = 0;
                }
                rem += am.getRemaining();
            }
            if (ty!=null)weaponName += rem + ty.getSuffix() + ",";
            weaponName = weaponName.substring(0, weaponName.length() - 1);
            weaponName += ")";
        }
        return weaponName;
    }

    public void addAmmo(OVAmmunition toAdd) {
        if (ammo.isEmpty()){
            //first type of ammo added should be the default, ensures that the initial mode value
            //for any weapon with ammo matches a valid ammo type i.e. an AC in a mech with only AP
            //rounds isn't defaulted to STD
            mode = toAdd.getAmmoType();
        }
        ammo.add(toAdd);
        // new ammo bin added, sort by type
        ammo.sort(Comparator.comparing(OVAmmunition::getAmmoType));
    }

    public void setTic(int ticId) {
        this.ticId = ticId;
    }

    public int getTic() {
        return ticId;
    }

    @Override
    public List<OVAmmunition> getAmmo() {
        return ammo;
    }

    public boolean hasArtemis() {
        return artemis;
    }
    public void setArtemis(boolean art){
        artemis = art;
        // if a weapon is artemis it should use artemis enabled missiles
        mode = WeaponMode.ARTIV;
    }

    @Override
    public MainActivity.Sounds getSoundEffect() {
        return getWeapon().getSoundEffect();
    }

    @Override
    public int getClusterDamage() {
        return weaponType.getDamageC()==0?weaponType.getDamage():weaponType.getDamageC();
    }

    private int getRemainingRounds(WeaponMode localMode) {
        int count = 0;
        for (OVAmmunition ammoBin : ammo) {
            if (ammoBin.getAmmoType() == localMode)
                count += ammoBin.getRemaining();
        }
        return count;
    }

    public static OVWeaponInstance getWeaponInstance(String[] weaponName, int key, boolean isISTech) {
        OVWeapon weapon = OVMtfReader.findWeaponByName(weaponName[0], isISTech);
        if (weapon==null){
            System.out.println("No Weapon found for name " + weaponName[0]);
            return null;
        }
        return new OVWeaponInstance(weaponName, key, weapon);
    }

    @Override
    public int getID() {
        return super.getID();
    }

    @Override
    public int compareTo(IEquipment iEquipment) {
        int ret = super.compareTo(iEquipment);
        if (ret == 0 && iEquipment instanceof OVWeaponInstance) {
            ret = weaponType.getDmgType().compareTo(((OVWeaponInstance) iEquipment).weaponType.getDmgType());
            // not sure this will ever be used as super sorts by location already
            if (ret == 0) {
                if (getLocation() == OVSegment.OVLocation.REAR ^ iEquipment.getLocation() == OVSegment.OVLocation.REAR) {
                    if (getLocation() == OVSegment.OVLocation.REAR) ret = -1;
                    else ret = 1;
                }
            }
        }
        return ret;
    }

    @Override
    public String getName() {
        if (Game.current!=null && !Game.current.isUseTics())
            return getWeaponName();
        return ticId + " - " + super.getName();
    }

    @Override
    public List<WeaponMode> getAvailableModes() {
        List<WeaponMode> modeList = new ArrayList<>();
        for (OVAmmunition rounds : ammo){
            if (!modeList.contains(rounds.getAmmoType()) && rounds.getRemaining()>0){
                modeList.add(rounds.getAmmoType());
            }
        }
        int rounds = getRemainingRounds(WeaponMode.STD);
        switch (weaponType.getDmgType()) {
            case RAPID:
                if (rounds > 1)
                    modeList.add(WeaponMode.MULTI2);
                if (rounds > 2)
                    modeList.add(WeaponMode.MULTI3);
                if (rounds > 3)
                    modeList.add(WeaponMode.MULTI4);
                if (rounds > 4)
                    modeList.add(WeaponMode.MULTI5);
                if (rounds > 5)
                    modeList.add(WeaponMode.MULTI6);
                break;
            case ULTRA:
                if (rounds > 1)
                    modeList.add(WeaponMode.MULTI2);
                break;
            case CLUSTER:
                //should be handled by the ammution type selections
//                if (getRemainingRounds(WeaponMode.CLUS) > 1)
//                    modeList.add(WeaponMode.CLUS);
                break;
            case DEFENCE:
                modeList.clear(); //get rid of the standard option, not used for defensive weapons
                if (weaponType.getName().toLowerCase().contains("ams")) {//rubbish way to recognise AMS
                    modeList.add(WeaponMode.OFF);
                    // should always have ammo to reach here as the weapon should be disabled if no more ammo is available
                    // laser AMS doesn't need ammo however
                    if (!weaponType.hasAmmo() || getRemainingRounds(WeaponMode.STD) > 0)
                        modeList.add(WeaponMode.AUTO);
                }
                break;
        }

        return modeList;
    }

    public void getAvailableModesSet(List<WeaponMode> list) {
        for (WeaponMode mode : getAvailableModes()) {
            if (!list.contains(mode)) {
                list.add(mode);
            }
        }
    }

    public boolean isIndirect() {
        return getName().toLowerCase(Locale.ENGLISH).contains("lrm"); //LRMs and thunderbolt missiles can be indirect
    }

    public String getStreamValue() {
        String stream = super.getStreamValue();
        stream += "," + weaponType.getId() + "," + jammed + "," + artemis + "," + ticId + "," + mode;
        return stream;
    }

    @Override
    public void updateFromStream(String[] data) {
        super.updateFromStream(data);
        if (data.length > 11) { //this check shouldn't be needed but makes sure there isn't an array error
            jammed = Boolean.parseBoolean(data[9]);
            artemis = Boolean.parseBoolean(data[10]); // artemis can be destroyed with the weapon still active so best to update here
            mode = WeaponMode.valueOf(data[12]);
            System.out.println("weapon update:jammed" + jammed + " - mode" + mode);
        } else System.out.println("Weapon update - not enough fields in input");
    }
    public OVWeapon getWeapon(){return weaponType;}
    @Override
    public String getDamageText() {
        return "" + weaponType.getDamageMax();
    }

}
