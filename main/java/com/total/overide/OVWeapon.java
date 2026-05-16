package com.total.overide;

import androidx.fragment.app.Fragment;

import com.total.overiden.IEquipment;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.TwoDSix;

import java.util.ArrayList;
import java.util.List;

public class OVWeapon {
    private static int[][] clusterTables = {{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2},//2
            {1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3},//3
            {1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4},//4
            {1, 2, 2, 3, 3, 3, 3, 4, 4, 5, 5},//5
            {2, 2, 3, 3, 4, 4, 4, 5, 5, 6, 6},//6
            {2, 2, 3, 4, 4, 4, 4, 6, 6, 7, 7},
            {3, 3, 4, 4, 5, 5, 5, 6, 6, 8, 8},
            {3, 3, 4, 5, 5, 5, 5, 7, 7, 9, 9},
            {3, 3, 4, 6, 6, 6, 6, 8, 8, 10, 10},
            {4, 4, 5, 7, 7, 7, 7, 9, 9, 11, 11},
            {4, 4, 5, 8, 8, 8, 8, 10, 10, 12, 12},
            {4, 4, 5, 8, 8, 8, 8, 11, 11, 13, 13},
            {5, 5, 6, 9, 9, 9, 9, 11, 11, 14, 14},
            {5, 5, 6, 9, 9, 9, 9, 12, 12, 15, 15},
            {5, 5, 7, 10, 10, 10, 10, 13, 13, 16, 16},
            {5, 5, 7, 10, 10, 10, 10, 14, 14, 17, 17},
            {6, 6, 8, 11, 11, 11, 11, 14, 14, 18, 18},
            {6, 6, 8, 11, 11, 11, 11, 15, 15, 19, 19},
            {6, 6, 9, 12, 12, 12, 12, 16, 16, 20, 20},
            {7, 7, 9, 13, 13, 13, 13, 17, 17, 21, 21},
            {7, 7, 9, 14, 14, 14, 14, 18, 18, 22, 22},
            {7, 7, 10, 15, 15, 15, 15, 19, 19, 23, 23},
            {8, 8, 10, 16, 16, 16, 16, 20, 20, 24, 24},
            {8, 8, 10, 16, 16, 16, 16, 21, 21, 25, 25},
            {9, 9, 11, 17, 17, 17, 17, 21, 21, 26, 26},
            {9, 9, 11, 17, 17, 17, 17, 22, 22, 27, 27},
            {9, 9, 11, 17, 17, 17, 17, 23, 23, 28, 28},
            {10, 10, 12, 18, 18, 18, 18, 23, 23, 29, 29},
            {10, 10, 12, 18, 18, 18, 18, 24, 24, 30, 30},
            {12, 12, 18, 24, 24, 24, 24, 32, 32, 40, 40}};//40

