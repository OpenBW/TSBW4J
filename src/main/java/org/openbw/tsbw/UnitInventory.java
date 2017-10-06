package org.openbw.tsbw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.SupplyDepot;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.VespeneGeyser;

public class UnitInventory {

	private static final Logger logger = LogManager.getLogger();
	
	private Group<PlayerUnit> destroyedUnits;
	private Squad<MobileUnit> scouts;
	private Squad<SCV> mineralWorkers;
	private Squad<SCV> vespeneWorkers;
	private Group<Building> underConstruction;
	private CommandCenter main = null;
	
	private Group<MineralPatch> allMineralPatches;
	private Group<VespeneGeyser> allVespeneGeysers;
	private Collection<PlayerUnit> allUnits;
	
	private List<Group<? extends PlayerUnit>> groups;
	
	public UnitInventory() {
		
	    this.allUnits = new HashSet<PlayerUnit>();
		this.destroyedUnits = new Group<PlayerUnit>();
		this.scouts = new Squad<MobileUnit>("scouts");
		this.mineralWorkers = new Squad<SCV>("mineral mining workers");
		this.vespeneWorkers = new Squad<SCV>("vespene mining workers");
		this.underConstruction = new Group<Building>();
		
		this.allMineralPatches = new Group<MineralPatch>();
		this.allVespeneGeysers = new Group<VespeneGeyser>();
		
		this.groups = new ArrayList<Group<? extends PlayerUnit>>();
	}
	
	public void initialize() {
		
	    this.allUnits.clear();
		this.groups.clear();
		
		this.destroyedUnits.clear();
		this.groups.add(destroyedUnits);
		this.scouts.clear();
		this.groups.add(scouts);
		this.mineralWorkers.clear();
		this.groups.add(mineralWorkers);
		this.vespeneWorkers.clear();
		this.groups.add(vespeneWorkers);
		this.underConstruction.clear();
		this.groups.add(underConstruction);
		
		this.allMineralPatches.clear();
		this.allVespeneGeysers.clear();
		
		this.main = null;
	}
	
	public CommandCenter getMain() {
		return main;
	}
	
	public void setMain(CommandCenter commandCenter) {
		this.main = commandCenter;
	}
	
	public Group<MineralPatch> getMineralPatches() {
		return this.allMineralPatches;
	}
	
	public Group<VespeneGeyser> getVespeneGeysers() {
		return this.allVespeneGeysers;
	}
	
	public Group<Refinery> getRefineries() {
		return this.allUnits.stream().filter(u -> u instanceof Refinery).map(u -> (Refinery)u).collect(Util.toGroup());
	}
	
	public Group<Building> getBuildings() {
		return this.allUnits.stream().filter(u -> u instanceof Building).map(u -> (Building)u).collect(Util.toGroup());
	}
	
	public Group<Building> getUnderConstruction() {
		return this.underConstruction;
	}
	
	public Squad<SCV> getAllWorkers() {
		return this.allUnits.stream().filter(u -> u instanceof SCV).map(u -> (SCV)u).collect(Util.toSquad());
	}
	
	public Squad<MobileUnit> getArmyUnits() {
		return this.allUnits.stream().filter(u -> u instanceof MobileUnit && !(u instanceof SCV)).map(u -> (MobileUnit)u).collect(Util.toSquad());
	}
	
	public Squad<MobileUnit> getScouts() {
		return this.scouts;
	}
	
	public Squad<SCV> getMineralWorkers() {
		return this.mineralWorkers;
	}
	
	public Squad<SCV> getVespeneWorkers() {
		return this.vespeneWorkers;
	}
	
	public List<MobileUnit> getAllMobileUnits() {
		
		return this.allUnits.stream().filter(u -> u instanceof MobileUnit).map(u -> (MobileUnit)u).collect(Collectors.toList());
	}
	
	/**
	 * Returns a list of all mobile units in range of the provided squad (any member of the squad).
	 * @param squad squad to check range against
	 * @return list of mobile units in range
	 */
	public List<MobileUnit> getMobileUnitsInRange(Squad<? extends PlayerUnit> squad) {
		
		List<MobileUnit> unitsInRange = new ArrayList<MobileUnit>();
		for (MobileUnit armyUnit : this.getAllMobileUnits()) {
			if (armyUnit.exists()) {
				for (PlayerUnit squadUnit : squad) {
					int range = 0;
					if (squadUnit.isFlying() && armyUnit.getAirWeapon() != null) {
						range = armyUnit.getAirWeapon().maxRange() + 32;
					} else if (armyUnit.getGroundWeapon() != WeaponType.None) {
						range = Math.max(160, armyUnit.getGroundWeapon().maxRange() + 32);
					}
					if (armyUnit.getDistance(squadUnit) <= range) {
						unitsInRange.add(armyUnit);
						break;
					}
				}
			}
		}
		return unitsInRange;
	}
	
