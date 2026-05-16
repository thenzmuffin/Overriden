package com.total.overide;

import androidx.fragment.app.FragmentManager;

import com.total.overiden.BSPStrikeTemplate;
import com.total.overiden.IEquipment;
import com.total.overiden.IWeapon;
import com.total.overiden.MainActivity;
import com.total.overiden.TooltipDialogFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OVMtfReader {
    public static List<OVWeapon> mvOVWeapons = null;
    private static final int[][] structure = { // these are TW values
            {20, 3, 6, 0, 3, 4, 3, 4, 5, 5}, //20 ton
            {25, 3, 8, 0, 4, 6, 4, 6, 6, 6}, //25 ton
            {30, 3, 10, 0, 5, 7, 5, 7, 7, 7}, //30 ton
            {35, 3, 11, 0, 6, 8, 6, 8, 8, 8}, //35 ton
            {40, 3, 12, 0, 6, 10, 6, 10, 10, 10}, //40 ton
            {45, 3, 14, 0, 7, 11, 7, 11, 11, 11}, //45 ton
            {50, 3, 16, 0, 8, 12, 8, 12, 12, 12}, //50 ton
            {55, 3, 18, 0, 9, 13, 9, 13, 13, 13}, //55 ton
            {60, 3, 20, 0, 10, 14, 10, 14, 14, 14}, //60 ton
            {65, 3, 21, 0, 10, 15, 10, 15, 15, 15}, //65 ton
            {70, 3, 22, 0, 11, 15, 11, 15, 15, 15}, //70 ton
            {75, 3, 23, 0, 12, 16, 12, 16, 16, 16}, //75 ton
            {80, 3, 25, 0, 13, 17, 13, 17, 17, 17}, //80 ton
            {85, 3, 27, 0, 14, 18, 14, 18, 18, 18}, //85 ton
            {90, 3, 29, 0, 15, 19, 15, 19, 19, 19}, //90 ton
            {95, 3, 30, 0, 16, 20, 16, 20, 20, 20}, //95 ton
            {100, 3, 31, 0, 17, 21, 17, 21, 21, 21}}; //100 ton
    private static final int[][] ovStructure = {
            {20, 1, 2, 0, 1, 1, 1, 1}, //20 ton
            {25, 1, 3, 0, 1, 2, 1, 2}, //25 ton
            {30, 1, 3, 0, 2, 2, 2, 2}, //30 ton
            {35, 1, 4, 0, 2, 3, 2, 3}, //35 ton
            {40, 1, 5, 0, 2, 3, 2, 3}, //40 ton
            {45, 1, 5, 0, 2, 4, 2, 4}, //45 ton
            {50, 1, 6, 0, 3, 4, 3, 4}, //50 ton
            {55, 1, 6, 0, 3, 4, 3, 4}, //55 ton
            {60, 1, 7, 0, 3, 5, 3, 5}, //60 ton
            {65, 1, 7, 0, 3, 5, 3, 5}, //65 ton
            {70, 1, 7, 0, 4, 5, 4, 5}, //70 ton
            {75, 1, 8, 0, 4, 5, 4, 5}, //75 ton
            {80, 1, 8, 0, 4, 6, 4, 6}, //80 ton
            {85, 1, 9, 0, 5, 6, 5, 6}, //85 ton
            {90, 1, 10, 0, 5, 6, 5, 6}, //90 ton
            {95, 1, 10, 0, 5, 7, 5, 7}, //95 ton
            {100, 1, 10, 0, 6, 7, 6, 7}}; //100 ton
    public static final int cHead = 0;
    public static final int cTorso = 1;
    public static final int cRear = 2;
    public static final int cLeftArm = 3;
    public static final int cLeftLeg = 4;
    public static final int cRightArm = 5;
    public static final int cRightLeg = 6;
    public static final int cLeftTorso = 7;
    public static final int cRightTorso = 8;
    public static final int cRearLeft = 9;
    public static final int cRearRight = 10;

    private static int equipCount = 0;

    public static OVUnitDesign readMTF(InputStream input) {
        OVHeader.EngineType engineType = null;
        equipCount = 0;
        OVUnitDesign design = new OVUnitDesign();
        //	FileInputStream fis = f.openFileInput(R.raw.archerarc_2k);
        InputStreamReader inputStreamReader =
                new InputStreamReader(input, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            boolean techInner = true;
            OVHeader header = (OVHeader) design.getHeader();
            // first line should be version
            String line = reader.readLine();
            if (line.startsWith("#"))line = reader.readLine();
            if (line.toLowerCase().contains("block")) {
                return TWBlkReader.readBLK(reader);
            }if (line.contains("Version:")) {
                header.setName(reader.readLine());
                header.setVariant(reader.readLine());
                reader.readLine(); // mul id
                line = reader.readLine();
                //				if (!line.contains(":1.0")) return; version check doesn't seem to make a difference up to 1.1
                // see 3039U/atlas AS7-d-dc for 1.1 example
            }

            while (line != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1) {
                    parts[0] = parts[0].toLowerCase();
                    switch (parts[0]) {
                        case "chassis":
                            header.setName(parts[1]);
                            break;
                        case "model":
                            header.setVariant(parts[1]);
                            break;
                        case "role":
                            header.parseRole(parts[1]);
                            break;
                        // Config, TechBase, Era, Source, Rules Level, Structure, Myomer, Engine
                        case "techbase":
                            techInner = parts[1].equalsIgnoreCase("inner sphere");
                            break;
                        case "mass":
                            header.setMass(Integer.parseInt(parts[1]));
                            //						structure.setMechStructure(mass);
                            break;
                        case "engine": // we need to know if it is standard/XL, Clan XL etc

                            if (parts[1].toLowerCase().contains("xxl"))
                                engineType = techInner ? OVHeader.EngineType.ISXXL : OVHeader.EngineType.CLANXXL;
                            else if (parts[1].toLowerCase().contains("xl"))
                                engineType = techInner ? OVHeader.EngineType.ISXL : OVHeader.EngineType.CLANXL;
                            else engineType = OVHeader.EngineType.STANDARD;
                            header.setEngine(engineType);
                            break;
                        case "heat sinks":
                            String[] subParts = parts[1].split(" ");
                            int sinkCount = 0;
                            if (subParts.length >= 1) {
                                sinkCount = Integer.parseInt(subParts[0]);
                            }
                            header.setTwHeatSinks(sinkCount);
                            if (subParts.length >= 2 && parts[1].toLowerCase().contains("double")) {
                                sinkCount *= 2;
                                header.setDoubleHeatSinks(true);
                            }
                            header.setHeatSinks(Math.floorDiv(sinkCount + 2, 5));
                            break;
                        case "walk mp":
                            header.setWalk(Integer.parseInt(parts[1]));
                            break;
                        case "jump mp":
                            header.setJump(Integer.parseInt(parts[1]));
                            break;
                        case "armor":
                            processArmour(design, reader, parts);
                            break;
                        case "weapons":
                            int count = Integer.parseInt(parts[1]);
                            for (int x = 0; x < count; x++) {
                                String[] lineData = reader.readLine().split(",");
                                // some versions of MTF files have the number of this weapon type as the first value so check for this:
                                String[] cardinal = lineData[0].split(" ");
                                int num;
                                try {
                                    num = Integer.parseInt(cardinal[0]);
                                } catch (NumberFormatException numEx) {
                                    num = 1;
                                }
                                for (int i = 0; i < num; i++) {
                                    OVWeaponInstance inst = OVWeaponInstance.getWeaponInstance(lineData, equipCount++, techInner);
                                    if (inst == null) {
                                        TooltipDialogFragment tooltip = new TooltipDialogFragment("Couldn't load due to weapon " + lineData[0]);

                                        FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
                                        tooltip.show(mgr, "Failed to Load");
                                        return null; //bail if we can't read the weapon
                                    }
                                    design.getEquipment().add(inst);
                                }
//                                design.getEquipment().add(OVWeaponInstance.getWeaponInstance(lineData, equipCount++, techInner));
                            }
                            break;
                        case "left arm":
                        case "left leg":
                        case "right arm":
                        case "right leg":
                        case "left torso":
                        case "right torso":
                        case "center torso":
                        case "head":
                            OVSegment.OVLocation temp = parseLocation(parts[0]);
                            if (temp != null) {
                                if (!techInner) {
                                    // all clan mechs have CASE by default
                                    OVEquipment local = new OVEquipment(OVEquipment.EquipmentType.CASE, equipCount++, temp, "CASE");
                                    local.setStatus(false); // can't be critted and has no active function so set as not operational
                                    design.getEquipment().add(local);
                                }
                                processCrits(design, reader, temp, techInner);
                            }
                            break;
                        default:
                            //skip line
                    }
                }
                line = reader.readLine();
            }

        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            TooltipDialogFragment tooltip = new TooltipDialogFragment(e.toString());

            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            tooltip.show(mgr, "Failed to Load correctly");
            return null; // if an exception was thrown then just bail on the load
//            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            TooltipDialogFragment tooltip = new TooltipDialogFragment(e.toString());

            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            tooltip.show(mgr, "Failed to Load correctly");
            return null; // if an exception was thrown then just bail on the load
        }
        OVCoreEquipment newCore = new OVCoreEquipment(engineType, equipCount++, OVSegment.OVLocation.CENTRETORSO);
        newCore.setCritSlots(6);//always 6 in the CT
        design.getEquipment().add(newCore);//engine
        int slots = 0;
        switch (engineType) {
            case ISXXL:
                slots = 6; // bulkiest engine type - 6 each side
            case CLANXXL:
                slots -= 2; // almost as bad, takes 4 each side
            case ISXL:
                slots--; // 3 crits in each side torso - still fatal!
            case CLANXL:
                slots--; //2 crits in each side torso: survives loss of a side torso
                newCore = new OVCoreEquipment(engineType, equipCount++, OVSegment.OVLocation.LEFTTORSO);
                newCore.setCritSlots(slots);
                design.getEquipment().add(newCore);
                newCore = new OVCoreEquipment(engineType, equipCount++, OVSegment.OVLocation.RIGHTTORSO);
                newCore.setCritSlots(slots);
                design.getEquipment().add(newCore);
                break;
        }
        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.GYRO, OVSegment.OVLocation.CENTRETORSO, equipCount++));//gyro
        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.SENSORS, OVSegment.OVLocation.HEAD, equipCount++));//gyro
        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.LIFESUPPORT, OVSegment.OVLocation.HEAD, equipCount++));//gyro
        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.COCKPIT, OVSegment.OVLocation.HEAD, equipCount++));//gyro
