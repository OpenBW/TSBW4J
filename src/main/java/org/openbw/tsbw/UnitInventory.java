package org.openbw.tsbw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.tsbw.unit.Barracks;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.Bunker;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.Geyser;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.MobileUnit;
import org.openbw.tsbw.unit.PlayerUnit;
import org.openbw.tsbw.unit.SupplyDepot;
import org.openbw.tsbw.unit.Unit;
import org.openbw.tsbw.unit.Worker;

import bwapi.Position;
import bwapi.UnitType;

public class UnitInventory {

	private static final Logger logger = LogManager.getLogger();
	
	private Group<MineralPatch> mineralPatches;
	private Group<Geyser> geysers;
	
	private Squad<MobileUnit> allArmyUnits;
	private Squad<Worker> allWorkers;
	
	private Group<PlayerUnit> destroyedUnits;
	
	private Group<Building> buildings;
	private Group<CommandCenter> commandCenters;
	private Group<Barracks> barracks;
	
	private Squad<Worker> miningWorkers;
	private Squad<MobileUnit> scouts;
	private CommandCenter main = null;
	
	private List<Building> underConstruction;
	private List<Group<? extends PlayerUnit>> groups;
	
	public UnitInventory() {
		
		this.mineralPatches = new Group<MineralPatch>("mineral patches");
		this.geysers = new Group<Geyser>("geysers");
		this.buildings = new Group<Building>("buildings");
		this.commandCenters = new Group<CommandCenter>("command centers");
		this.barracks = new Group<Barracks>("barracks");
		this.allArmyUnits = new Squad<MobileUnit>("army units");
		this.allWorkers = new Squad<Worker>("workers");
		this.miningWorkers = new Squad<Worker>("mining workers");
		this.scouts = new Squad<MobileUnit>("scouts");
		this.destroyedUnits = new Group<PlayerUnit>("destroyed units");
		this.underConstruction = new ArrayList<Building>();
		
		this.groups = new ArrayList<Group<? extends PlayerUnit>>();
	}
	
	public void initialize() {
		
		this.groups.clear();
		
		this.mineralPatches.clear();
		this.geysers.clear();
		this.buildings.clear();
		this.groups.add(buildings);
		this.commandCenters.clear();
		this.groups.add(commandCenters);
		this.barracks.clear();
		this.groups.add(barracks);
		this.allArmyUnits.clear();
		this.groups.add(allArmyUnits);
		this.allWorkers.clear();
		this.groups.add(allWorkers);
		this.miningWorkers.clear();
		this.groups.add(miningWorkers);
		this.scouts.clear();
		this.groups.add(scouts);
		this.destroyedUnits.clear();
		this.underConstruction.clear();
		this.main = null;
	}
	
	public CommandCenter getMain() {
		return main;
	}
	
	public void setMain(CommandCenter commandCenter) {
		this.main = commandCenter;
	}
	
	public void register(Geyser geyser) {
		this.geysers.add(geyser);
	}
	
	public void register(MineralPatch mineralPatch) {
		this.mineralPatches.add(mineralPatch);
	}
	
	public void register(Bunker bunker) {
		
		if (bunker.isCompleted()) {
			this.underConstruction.remove(bunker);
			this.buildings.add(bunker);
		} else {
			this.underConstruction.add(bunker);
		}
	}

	public void register(Barracks barracks) {
		
		if (barracks.isCompleted()) {
			this.underConstruction.remove(barracks);
			this.barracks.add(barracks);
			this.buildings.add(barracks);
		} else {
			this.underConstruction.add(barracks);
		}
	}
	
	public void register(CommandCenter commandCenter) {
		
		if (commandCenter.isCompleted()) {
			if (main == null) {
				main = commandCenter;
			}
			this.underConstruction.remove(commandCenter);
			this.commandCenters.add(commandCenter);
			this.buildings.add(commandCenter);
		} else {
			this.underConstruction.add(commandCenter);
		} 
	}
	
	public void register(Building building) {
		
		if (building.isCompleted()) {
			this.underConstruction.remove(building);
			this.buildings.add(building);
		} else {
			this.underConstruction.add(building);
		}
	}
	
	public void register(Worker worker) {
		this.allWorkers.add(worker);
	}
	
	public void register(MobileUnit armyUnit) {
		this.allArmyUnits.add(armyUnit);
	}
	
	public <T extends MobileUnit> Squad<T> createSquad(String name, List<T> units) {
		
		Squad<T> squad = new Squad<T>(name, units);
		this.groups.add(squad);
		logger.debug("created squad {}", name);
		return squad;
	}
	
	public <T extends MobileUnit> Squad<T> createSquad(Class<T> t, String name) {
		
		Squad<T> squad = new Squad<T>(name);
		this.groups.add(squad);
		logger.debug("created empty squad {}", name);
		return squad;
	}
	
	public Group<MineralPatch> getMineralPatches() {
		return this.mineralPatches;
	}
	
	public Group<Geyser> getGeysers() {
		return this.geysers;
	}
	
	public Group<Building> getBuildings() {
		return this.buildings;
	}
	
	public List<Building> getUnderConstruction() {
		return this.underConstruction;
	}
	
	public Squad<Worker> getAllWorkers() {
		return this.allWorkers;
	}
	
	public Squad<MobileUnit> getArmyUnits() {
		return this.allArmyUnits;
	}
	
	public Squad<MobileUnit> getScouts() {
		return this.scouts;
	}
	
	public Squad<Worker> getMiningWorkers() {
		return this.miningWorkers;
	}
	
