package org.openbw.tsbw;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Key;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.building.BarracksConstruction;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.CommandCenterConstruction;
import org.openbw.tsbw.building.ConstructionType;
import org.openbw.tsbw.building.FactoryConstruction;
import org.openbw.tsbw.building.RefineryConstruction;
import org.openbw.tsbw.building.SupplyDepotConstruction;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.MiningStrategy;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.strategy.StrategyFactory;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.UnitFactory;
import org.openbw.tsbw.unit.VespeneGeyser;

import bwta.BWTA;

public abstract class Bot {

	private static final Logger logger = LogManager.getLogger();

	private BotEventListener eventListener;
	private BW bw;
	
	protected Player player1;
	protected Player player2;
	
	protected Map<Player, UnitInventory> unitInventories;
	protected MapAnalyzer mapAnalyzer;
	protected MapDrawer mapDrawer;
	protected InteractionHandler interactionHandler;
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
		this.bw = new BW(this.eventListener);
		this.bw.setUnitFactory(new UnitFactory(this.bw));
		logger.debug("starting game...");
		bw.startGame();
	}
	
	public Bot(MiningFactory miningFactory, ScoutingFactory scoutingFactory, StrategyFactory strategyFactory) {
		
		this.miningFactory = miningFactory;
		this.scoutingFactory = scoutingFactory;
		this.strategyFactory = strategyFactory;
		
		this.eventListener = new BotEventListener(this);
		
		this.unitInventories = new HashMap<Player, UnitInventory>();
	}
	
	public abstract void onStart();
	
	/* default */ final void internalOnStart() {
		
		logger.info("--- game started at {}.", new Date());
		logger.debug("CWD: {}", System.getProperty("user.dir"));
		
		this.interactionHandler = bw.getInteractionHandler();
        this.mapDrawer = bw.getMapDrawer();
        this.mapAnalyzer = new MapAnalyzer(bw, new BWTA());
        
		this.gameStarted = false;
		
		mapAnalyzer.analyze();
		
		logger.info("playing on {} (hash: {})", this.mapAnalyzer.getBWMap().mapFileName(), this.mapAnalyzer.getBWMap().mapHash());
		
		for (Player player : bw.getAllPlayers()) {
			UnitInventory unitInventory = new UnitInventory();
			unitInventory.initialize();
			this.unitInventories.put(player, unitInventory);
		}
		
		this.player1 = this.interactionHandler.self();
		this.player2 = this.interactionHandler.enemy();
		
		// set custom construction providers
        ConstructionType.Terran_Command_Center.setConstructionProvider(new CommandCenterConstruction(player1.getStartLocation()));
        ConstructionType.Terran_Factory.setConstructionProvider(new FactoryConstruction());
        ConstructionType.Terran_Supply_Depot.setConstructionProvider(new SupplyDepotConstruction());
        ConstructionType.Terran_Barracks.setConstructionProvider(new BarracksConstruction());
        ConstructionType.Terran_Refinery.setConstructionProvider(new RefineryConstruction());
        
		this.buildingPlanner = new BuildingPlanner(this.unitInventories.get(interactionHandler.self()), this.mapAnalyzer, this.interactionHandler);
		this.buildingPlanner.initialize();
		
		this.scoutingStrategy = this.scoutingFactory.getStrategy(this.mapAnalyzer, this.mapDrawer, this.interactionHandler);
		this.miningStrategy = this.miningFactory.getStrategy(this.mapAnalyzer, this.mapDrawer, this.interactionHandler);
		this.gameStrategy = strategyFactory.getStrategy(this.bw, this.mapAnalyzer, this.scoutingStrategy, this.buildingPlanner, this.unitInventories.get(player1), this.unitInventories.get(player2));
		
		this.scoutingStrategy.initialize(this.unitInventories.get(this.player1).getScouts(), this.unitInventories.get(this.player1), this.unitInventories.get(player2));
		this.gameStrategy.initialize();
		
		this.interactionHandler.enableLatCom(false);
		logger.info("latency: {} ({}). latency compensation: {}", this.interactionHandler.getLatency(), 
				this.interactionHandler.getLatencyFrames(), this.interactionHandler.isLatComEnabled());
	
		this.bw.getAllUnits().stream().filter(u -> u instanceof MineralPatch)
				.forEach(u -> unitInventories.get(player1).register((MineralPatch)u));
		
		this.bw.getAllUnits().stream().filter(u -> u instanceof VespeneGeyser)
		.forEach(u -> unitInventories.get(player1).register((VespeneGeyser)u));

		this.onStart();
	}
	
	public void onEnd(boolean isWinner) {
		
		logger.info("--- ending game - {}.", (isWinner? "WIN": "LOSS"));
		this.gameStrategy.stop();
	}
	
	public void onFrame() {
		
		int frameCount = interactionHandler.getFrameCount();
//		long milliSeconds = System.currentTimeMillis();
		
		if (!gameStarted || frameCount < 1) {
			logger.info("frame 0 starting at {}.", new Date());
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
			
			// some simple interaction: enable global map drawing or change logging output
			if (interactionHandler.isKeyPressed(Key.K_CONTROL) && interactionHandler.isKeyPressed(Key.K_T)) {
				mapDrawer.setEnabled(!mapDrawer.isEnabled());
				interactionHandler.sendText("map drawing enabled: " + mapDrawer.isEnabled());
			} else if (interactionHandler.isKeyPressed(Key.K_CONTROL) && interactionHandler.isKeyPressed(Key.K_R)) {
				toggleCleanLogging();
			}
		}
		
		buildingPlanner.run(player1.minerals(), player1.gas(), frameCount);
		
		int availableMinerals = player1.minerals() - buildingPlanner.getQueuedMinerals();
		int availableGas = player1.gas() - buildingPlanner.getQueuedGas();
		int availableSupply = player1.supplyTotal() - player1.supplyUsed();
		
		gameStrategy.run(frameCount, availableMinerals, availableGas, availableSupply);
		
		drawGameInfo();
		
	}
	
	private void drawGameInfo() {
		
		buildingPlanner.drawConstructionSites(this.mapDrawer);
		
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

	public void onReceiveText(Player player, String text) {
		// do nothing
		
	}

	public void onPlayerLeft(Player player) {
		// do nothing
	}
	

	public void onNukeDetect(Position target) {
		// do nothing
	}
	
	private void addToInventory(Unit unit, UnitInventory inventory, int timeSpotted) {
		
	    if (inventory == null) {
	        System.out.println("should not be null");
	    }
	    if (unit instanceof PlayerUnit) {
	    	
	        inventory.register((PlayerUnit)unit);
	    } else if (unit instanceof MineralPatch) {
	    	
	        inventory.register((MineralPatch)unit);
	    } else if (unit instanceof VespeneGeyser) {
	    	
	        inventory.register((VespeneGeyser)unit);
	    }
	}
	
	/* default */ final void onUnitDiscover(Unit unit) {
		
		// TODO handle (enemy) units that are discovered
		logger.trace("onDiscover: discovered {}.", unit);
		if (unit instanceof PlayerUnit) {
			
			PlayerUnit playerUnit = (PlayerUnit) unit;
			if (!this.interactionHandler.self().equals(playerUnit.getPlayer())) {
				
				UnitInventory inventory = this.unitInventories.get(playerUnit.getPlayer());
				if (inventory == null) {
					logger.error("no inventory found for player {} (unit {}).", playerUnit.getPlayer(), playerUnit);
				} else {
					addToInventory(unit, inventory, interactionHandler.getFrameCount());
				}
			}
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

	/* default */  final void onUnitCreate(Unit unit) {
		
		logger.trace("onCreate: New {} unit created.", unit);
		if (unit instanceof Building) {
			this.addToInventory(unit, this.unitInventories.get(((Building) unit).getPlayer()), interactionHandler.getFrameCount());
		}
	}
	
	/* default */  final void onUnitDestroy(Unit unit) {
		
		logger.debug("destroyed {}", unit);

		if (unit instanceof PlayerUnit) {
			this.unitInventories.get(((PlayerUnit) unit).getPlayer()).onUnitDestroy((PlayerUnit) unit, this.interactionHandler.getFrameCount());
		} else if (unit instanceof MineralPatch) {
			this.unitInventories.get(this.player1).onUnitDestroy((MineralPatch)unit, this.interactionHandler.getFrameCount());
		}
	}
	
	public void onUnitMorph(Unit unit) {
		// do nothing
	}
	
	/* default */ final void internalOnUnitMorph(Unit unit) {
		
		if (unit instanceof Refinery) {
			UnitInventory inventory = unitInventories.get(((Refinery) unit).getPlayer());
			inventory.getVespeneGeysers().stream().filter(r -> r.getId() == unit.getId()).findFirst().ifPresent(c -> inventory.getVespeneGeysers().remove(c));
			onUnitCreate(unit);
//		} else if (unit instanceof VespeneGeyser) {
//			UnitInventory inventory = unitInventories.get(((Refinery) unit).getPlayer());
//			inventory.getRefineries().stream().filter(r -> r.getId() == unit.getId()).findFirst().ifPresent(c -> onUnitDestroy(c));
//			onUnitComplete(unit);
		} else {
			onUnitMorph(unit);
		}
	}

	public void onUnitRenegade(Unit unit) {
		// do nothing
		
	}

	public void onSaveGame(String gameName) {
		// do nothing
	}
	
	/* default */  final void onUnitComplete(Unit unit) {
		
		logger.trace("completed {}.", unit);
		
		UnitInventory inventory;
        if (unit instanceof PlayerUnit) {
            inventory = this.unitInventories.get(((PlayerUnit) unit).getPlayer());
        } else {
            inventory = this.unitInventories.get(this.player1);
        }
        addToInventory(unit, inventory, interactionHandler.getFrameCount());
		
		// Once the initial 4 workers and the command centers have fired their triggers we truly start the game
		if (!gameStarted && unitInventories.get(this.player1).getAllWorkers().size() == 4 && !unitInventories.get(this.player1).getCommandCenters().isEmpty()) {
			
			unitInventories.get(this.player1).getMineralWorkers().addAll(unitInventories.get(this.player1).getAllWorkers());
			
			miningStrategy.initialize(unitInventories.get(this.player1).getCommandCenters(), unitInventories.get(this.player1).getRefineries(), unitInventories.get(this.player1).getMineralWorkers(), unitInventories.get(this.player1).getVespeneWorkers(), unitInventories.get(this.player1).getMineralPatches());
			gameStrategy.start(player1.minerals(), player1.gas());
			gameStarted = true;
		}
	}
}