	/**
	 * Returns a list of mobile units within given radius from given position.
	 * @param position
	 * @param radius
	 * @return list of mobile units
	 */
	public List<MobileUnit> getMobileUnitsInRadius(Position position, int radius) {
		
		return this.allUnits.parallelStream().filter(t -> t instanceof MobileUnit && position.getDistance(t.getPosition()) <= radius).map(t -> (MobileUnit)t).collect(Collectors.toList());
	}
	
	/**
	 * Returns a list of mobile units within given radius from given unit.
	 * @param unit
	 * @param radius
	 * @return list of mobile units
	 */
	public List<MobileUnit> getMobileUnitsInRadius(Unit unit, int radius) {
		
		return getMobileUnitsInRadius(unit.getPosition(), radius);
	}
	
	// TODO For now just assume that mining workers are always available. later maybe give the developer a way to choose behavior.
	public Squad<SCV> getAvailableWorkers() {
		return this.mineralWorkers;
	}

	public Group<PlayerUnit> getDestroyedUnits() {
		return this.destroyedUnits;
	}

	public Group<CommandCenter> getCommandCenters() {
		
		return this.allUnits.stream().filter(u -> u instanceof CommandCenter).map(u -> (CommandCenter)u).collect(Util.toGroup());
	}
	
	public Group<Barracks> getBarracks() {
		
		return this.allUnits.stream().filter(u -> u instanceof Barracks).map(u -> (Barracks)u).collect(Util.toGroup());
	}
	
	public Group<Factory> getFactories() {
		
		return this.allUnits.stream().filter(u -> u instanceof Factory).map(u -> (Factory)u).collect(Util.toGroup());
	}
	
	public void register(PlayerUnit unit) {
		
		if (unit instanceof Building) {
			if (unit.isCompleted()) {
				this.underConstruction.remove(unit);
				this.allUnits.add(unit);
				if (this.main == null && unit instanceof CommandCenter) {
					this.main = (CommandCenter)unit;
				}
			} else {
				this.underConstruction.add((Building)unit);
			}
		} else {
			this.allUnits.add(unit);
			if (unit instanceof SCV) {
				this.mineralWorkers.add((SCV) unit);
			}
		}
	}
	
	public void register(VespeneGeyser geyser) {
		this.allVespeneGeysers.add(geyser);
	}
	
	public void register(MineralPatch patch) {
		this.allMineralPatches.add(patch);
	}
	
	public void onUnitDestroy(MineralPatch patch, int frameCount) {
		
		logger.trace("{} got destroyed", patch);
		this.allMineralPatches.remove(patch);
	}
	
	public void onUnitDestroy(PlayerUnit unit, int frameCount) {
		
		logger.trace("{} got destroyed", unit);
		for (Group<? extends PlayerUnit> group : this.groups) {
			group.remove(unit);
		}
		if (unit.equals(this.main)) {
			this.main = null; // TODO assign next command center as main if there is one
		}
		this.destroyedUnits.add(unit);
	}
	
	public int getSupplyUsed() {
		
		int supply = 0;
		for (MobileUnit unit : this.getAllMobileUnits()) {
			supply += unit.getSupplyRequired();
		}
		return supply;
	}

	public int getSupplyTotal() {
		
		int supply =  0;
		for (Building building : this.getBuildings()) {
			if (building instanceof CommandCenter) {
				supply += 10;
			}
			if (building instanceof SupplyDepot) {
				supply += 8;
			}
		}
		return supply;
	}

	public <T extends MobileUnit> Squad<T> createSquad(Class<T> t, String name) {
		
		Squad<T> squad = new Squad<T>(name);
		this.groups.add(squad);
		logger.debug("created empty squad {}", name);
		return squad;
	}

	public boolean update(Unit bwUnit, int timeSpotted) {
		// TODO Auto-generated method stub
		return false;
	}
}