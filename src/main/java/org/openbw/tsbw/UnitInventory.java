package org.openbw.tsbw;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Starport;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;
import org.openbw.tsbw.unit.VespeneGeyser;

public class UnitInventory {

	private static final Logger logger = LogManager.getLogger();
	
	private Group<MineralPatch> mineralPatches;
	private Group<VespeneGeyser> vespeneGeysers;
	private Group<CommandCenter> commandCenters;
	private Group<GasMiningFacility> refineries;
	private Group<Barracks> barracks;
	private Group<Factory> factories;
	private Group<Starport> starports;
	private Group<Building> buildings;
	private Group<Building> constructions;
	private Collection<PlayerUnit> allUnits;
	
	private Group<PlayerUnit> destroyedUnits;
	private Squad<MobileUnit> scouts;
	
	private Squad<SCV> workers;
	private Squad<MobileUnit> armyUnits;
	
	private Collection<Bullet> bullets;
	private MapAnalyzer mapAnalyzer;
	
	private int observedMineralsSpent;
	private int observedGasSpent;
	
	public UnitInventory() {
		
		this.mineralPatches = new Group<>();
		this.vespeneGeysers = new Group<>();
		this.commandCenters = new Group<>();
		this.refineries = new Group<>();
		this.barracks = new Group<>();
		this.factories = new Group<>();
		this.starports = new Group<>();
		this.buildings = new Group<>();
		this.constructions = new Group<>();
		this.allUnits = ConcurrentHashMap.newKeySet();
		this.destroyedUnits = new Group<>();
		this.scouts = new Squad<>();
		this.workers = new Squad<>();
		this.armyUnits = new Squad<>();
		this.bullets = ConcurrentHashMap.newKeySet();
	}

	public void initialize(Collection<Bullet> bullets, MapAnalyzer mapAnalyzer) {
		
		this.observedGasSpent = 0;
		this.observedMineralsSpent = -600;
		this.bullets = bullets;
		this.mapAnalyzer = mapAnalyzer;
		this.mineralPatches.clear();
		this.vespeneGeysers.clear();
		this.commandCenters.clear();
		this.refineries.clear();
		this.barracks.clear();
		this.factories.clear();
		this.starports.clear();
		this.buildings.clear();
		this.constructions.clear();
		this.allUnits.clear();
		this.destroyedUnits.clear();
		this.scouts.clear();
		this.workers.clear();
		this.armyUnits.clear();
	}
	
	public void register(Unit unit) {
		
		boolean addedPlayerUnit = false;
		
		if (unit instanceof VespeneGeyser) {
			
			this.vespeneGeysers.add((VespeneGeyser)unit);
		} else if (unit instanceof MineralPatch) {
			
			MineralPatch patch = (MineralPatch)unit;
			boolean success = this.mineralPatches.add(patch);
			if (success) {
				patch.initialize(this.mapAnalyzer, this.mineralPatches);
			}
		} else {
			
			if (unit instanceof Building) {
			
				Building building = (Building) unit;
				if (building.isCompleted()) {
					
					if (building instanceof CommandCenter) {
						
						this.commandCenters.add((CommandCenter)building);
					} else if (building instanceof GasMiningFacility) {
											
						this.refineries.add((GasMiningFacility)building);
					} else if (building instanceof Barracks) {
						
						this.barracks.add((Barracks)building);
					} else if (building instanceof Factory) {
						
						this.factories.add((Factory)building);
					} else if (building instanceof Starport) {
						
						this.starports.add((Starport)building);
					}
					this.constructions.remove(building);
					this.buildings.add(building);
					addedPlayerUnit = this.allUnits.add(building);
					
				} else {
					
					this.constructions.add(building);
				}
			} else {
				
				if (unit instanceof PlayerUnit) {
					
					if (unit instanceof SCV) {
						
						this.workers.add((SCV) unit);
					} else if (unit instanceof MobileUnit) {
						
						this.armyUnits.add((MobileUnit)unit);
					}
					addedPlayerUnit = this.allUnits.add((PlayerUnit)unit);
					
				} else {
					
					// TODO e.g. critter
				}
			}
		}
		
		if (addedPlayerUnit) {
			
			this.observedMineralsSpent += ((PlayerUnit) unit).getMineralPrice();
			this.observedGasSpent += ((PlayerUnit) unit).getGasPrice();
		}
	}
	
