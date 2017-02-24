package org.openbw.tsbw;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;

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
	public static final TechType[] techTypes = new TechType[] {
			TechType.Stim_Packs,
		    TechType.Lockdown,
		    TechType.EMP_Shockwave,
		    TechType.Spider_Mines,
		    TechType.Scanner_Sweep,
		    TechType.Tank_Siege_Mode,
		    TechType.Defensive_Matrix,
		    TechType.Irradiate,
		    TechType.Yamato_Gun,
		    TechType.Cloaking_Field,
		    TechType.Personnel_Cloaking,
		    TechType.Restoration,
		    TechType.Optical_Flare,
		    TechType.Healing,
		    TechType.Nuclear_Strike,
		    TechType.Burrowing,
		    TechType.Infestation,
		    TechType.Spawn_Broodlings,
		    TechType.Dark_Swarm,
		    TechType.Plague,
		    TechType.Consume,
		    TechType.Ensnare,
		    TechType.Parasite,
		    TechType.Lurker_Aspect,
		    TechType.Psionic_Storm,
		    TechType.Hallucination,
		    TechType.Recall,
		    TechType.Stasis_Field,
		    TechType.Archon_Warp,
		    TechType.Disruption_Web,
		    TechType.Mind_Control,
		    TechType.Dark_Archon_Meld,
		    TechType.Feedback,
		    TechType.Maelstrom
	};
	
	public static final UpgradeType[] upgradeTypes = new UpgradeType[]{
			
		    UpgradeType.Terran_Infantry_Armor, 
		    UpgradeType.Terran_Vehicle_Plating, 
		    UpgradeType.Terran_Ship_Plating, 
		    UpgradeType.Terran_Infantry_Weapons, 
		    UpgradeType.Terran_Vehicle_Weapons, 
		    UpgradeType.Terran_Ship_Weapons, 
		    UpgradeType.U_238_Shells, 
		    UpgradeType.Ion_Thrusters, 
		    UpgradeType.Titan_Reactor, 
		    UpgradeType.Ocular_Implants, 
		    UpgradeType.Moebius_Reactor, 
		    UpgradeType.Apollo_Reactor, 
		    UpgradeType.Colossus_Reactor, 
		    UpgradeType.Caduceus_Reactor, 
		    UpgradeType.Charon_Boosters, 
		    UpgradeType.Zerg_Carapace, 
		    UpgradeType.Zerg_Flyer_Carapace, 
		    UpgradeType.Zerg_Melee_Attacks, 
		    UpgradeType.Zerg_Missile_Attacks, 
		    UpgradeType.Zerg_Flyer_Attacks, 
		    UpgradeType.Ventral_Sacs, 
		    UpgradeType.Antennae, 
		    UpgradeType.Metabolic_Boost, 
		    UpgradeType.Adrenal_Glands, 
		    UpgradeType.Muscular_Augments, 
		    UpgradeType.Grooved_Spines, 
		    UpgradeType.Gamete_Meiosis, 
		    UpgradeType.Metasynaptic_Node, 
		    UpgradeType.Chitinous_Plating, 
		    UpgradeType.Anabolic_Synthesis, 
		    UpgradeType.Protoss_Ground_Armor, 
		    UpgradeType.Protoss_Air_Armor, 
		    UpgradeType.Protoss_Ground_Weapons, 
		    UpgradeType.Protoss_Air_Weapons, 
		    UpgradeType.Protoss_Plasma_Shields, 
		    UpgradeType.Singularity_Charge, 
		    UpgradeType.Leg_Enhancements, 
		    UpgradeType.Scarab_Damage, 
		    UpgradeType.Reaver_Capacity, 
		    UpgradeType.Gravitic_Drive, 
		    UpgradeType.Sensor_Array, 
		    UpgradeType.Gravitic_Boosters, 
		    UpgradeType.Khaydarin_Amulet, 
		    UpgradeType.Apial_Sensors, 
		    UpgradeType.Gravitic_Thrusters, 
		    UpgradeType.Carrier_Capacity, 
		    UpgradeType.Khaydarin_Core, 
		    UpgradeType.Argus_Jewel, 
		    UpgradeType.Argus_Talisman, 
		    UpgradeType.Upgrade_60
	};
	
	public static final WeaponType[] weaponTypes = new WeaponType[]{
			WeaponType.Gauss_Rifle,
			WeaponType.Gauss_Rifle_Jim_Raynor,
		    WeaponType.C_10_Canister_Rifle,
		    WeaponType.C_10_Canister_Rifle_Sarah_Kerrigan,
		    WeaponType.Fragmentation_Grenade,
		    WeaponType.Fragmentation_Grenade_Jim_Raynor,
		    WeaponType.Spider_Mines,
		    WeaponType.Twin_Autocannons,
		    WeaponType.Hellfire_Missile_Pack,
		    WeaponType.Twin_Autocannons_Alan_Schezar,
		    WeaponType.Hellfire_Missile_Pack_Alan_Schezar,
		    WeaponType.Arclite_Cannon,
		    WeaponType.Arclite_Cannon_Edmund_Duke,
		    WeaponType.Fusion_Cutter,
		    WeaponType.Gemini_Missiles,
		    WeaponType.Burst_Lasers,
		    WeaponType.Gemini_Missiles_Tom_Kazansky,
		    WeaponType.Burst_Lasers_Tom_Kazansky,
		    WeaponType.ATS_Laser_Battery,
		    WeaponType.ATA_Laser_Battery,
		    WeaponType.ATS_Laser_Battery_Hero,
		    WeaponType.ATA_Laser_Battery_Hero,
		    WeaponType.ATS_Laser_Battery_Hyperion,
		    WeaponType.ATA_Laser_Battery_Hyperion,
		    WeaponType.Flame_Thrower,
		    WeaponType.Flame_Thrower_Gui_Montag,
		    WeaponType.Arclite_Shock_Cannon,
		    WeaponType.Arclite_Shock_Cannon_Edmund_Duke,
		    WeaponType.Longbolt_Missile,
		    WeaponType.Yamato_Gun,
		    WeaponType.Nuclear_Strike,
		    WeaponType.Lockdown,
		    WeaponType.EMP_Shockwave,
		    WeaponType.Irradiate,
		    WeaponType.Claws,
		    WeaponType.Claws_Devouring_One,
		    WeaponType.Claws_Infested_Kerrigan,
		    WeaponType.Needle_Spines,
		    WeaponType.Needle_Spines_Hunter_Killer,
		    WeaponType.Kaiser_Blades,
		    WeaponType.Kaiser_Blades_Torrasque,
		    WeaponType.Toxic_Spores,
		    WeaponType.Spines,
		    WeaponType.Acid_Spore,
		    WeaponType.Acid_Spore_Kukulza,
		    WeaponType.Glave_Wurm,
		    WeaponType.Glave_Wurm_Kukulza,
		    WeaponType.Seeker_Spores,
		    WeaponType.Subterranean_Tentacle,
		    WeaponType.Suicide_Infested_Terran,
		    WeaponType.Suicide_Scourge,
		    WeaponType.Parasite,
		    WeaponType.Spawn_Broodlings,
		    WeaponType.Ensnare,
		    WeaponType.Dark_Swarm,
		    WeaponType.Plague,
		    WeaponType.Consume,
		    WeaponType.Particle_Beam,
		    WeaponType.Psi_Blades,
		    WeaponType.Psi_Blades_Fenix,
		    WeaponType.Phase_Disruptor,
		    WeaponType.Phase_Disruptor_Fenix,
		    WeaponType.Psi_Assault,
		    WeaponType.Psionic_Shockwave,
		    WeaponType.Psionic_Shockwave_TZ_Archon,
		    WeaponType.Dual_Photon_Blasters,
		    WeaponType.Anti_Matter_Missiles,
		    WeaponType.Dual_Photon_Blasters_Mojo,
		    WeaponType.Anti_Matter_Missiles_Mojo,
		    WeaponType.Phase_Disruptor_Cannon,
		    WeaponType.Phase_Disruptor_Cannon_Danimoth,
		    WeaponType.Pulse_Cannon,
		    WeaponType.STS_Photon_Cannon,
		    WeaponType.STA_Photon_Cannon,
		    WeaponType.Scarab,
		    WeaponType.Stasis_Field,
		    WeaponType.Psionic_Storm,
		    WeaponType.Warp_Blades_Zeratul,
		    WeaponType.Warp_Blades_Hero,
		    WeaponType.Independant_Laser_Battery,
		    WeaponType.Twin_Autocannons_Floor_Trap,
		    WeaponType.Hellfire_Missile_Pack_Wall_Trap,
		    WeaponType.Flame_Thrower_Wall_Trap,
		    WeaponType.Hellfire_Missile_Pack_Floor_Trap,
		    WeaponType.Neutron_Flare,
		    WeaponType.Disruption_Web,
		    WeaponType.Restoration,
		    WeaponType.Halo_Rockets,
		    WeaponType.Corrosive_Acid,
		    WeaponType.Mind_Control,
		    WeaponType.Feedback,
		    WeaponType.Optical_Flare,
		    WeaponType.Maelstrom,
		    WeaponType.Subterranean_Spines,
		    WeaponType.Warp_Blades,
		    WeaponType.C_10_Canister_Rifle_Samir_Duran,
		    WeaponType.C_10_Canister_Rifle_Infested_Duran,
		    WeaponType.Dual_Photon_Blasters_Artanis,
		    WeaponType.Anti_Matter_Missiles_Artanis,
		    WeaponType.C_10_Canister_Rifle_Alexei_Stukov
	};
}