    public enum WeaponType {
        STANDARD(""),
        CLUSTER("cl"),
        MISSILE(""),
        STREAK("st"),
        ULTRA(""),
        RAPID(""),
        PULSE(""),
        SPECIAL(""),
        DEFENCE("");
        final String suffix;
        WeaponType(String suffix){
            this.suffix = suffix;
        }
        String getSuffix(){return suffix;}
    }
    private int id = 0;
    private String name;
    private final boolean techIS;
    private int baseDamage;
    private int damageC; // damage per C dice hit (defaults to 1 for cluster weapons if 0)
    private final int noOfDice; // if specified overrides the default of 1 per OV point of damage
    private int damageMax;
    private int heat; //TW heat
    private final int heatDamage; //TW heat inflicted by weapon
    private int heatDamageDice = 0;
    private final int ammoPerTon;
    private final WeaponType dmgType;
    private final String ticGroup;
    private final OVRange range;
    private final OVRange twRange;
    private IWeapon.DamageType damageType;
    private final int explosiveWeapon;
    private final int explosiveAmmo;
    private final String[] critNames;
    private final IWeapon.WeaponMode defaultMode;
    private final int twClusterTable;
    private final int twClusterDamage;
    private final int twClusterGroup;
    private final int targetingMod;
    private MainActivity.Sounds soundEffect;
    public OVWeapon(String pData) {
        super();

        String[] data = pData.split(",");
        int i = 0;
        id = Integer.parseInt(data[i++]);
        name = data[i++];
        damageMax = Integer.parseInt(data[i++]);
        dmgType = WeaponType.valueOf(data[i++]); // weapon type
        techIS = data[i++].equalsIgnoreCase("IS"); // is or clan
        heat = Integer.parseInt(data[i++]);
        if (data[i].contains("D")) {
            heatDamageDice = Integer.parseInt(data[i++].substring(1));
            heatDamage = 0;
        }else heatDamage = Integer.parseInt(data[i++]);
        ammoPerTon = Integer.parseInt(data[i++]);
        baseDamage = Integer.parseInt(data[i++]);
        noOfDice = Integer.parseInt(data[i++]);
        damageC = Integer.parseInt(data[i++]);
        range = new OVRange();

        range.pointBlankR = Integer.parseInt(data[i++]);
        range.shortR = Integer.parseInt(data[i++]);
        range.mediumR = Integer.parseInt(data[i++]);
        range.longR = Integer.parseInt(data[i++]);
        range.extremeR = Integer.parseInt(data[i++]);

        ticGroup = data[i++];
        critNames = data[i++].split(":");

        damageType      = IWeapon.DamageType.valueOf(data[i++]);
        explosiveWeapon = Integer.parseInt(data[i++]);
        explosiveAmmo   = Integer.parseInt(data[i++]);
        // holding TW Range values in an OVRange object for now - point blank is actually minimum range
        twRange = new OVRange();
        twRange.pointBlankR = Integer.parseInt(data[i++]);
        twRange.shortR      = Integer.parseInt(data[i++]);
        twRange.mediumR     = Integer.parseInt(data[i++]);
        twRange.longR       = Integer.parseInt(data[i++]);
        defaultMode         = IWeapon.WeaponMode.valueOf(data[i++]);
        twClusterTable      = Integer.parseInt(data[i++]);
        twClusterDamage     = Integer.parseInt(data[i++]);
        twClusterGroup      = Integer.parseInt(data[i++]);
        soundEffect         = MainActivity.Sounds.valueOf(data[i++]);
        targetingMod        = Integer.parseInt(data[i++]);
    }

    public int getHeatDamage() {
        // not saving the dice roll where variable heat is applied...
        if (heatDamageDice>0){
            return (new TwoDSix(heatDamageDice, TwoDSix.RollType.LOCATION)).getTotal();
        }
        return heatDamage;
    }

    public Fragment getWeaponDisplay() {
        return null;
    }
    public WeaponType getDmgType() {
        return dmgType;
    }