//        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.ACTUATOR, OVSegment.OVLocation.RIGHTLEG,equipCount++));//gyro
//        design.getEquipment().add(new OVCoreEquipment(OVEquipment.EquipmentType.ACTUATOR, OVSegment.OVLocation.LEFTLEG,equipCount++));//gyro
        design.linkSegments();
        assignTics(design);
        return design;
    }

    private static final String[] narc = {"isnarcbeacon"};

    private static boolean parseActuators(String lowerDesc,
                                          OVUnitDesign design,
                                          OVSegment.OVLocation location) {
        boolean hasOptional = false;
        switch (lowerDesc) {
            case "shoulder":
            case "hip":
                //shoulder and hip will always be first in the list
                actuator = new TWActuator( location, equipCount++,0);
                design.getEquipment().add(actuator);
                actuator.setTop(true);
                break;
            case "upper arm actuator":
            case "upper leg actuator":
                //just in case check for null
                if (actuator!=null) actuator.setUpper(true);
                break;
            case "lower arm actuator":
            case "lower leg actuator":
                if (actuator!=null) actuator.setLower(true);
                break;
            case "hand actuator":
            case "foot actuator":
                if (actuator!=null) actuator.setBottom(true);
                break;
            default:
                return false;
        }
        return true;
    }

    private static TWActuator actuator = null;
    private static final String[] heatSinkNames = {"heatsink", "heat sink", "cldoubleheatsink", "isdoubleheatsink"};
    private static boolean isHeatSink(String lowerDesc){
        for (String heatSinkName : heatSinkNames) {
            if (lowerDesc.equals(heatSinkName)) return true;
        }
        return false;
    }
    private static void processCrits(OVUnitDesign design, BufferedReader reader, OVSegment.OVLocation location, boolean techInner) {
        try {
            int heatsinks = 0;
            OVEquipment melee = null;
            actuator = null;
            for (int i = 0; i < 12; i++) {
                String lowerDesc = reader.readLine().toLowerCase();
                if (parseActuators(lowerDesc, design, location))
                    continue;
                if (lowerDesc.equals("iscase")) {
                    OVEquipment local = new OVEquipment(OVEquipment.EquipmentType.CASE, equipCount++, location, "CASE");
                    local.setStatus(false); // can't be critted and has no active function so set as not operational
                    design.getEquipment().add(local);
                } else if (isHeatSink(lowerDesc)) {
                    // get the number of heat sink slots and work it out at the end
                    heatsinks++;
                } else if (lowerDesc.equals("tsm")){
                    // TSM really needs to be added to OVState instead of as Equipment.
                    // it can't be critted and is not located in any one place in the mech so needs
                    // to be treated differently
                    design.getHeader().setTsm(true);
//                    design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.TSM, equipCount++, location, "Triple Strength Myomer"));
                }else if (lowerDesc.equals("jump jet")){
                    design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.JUMPJET, equipCount++, location, "Jump Jet"));
                }else if (lowerDesc.contains("ammo")) {
                    //is this an Ammo line? If so we should parse it here and create the ammo record to be collected later on the link funciton
                    String[] parts = lowerDesc.split("ammo");
                    findAmmoType(parts, location, design.getEquipment());
                } else if (lowerDesc.equals("isnarc pods")) {
                    // special case because of course the MTF doesn't use the word ammo for narc or the name of the weapon they relate to either
                    findAmmoType(narc, location, design.getEquipment());
                } else if (lowerDesc.contains("artemisiv")) {
                    design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.ARTEMISIV, equipCount++, location, "Artemis IV"));
                } else if (lowerDesc.contains("artemisv")) {
                    design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.ARTEMISV, equipCount++, location, "Artemis V"));
                } else if (lowerDesc.contains("masc")) {//check for masc so it picks up clan and is
                    design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.MASC, equipCount++, location, "MASC"));
                } else if (lowerDesc.contains("targeting computer")) {
                    if (!design.hasEquipment(OVEquipment.EquipmentType.TARGETING)) //only need one
                        design.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.TARGETING, equipCount++, location, "Targeting Computer"));
                } else if (lowerDesc.contains("hatchet")){
                    if (melee==null) {
                        melee = new OVEquipment(OVEquipment.EquipmentType.HATCHET, equipCount++, location, "Hatchet");
                        design.getEquipment().add(melee);
                    } else {
                        // hatchet already created, add crit slot
                        melee.setCritSlots(melee.getCritSlots()+1);
                    }
//                            new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.HATCHET,
//                            Math.floorDiv(design.getHeader().getMass(),5),
//                            PhysicalWeapon.PhysicalHitGrouping.FULL));
                }
            }
            //generate heat sinks
            int hsSlots = 1;
            if (design.getHeader().isDoubleHeatSinks()) hsSlots += (techInner) ? 2 : 1;
            heatsinks = Math.floorDiv(heatsinks, hsSlots);
            for (int i = 0; i < heatsinks; i++) {
                OVEquipment hs = new OVCoreEquipment(OVEquipment.EquipmentType.HEATSINK, location, equipCount++);
                design.getEquipment().add(hs);
                if (hsSlots > 1) hs.setCritSlots(hsSlots);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processArmour(OVUnitDesign design, BufferedReader reader, String[] topLine) {
        int[] armour = new int[11];
        OVSegment.ArmourType armType = OVSegment.ArmourType.STANDARD;
        try {
            //check if the armour type is not standard
            if (topLine.length > 1 && topLine[1].length() >= 5) {
                String type = topLine[1].substring(0, 5).toLowerCase(Locale.ENGLISH);
                if (!type.equals("stand")) {// check it isn't standard for efficiency
                    if (type.equals("harde")) {
                        armType = OVSegment.ArmourType.HARDENED;
                    } else if (type.equals("steal")) {
                        armType = OVSegment.ArmourType.STEALTH;
                    } else if (type.equals("balli")) {
                        armType = OVSegment.ArmourType.BALLISTIC;
                    } else if (type.equals("refle")) {
                        armType = OVSegment.ArmourType.REFLECTIVE;
                    } else if (type.equals("ferro")) {
                        // there are several types of armour that start with ferro
                        if (topLine[1].substring(6, 9).toLowerCase(Locale.ENGLISH).equals("lame"))
                            armType = OVSegment.ArmourType.FERROLAM;
                    }
                }
            }
            design.getHeader().setArmourType(armType);

            String line = reader.readLine();
            while (line != null && line.length() > 9) { // a blank line or one less than 9 characters long means we have reached the end of valid armour entries
                String[] parts = line.split(":");
                parts[0] = parts[0].toLowerCase();
                switch (parts[0]) {
                    case "la armor":
                        armour[cLeftArm] = Integer.parseInt(parts[1]);
                        break;
                    case "ra armor":
                        armour[cRightArm] = Integer.parseInt(parts[1]);
                        break;
                    case "lt armor":
                        armour[cLeftTorso] = Integer.parseInt(parts[1]);
                        break;
                    case "rt armor":
                        armour[cRightTorso] = Integer.parseInt(parts[1]);
                        break;
                    case "ct armor":
                        armour[cTorso] = Integer.parseInt(parts[1]);
                        break;
                    case "hd armor":
                        armour[cHead] = Integer.parseInt(parts[1]);
                        break;
                    case "ll armor":
                        armour[cLeftLeg] = Integer.parseInt(parts[1]);
                        break;
                    case "rl armor":
                        armour[cRightLeg] = Integer.parseInt(parts[1]);
                        break;
                    case "rtl armor":
                        armour[cRearLeft] = Integer.parseInt(parts[1]);
                        break;
                    case "rtr armor":
                        armour[cRearRight] = Integer.parseInt(parts[1]);
                        break;
                    case "rtc armor":
                        armour[cRear] = Integer.parseInt(parts[1]);
                        break;
                    default:
                }
                line = reader.readLine();
            }

            int[] struct = structure[0]; //set default
            boolean found = false;
            for (int i = 0; i < structure.length && !found; i++) {
                if (structure[i][0] == design.getHeader().getMass()) {
                    struct = structure[i];
                    found = true;
                }
            }

            List<OVSegment> segs = design.getSegments();
            segs.add(new OVSegment(OVSegment.OVLocation.HEAD, armour[cHead], struct[cHead + 1], 0, design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.LEFTARM, armour[cLeftArm], struct[cLeftArm + 1], 0, design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.RIGHTARM, armour[cRightArm], struct[cRightArm + 1], 0, design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.LEFTLEG, armour[cLeftLeg], struct[cLeftLeg + 1], 0, design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.RIGHTLEG, armour[cRightLeg], struct[cRightLeg + 1], 0, design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.CENTRETORSO, armour[cTorso], struct[cTorso + 1], armour[cRear], design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.LEFTTORSO, armour[cLeftTorso], struct[cLeftTorso + 1], armour[cRearLeft], design.getHeader().getArmourType()));
            segs.add(new OVSegment(OVSegment.OVLocation.RIGHTTORSO, armour[cRightTorso], struct[cRightTorso + 1], armour[cRearRight], design.getHeader().getArmourType()));

            // old rules for creating OV armour in the catalog
//            segs.add(new OVSegment(OVSegment.OVLocation.HEAD, getHeadArmour(armour[cHead]), struct[cHead+1], 0, design.getHeader().getArmourType()));
//            segs.add(new OVSegment(OVSegment.OVLocation.LEFTARM, Math.floorDiv(armour[cLeftArm] + 1, 3), struct[cLeftArm+1], 0, design.getHeader().getArmourType()));
//            segs.add(new OVSegment(OVSegment.OVLocation.RIGHTARM, Math.floorDiv(armour[cRightArm] + 1, 3), struct[cRightArm+1], 0, design.getHeader().getArmourType()));
//            segs.add(new OVSegment(OVSegment.OVLocation.LEFTLEG, Math.floorDiv(armour[cLeftLeg] + 1, 3), struct[cLeftLeg+1], 0, design.getHeader().getArmourType()));
//            segs.add(new OVSegment(OVSegment.OVLocation.RIGHTLEG, Math.floorDiv(armour[cRightLeg] + 1, 3), struct[cRightLeg+1], 0, design.getHeader().getArmourType()));
//            segs.add(new OVSegment(OVSegment.OVLocation.TORSO, Math.floorDiv(armour[cTorso] + 3, 6), struct[cTorso+1], Math.floorDiv(armour[cRear] + 3, 6), design.getHeader().getArmourType()));

        } catch (IOException x) {
            System.err.println(x.toString());
        }

    }

    public static int getHeadArmour(int twArmour) {
        int ret = 0;
        switch (twArmour) {
            case 0:
            case 1:
            case 2:
                ret = 1;
                break;
            case 3:
            case 4:
            case 5:
                ret = 2;
                break;
            case 6:
            case 7:
                ret = 3;
                break;
            case 8:
            case 9:
                ret = 4;
                break;
        }
        return ret;
    }

    public static void readWeapons(InputStream input) {
        mvOVWeapons = new ArrayList<>();
        InputStreamReader inputStreamReader =
                new InputStreamReader(input, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            reader.readLine(); // first line is just a header line so needs to be skipped
            while ((line = reader.readLine()) != null) {
                mvOVWeapons.add(new OVWeapon(line));
            }
            inputStreamReader.close();
        } catch (IOException x) {
            System.err.println(x.toString());
        }
    }

    public static List<BSPStrikeTemplate> readBSPTemplates(InputStream input){
        List<BSPStrikeTemplate> list = new ArrayList<>();
        InputStreamReader inputStreamReader =
                new InputStreamReader(input, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            reader.readLine(); // first line is just a header line so needs to be skipped
            while ((line = reader.readLine()) != null) {
                list.add(new BSPStrikeTemplate(line));
            }
            inputStreamReader.close();
        } catch (IOException x) {
            System.err.println(x.toString());
        }
        return list;
    }

    public static OVWeapon findOVWeaponByName(String name) {
        OVWeapon ret = null;
        for (int i = 0; i < mvOVWeapons.size(); i++)
            if (mvOVWeapons.get(i).matchName(name.toLowerCase())) {
                ret = mvOVWeapons.get(i);
                break;
            }
        return ret;
    }

    public static OVWeapon findOVWeaponByID(int id) {
        OVWeapon ret = null;
        for (int i = 0; i < mvOVWeapons.size(); i++) {
            if (mvOVWeapons.get(i).getId() == id) {
                ret = mvOVWeapons.get(i);
                break;
            }
        }
        return ret;
    }

    public static OVWeapon findWeaponByName(String name, boolean isTech) {
        String strip = "1234567890 ";
        String temp = name;
        OVWeapon ret = null;
        // mtf files have a variable format, sometimes the weapon line has an index number at the front
        // sometimes it doesn't, remove any leading space and numbers
        // if any weapons name starts with a number we're stuffed!
        while (strip.contains(temp.substring(0, 1))) {
            temp = temp.substring(1);
        }
        if (temp.endsWith("(omnipod)")) temp = temp.substring(0, temp.length() - 9).stripTrailing();
        if (temp.length() > 0)
            for (int i = 0; i < mvOVWeapons.size(); i++) {
                if (isTech == mvOVWeapons.get(i).isISTech()) // some weapons have the same name for CLAN vs IS versions
                    if (mvOVWeapons.get(i).matchName(temp.toLowerCase())) {
                        ret = mvOVWeapons.get(i);
                        break;
                    }
            }
        return ret;
    }

    public static OVSegment.OVLocation parseLocation(String text) {
        OVSegment.OVLocation location = null;

        switch (text.strip().toLowerCase()) {
            case "left arm":
                location = OVSegment.OVLocation.LEFTARM;
                break;
            case "right arm":
                location = OVSegment.OVLocation.RIGHTARM;
                break;
            case "left torso":
                location = OVSegment.OVLocation.LEFTTORSO;
                break;
            case "right torso":
                location = OVSegment.OVLocation.RIGHTTORSO;
                break;
            case "center torso":
                location = OVSegment.OVLocation.CENTRETORSO;
                break;
            case "left torso (r)":
                location = OVSegment.OVLocation.LTREAR;
                break;
            case "right torso (r)":
                location = OVSegment.OVLocation.RTREAR;
                break;
            case "center torso (r)":
                location = OVSegment.OVLocation.CTREAR;
                break;
            case "head":
                location = OVSegment.OVLocation.HEAD;
                break;
            case "left leg":
                location = OVSegment.OVLocation.LEFTLEG;
                break;
            case "right leg":
                location = OVSegment.OVLocation.RIGHTLEG;
                break;
            default:
                location = OVSegment.OVLocation.valueOf(text);
        }
        return location;
    }

    public static void findAmmoType(String[] input, OVSegment.OVLocation location, List<IEquipment> list) {
        OVWeapon type = null;
        boolean half = false;
        //       boolean guided = false; //Artemis missiles
        boolean isISTech = true;
        boolean narc = false;
        IWeapon.WeaponMode special = IWeapon.WeaponMode.STD;
        //       boolean found = false;
        for (int i = 0; i < input.length && type == null; i++) {
            String formatted = input[i].strip();
            formatted = formatted.toLowerCase(Locale.ENGLISH);
            if (isISTech && formatted.contains("clan")) {
                isISTech = false;
                if (formatted.length() <= 5) continue;
                formatted = formatted.replaceFirst("clan", "");
                formatted = formatted.strip();
                //unfortunately some clan items don't start with the word clan (e.g. clan AMS)
            } else if (formatted.startsWith("cl")) isISTech = false;
            special = IWeapon.WeaponMode.STD;
            if (formatted.endsWith(" cl") || formatted.endsWith(" cluster")) {
                int trim = formatted.endsWith(" cl") ? 3 : 8;
                formatted = formatted.substring(0, formatted.length() - trim);
                special = IWeapon.WeaponMode.CLUS;
            } else if (formatted.contains("artemis")) {
                // artemis ammo might have artemis or artemis-capable in the description so strip
                // them separately
                formatted = formatted.replaceFirst("artemis v", "");
                formatted = formatted.replaceFirst("artemis", "");
                formatted = formatted.replaceFirst("-capable", "");
                formatted = formatted.strip();
                special = IWeapon.WeaponMode.ARTIV;
            } else if (formatted.equals("isnarcbeacon")){
                // empty block here stops the narc pods themselves getting stripped by the
                // below block for LRM/SRM ammo that is narc capable
            } else if (formatted.contains("narc")) {
                // artemis ammo might have artemis or artemis-capable in the description so strip
                // them separately
                formatted = formatted.replaceFirst("narc", "");
                formatted = formatted.replaceFirst("-capable", "");
                formatted = formatted.strip();
                narc = true;
            }
            if (formatted.contains("half")) {
                type = findWeaponByName("machine gun", isISTech);
                half = true;
            } else if (formatted.length() > 1) { //avoid blank fields
                type = findWeaponByName(formatted, isISTech);
            }
        }
        if (type == null) {
            String error = "";
            for (int i = 0; i < input.length; i++) error += input[i];

            throw new RuntimeException("Couldn't link Ammo to a weapon " + error); //bail if we can't read the weapon
        }
        // at first was merging different ammo bins of the same type together, however this means a
        // mech with 2 tons of ammo cannot split ammo types
        if (narc) {
            special = IWeapon.WeaponMode.NARC;
        }
        //reading from an mtf here so should be at full capacity
        list.add(new OVAmmunition(location, equipCount++, type, half, special));
//        }
    }

    public static void assignTics(OVUnitDesign design) {
        String thisGroup = "";
        OVTic tic = null;
        boolean rear;
        int ticCount = 0;
        OVWeapon weapType;
        List<IEquipment> weapons = design.getEquipment();
        Collections.sort(weapons);
        rear = isInRear(weapons.get(0).getLocation());

        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getType() != OVEquipment.EquipmentType.WEAPON) {
                continue;
            }
            // if we are at a new group type
            weapType = ((OVWeaponInstance) weapons.get(i)).getWeapon();
            if (!thisGroup.equals(weapType.getTicGroup()) || rear != isInRear(weapons.get(i).getLocation())) {
                tic = new OVTic(ticCount++);
                design.getWeapons().add(tic);
                thisGroup = weapType.getTicGroup();
            }
            // if the current tic is at max size
            if (tic != null && tic.size() > 0) { // a tic can never be too large with only 1 weapon!
                if (!tic.canAddWeapon(weapType)) {
                    tic = new OVTic(ticCount++);
                    design.getWeapons().add(tic);
                }
            }
            if (tic != null) tic.addWeapon((OVWeaponInstance) weapons.get(i));
            rear = isInRear(weapons.get(i).getLocation());
        }
    }

    private static boolean isInRear(OVSegment.OVLocation loc) {
        return loc == OVSegment.OVLocation.REAR ||
                loc == OVSegment.OVLocation.RTREAR ||
                loc == OVSegment.OVLocation.CTREAR ||
                loc == OVSegment.OVLocation.LTREAR;
    }
}
