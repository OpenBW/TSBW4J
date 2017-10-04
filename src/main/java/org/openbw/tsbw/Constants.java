package org.openbw.tsbw;

import org.openbw.bwapi4j.type.UnitType;

public class Constants {

	/**
	 * The SCV estimated average speed considering it will mostly not travel at top speed.
	 * Used to calculate estimated mining during travel.
	 */
	public static final double AVERAGE_SCV_SPEED = 4.5;
	
	/**
	 * The SCV acceleration in pixels per frame per frame
	 */
	public static final double SCV_ACCELERATION = 0.25;
	
	/**
	 * The duration of a marine attack in frames. This is part of the cooldown.
	 * That is, the marine shoots for x frames, then rests for (cooldown - x) frames.
	 */
	public static final int MARINE_ATTACK_ANIMATION_DURATION = 9;
	/**
	 * Time the SCV spends at the mineral patch mining, before it obtains a cargo.
	 * It's actually a random value between 77 and 82
	 */
	public static final double MINING_TIME = 80.0;
	
	/**
	 * Number of minerals a worker returns per trip.
	 */
	public static final double MINERALS_PER_ROUNDTRIP = 8.0;
	
	/**
	 * The amount of minerals a player starts with.
	 */
	public static final int INCOME_0 = 50;
	
	/**
	 * Viewport width in tiles.
	 */
	public static final int SCREEN_WIDTH = 20;
	
	/**
	 * Viewport height in tiles.
	 */
	public static final int SCREEN_HEIGHT = 12;
	
	public static UnitType[] zergTypes = new UnitType[]{
			UnitType.Zerg_Larva,
		    UnitType.Zerg_Egg,
		    UnitType.Zerg_Zergling,
		    UnitType.Zerg_Hydralisk,
		    UnitType.Zerg_Ultralisk,
		    UnitType.Zerg_Broodling,
		    UnitType.Zerg_Drone,
		    UnitType.Zerg_Overlord,
		    UnitType.Zerg_Mutalisk,
		    UnitType.Zerg_Guardian,
		    UnitType.Zerg_Queen,
		    UnitType.Zerg_Defiler,
		    UnitType.Zerg_Scourge,
		    UnitType.Zerg_Infested_Terran,
		    UnitType.Zerg_Cocoon,
		    UnitType.Zerg_Devourer,
		    UnitType.Zerg_Lurker_Egg,
		    UnitType.Zerg_Lurker,
		    UnitType.Zerg_Infested_Command_Center,
		    UnitType.Zerg_Hatchery,
		    UnitType.Zerg_Lair,
		    UnitType.Zerg_Hive,
		    UnitType.Zerg_Nydus_Canal,
		    UnitType.Zerg_Hydralisk_Den,
		    UnitType.Zerg_Defiler_Mound,
		    UnitType.Zerg_Greater_Spire,
		    UnitType.Zerg_Queens_Nest,
		    UnitType.Zerg_Evolution_Chamber,
		    UnitType.Zerg_Ultralisk_Cavern,
		    UnitType.Zerg_Spire,
		    UnitType.Zerg_Spawning_Pool,
		    UnitType.Zerg_Creep_Colony,
		    UnitType.Zerg_Spore_Colony,
		    UnitType.Zerg_Sunken_Colony,
		    UnitType.Zerg_Extractor};
	
	public static UnitType[] protossTypes = {
			UnitType.Protoss_Corsair,
		    UnitType.Protoss_Dark_Templar,
		    UnitType.Protoss_Dark_Archon,
		    UnitType.Protoss_Probe,
		    UnitType.Protoss_Zealot,
		    UnitType.Protoss_Dragoon,
		    UnitType.Protoss_High_Templar,
		    UnitType.Protoss_Archon,
		    UnitType.Protoss_Shuttle,
		    UnitType.Protoss_Scout,
		    UnitType.Protoss_Arbiter,
		    UnitType.Protoss_Carrier,
		    UnitType.Protoss_Interceptor,
		    UnitType.Protoss_Reaver,
		    UnitType.Protoss_Observer,
		    UnitType.Protoss_Scarab,
		    UnitType.Protoss_Nexus,
		    UnitType.Protoss_Robotics_Facility,
		    UnitType.Protoss_Pylon,
		    UnitType.Protoss_Assimilator,
		    UnitType.Protoss_Observatory,
		    UnitType.Protoss_Gateway,
		    UnitType.Protoss_Photon_Cannon,
		    UnitType.Protoss_Citadel_of_Adun,
		    UnitType.Protoss_Cybernetics_Core,
		    UnitType.Protoss_Templar_Archives,
		    UnitType.Protoss_Forge,
		    UnitType.Protoss_Stargate,
		    UnitType.Protoss_Fleet_Beacon,
		    UnitType.Protoss_Arbiter_Tribunal,
		    UnitType.Protoss_Robotics_Support_Bay,
		    UnitType.Protoss_Shield_Battery
	};
	
	public static UnitType[] terranTypes = {
			UnitType.Terran_Marine,
		    UnitType.Terran_Ghost,
		    UnitType.Terran_Vulture,
		    UnitType.Terran_Goliath,
		    UnitType.Terran_Siege_Tank_Tank_Mode,
		    UnitType.Terran_SCV,
		    UnitType.Terran_Wraith,
		    UnitType.Terran_Science_Vessel,
		    UnitType.Terran_Dropship,
		    UnitType.Terran_Battlecruiser,
		    UnitType.Terran_Vulture_Spider_Mine,
		    UnitType.Terran_Nuclear_Missile,
		    UnitType.Terran_Siege_Tank_Siege_Mode,
		    UnitType.Terran_Firebat,
		    UnitType.Terran_Medic,
		    UnitType.Terran_Valkyrie,
		    UnitType.Terran_Command_Center,
		    UnitType.Terran_Comsat_Station,
		    UnitType.Terran_Nuclear_Silo,
		    UnitType.Terran_Supply_Depot,
		    UnitType.Terran_Refinery,
		    UnitType.Terran_Barracks,
		    UnitType.Terran_Academy,
		    UnitType.Terran_Factory,
		    UnitType.Terran_Starport,
		    UnitType.Terran_Control_Tower,
		    UnitType.Terran_Science_Facility,
		    UnitType.Terran_Covert_Ops,
		    UnitType.Terran_Physics_Lab,
		    UnitType.Terran_Machine_Shop,
		    UnitType.Terran_Engineering_Bay,
		    UnitType.Terran_Armory,
		    UnitType.Terran_Missile_Turret,
		    UnitType.Terran_Bunker
	};
}