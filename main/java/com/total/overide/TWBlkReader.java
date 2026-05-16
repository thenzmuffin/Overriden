package com.total.overide;

import androidx.fragment.app.FragmentManager;

import com.total.overiden.IEquipment;
import com.total.overiden.IUnitHeader;
import com.total.overiden.MainActivity;
import com.total.overiden.TooltipDialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TWBlkReader {
    private static OVSegment.OVLocation[] sections = null;
    private static int equipCount = 0;
    private static boolean istech = false;
    private enum Tag {
        TYPE("unittype"),
        TECH("type"),
        NAME("name"),
        MODEL("model"),
        ROLE("role"),
        MOTION("motion_type"),
        CRUISE("cruisemp"),
        ENGINE("engine_type"),
        ARMOUR("armor"),
        BODY("body equipment"),
        FRONT("front equipment"),
        LEFT("left equipment"),
        RIGHT("right equipment"),
        REAR("rear equipment"),
        TURRET("turret equipment"),
        TONNAGE("tonnage");
        public String tag;
        Tag(String tag){
            this.tag = tag;
        }
    }
    public static OVUnitDesign readBLK(BufferedReader reader) {
        TooltipDialogFragment tooltip=null;
        OVUnitDesign vehicle = new OVUnitDesign();
        equipCount = 0; // reset for this read
        try {
            String line = reader.readLine();
            while (line != null){
                line = line.strip().toLowerCase();
                if (line.matches("^<[a-z_ ]+>$")){
                    String key = line.substring(1,line.length()-1);
                    for(Tag check : Tag.values()){
                        if ((key.equals(check.tag))){
                            processSection(reader,check, vehicle);
                            break;
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            tooltip = new TooltipDialogFragment(e.toString());
//            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            tooltip = new TooltipDialogFragment(e.toString());
        }
        if (tooltip != null) {
            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            tooltip.show(mgr, "Failed to Load correctly");
            return null; // if an exception was thrown then just bail on the load
        }

        // now we know the weight of the vehicle we can set the structure
        int struct = Math.floorDiv(vehicle.getHeader().getMass()+5,10);
        for (OVSegment seg : vehicle.getSegments()){
            seg.setArmour(seg.getArmour(),struct);
        }

        vehicle.linkSegments();
        assignTics(vehicle);
        return vehicle;
    }
    private static void processSection(BufferedReader reader, Tag tag, OVUnitDesign vehicle) throws IOException {
        OVHeader header = (OVHeader)vehicle.getHeader();

        switch (tag){
            case TYPE:
                switch (reader.readLine().strip().toLowerCase()){
                    case "tank":
                        sections = new OVSegment.OVLocation[]{
                                OVSegment.OVLocation.FRONT,
                                OVSegment.OVLocation.LEFT,
                                OVSegment.OVLocation.RIGHT,
                                OVSegment.OVLocation.REARSIDE,
                                OVSegment.OVLocation.TURRET};
                        break;
                }
                break;
            case TECH:
                istech =  !(reader.readLine().toLowerCase().contains("clan"));
                break;
            case NAME:
                header.setName(reader.readLine());
                reader.readLine(); //read end tag
                break;
            case MODEL:
                header.setVariant(reader.readLine());
                reader.readLine(); //read end tag
                break;
            case ROLE:
                header.parseRole(reader.readLine());
                reader.readLine(); //read end tag
                break;
            case MOTION:
                header.setUnitType(IUnitHeader.UnitType.parse(reader.readLine()));
                reader.readLine(); //read end tag
                break;
            case CRUISE:
                header.setWalk(Integer.parseInt(reader.readLine()));
                reader.readLine();
                break;
            case ENGINE:
                // engine type is stored as a numeric value, not sure what they correspond to so can't parse it yet
                header.setEngine(OVHeader.EngineType.STANDARD);
                break;
            case ARMOUR:
                int count = 0;
                // has a variable number of integers representing the number of pips for each location
                do{
                    String line = reader.readLine();
                    if (line.contains("<"))break; //we've reached the end
                    vehicle.getSegments().add(
                            new OVSegment(sections[count],
                                    Integer.parseInt(line),
                                    1, //structure will be set later
                                    0,
                                    OVSegment.ArmourType.STANDARD));
                    count++;
                }while(count<sections.length);
                break;
            case BODY:
                equipCount = parseEquipment(vehicle, OVSegment.OVLocation.BODY, reader, equipCount);
                break;
            case FRONT:
                equipCount = parseEquipment(vehicle, OVSegment.OVLocation.FRONT, reader, equipCount);
                break;
            case LEFT:
                equipCount = parseEquipment(vehicle,OVSegment.OVLocation.LEFT,reader,equipCount);
                break;
            case RIGHT:
                equipCount = parseEquipment(vehicle,OVSegment.OVLocation.RIGHT,reader,equipCount);
                break;
            case REAR:
                equipCount = parseEquipment(vehicle,OVSegment.OVLocation.REARSIDE,reader,equipCount);
                break;
            case TURRET:
                equipCount = parseEquipment(vehicle,OVSegment.OVLocation.TURRET,reader,equipCount);
                break;
            case TONNAGE:
                header.setMass((int)Float.parseFloat(reader.readLine()));
                reader.readLine();
                break;
        }
    }
    private static int parseEquipment(OVUnitDesign vehicle, OVSegment.OVLocation loc, BufferedReader reader, int equipCount) throws IOException{
        int count = equipCount;
        String line = reader.readLine().toLowerCase();

        while (line!=null && !line.contains("<")) {
            if (line.contains("ammo")){
                String[] parts = line.toLowerCase().split("ammo");
                OVMtfReader.findAmmoType(parts, loc, vehicle.getEquipment());
            } else if (line.contains("artemisiv")) {
                vehicle.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.ARTEMISIV, equipCount++, loc, "Artemis IV"));
            } else if (line.contains("artemisv")) {
                vehicle.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.ARTEMISV, equipCount++, loc, "Artemis V"));
            } else if (line.contains("targeting computer")) {
                if (!vehicle.hasEquipment(OVEquipment.EquipmentType.TARGETING)) //only need one
                    vehicle.getEquipment().add(new OVEquipment(OVEquipment.EquipmentType.TARGETING, equipCount++, loc, "Targeting Computer"));
            } else if (line.contains("iscase")||line.contains("clcase")){
// skip processing for now, case needs to be added in
            } else {
                // strip tech base prefix if present
                if (line.startsWith("is ")){
                    line = line.substring(3);
                    istech = true;
                } else if (line.startsWith("cl ")){
                    line = line.substring(3);
                    istech = false;
                }
                String[] lineData = new String[]{line, loc.name()};
                IEquipment inst = OVWeaponInstance.getWeaponInstance(lineData, count++, istech);
                if (inst == null) {
                    TooltipDialogFragment tooltip = new TooltipDialogFragment("Couldn't load due to weapon " + line);

                    FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
                    tooltip.show(mgr, "Failed to Load");
                    line = reader.readLine().toLowerCase();
                    continue;
                }
                vehicle.getEquipment().add(inst);
            }

            line = reader.readLine().toLowerCase();
        }
        return count;
    }

    public static void assignTics(OVUnitDesign design) {
        String thisGroup = "";
        OVSegment.OVLocation oldLocation = null;
        OVTic tic = null;
        int ticCount = 0;
        OVWeapon weapType;
        List<IEquipment> weapons = design.getEquipment();
        Collections.sort(weapons);

        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getType() != OVEquipment.EquipmentType.WEAPON) {
                continue;
            }
            // if we are at a new group type or the location has changed
            weapType = ((OVWeaponInstance) weapons.get(i)).getWeapon();
            if (!thisGroup.equals(weapType.getTicGroup()) ||
                    weapons.get(i).getLocation()!=oldLocation) {
                tic = new OVTic(ticCount++);
                design.getWeapons().add(tic);
                thisGroup = weapType.getTicGroup();
                oldLocation = weapons.get(i).getLocation();
            }
            // if the current tic is at max size
            if (tic != null && tic.size() > 0) { // a tic can never be too large with only 1 weapon!
                if (!tic.canAddWeapon(weapType)) {
                    tic = new OVTic(ticCount++);
                    design.getWeapons().add(tic);
                }
            }
            if (tic != null) tic.addWeapon((OVWeaponInstance) weapons.get(i));
        }
    }
}
