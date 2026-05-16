package com.total.overiden;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import javax.swing.JLabel;
//import javax.swing.JPanel;



public class DamageMessage implements IDamageRecord {
	private int index=-1; //key to ordering messages in the display
	private String description;
	private boolean applied = true;
	private long key = 0;
	private TwoDSix dice = null;
	private boolean sent = false; // if playing via bluetooth has it been sent yet?

	private IChildLink parent = null;
	private final List<IChildLink> children = new ArrayList<>();
	public DamageMessage(String description) {
		super();
		this.description = description;
	}
	public DamageMessage(String description,int index, IChildLink parent) {
		super();
		this.description = description;
		this.index = index;
		setParent(parent);
	}

	public DamageMessage(String description, TwoDSix cluster) {
		super();
		this.description = description;
		this.dice = cluster;
	}
	public DamageMessage(IUnitData unit, String data) {
		// used in bluetooth connections only
		super();
		String[] parts = data.split(",");
		// called from stream input
		description = parts[0];
		applied = Boolean.parseBoolean(parts[1]);
		// don't use the key, this is specific to the database in use

		if (parts.length > 3 && parts[3].length() > 2) // if the dice roll is blank there won't be a 4th array entry
			dice = new TwoDSix(parts[3].replace("|", ","));
		unit.getTurn().addDamage(this);//  getDamageRecords().add(this);
		// TODO build the parent and child relationships
	}
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
//	@Override
//	public void addDamageLine(Fragment p) {
//	//	JLabel l = new JLabel(description);
//	//	l.setBackground(Color.red); l.setOpaque(true);
//	//	p.add(l,c);
//
//	}

	@Override
	public int getTotalDamage() {
		return 0;
	}

	public void actionCrit(IUnitData unit, int location) {
		// nothing to do here?
		unit.getTurn().addDamage(this);
	}
	public void toFile(OutputStream pOut) {
		
		try {
			pOut.write(("DAMAGEMESSAGE," + description).getBytes());
			pOut.write('\n');
		} catch (IOException x) {
			System.err.println(x);
		}
	}
//	@Override
//	public void linkTargets(List<TargetData> targets) {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public IWeapon getWeapon() {
		return null;
	}

	@Override
	public TargetData getTarget() {
		return null;
	}

	@Override
	public TwoDSix getClusterDice() {
		return dice;
	}

	@Override
	public List<DamageRecord.DamageGrouping> getDamage() {
		return Collections.emptyList();
	}

	@Override
	public void applyDamage(IUnitData unit) {
		applied = true;
	}

	public String getDescription() {
		return description;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}

	protected String getSingleStreamValue(){
		String stream = description + "," + applied + "," + key + ",";
		String diceSt = "";
		if (dice!=null)diceSt = dice.toString().replace(",","|");
		stream += diceSt;
		stream += "," + index;
		return stream;
	}
	@Override
	public List<String> getStreamValue() {
		List<String> list = new ArrayList<>();
		list.add("DAMAGEMSG:" + getSingleStreamValue() + "\n");
		return list;
	}

	public void setDice(TwoDSix dice) {
		this.dice = dice;
	}
	public boolean alreadySent(){
		return sent;
	}
	public void markAsSent(){sent = true;}
	public void setIndex(int i){index = i;}
	public int getIndex(){return index;}

	@Override
	public void setEdgeUsed(boolean used) {

	}

	@Override
	public boolean isEdgeUsed() {
		return false;
	}

	@Override
	public void setParent(IChildLink parent) {
		this.parent = parent;
		if (parent!=null){
			//make sure this is also a child on the parent
			parent.getChildren().add(this);
		}
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
		children.clear();
		turn.getDamageRecords().remove(this);
	}
}
