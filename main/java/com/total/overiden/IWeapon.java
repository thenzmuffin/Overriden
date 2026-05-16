package com.total.overiden;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.total.overide.OVAmmunition;
import com.total.overide.OVSegment;
import com.total.overide.OVWeapon;

import java.util.List;

public interface IWeapon extends IEquipment{
    public enum DamageType {
        DB, //Direct Ballistic
        DE, //Direct Energy
        P, //Pulse
        RE, //Re-engineered Lasers
        M, //Missile
        E, //Electronics
        PB, //Point Blank
        AE; //Area Effect
    }
    enum WeaponMode {
        STD("STD",1, R.color.Chartreuse,"",1),
        CLUS("CLUS",1, R.color.Blue,"cl",1),
        MULTI2("2 Shots", 2, R.color.Orange,"",1),
        MULTI3("3 Shots", 3, R.color.Cyan,"",1),
        MULTI4("4 Shots", 4, R.color.Chartreuse,"",1),
        MULTI5("5 Shots", 5, R.color.Orchid,"",1),
        MULTI6("6 Shots", 6, R.color.Navy,"",1),
        AP("Armour Piercing", 1, R.color.Green,"ap",0.5),
        CASELESS("Caseless", 1, R.color.Purple,"case",2),
        PRECISION("Precision", 1, R.color.Red,"pr",0.5),
        AUTO("Auto",1,R.color.Blue,"",1),
        TAG("TAG",1,R.color.Blue,"",1),
        OFF("Off",1,R.color.Chartreuse,"",1),

        ARTIV("Artemis IV", 1,R.color.FireBrick,"aiv",1),
        ARTV("Artemis V", 1,R.color.DeepSkyBlue,"av",1),
//        LRMSTD("LRM",1,R.color.Chocolate,"",1),
        NARC("NARC",1,R.color.Blue,"n",1),
//        LRMNARC("NARC-LRM",1,R.color.Blue,"n",1),
        SEMIGUIDED("Semi-Guided",1,R.color.DarkOrchid,"sg",1),
//        SRMSTD("SRM",1,R.color.Chocolate,"",1),
//        SRMNARC("NARC-SRM",1,R.color.Blue,"n",1),
        INFERNO("Inferno",1,R.color.DeepPink,"i",1);
        private String label;
        private int rounds;
        private int colID;
        private final String suffix;
        private final double multiplier; //ammo per ton changed by this amount
        WeaponMode(String label,int rounds, int colID, String suffix, double mult){
            this.label = label;
            this.rounds = rounds;
            this.colID = colID;
            this.suffix = suffix;
            multiplier = mult;
        }
        public String getLabel(){return label;}
        public int getRounds(){return rounds;}
        public Paint getColor(View view){
            Paint gen = new Paint(Paint.ANTI_ALIAS_FLAG);
            gen.setColor(view.getResources().getColor(colID,null));
            return gen;
        }
        public double getMultiplier(){return multiplier;}
        public String getSuffix(){return suffix;}
    }
//    public int getBaseDamage();
    int getID();

    int getHeat();
    void fireWeapon();
    void checkWeaponJam(int rolled);
    boolean isJammed();
    void hit(TargetData target, TargetData.LocTable table);
    WeaponMode getWeaponMode();
    boolean setWeaponMode(WeaponMode mode);
    List<WeaponMode> getAvailableModes();
    OVSegment.OVLocation getLocation();
    int getRangeMod(int range);
    int getHeatDamage();
    DamageType getDamageType();
    OVWeapon.WeaponType getWeaponType();
    String getLocationText();
    boolean isMultiMode(); //is there a choice to be made on firing mode
    List<OVAmmunition> getAmmo();
    boolean isIndirect();
    String getStreamValue();
    String getDamageText();
    boolean hasArtemis();
    void setArtemis(boolean art);
    MainActivity.Sounds getSoundEffect();
    int getClusterDamage(); //returns the amount of damage done by a single cluster
}
