package com.total.overiden;

import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Stores a record of damage received by a unit
 */
public class DamageRecord implements IDamageRecord {
	public static class DamageGrouping {

		public int damage;
		private TwoDSix location = null; // 2D6 location roll

		public boolean partialCoverMiss = false; // flag if hit in the legs with partial cover
		private OVSegment.OVLocation convertedLocation = null;
		public DamageGrouping(int dmg, TwoDSix loc) {
			super();
			damage = dmg;
			location = loc;

		}
		public DamageGrouping(String data) {
			super();
			String[] parts = data.split(",");
			damage = Integer.parseInt(parts[0]);

			partialCoverMiss = Boolean.parseBoolean(parts[2]);
			convertedLocation = OVSegment.OVLocation.valueOf(parts[3]);
			if (parts[1].length()>2)location = new TwoDSix(parts[1].replace("|",","));

		}

		public OVSegment.OVLocation getConvertedLocation() {
			return convertedLocation;
		}

		public TwoDSix getLocation() {
			return location;
		}

		public void setConvertedLocation(OVSegment.OVLocation convertedLocation) {
			this.convertedLocation = convertedLocation;
		}
		public String getStreamValue() {
			String stream = "DAMAGEGROUP:" + damage + ",";
			String dice = "";
			if (location != null)dice = location.toString().replace(",","|");
			stream += dice;
			stream += "," + partialCoverMiss + "," + convertedLocation + "\n";
			return stream;
		}
	}
	private long key = 0;
	private int index=-1;
	private List<DamageGrouping> damage; // SRM or LRM or cluster weapons in general can have multiple locations
	//	private IUnitData source; //can be null? unit that inflicted damage? sometimes it comes from a fall so no source.  Do we need the source of the damage?
	private boolean applied = false;
	private final TargetData target;
//	private int targetKey =-1;
	private IWeapon weapon;
	private TwoDSix clusterDice=null;

	private int heatDamage = 0;
	private boolean sent = false; // if playing via bluetooth has it been sent yet?
	private boolean edgeUsed = false;
	private IChildLink parent = null;
	private final List<IChildLink> children = new ArrayList<>();
	public DamageRecord(IWeapon i, TargetData target) {
		super();
		damage = new ArrayList<>();
		weapon = i;
		this.target = target;
		if (target!=null && target.getShooter()!=null)
			target.getShooter().getTurn().addDamageDelivered(this);
	}

	public DamageRecord( TargetData target) {
		super();
		damage = new ArrayList<>();
		weapon = null;
		this.target = target;
		if (target.getShooter()!=null)
			target.getShooter().getTurn().addDamageDelivered(this);
	}

	public IWeapon getWeapon() {
		return weapon;
	}

	public void setWeapon(IWeapon weapon) {
		this.weapon = weapon;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}

	@Override
	public List<String> getStreamValue() {
		// Note that the damage record should always be preceeded by a TARGETDATA record so
		// it gets linked correctly
		List<String> list = new ArrayList<>();
		String stream = "DAMAGERECORD:";
		String dice= "";
		if (clusterDice!=null)
			dice = clusterDice.toString().replace(",","|");
		stream += key + "," + applied + "," + weapon.getID() + "," + dice + "," + heatDamage + "\n";
		list.add(stream);

		for (DamageGrouping group : damage){
			list.add(group.getStreamValue());
		}
		return list;
	}

	public List<DamageGrouping> getDamage() {
		return damage;
	}

	public int getHeatDamage() {
		return heatDamage;
	}

	public void setHeatDamage(int heatDamage) {
		this.heatDamage = heatDamage;
	}

	public void setDamage(List<DamageGrouping> damage) {
		this.damage = damage;
	}

	public TwoDSix getClusterDice() {
		return clusterDice;
	}

