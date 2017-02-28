package org.openbw.tsbw;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.bwapi.Player;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.MiningStrategy;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.strategy.StrategyFactory;
import org.openbw.tsbw.unit.Academy;
import org.openbw.tsbw.unit.Armory;
import org.openbw.tsbw.unit.Barracks;
import org.openbw.tsbw.unit.Battlecruiser;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.Bunker;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.Dropship;
import org.openbw.tsbw.unit.EngineeringBay;
import org.openbw.tsbw.unit.Factory;
import org.openbw.tsbw.unit.Firebat;
import org.openbw.tsbw.unit.Geyser;
import org.openbw.tsbw.unit.Ghost;
import org.openbw.tsbw.unit.Goliath;
import org.openbw.tsbw.unit.Hatchery;
import org.openbw.tsbw.unit.Marine;
import org.openbw.tsbw.unit.Medic;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.MissileTurret;
import org.openbw.tsbw.unit.MobileUnit;
import org.openbw.tsbw.unit.Nexus;
import org.openbw.tsbw.unit.PhotonCannon;
import org.openbw.tsbw.unit.Refinery;
import org.openbw.tsbw.unit.ScienceFacility;
import org.openbw.tsbw.unit.ScienceVessel;
import org.openbw.tsbw.unit.SiegeTank;
import org.openbw.tsbw.unit.Starport;
import org.openbw.tsbw.unit.SunkenColony;
import org.openbw.tsbw.unit.SupplyDepot;
import org.openbw.tsbw.unit.UnitFactory;
import org.openbw.tsbw.unit.Valkyrie;
import org.openbw.tsbw.unit.Vulture;
import org.openbw.tsbw.unit.Worker;
import org.openbw.tsbw.unit.Wraith;

