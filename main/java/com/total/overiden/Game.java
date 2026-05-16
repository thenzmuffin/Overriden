package com.total.overiden;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.total.artificial.AiForceList;
import com.total.overide.OVDatabaseForce;
import com.total.overide.OVSegment;
import com.total.overide.OVSegmentInst;
import com.total.overide.OVUnitData;
import com.total.overide.TWUnitData;
import com.total.overide.OVWeaponInstance;
import com.total.scenario.ScenarioDB;

import java.util.ArrayList;
import java.util.List;

/*
 * This class contains all the data/state for a game
 * 2 forces (one can be high level only - list of unit names)
 * 
 */
public class Game {
	public enum PlayerType {
		MANUAL, //("Manual Player"),
		CLIENT, //("Manual Player - Client"),
		AI, //("AI Run Player"),
		BLUETOOTH; //("Via Bluetooth");
//		private String description;

		PlayerType(/*String desc*/) {
			//description = desc;
		}
//		@NonNull
//		@Override
//		public String toString(){
//			return description;
//		}
	}

	public static Game current;
	// force 1
	private int gameKey = -1;
	private ForceList one;
	//force 2
	private ForceList two;
	private PlayerType forceOneType = PlayerType.MANUAL;
	private PlayerType forceTwoType;
	// current turn?
	// list of turn objects?
	private Turn thisTurn;
	private final List<Turn> previousTurns;
	private String gameName;
	private ForceList.ForceType type;
	private BluetoothInputThread comms = null;

	private Scenario scenario = null;
	private boolean active = true;

	//game config:
	private boolean smartHeat = false;
	private boolean pilotDamage = false;
	private boolean useTics = true;
	private boolean blindBSP = true;
	private boolean balanceInitiative = true; // apply modifier to initiative roll based on winner last turn
	private int initiativeBuff = 2; // bonus for having lost initiative last turn
	private boolean hexless = false; // only valid for TW games at the moment
	private final boolean displayAmmo = true;
	private boolean externalECM = true;
	private boolean unitECMActive = false;
	private int immobileMod = -2; //target modifier for an immobile target
	private boolean ovForcedWithdrawal = false;
	private boolean bspStrikeActive = false;
	private boolean soundEffects = true;