    public IWeapon.DamageType getDamageType() {
        return damageType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getDamage() {
        return baseDamage;
    }
    public void setDamage(int damage) {
        this.baseDamage = damage;
    }

    public int getDamageC() {
        return damageC;
    }
    public void setDamageC(int damageC) {
        this.damageC = damageC;
    }
    public int getDamageMax() {
        return damageMax;
    }
    public void setDamageMax(int damageMax) {
        this.damageMax = damageMax;
    }
    public int getHeat() {
        return heat;
    }
    public void setHeat(int heat) {
        this.heat = heat;
    }

    public boolean matchName(String match) {
        boolean ret = false;
        if (name.equalsIgnoreCase(match)) ret = true;
        else {
            for (int i = 0;i < critNames.length && !ret;i++){
                if (critNames[i].equalsIgnoreCase(match)) ret = true;
            }
        }
        return ret;
    }

    public int getId() {
        return id;
    }
    public OVRange getRange() {
        return range;
    }
    public OVRange getTwRange() {
        return twRange;
    }
    public String getTicGroup() {
        return ticGroup;
    }

    public int getNoOfDice() {
        return noOfDice;
    }

    public boolean switchableWeapon() {
        return dmgType == WeaponType.CLUSTER || dmgType == WeaponType.ULTRA;
    }

    public boolean hasAmmo() {
        return ammoPerTon > 0;
    }
    public int getAmmoPerTon(){
        return ammoPerTon;
    }
    public int isExplosiveAmmo(){
        return explosiveAmmo;
    }

    public int isExplosiveWeapon() {
        return explosiveWeapon;
    }
    public boolean isISTech(){
        return techIS;
    }
    public static int getNumberOfHits(int clusterRoll, int noOfClusters){
        // The cluster tables start at 2 (so index 0 is cluster table for 2, thus minus 2
        // the individual cluster tables return values for values from 2-12 so first entry
        // is for 2, thus -2 on that one as well
        if (clusterRoll > 1 && clusterRoll < 13) {
            if (noOfClusters <= 30)
                return clusterTables[noOfClusters - 2][clusterRoll - 2];
            else if (noOfClusters == 40)
                return clusterTables[29][clusterRoll - 2];
        }
        return 0; // shouldn't get here but stop the crash
    }

    public IWeapon.WeaponMode getDefaultMode() {
        return defaultMode;
    }
    public IWeapon.WeaponMode[] getAvailableAmmoTypes() {
        List<IWeapon.WeaponMode> types = new ArrayList<>();
        String lowerName = name.toLowerCase();
        switch(dmgType){
            case MISSILE:
                // SRM: STD, NARC, Inferno
                // LRM: STD, NARC, Semi-guided
                // MML: STDSRM, STDLRM, SRMNARC, LRMNARC, Semi-guided, Inferno
                types.add(IWeapon.WeaponMode.STD);
                if (lowerName.contains("srm") || lowerName.contains("mml")||lowerName.contains("lrm")) {
                    types.add(IWeapon.WeaponMode.ARTIV);
                    types.add(IWeapon.WeaponMode.NARC);
                    if (lowerName.contains("srm") || lowerName.contains("mml")) {
                        types.add(IWeapon.WeaponMode.INFERNO);
                    }
                    if (lowerName.contains("lrm") || lowerName.contains("mml")) {
                        types.add(IWeapon.WeaponMode.SEMIGUIDED);
                    }
                }
                break;
            case STANDARD:
                types.add(IWeapon.WeaponMode.STD);
                if (lowerName.contains("ac")){
                    types.add(IWeapon.WeaponMode.AP);
                    types.add(IWeapon.WeaponMode.CASELESS);
                    types.add(IWeapon.WeaponMode.PRECISION);
                }
                break;
            case CLUSTER:
                types.add(IWeapon.WeaponMode.STD);
                types.add(IWeapon.WeaponMode.CLUS);
                break;
            default:
                types.add(IWeapon.WeaponMode.STD);
                break;
        }
        IWeapon.WeaponMode[] availableAmmoTypes = new IWeapon.WeaponMode[types.size()];
        int i = 0;
        for (IWeapon.WeaponMode mode : types){
            availableAmmoTypes[i++] = mode;
        }
        return availableAmmoTypes;
    }

    public int getTwClusterTable() {
        return twClusterTable;
    }

    public int getTwClusterDamage() {
        return twClusterDamage;
    }

    public int getTwClusterGroup() {
        return twClusterGroup;
    }

    public int getTwRangeMod(int range){
        // what about weapons like MRMs that have a +1 to hit on them?
        int mod = 20;
        if (range <= twRange.shortR){
            mod = 0;
            // check minimum range stored in pb
            if (range <=twRange.pointBlankR) mod += (twRange.pointBlankR + 1) - range;
        } else if (range <= twRange.mediumR) mod = 2;
        else if (range <= twRange.longR) mod = 4;
        return mod;
    }

    public MainActivity.Sounds getSoundEffect() {
        return soundEffect;
    }

    public int getTargetingMod() {
        return targetingMod;
    }
}