	public Set<MobileUnit> getAllMobileUnits() {
		
		Set<MobileUnit> units = new HashSet<MobileUnit>();
		units.addAll(allWorkers);
		units.addAll(allArmyUnits);
		units.addAll(destroyedUnits.stream().filter(u -> u instanceof MobileUnit).map(u -> (MobileUnit)u).collect(Collectors.toList()));
		
		return units;
	}
	
	/**
	 * Returns a list of all mobile units in range of the provided squad (any member of the squad).
	 * @param squad squad to check range against
	 * @return list of mobile units in range
	 */
	public List<MobileUnit> getMobileUnitsInRange(Squad<? extends PlayerUnit> squad) {
		
		List<MobileUnit> unitsInRange = new ArrayList<MobileUnit>();
		for (MobileUnit armyUnit : allArmyUnits) {
			if (armyUnit.exists()) {
				for (PlayerUnit squadUnit : squad) {
					int range = 0;
					if (squadUnit.isFlying() && armyUnit.getAirWeapon() != null) {
						range = armyUnit.getAirWeapon().maxRange() + 32;
					} else if (armyUnit.getGroundWeapon() != null) {
						range = Math.max(160, armyUnit.getGroundWeapon().maxRange() + 32);
					}
					if (armyUnit.getDistance(squadUnit) <= range) {
						unitsInRange.add(armyUnit);
						break;
					}
				}
			}
		}
		for (Worker workerUnit : allWorkers) {
			if (workerUnit.exists()) {
				for (PlayerUnit squadUnit : squad) {
					if (workerUnit.getDistance(squadUnit) <= 160) {
						unitsInRange.add(workerUnit);
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
		
		List<MobileUnit> allMobileUnits = new ArrayList<MobileUnit>();
		allMobileUnits.addAll(allArmyUnits);
		allMobileUnits.addAll(allWorkers);
		
		return allMobileUnits.parallelStream().filter(t -> position.getDistance(t.getPosition()) <= radius).collect(Collectors.toList());
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
	
	/**
	 * Returns a list of units (including buildings) within given radius from given unit.
	 * @param unit
	 * @param radius
	 * @return
	 */
	public List<PlayerUnit> getUnitsInRadius(Unit unit, int radius) {
		
		List<PlayerUnit> allUnits = new ArrayList<PlayerUnit>();
		allUnits.addAll(allArmyUnits);
		allUnits.addAll(allWorkers);
		allUnits.addAll(buildings);
		
		return unit.getUnitsInRadius(radius, allUnits);
	}
	
	// TODO For now just assume that mining workers are always available. later maybe give the developer a way to choose behavior.
	public Squad<Worker> getAvailableWorkers() {
		return miningWorkers;
	}

	public Group<PlayerUnit> getDestroyedUnits() {
		return this.destroyedUnits;
	}

	public Group<CommandCenter> getCommandCenters() {
		return this.commandCenters;
	}
	
	public Group<Barracks> getBarracks() {
		return this.barracks;
	}
	
	public void onUnitDestroy(bwapi.Unit bwUnit, int frameCount) {
		
		if (bwUnit.getType().equals(UnitType.Resource_Mineral_Field)) {
			for (MineralPatch patch : this.mineralPatches) {
				if (patch.getID() == bwUnit.getID()) {
					patch.update(frameCount);
					this.mineralPatches.remove(patch);
					break;
				}
			}
		} else {
			PlayerUnit unitToRemove = null;
			for (Group<? extends PlayerUnit> group : this.groups) {
				PlayerUnit unit = group.getValue(bwUnit.getID());
				if (unit != null) {
					group.remove(unit);
					unitToRemove = unit;
				}
			}
			if (unitToRemove != null) {
				if (unitToRemove.equals(this.main)) {
					this.main = null;
				}
				this.destroyedUnits.add(unitToRemove);
			}
		}
	}
	
	public boolean update(bwapi.Unit bwUnit, int frameCount) {
		
		if (bwUnit.getType().isWorker()) {
			for (Worker worker : this.allWorkers) {
				if (worker.getID() == bwUnit.getID()) {
					worker.update(frameCount);
					worker.update(bwUnit.getPosition(), bwUnit.getHitPoints());
					return true;
				}
			}
		} else if (bwUnit.getType().equals(UnitType.Resource_Mineral_Field)) {
			for (MineralPatch patch : this.mineralPatches) {
				if (patch.getID() == bwUnit.getID()) {
					patch.update(frameCount, bwUnit.getResources());
					return true;
				}
			}
		} else if (bwUnit.getType().equals(UnitType.Resource_Vespene_Geyser)) {
			for (Geyser geyser : this.geysers) {
				if (geyser.getID() == bwUnit.getID()) {
					geyser.update(frameCount, bwUnit.getResources());
					return true;
				}
			}
		} else if (bwUnit.getType().isBuilding()) {
			for (Building building : this.buildings) {
				if (building.getID() == bwUnit.getID()) {
					building.update(frameCount);
					building.update(bwUnit.getPosition(), bwUnit.getHitPoints());
					return true;
				}
			}
		} else {
			for (MobileUnit armyUnit : this.allArmyUnits) {
				if (armyUnit.getID() == bwUnit.getID()) {
					armyUnit.update(frameCount);
					armyUnit.update(bwUnit.getPosition(), bwUnit.getHitPoints());
					return true;
				}
			}
		}
		return false;
	}

	public int getSupplyUsed() {
		
		int supply = this.allWorkers.size();
		for (MobileUnit unit : this.allArmyUnits) {
			supply += unit.getSupplyRequired();
		}
		return supply;
	}

	public int getSupplyTotal() {
		
		int supply =  0;
		for (Building building : this.buildings) {
			if (building instanceof CommandCenter) {
				supply += 10;
			}
			if (building instanceof SupplyDepot) {
				supply += 8;
			}
		}
		return supply;
	}
}