	public void unregister(Unit unit) {
		
		logger.trace("registering {}.", unit);
		if (unit instanceof VespeneGeyser) {
			
			this.vespeneGeysers.destroy((VespeneGeyser)unit);
		} else if (unit instanceof MineralPatch) {
			
			this.mineralPatches.destroy((MineralPatch)unit);
		} else {
			
			if (unit instanceof Building) {
			
				Building building = (Building) unit;
				if (building.isCompleted()) {
					
					if (building instanceof CommandCenter) {
						
						this.commandCenters.destroy((CommandCenter)building);
					} else if (building instanceof GasMiningFacility) {
											
						this.refineries.destroy((GasMiningFacility)building);
					} else if (building instanceof Barracks) {
						
						this.barracks.destroy((Barracks)building);
					} else if (building instanceof Factory) {
						
						this.factories.destroy((Factory)building);
					} else if (building instanceof Starport) {
						
						this.starports.destroy((Starport)building);
					}
					this.buildings.destroy(building);
					this.allUnits.remove(building);
				} else {
					
					this.constructions.destroy(building);
				}
			} else {
				
				if (unit instanceof SCV) {
					
					this.workers.destroy((SCV)unit);
				} else if (unit instanceof MobileUnit) {
					
					this.armyUnits.destroy((MobileUnit)unit);
				}
				this.allUnits.remove((PlayerUnit)unit);
			}
			this.destroyedUnits.add((PlayerUnit)unit);
		}
	}
	
	public Group<MineralPatch> getMineralPatches() {
		
		return this.mineralPatches;
	}

	public Group<VespeneGeyser> getVespeneGeysers() {
		
		return this.vespeneGeysers;
	}

	public Group<CommandCenter> getCommandCenters() {
		
		return this.commandCenters;
	}

	public Group<GasMiningFacility> getRefineries() {
		return this.refineries;
	}
	
	public Group<Barracks> getBarracks() {
		
		return this.barracks;
	}

	public Group<Factory> getFactories() {
		
		return this.factories;
	}

	public Group<Starport> getStarports() {
		return this.starports;
	}

	public Group<Building> getBuildings() {
		
		return this.buildings;
	}

	public Group<Building> getConstructions() {
		
		return this.constructions;
	}

	public Collection<PlayerUnit> getAllUnits() {
		
		return this.allUnits;
	}

	public Group<PlayerUnit> getDestroyedUnits() {
		
		return this.destroyedUnits;
	}

	public Squad<MobileUnit> getScouts() {
		
		return this.scouts;
	}

	public Squad<SCV> getWorkers() {
		
		return this.workers;
	}

	public Squad<MobileUnit> getArmyUnits() {
		
		return this.armyUnits;
	}

	public Collection<Bullet> getBullets() {
		
		return this.bullets;
	}
	
	public CommandCenter getMain() {
		
		if (this.commandCenters.isEmpty()) {
			return null;
		}
		return this.commandCenters.first();
	}
	
	public SCV getAvailableWorker() {
		
		return this.workers.stream().filter(w -> w.isAvailable()).findFirst().orElse(null);
	}
	
	public Stream<SCV> getAvailableWorkers() {
		
		return this.workers.stream().filter(w -> w.isAvailable());
	}
	
	public int getObservedMineralsSpent() {
		
		return this.observedMineralsSpent + this.constructions.stream().mapToInt(b -> b.getMineralPrice()).sum();
	}
	
	public int getObservedGasSpent() {
		
		return this.observedGasSpent + this.constructions.stream().mapToInt(b -> b.getGasPrice()).sum();
	}
}