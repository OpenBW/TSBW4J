package org.openbw.tsbw.building_old;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.UnitInventory;


public enum Construction implements Mechanical {

	Terran_Command_Center(UnitType.Terran_Command_Center),
	Terran_Supply_Depot(UnitType.Terran_Supply_Depot),
	Terran_Refinery(UnitType.Terran_Refinery),
	Terran_Barracks(UnitType.Terran_Barracks),
	Terran_Academy(UnitType.Terran_Academy),
	Terran_Factory(UnitType.Terran_Factory),
	Terran_Starport(UnitType.Terran_Starport),
	Terran_Science_Facility(UnitType.Terran_Science_Facility),
	Terran_Engineering_Bay(UnitType.Terran_Engineering_Bay),
	Terran_Armory(UnitType.Terran_Armory),
	Terran_Missile_Turret(UnitType.Terran_Missile_Turret),
	Terran_Bunker(UnitType.Terran_Bunker),
	Zerg_Hatchery(UnitType.Zerg_Hatchery),
	Zerg_Nydus_Canal(UnitType.Zerg_Nydus_Canal),
	Zerg_Hydralisk_Den(UnitType.Zerg_Hydralisk_Den),
	Zerg_Defiler_Mound(UnitType.Zerg_Defiler_Mound),
	Zerg_Queens_Nest(UnitType.Zerg_Queens_Nest),
	Zerg_Evolution_Chamber(UnitType.Zerg_Evolution_Chamber),
	Zerg_Ultralisk_Cavern(UnitType.Zerg_Ultralisk_Cavern),
	Zerg_Spire(UnitType.Zerg_Spire),
	Zerg_Spawning_Pool(UnitType.Zerg_Spawning_Pool),
	Zerg_Creep_Colony(UnitType.Zerg_Creep_Colony),
	Zerg_Spore_Colony(UnitType.Zerg_Spore_Colony),
	Zerg_Sunken_Colony(UnitType.Zerg_Sunken_Colony),
	Zerg_Extractor(UnitType.Zerg_Extractor),
	Protoss_Nexus(UnitType.Protoss_Nexus),
	Protoss_Robotics_Facility(UnitType.Protoss_Robotics_Facility),
	Protoss_Pylon(UnitType.Protoss_Pylon),
	Protoss_Assimilator(UnitType.Protoss_Assimilator),
	Protoss_Observatory(UnitType.Protoss_Observatory),
	Protoss_Gateway(UnitType.Protoss_Gateway),
	Protoss_Photon_Cannon(UnitType.Protoss_Photon_Cannon),
	Protoss_Citadel_of_Adun(UnitType.Protoss_Citadel_of_Adun),
	Protoss_Cybernetics_Core(UnitType.Protoss_Cybernetics_Core),
	Protoss_Templar_Archives(UnitType.Protoss_Templar_Archives),
	Protoss_Forge(UnitType.Protoss_Forge),
	Protoss_Stargate(UnitType.Protoss_Stargate),
	Protoss_Fleet_Beacon(UnitType.Protoss_Fleet_Beacon),
	Protoss_Arbiter_Tribunal(UnitType.Protoss_Arbiter_Tribunal),
	Protoss_Robotics_Support_Bay(UnitType.Protoss_Robotics_Support_Bay),
	Protoss_Shield_Battery(UnitType.Protoss_Shield_Battery);

	private UnitType unitType;
	private DefaultConstruction constructionProvider;

	private Construction(UnitType type) {
		this.unitType = type;
	}
	
	public UnitType getType() {
		return this.unitType;
	}
	
	public void setConstructionProvider(DefaultConstruction constructionProvider) {
		this.constructionProvider = constructionProvider;
	}
	
	public TilePosition getBuildTile(SCV builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		return constructionProvider.getBuildTile(builder, unitInventory, projects);
	}
	
	public TilePosition getBuildTile(SCV builder, TilePosition aroundHere, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		return this.constructionProvider.getBuildTile(builder, aroundHere, unitInventory, projects);
	}
	
	public int getMineralPrice() {
		return this.unitType.mineralPrice();
	}
	
	public int getGasPrice() {
		return this.unitType.gasPrice();
	}
	
	public int tileHeight() {
		return this.unitType.tileHeight();
	}
	
	public int tileWidth() {
		return this.unitType.tileWidth();
	}
}