	public Game() {
		super();
		one = new ForceList(ForceList.ForceType.OV);
		two = new ForceList(ForceList.ForceType.OV);
		type = ForceList.ForceType.OV;
		OVUnitData.configureLocationTables();
		thisTurn = new Turn(1);
		previousTurns = new ArrayList<>();

		current = this;
		gameName = "TheGame";
	}
	public Game(String name, ForceList one, ForceList two) {
		super();
		this.one = one;
		this.two = two;
		gameName = name;
		thisTurn = new Turn(1);
		if (one!=null)one.startTurn();
		if (two!=null)two.startTurn();
		previousTurns = new ArrayList<>();


		type = (one!=null)?one.getType():ForceList.ForceType.OV;
		if (type == ForceList.ForceType.OV)
			OVUnitData.configureLocationTables();
		else TWUnitData.configureLocationTables();
		current = this;
	}
	public boolean isPilotDamage() {
		return pilotDamage;
	}
	public void setPilotDamage(boolean pilotDamage) {
		this.pilotDamage = pilotDamage;
	}
	public boolean isHexless(){return hexless;}
	public void setHexless(boolean hexless){
		this.hexless = hexless;
	}
	public boolean isUseTics() {
		return useTics;
	}
	public void setUseTics(boolean useTics) {
		this.useTics = useTics;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean act) {
		active = act;
	}
	public int getGameKey() {
		return gameKey;
	}
	public void endGame(Activity act) {
		try (DatabaseGame game = new DatabaseGame(act)) {
			active = false;
			game.addGame(this);
		}
		try (OVDatabaseForce force = new OVDatabaseForce(act)) {
			one.setInUse(false);
			force.addHeader(one);
			two.setInUse(false);
			force.addHeader(two);
		}
	}
	public void setGameKey(int key) {
		gameKey = key;
	}
	public ForceList getForce(int i) {
		return (i == 0) ? one : two;
	}
	public boolean isDisplayAmmo() {
		return displayAmmo;
	}
	public void setForce(int index, ForceList list) {
		// make sure type settings are correct for the selected lists
		if (one.getType()!=list.getType()){
			type = list.getType();
			if (type == ForceList.ForceType.OV)
				OVUnitData.configureLocationTables();
			else TWUnitData.configureLocationTables();
		}
		if (index == 0)	one = list;
		else two = list;


		// when updating a force list we should recompile the turn list
		resetTurnList();
	}
	private void resetTurnList(){
		//remove all unitturns from the turn list and readd from the two assigned forces
		thisTurn.getUnitTurns().clear();
		if (one !=null)	 addTurnsForList(one.getAllUnits());
		if (two != null) addTurnsForList(two.getAllUnits());
	}
	private void addTurnsForList(List<IUnitData> list){
		for (IUnitData data : list) {
			if (data.getTurn()!=null) thisTurn.getUnitTurns().add(data.getTurn());
		}
	}
	public int getTurnNumber() {
		return thisTurn != null ? thisTurn.getTurnNumber() : 0;
	}
	public Turn getThisTurn() {
		return thisTurn;
	}
	public IUnitData findUnitByKey(int key) {
		IUnitData ret = one.getUnitByKey(key);
		if (ret == null) {
			ret = two.getUnitByKey(key);
		}
		return ret;
	}
	public void endTurn() {
		int lastWinner = -1;
		assert one!=null;
		assert two!=null;
		one.endTurn();
		two.endTurn();
		previousTurns.add(thisTurn);
		if (balanceInitiative)
			lastWinner = thisTurn.getInitiative();
		thisTurn = new Turn(thisTurn.getTurnNumber() + 1);
		one.startTurn();
		two.startTurn();

		// reset unit turns and link the new ones to the Turn object
		one.addUnitTurns(thisTurn);
		two.addUnitTurns(thisTurn);
		// target lists should be pre-generated
//		one.generateTargetLists(two);
//		two.generateTargetLists(one);

		// if using balanced initiative
		if (balanceInitiative){
			if (lastWinner==0){
				two.setInitiativeModifier(two.getInitiativeModifier() + initiativeBuff);
			} else {
				one.setInitiativeModifier(one.getInitiativeModifier() + initiativeBuff);
			}
		}
		// Start any initial automated tasks for forcelist


	}
	public void endPhase(Turn.Phase phase){
		// in the resolve phase we switch off ecm active and reset it within the forcelist
		// checks if any units still have active ECM
		if (phase== Turn.Phase.RESOLVE)Game.current.setUnitECMActive(false);
		one.endPhase(phase);
		two.endPhase(phase);
	}
	public String getName() {
		return gameName;
	}
	public void setName(String name) {
		gameName = name;
	}
	public int getMassiveDamageThreshold() {
		int ret = 10; //OV default
		if (type != ForceList.ForceType.OV) {
			ret = 20;
		}
		return ret;
	}
	public void applyDamage(Fragment frag) {
		thisTurn.applyDamage();
		updateUnitStatus(frag);
	}
	public void updateUnitStatus(Fragment frag) {
		try (OVDatabaseForce forceDB = new OVDatabaseForce(frag.getActivity());
			 DatabaseGame dbGame = new DatabaseGame(frag.getActivity())) {
			for (int i = 0; i < one.getCount(); i++) {
				forceDB.updateUnitData(one.getUnit(i));
				dbGame.addDamageRecords(null, one.getUnit(i).getTurn().getDamageRecords(), one.getUnit(i).getTurn().getKey());

			}
			for (int i = 0; i < two.getCount(); i++) {
				forceDB.updateUnitData(two.getUnit(i));
				dbGame.addDamageRecords(null, two.getUnit(i).getTurn().getDamageRecords(), two.getUnit(i).getTurn().getKey());
			}
		}
		// need to update damage records to signal that the damage has now been applied as the
		// above updates are adding the applied damage to the database
	}
	public Turn.Phase getCurrentPhase() {
		Turn.Phase currentPhase = Turn.Phase.INITIATIVE;
		while (thisTurn.isPhaseComplete(Turn.Phase.getNextPhase(currentPhase)) && currentPhase != Turn.Phase.RESOLVE) {
			currentPhase = Turn.Phase.getNextPhase(currentPhase);
		}
		return currentPhase;
	}
	public PlayerType getForceTwoType() {
		return forceTwoType;
	}
	public PlayerType getForceOneType() {
		return forceOneType;
	}
	public void setForceTwoType(PlayerType forceTwoType) {
		this.forceTwoType = forceTwoType;
	}
	public void setForceOneType(PlayerType forceTwoType) {
		this.forceOneType = forceTwoType;
	}
	public void upDateOpFor(List<String> list, String deviceName) {
		ForceList transferredForce = new ForceList(list, deviceName);
		try (OVDatabaseForce forceDB = new OVDatabaseForce(MainActivity.currentActivity)) {
			forceDB.createExternalForce(transferredForce);
		}
		// We only need to set the force if the force hasn't already been linked(continuing an
		// existing game will not need it to be relinked)
//		it is always force one when it comes from another device
		if (transferredForce.getKey()!=two.getKey())
			setForce(1, transferredForce);

		//is there a screen that needs to be updated?
		MainActivity.currentActivity.updateScreenContents();
	}
	synchronized public void inGameUpdate(List<String> list, String deviceName){
		try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity);
			 OVDatabaseForce dbForce = new OVDatabaseForce(MainActivity.currentActivity)) {

			TargetData targetData = null;
			TargetWeapon weapon = null;
			IUnitData unit = null;
			DamageRecord dmgRecord = null;
			TurnViewModel model = new ViewModelProvider(MainActivity.currentActivity).get(TurnViewModel.class);
			Game game = model.getGame();
			for (String item : list) {
				String[] parts = item.split(":");
				switch (parts[0]) {
					case "GAMENAME":
						updateGame(parts[1], game);
						break;
					case "FORCELIST":
						upDateOpFor(list, deviceName);
						return; // end all processing after this command
//						break;
					case "RESETDICE":
						game.getThisTurn().setDiceOne(null);
						game.getThisTurn().setDiceTwo(null);
						break;
					case "WINNER": // initiative screen
						game.getThisTurn().setInitiative(Integer.parseInt(parts[1]));
						break;
					case "DICE1": // initiative screen
						game.getThisTurn().setDiceTwo(new TwoDSix(parts[1]));
						break;
					case "DICE2": //initiative screen
						game.getThisTurn().setDiceOne(new TwoDSix(parts[1]));
						break;
					case "MOVEUPDATE":
						moveUpdate(parts[1], gameDB, dbForce);
						break;
					case "TURNRESOLVED":
						updateTurnResolved(parts[1]);
						break;
					case "TARGETCOMP":
						// set targetting phase as complete or incomplete
						unit = updatePhaseComplete(Turn.Phase.TARGET, parts[1]);
						// DB update in DBTARGET
						break;
					case "TARGETDATA":
						targetData = targetUpate(parts[1], unit);
						// DB update in DBTARGET
						break;
					case "TARGETWEAPON":
						// must have loaded the related target data before this is processed
						weapon = targetWeaponUpdate(targetData, parts[1]);
						// DB update in DBTARGET
						// save the weapon record in case we need it for DB update
						break;
					case "BSPSTRIKE":
						// need to know which force to attach to / update
						bspstrikeUpdate(parts[1]);
						break;
					case "PHYSICALWEAPON":
						if (unit!=null)gameDB.addPhysicalWeapon(null,new PhysicalWeapon(parts[1]),unit.getTurn().getKey());
						else System.out.println("PHYSICALWEAPON - no unit found");
						break;
					case "UPDATEEXTUNIT":
						// load the unit with specified external key for use in subsequent lines
						unit = getForce(1).getUnitByExtKey(Integer.parseInt(parts[1]));
						break;
					case "UPDATELOCUNIT":
						// load the unit with specified external key for use in subsequent lines
						unit = getForce(0).getUnitByKey(Integer.parseInt(parts[1]));
						break;
					case "UNITTURN":
						if (unit!=null)unit.getTurn().updateFromStream(parts[1].split(","));
						break;
					case "EQUIPMENT":
						System.out.println("Equipment message received:"+parts[1]);
						// Equipment comes in a lot of types, might be: ammo used, equipment destroyed(critted)
						// defensive weapon set/mode changed
						updateEquipment(unit, parts[1]);
						break;
					case "STATE":
						if (unit != null)
							unit.getState().updateState(parts[1].split(","));
						break;
					case "DAMAGERECORD":
						// need to save the damage record as damage groups will follow that need to be attached
						if (targetData != null) dmgRecord = addDamageRecord(targetData, weapon, parts[1]);
						break;
					case "DAMAGEGROUP":
						if (dmgRecord != null) dmgRecord.addGroupingFromStream(parts[1]);
						break;
					case "DAMAGEMSG":
						// needs to handle both a standard message and a critical hit
						if (unit != null) new DamageMessage(unit, parts[1]);
						break;
					case "SEGMENTDMG":
						if (unit != null) updateSegmentDamage(unit, parts[1]);
						break;
					case "CRITICALHIT":
						if (unit != null) {
							// when creating a critical hit we need to mark it as sent otherwise it
							// gets returned to the source system. the constructor adds it to the
							// unitTurn
							(new CriticalHit(unit, parts[1])).markAsSent();
						}
						break;
					case "GENERICCHECK":
						// needs to create a new check in this case and it could be any type!
						if (unit != null) updateGenericCheck(unit, parts[1], weapon, gameDB);
						//unit.getTurn().addCheck(GenericCheck.newInstance(parts[1],unit));
						break;
					case "DBTARGET":
						// target data and associated targetweapons to be updated to the DB
						if (targetData != null && unit!=null) {
							gameDB.addTargetingComplete(targetData.getShooter());
							// make sure any defensive weapons are added
							unit.getTurn().clearDefensiveWeapons();
							for (TargetWeapon targetWeapon : unit.getTurn().getWeaponList()) {
								if (targetWeapon.getWeapon().getWeaponMode()== IWeapon.WeaponMode.AUTO){
									// this is a defensive weapon (AMS) save it as active
									unit.getTurn().setDefensiveWeapon(targetWeapon);
								}
							}
						} else
							System.out.println("ATTEMPT TO UPDATE TARGETTING DATA FAILED (NO SHOOTER)");
						unit = null; // clear the saved unit just in case
						break;
					case "DBSHOOTFROM":
						if (weapon != null) {
							gameDB.updateTargetWeapon(weapon);
							dbForce.updateAmmunition(weapon.getWeapon().getAmmo(), weapon.getTargetData().getShooter().getKey());
						} else
							System.out.println("ATTEMPT TO UPDATE SHOOTING DATA FAILED (NO WEAPON)");
						break;
					case "DBSHOOTHIT":
						if (unit != null) {
							gameDB.addUnitTurn(null, unit.getTurn());
							dbForce.updateUnitData(unit);
						} else
							System.out.println("ATTEMPT TO UPDATE SHOOTING DATA FAILED (NO TARGET)");
						break;
					case "DBPHYSICAL":
						if (targetData != null && weapon != null) {
							targetData.getShooter().getTurn().setPhysicalAttack(weapon); // the target generation doesn't know this is a physical attack so
							gameDB.addPhysicalTarget(targetData.getShooter());
						}
						break;
				}
			}
		}
		MainActivity.currentActivity.updateScreenContents();

	}
	private void updateGenericCheck( IUnitData unit, String input, TargetWeapon weapon, DatabaseGame gameDB){
		GenericCheck newCheck = null;
		// we need to determine whether this record already exists or if it needs to be created
		String[] parts = input.split(",");
		int key = Integer.parseInt(parts[0]);
		for (GenericCheck check : unit.getTurn().getTurnChecks()){
			if (check.getKey()==key){
				check.updateFromString(parts);
				//update this check
				newCheck = check;
				break;
			}
		}
		if (newCheck==null) {
			//if we reach this point then the check doesn't already exist so we need to create it
			newCheck = GenericCheck.newInstance(parts, unit, null);
			unit.getTurn().addCheck(newCheck);
			// this is a new check being added, if it is a massive damage pilot check we need to set the
			// turn to reflect that it has already been generated so:
			// only do this for a pilot check and then make sure it is due to damage
			if (newCheck.getCheckType() == GenericCheck.CheckType.PILOT &&
					PilotCheck.PilotCheckType.valueOf(parts[6]) == PilotCheck.PilotCheckType.DAMAGE) {
				if (weapon != null &&
						weapon.getWeapon() instanceof PhysicalWeapon)
					unit.getTurn().setHasPhysDamagePsr(true);
				else unit.getTurn().setHasDamagePsr(true);
			}
		}
		// add the update to the database
		gameDB.addCheckRecord(null,newCheck,unit.getTurn());
	}
	private DamageRecord addDamageRecord(TargetData targetData, TargetWeapon targetWeapon, String data){
		/* Record structure
		 * 0 : record key
		 * 1 : boolean - damage has been applied
		 * 2 : weapon id (OVTic) which caused the damage (can be a physical weapon)
		 * 3 : cluster dice
		 * 4 : int - damage inflicted as heat
		 */
		DamageRecord record = null;
		if (targetData!=null) {
			String[] parts = data.split(",");
			/* This method always adds a new damage record and will be immediately followed by a
			 * minimum of one Damage Group to be added to it
			 */
			IWeapon weap = null;
			if (targetData.getShooter()!=null) {
				int weaponID = Integer.parseInt(parts[2]);
				// standard weapon case
				if (weaponID < 900) {
					// find the weapon which caused the damage
					for (IWeapon weapon : targetData.getShooter().getWeapons()) {
						if (weapon.getID() == weaponID) {
							weap = weapon;
							break;
						}
					}
				} else {
					// in this case it will be a physical attack (greater than 900 but shooter exists)
					if (targetWeapon !=null && targetWeapon.getWeapon().getID()==weaponID){
						// use the last TargetWeapon object, in the case of a physical attack it should match
						weap = targetWeapon.getWeapon();
					}
				}

			} else {
				int weaponID = Integer.parseInt(parts[2]);
				if (weaponID>4000){
					weap = new BspWeapon(findBspStrike(weaponID-4000, true));
				} else {
					try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)) {
						weap = gameDB.loadPhysicalWeapon(null, targetData.getTarget().getTurn(), weaponID);
					}
					// TODO: What about ammo explosions??
					// if we have a fall there is no shooter
//				int calcDamage = Math.floorDiv(targetData.getTarget().getHeader().getMass()+25,30);
//				weap = new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.FALL,calcDamage, PhysicalWeapon.PhysicalHitGrouping.FULL);
				}
			}
			record = new DamageRecord(weap, targetData);
			record.markAsSent(); // we received this from the other device so mark not to be sent back
			record.setHeatDamage(Integer.parseInt(parts[4]));
			if (parts[3].length() > 2)record.setClusterDice(new TwoDSix(parts[3].replace("|",",")));
			record.setApplied(Boolean.parseBoolean(parts[1]));
			// if we do a direct add it avoids adding a PSR check automatically that shouldn't be there!
			targetData.getTarget().getTurn().getDamageRecords().add(record);
			//need to manually update the index on the record so it displays in the correct order
			record.setIndex(targetData.getTarget().getTurn().getDamageRecords().size());
			// don't set the key as this is used internally in the DB only. Means it cannot be
			// updated externally once it is created
	//		record.setKey(Long.parseLong(parts[0]));

		}
		return record;
	}
	private void updateSegmentDamage(IUnitData unit, String data){
		if (unit!=null) {
			String[] parts = data.split(",");
			OVSegmentInst seg = (OVSegmentInst)unit.getSegment(OVSegment.OVLocation.valueOf(parts[0]));
			seg.updateDamageFromStream(parts);
			if (Game.current.isSoundEffects()){
				switch (seg.getLocation()){
					case HEAD:
					case TORSO:
					case CENTRETORSO:
						if (seg.getStructureTurnDmg()<=0)
							MainActivity.currentActivity.playSound(MainActivity.Sounds.BOOM);
						break;
					case RIGHTARM:
					case LEFTARM:
						if (seg.getStructureTurnDmg()<=0)
							MainActivity.currentActivity.playSound(MainActivity.Sounds.LOSTARM);
						break;
				}
			}
		}
	}
	private void updateEquipment(IUnitData unit, String data){
		if (unit!=null) {
			String[] parts = data.split(",");
			for (IEquipment equip : unit.getEquipment()) {
				if (equip.getID() == Integer.parseInt(parts[2])) {
					equip.updateFromStream(parts);
					// Defensive weapons may have been activated, if so they should be added to the defensive weapon list
					if (equip instanceof OVWeaponInstance && ((OVWeaponInstance)equip).getWeaponMode()== IWeapon.WeaponMode.AUTO){
						unit.getTurn().updateDefensiveWeapons();
					}
					break;
				}
			}
		}
	}
	private void updateTurnResolved(String data) {
		String[] parts = data.split(",");

		IUnitData unit = getForce(1).getUnitByExtKey(Integer.parseInt(parts[0]));
		if (unit != null) {
			unit.getTurn().setTurnResolved(true);
		}
	}
	private IUnitData updatePhaseComplete(Turn.Phase phase, String data) {
		IUnitData unit = null;
		String[] parts = data.split(",");
		if (phase == Turn.Phase.TARGET) {
			unit = getForce(1).getUnitByExtKey(Integer.parseInt(parts[0]));
			if (unit != null) {
				boolean complete = Boolean.parseBoolean(parts[1]);
				unit.getTurn().setTargetingComplete(complete);
				if (!complete){
					// if the message that has arrived is to deselect targeting complete then we
					// should delete the targeting data
					for (TargetData targetData : unit.getTurn().getAllTargets()){
						targetData.getWeapons().clear();
					}
					try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)){
						gameDB.addTargetingReverted(unit);
					}
				}
			}
		}
		return unit;
	}
	public void reorderUnits(){
		one.reorderUnits();
		two.reorderUnits();
	}
	private void updateGame(String data, Game game) {
		String[] parts = data.split(",");
		game.setName(parts[0]);
		game.setSmartHeat(Boolean.parseBoolean(parts[1]));
		game.setPilotDamage(Boolean.parseBoolean(parts[2]));
		game.setUseTics(Boolean.parseBoolean(parts[3]));
		game.setHexless(Boolean.parseBoolean(parts[4]));
		game.setExternalECM(Boolean.parseBoolean(parts[5]));
		game.setOvForcedWithdrawal(Boolean.parseBoolean(parts[6]));
		game.setBspStrikeActive(Boolean.parseBoolean(parts[7]));
	}
	private void moveUpdate(String data, DatabaseGame gameDB, OVDatabaseForce forceDB){
		String[] parts = data.split(",");
		for (IUnitData unit : getForce(1).getAllUnits()){
			// Move updates should only ever be for the externally supplied force
			if (unit.getState().getExternalID()==Integer.parseInt(parts[2]) ){
				unit.getTurn().getMoveData().setType(UnitMove.MoveType.valueOf(parts[4]),Boolean.parseBoolean(parts[9]));
				unit.getTurn().getMoveData().setMoveLocked(Boolean.parseBoolean(parts[5]));
				unit.getTurn().getMoveData().setStood(Boolean.parseBoolean(parts[6]));
				unit.getTurn().getMoveData().setHexesMoved(Integer.parseInt(parts[7]));
				unit.getTurn().getMoveData().setTmm(Integer.parseInt(parts[8]));
				unit.getState().setProne(Boolean.parseBoolean(parts[9]));
				unit.getTurn().setReservePhysicalAttack(PhysicalWeapon.PhysicalWeaponType.valueOf(parts[10]));
				if (unit.getTurn().getReservePhysicalAttack()!= PhysicalWeapon.PhysicalWeaponType.NONE)
					unit.getTurn().setTargetingComplete(true);  // if charge or DFA has been assigned then no shooting this turn
				gameDB.updateMovePhase(unit.getTurn(),null);
				forceDB.addUnitHeader(unit.getHeader(), unit.getState());
				break;
			}
		}
	}
	public TargetData targetUpate(String data, IUnitData derivedUnit) {
		/* 0: targetdata id
		 * 1: unit external id
		 * 2:target unit id
		 * 3: range
		 * 4: other (mod)
		 * 5: partial cover
		 * 6: facing
		 * 7: forward arc
		 * 8: indirect
		 * 9: spotterkey
		 * 10: ecmstatus
		 * 11: location table
		 * 12: number of weapons
		 */
		TargetData target = null;
		String[] parts = data.split(",");
		// for falls/ammo explosions there is no shooter and the record must be handled differently
		if (Integer.parseInt(parts[1])<0){
			// for this scenario always create a new TargetData (fall)
				if (derivedUnit != null) { // else something has gone wrong!!!!
					target = new TargetData(null, derivedUnit);
					target.setRange(Integer.parseInt(parts[3]));
					target.setOther(Integer.parseInt(parts[4]));
					target.setPartialCover(Boolean.parseBoolean(parts[5]));
					target.setFacing(TargetData.LocTable.valueOf(parts[6]));
					target.setForwardArc(Boolean.parseBoolean(parts[7]));
					target.setIndirect(Boolean.parseBoolean(parts[8]));
					target.setSpotterKey(Integer.parseInt(parts[9]));
					target.setEcmBubble(Integer.parseInt(parts[10]));
				}

		} else {
			// find the matching unit
			IUnitData unit = getForce(1).getUnitByExtKey(Integer.parseInt(parts[1]));
			if (unit != null) {
				// we have found the shooter
				// try and find the targetData in case it already exists
				for (TargetData targ : unit.getTurn().getAllTargets()) {
					if (targ.getTarget().getKey() == Integer.parseInt(parts[2])) {
						target = targ;
						break;
					}
				}
				if (target == null) {
					IUnitData findTarg = getForce(0).getUnitByKey(Integer.parseInt(parts[2]));
					if (findTarg != null) { // else something has gone wrong!!!!
						target = new TargetData(unit, findTarg);
						unit.getTurn().addTarget(target);
					}
				}
				if (target != null) {
					// finally we have the target to update
					target.setRange(Integer.parseInt(parts[3]));
					target.setOther(Integer.parseInt(parts[4]));
					target.setPartialCover(Boolean.parseBoolean(parts[5]));
					target.setFacing(TargetData.LocTable.valueOf(parts[6]));
					target.setForwardArc(Boolean.parseBoolean(parts[7]));
					target.setIndirect(Boolean.parseBoolean(parts[8]));

					// set the range going the other way as it must be the same, only if it hasn't already been set
					// we aren't stopping the players from putting incorrect values if they really want to!
					target.getTarget().getTurn().setReciprocalRange(target.getShooter().getKey(),target.getRange());
				}
			}
		}
		return target;
	}
	public TargetWeapon targetWeaponUpdate(TargetData target, String data){
		System.out.println("TARGET WEAPON UPDATE:" + data);
		/* 0: weapon ID
		 * 1: to hit number
		 * 2: rolled
		 * 3: status
		 * 4: locked
		 * 5: location table
		 * 6: weapon name (physical only)
		 * 7: damage (physical only)
		 * 8: physical grouping (physical only)
		 */
		TargetWeapon weapon = null;
		String[] parts = data.split(",");
		if (target != null) {
			// does the weapon already have an entry?
			for (TargetWeapon weap :target.getWeapons()){
				if (weap.getWeapon().getID()==Integer.parseInt(parts[0])){
					weapon = weap;
					break;
				}
			}
			if (weapon==null){
				int weaponID = Integer.parseInt(parts[0]);
				if (weaponID > 900){
					if (weaponID > 4000) {
					// This is a BSP Strike Weapon
						BSPStrike strike = findBspStrike(weaponID-4000, true);
						if (strike!=null){
							weapon = new TargetWeapon(target,new BspWeapon(strike));
						} else {
							System.out.println("BSP Strike Not found when creating Target Weapon");
						}
					} else if (parts.length >=9) //should throw an error if not
						//this is a physical attack weapon that needs to be generated
						weapon = new TargetWeapon(target,new PhysicalWeapon(PhysicalWeapon.PhysicalWeaponType.valueOf(parts[6]), Integer.parseInt(parts[7]), PhysicalWeapon.PhysicalHitGrouping.valueOf(parts[8])));
					else
						System.out.println("Physical weapon with too few fields provided");
				} else {                // we need to find the weapon in the weapons list for the unit
					for (IWeapon weap : target.getShooter().getWeapons()) {
						if (weap.getID() == weaponID) {
							weapon = new TargetWeapon(target, weap);
							break;
						}
					}
				}
			}
// have we found the weapon? then populate the data(it is already attached to the targetdata)
			if (weapon!=null){
				weapon.setToHit(Integer.parseInt(parts[1]));
				if (parts[2].length()>2) { //this means the dice text exists
					// commas had to be replaced to make sure it didn't mess with parsing the
					// larger string
					weapon.setRolled(new TwoDSix(parts[2].replace("|",",")));
				}
				weapon.setStatus(TargetWeapon.ShotStatus.valueOf(parts[3])); // should always be not shot but just in case!
				weapon.setLocked(Boolean.parseBoolean(parts[4]));
				weapon.setLocationTable(TargetData.LocTable.valueOf(parts[5]));
			}

		}
		return weapon;
	}
	public void bspstrikeUpdate( String data){
		String[] parts = data.split(",");
		int key = BSPStrike.getKey(parts);
		if (key<0){
			// only ever get an undefined key when it is foreign owned
			// we always receive these at the beginning of the game, DB update is inefficient but only happens once
			BSPStrike created = new BSPStrike(data);
			Game.current.getForce(1).getBspStrikes(Turn.Phase.SETUP).add(created);
			try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)){
				gameDB.addBspStrikeCards(Game.current.getForce(1));
			}
			// send it back with the updated key
			UpdatePlayerActions.bspCardUpdate(created);

		} else {
			// need to add 4000 to the key to make sure the record is located correctly if it already exists
			BSPStrike found = findBspStrike(key, false);

			if (found != null) {
				found.updateFromStream(parts);
				// don't need to update the link to the force as this already exists
				try (DatabaseGame gameDB = new DatabaseGame(MainActivity.currentActivity)) {
					gameDB.addBspStrikeCard(found);
				}
			} else {
				System.out.println("BSPStrike record not found during update. key:" + key);
			}
		}
	}
	public static BSPStrike findBspStrike(int key, boolean foreign){
		BSPStrike found = null;

		// find the card being updated, could be either force but should already exist
		// check the foreign cards first as these are updated more often
		for (BSPStrike updated : Game.current.getForce(1).getBspStrikes(Turn.Phase.SETUP)) {
			if ((!foreign && updated.getKey() == key) || (foreign && updated.getForeignKey() == key)) {
				found = updated;
				break;
			}
		}
		if (found == null)
			for (BSPStrike updated : Game.current.getForce(0).getBspStrikes(Turn.Phase.SETUP)) {
				if ((!foreign && updated.getKey() == key) || (foreign && updated.getForeignKey() == key)) {
					found = updated;
					break;
				}
			}
		return found;
	}
	public BluetoothInputThread getComms() {
		return comms;
	}
	public void setComms(BluetoothInputThread comms) {
		this.comms = comms;
	}
	public boolean isSmartHeat() {
		return smartHeat;
	}
	public void setSmartHeat(boolean smartHeat) {
		this.smartHeat = smartHeat;
	}
	public boolean isExternalECM() {
		return externalECM;
	}
	public void setExternalECM(boolean externalECM) {
		this.externalECM = externalECM;
	}
	public boolean isUnitECMActive() {
		return unitECMActive;
	}
	public void setUnitECMActive(boolean unitECMActive) {
		this.unitECMActive = unitECMActive;
	}
	public boolean isSoundEffects() {
		return soundEffects;
	}
	public void setSoundEffects(boolean soundEffects) {
		this.soundEffects = soundEffects;
	}
	public int getImmobileMod() {
		return immobileMod;
	}
	public void setImmobileMod(int mod) {
		immobileMod = mod;
	}
	public boolean isOvForcedWithdrawal() {
		return ovForcedWithdrawal;
	}
	public void setOvForcedWithdrawal(boolean ovForcedWithdrawal) {
		this.ovForcedWithdrawal = ovForcedWithdrawal;
	}
	public void setBspStrikeActive(boolean setBspStrikeActive) {
		this.bspStrikeActive = setBspStrikeActive;
	}
	public boolean isPhaseComplete(Turn.Phase phase){
		boolean initPhase = true;
		if (phase== Turn.Phase.INITIATIVE){
			initPhase = thisTurn.isPhaseComplete(phase);
		}
		return initPhase && one.isPhaseComplete(phase) && two.isPhaseComplete(phase);
	}
	public boolean isBspStrikeActive() {
		return bspStrikeActive;
	}
	public boolean isGameOV(){return type== ForceList.ForceType.OV;}
	public boolean isBlindBSP() {
		return blindBSP;
	}
//	public void setBlindBSP(boolean blindBSP) {
//		this.blindBSP = blindBSP;
//	}
	public AiForceList getAiForce(){
		if (two instanceof AiForceList)
			return (AiForceList)two;
		else return null;
	}
	public boolean eventExists(Scenario.ScenarioEvent event){
		if (scenario != null) {
			return scenario.eventExists(event);
		} else return false;
	}
	public void eventTriggered(Scenario.ScenarioEvent event, int paramInt, IUnitData unit, Fragment frag){
		if (scenario != null){
			scenario.checkEvent(event, paramInt, unit, frag);
		}
	}
	public boolean isBalanceInitiative() {
		return balanceInitiative;
	}
	public void setBalanceInitiative(boolean balanceInitiative) {
		this.balanceInitiative = balanceInitiative;
	}
	public void setScenario(long key){
		if (key>=0){
			try (ScenarioDB db = new ScenarioDB(MainActivity.currentActivity)){
				scenario = db.loadScenario(key);
			}
		}else scenario = null;
	}
	public long getScenarioKey(){
		if (scenario!=null){
			return scenario.getInstanceKey();
		}else
			return -1;
	}
	public void generateScenarioData(int key){

	}
}