	public String getWeaponName() {
		if (weapon==null)return "Unknown";
		return weapon.getName();
	}
	public void addGroupingFromStream(String data){
		damage.add(new DamageGrouping(data));
	}
	public DamageGrouping addGrouping(int dmg, TargetData.LocTable table){
		TwoDSix dice;
		if (table== TargetData.LocTable.FULL)dice = new TwoDSix(2, TwoDSix.RollType.LOCATION);
		else dice = new TwoDSix(1, TwoDSix.RollType.LOCATION);
		DamageGrouping group = new DamageGrouping(dmg, dice);

		// if the damage is already applied then the converted location will be assigned directly,
		// avoid calling this as it can result in new crits being created due to through armour criticals
		if (!applied)
			group.setConvertedLocation( target.getTarget().convertLocation(dice.getTotal(),target.getFacing(),table, true,DamageRecord.this) );
		damage.add(group);
		return group;
	}
	public DamageGrouping addGrouping(int dmg) {
		TwoDSix diceRoll = new TwoDSix();
		return addGrouping(dmg,diceRoll);
	}
	public DamageGrouping addGrouping(int dmg, TwoDSix location) {
		DamageGrouping group = new DamageGrouping(dmg, location);
		// if the damage is already applied then the converted location will be assigned directly,
		// avoid calling this as it can result in new crits being created due to through armour criticals
		if (!applied)
			group.setConvertedLocation( target.getTarget().convertLocation(location.getTotal(),target.getFacing(),TargetData.LocTable.FULL, true,this) );
		damage.add(group);
		return group;
	}
	public DamageGrouping addGrouping(int dmg, OVSegment.OVLocation loc) {
		DamageGrouping group = new DamageGrouping(dmg, null);
		group.setConvertedLocation( loc );
		damage.add(group);
		return group;
	}
	public void applyDamage(IUnitData unit) {
		if (!applied) {
			System.out.println("applyDamage - Damage Record");
//			if (hit == null) hit = target.getTarget();
			for (int i = 0; i < damage.size();i++) {
				unit.applyDamage(weapon,this, damage.get(i), target);
				//				target.getTarget().applyDamage(damage.get(i).damage, damage.get(i).location, target.getFacing());
			}

			// what about heat damage? Where does this get applied?
			if (heatDamage!=0)unit.getTurn().addExternalHeat(heatDamage);
			applied = true;
		}
	}

	@Override
	public void setKey(long key) {
		this.key = key;
	}

	@Override
	public ChildType getRecordType() {
		return ChildType.DAMAGE;
	}

	@Override
	public long getKey() {
		return key;
	}

	public OVSegment.OVLocation getGroupingLocation(int index) {
		OVSegment.OVLocation loc = null;
		if (index<damage.size()) {
			loc = damage.get(index).getConvertedLocation();
		}
		return loc;
	}
	public int getTotalDamage() {
		int dmg = 0;
		for (int i = 0;i < damage.size();i++) {
			dmg += damage.get(i).damage;
		}		
		return dmg;
	}

	public TargetData getTarget() {
		return target;
	}

	public void setClusterDice(TwoDSix dice){
		clusterDice = dice;
	}
	public boolean alreadySent(){
		return sent;
	}
	public void markAsSent(){sent = true;}
	public void setIndex(int i){index = i;}
	public int getIndex(){return index;}

	@Override
	public boolean isEdgeUsed() {
		return edgeUsed;
	}

	@Override
	public void setParent(IChildLink parent) {
		this.parent = parent;
	}

	@Override
	public IChildLink getParent() {
		return parent;
	}

	@Override
	public List<IChildLink> getChildren() {
		return children;
	}

	@Override
	public void reverseCrit(UnitTurn turn) {
		// reverse all children
		for (IChildLink child : children){
			child.reverseCrit(turn);
		}
		turn.getDamageRecords().remove(this);
		for (DamageGrouping group : damage){
			OVSegmentInst seg = (OVSegmentInst) turn.getUnit().getSegment(group.getConvertedLocation());
			int armour,armourDmg,structDmg,struct;
			if (group.getConvertedLocation().isRear()){
				armour = seg.getArmourRear();
				armourDmg = seg.getRearTurnDmg();
			} else {
				armour = seg.getArmour();
				armourDmg = seg.getArmourTurnDmg();
			}
			struct = seg.getStructure();
			structDmg = seg.getStructureTurnDmg();
			structDmg += group.damage;
			int damage = 0;
			if (structDmg > struct){
				damage = structDmg - struct;
				structDmg = struct;
			}
			if (damage>0)
				armourDmg += damage;
			seg.setStructureTurnDmg(structDmg);
			if (group.getConvertedLocation().isRear())
				seg.setRearTurnDmg(Math.max(armourDmg,armour));
			else
				seg.setArmourTurnDmg(Math.max(armourDmg,armour));
		}
	}

	@Override
	public void setEdgeUsed(boolean edgeUsed) {
		this.edgeUsed = edgeUsed;
	}

}