import bwapi.Game;
import bwapi.Key;
import bwapi.Mirror;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public abstract class Bot {

	private static final Logger logger = LogManager.getLogger();

	private Mirror mirror;
	private BotEventListener eventListener;
	
	protected Player player1;
	protected Player player2;
	
	protected UnitInventory unitInventory1;
	protected UnitInventory unitInventory2;
	protected BWMap bwMap;
	protected MapDrawer mapDrawer;
	protected InteractionHandler interactionHandler;
	protected DamageEvaluator damageEvaluator;
	protected BuildingPlanner buildingPlanner;
	
	protected MiningFactory miningFactory;
	protected MiningStrategy miningStrategy;
	
	protected ScoutingFactory scoutingFactory;
	protected ScoutingStrategy scoutingStrategy;
	
	protected StrategyFactory strategyFactory;
	protected AbstractGameStrategy gameStrategy;
	
	protected boolean scoutingEnabled = true;
	protected boolean cleanLogging = false;
	protected boolean gameStarted = false;
	
	public final void run() {
		
		logger.trace("executing run().");
		this.mirror = new Mirror();
		this.mirror.getModule().setEventListener(this.eventListener);
		logger.debug("starting game...");
		this.mirror.startGame();
	}
	
	public Bot(MiningFactory miningFactory, ScoutingFactory scoutingFactory, StrategyFactory strategyFactory) {
		
		this.miningFactory = miningFactory;
		this.scoutingFactory = scoutingFactory;
		this.strategyFactory = strategyFactory;
		
		this.eventListener = new BotEventListener(this);
		
		this.unitInventory1 = new UnitInventory();
		this.unitInventory2 = new UnitInventory();
		
		this.player1 = new Player(this.unitInventory1);
		this.player2 = new Player(this.unitInventory2);
		
		this.bwMap = new BWMap();
		this.damageEvaluator = new DamageEvaluator();
		this.interactionHandler = new InteractionHandler();
		this.mapDrawer = new MapDrawer(false);

		this.buildingPlanner = new BuildingPlanner(unitInventory1, interactionHandler, bwMap);
	}
	
	public abstract void onStart();
	
	/* default */ final void internalOnStart() {
		
		logger.info("--- starting game - {}", new Date());
		logger.debug("CWD: {}", System.getProperty("user.dir"));
		
		this.gameStarted = false;
		Game game = mirror.getGame();
		
		this.player1.initialize(game.self());
		this.player2.initialize(game.enemy());
		
		this.mapDrawer.initialize(game);
		this.damageEvaluator.initialize(game);
		this.bwMap.initialize(game);
		this.interactionHandler.initialize(game);
		logger.info("playing on {} (hash: {})", this.bwMap.mapFileName(), this.bwMap.mapHash());
		
		this.unitInventory1.initialize();
		this.unitInventory2.initialize();
		
		this.buildingPlanner.initialize();
		
		this.scoutingStrategy = this.scoutingFactory.getStrategy(bwMap, mapDrawer);
		this.miningStrategy = this.miningFactory.getStrategy(mapDrawer, interactionHandler);
		this.gameStrategy = strategyFactory.getStrategy(mapDrawer, bwMap, scoutingStrategy, player1, 
				player2, buildingPlanner, damageEvaluator);
		
		this.scoutingStrategy.initialize(unitInventory1.getScouts(), unitInventory1);
		this.gameStrategy.initialize();

		logger.info("latency: {} ({}). latency compensation: {}", game.getLatency(), game.getLatencyFrames(), game.isLatComEnabled());
	
		for (bwapi.Unit mineralPatch : game.getStaticMinerals()) {
			this.addToInventory(mineralPatch, unitInventory1, 0);
			this.addToInventory(mineralPatch, unitInventory2, 0);
		}
		for (bwapi.Unit geyser : game.getStaticGeysers()) {
			this.addToInventory(geyser, unitInventory1, 0);
			this.addToInventory(geyser, unitInventory2, 0);
		}
		
		game.setTextSize(bwapi.Text.Size.Enum.Default);
		this.onStart();
	}
	
	public void onEnd(boolean isWinner) {
		
		logger.info("--- ending game - {}.", (isWinner? "WIN": "LOSS"));
	}
	
	public void onFrame() {
		
		int frameCount = interactionHandler.getFrameCount();
		
		if (!gameStarted || frameCount < 1) {
			return;
		}
		
		miningStrategy.run(frameCount);
		
		/*
		 * Do every 5 frames (just for performance reasons)
		 */
		if (frameCount % 5 == 0) {
			
			if (scoutingEnabled) {
				scoutingStrategy.run(frameCount);
			}
			
			buildingPlanner.run(player1.minerals(), player1.gas(), frameCount);
			
			// some simple interaction: enable global map drawing or change logging output
			if (interactionHandler.getKeyState(Key.K_CONTROL) && interactionHandler.getKeyState(Key.K_T)) {
				mapDrawer.setEnabled(!mapDrawer.isEnabled());
				interactionHandler.sendText("map drawing enabled: " + mapDrawer.isEnabled());
			} else if (interactionHandler.getKeyState(Key.K_CONTROL) && interactionHandler.getKeyState(Key.K_R)) {
				toggleCleanLogging();
			}
		}
		
		int availableMinerals = player1.minerals() - buildingPlanner.getQueuedMinerals();
		int availableGas = player1.gas() - buildingPlanner.getQueuedGas();
		int availableSupply = player1.supplyTotal() - player1.supplyUsed();
		
		gameStrategy.run(frameCount, availableMinerals, availableGas, availableSupply);
		
		drawGameInfo();
	}
	
	private void drawGameInfo() {
		
		mapDrawer.drawTextScreen(450, 25, "game time: " + interactionHandler.getFrameCount());
		mapDrawer.drawTextScreen(530, 35, "FPS: " + interactionHandler.getFPS());
	}

	private void toggleCleanLogging() {
		
		this.cleanLogging = !cleanLogging;
		interactionHandler.sendText("clean logging: " + cleanLogging);
		String appenderToAdd = cleanLogging ? "Clean" : "Console";
		String appenderToRemove = cleanLogging ? "Console" : "Clean";
		
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        
        for (org.apache.logging.log4j.core.Logger logger : ctx.getLoggers()) {
        	
        	logger.removeAppender(config.getAppender(appenderToRemove));
        	config.addLoggerAppender(logger, config.getAppender(appenderToAdd));
        }
        ctx.updateLoggers();
		
	}
	
	public void onSendText(String text) {
		// do nothing
		
	}

	public void onReceiveText(bwapi.Player player, String text) {
		// do nothing
		
	}

	public void onPlayerLeft(bwapi.Player player) {
		// do nothing
	}
	

	public void onNukeDetect(Position target) {
		// do nothing
	}
	
	private void addToInventory(Unit bwUnit, UnitInventory inventory, int timeSpotted) {
		
		UnitType type = bwUnit.getType();
		boolean exists = inventory.update(bwUnit, timeSpotted);
		logger.trace("adding {} {}. exists: {}", type, bwUnit.getID(), exists);
		
		if (!exists) {
			try {
				if (type.equals(UnitType.Resource_Vespene_Geyser)) {
					
					inventory.register(UnitFactory.create(Geyser.class, bwUnit, this.bwMap));
				} else if (type.equals(UnitType.Resource_Mineral_Field)) {
					
					inventory.register(UnitFactory.create(MineralPatch.class, bwUnit, this.bwMap));
				} else if (type.isRefinery()) {
					
					inventory.register(UnitFactory.create(Refinery.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Academy)) {
					
					inventory.register(UnitFactory.create(Academy.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Armory)) {
					
					inventory.register(UnitFactory.create(Armory.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Barracks)) {
					
					inventory.register(UnitFactory.create(Barracks.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Battlecruiser)) {
					
					inventory.register(UnitFactory.create(Battlecruiser.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Bunker)) {
					
					inventory.register(UnitFactory.create(Bunker.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Command_Center)) {
					
					inventory.register(UnitFactory.create(CommandCenter.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Dropship)) {
					
					inventory.register(UnitFactory.create(Dropship.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Engineering_Bay)) {
					
					inventory.register(UnitFactory.create(EngineeringBay.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Factory)) {
					
					inventory.register(UnitFactory.create(Factory.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Firebat)) {
					
					inventory.register(UnitFactory.create(Firebat.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Ghost)) {
					
					inventory.register(UnitFactory.create(Ghost.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Goliath)) {
					
					inventory.register(UnitFactory.create(Goliath.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Marine)) {
					
					inventory.register(UnitFactory.create(Marine.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Medic)) {
					
					inventory.register(UnitFactory.create(Medic.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Missile_Turret)) {
					
					inventory.register(UnitFactory.create(MissileTurret.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Science_Facility)) {
					
					inventory.register(UnitFactory.create(ScienceFacility.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Science_Vessel)) {
					
					inventory.register(UnitFactory.create(ScienceVessel.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Siege_Tank_Tank_Mode) || type.equals(UnitType.Terran_Siege_Tank_Siege_Mode)) {
					
					inventory.register(UnitFactory.create(SiegeTank.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Starport)) {
					
					inventory.register(UnitFactory.create(Starport.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Supply_Depot)) {
					
					inventory.register(UnitFactory.create(SupplyDepot.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Valkyrie)) {
					
					inventory.register(UnitFactory.create(Valkyrie.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Vulture)) {
					
					inventory.register(UnitFactory.create(Vulture.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Terran_Wraith)) {
					
					inventory.register(UnitFactory.create(Wraith.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Zerg_Hatchery)) {
					
					inventory.register(UnitFactory.create(Hatchery.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Protoss_Nexus)) {
					
					inventory.register(UnitFactory.create(Nexus.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Protoss_Photon_Cannon)) {
					
					inventory.register(UnitFactory.create(PhotonCannon.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.equals(UnitType.Zerg_Sunken_Colony)) {
					
					inventory.register(UnitFactory.create(SunkenColony.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.isBuilding()) {
					
					inventory.register(UnitFactory.create(Building.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (type.isWorker()) {
					
					inventory.register(UnitFactory.create(Worker.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				} else if (!bwUnit.getPlayer().isNeutral()) {
					
					inventory.register(UnitFactory.create(MobileUnit.class, damageEvaluator, bwMap, bwUnit, timeSpotted));
				}
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {

				logger.fatal("Could not create unit: " + e.getMessage(), e);
				System.exit(1);
			}
		}
	}
	
	/* default */ final void onUnitDiscover(Unit bwUnit) {
		
		logger.debug("onDiscover: discovered {} with ID {}", bwUnit.getType(), bwUnit.getID());

		if (bwUnit.getPlayer().getID() == player2.getID()) {
			addToInventory(bwUnit, unitInventory2, interactionHandler.getFrameCount());
		}
	}
	
	public void onUnitEvade(Unit unit) {
		// do nothing
		
	}

	public void onUnitShow(Unit unit) {
		// do nothing
		
	}

	public void onUnitHide(Unit unit) {
		// do nothing
		
	}

	/* default */  final void onUnitCreate(Unit bwUnit) {
		
		logger.debug("onCreate: New {} unit created ", bwUnit.getType());
		if (bwUnit.getPlayer().getID() == player1.getID()) {
			
			if (bwUnit.getType().isBuilding()) {
				
				this.addToInventory(bwUnit, unitInventory1, interactionHandler.getFrameCount());
				if (bwUnit.getBuildUnit() != null) {
					Worker worker = unitInventory1.getAllWorkers().getValue(bwUnit.getBuildUnit().getID());
					buildingPlanner.onConstructionStarted(worker);
				}
			}
		}
	}
	
	/* default */  final void onUnitDestroy(Unit bwUnit) {
		
		logger.debug("destroyed {} with ID {}", bwUnit.getType(), bwUnit.getID());

		if (bwUnit.getPlayer().getID() == player1.getID()) {
			
			onUnitDestroy(bwUnit, unitInventory1);
		} else if (bwUnit.getPlayer().getID() == player2.getID()) {
			
			onUnitDestroy(bwUnit, unitInventory2);
		} else if (bwUnit.getType().equals(UnitType.Resource_Mineral_Field)) {
			
			onUnitDestroy(bwUnit, unitInventory1);
			onUnitDestroy(bwUnit, unitInventory2);
		}
	}
	
	private void onUnitDestroy(Unit bwUnit, UnitInventory unitInventory) {
		
		unitInventory.onUnitDestroy(bwUnit, interactionHandler.getFrameCount());
		
	}
	
	public void onUnitMorph(Unit unit) {
		// do nothing
	}
	
	/* default */ final void internalOnUnitMorph(Unit unit) {
		
		if (unit.getType().isRefinery()) {
			onUnitComplete(unit);
		} else {
			onUnitMorph(unit); // TODO remove old unit from inventory, add new unit to inventory
		}
	}

	public void onUnitRenegade(Unit unit) {
		// do nothing
		
	}

	public void onSaveGame(String gameName) {
		// do nothing
	}
	
	/* default */  final void onUnitComplete(Unit bwUnit) {
		
		logger.debug("completed {} with ID {}", bwUnit.getType(), bwUnit.getID());
		
		if (bwUnit.getPlayer().getID() == player1.getID()) {
			addToInventory(bwUnit, unitInventory1, interactionHandler.getFrameCount());
		}
		
		// Once the initial 4 workers and the command centers have fired their triggers we truly start the game
		if (!gameStarted && unitInventory1.getAllWorkers().size() == 4 && !unitInventory1.getCommandCenters().isEmpty()) {
			
			unitInventory1.getMiningWorkers().addAll(unitInventory1.getAllWorkers());
			miningStrategy.initialize(unitInventory1.getCommandCenters(), unitInventory1.getMiningWorkers(), unitInventory1.getMineralPatches());
			gameStrategy.start(player1.minerals(), player1.gas());
			gameStarted = true;
		}
	}
	
	public void onPlayerDropped(bwapi.Player player) {
		// do nothing
	}
}