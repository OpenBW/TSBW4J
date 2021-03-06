package org.openbw.tsbw;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.building.BarracksConstruction;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.CommandCenterConstruction;
import org.openbw.tsbw.building.ConstructionType;
import org.openbw.tsbw.building.FactoryConstruction;
import org.openbw.tsbw.building.RefineryConstruction;
import org.openbw.tsbw.building.SupplyDepotConstruction;
import org.openbw.tsbw.example.scouting.DefaultScoutingStrategy;
import org.openbw.tsbw.mining.ResourceGatherer;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.strategy.StrategyFactory;
import org.openbw.tsbw.unit.FrameUpdate;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;
import org.openbw.tsbw.unit.UnitFactory;
import org.openbw.tsbw.unit.VespeneGeyser;
import org.openbw.tsbw.unit.WorkerBoard;

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
	protected ResourceGatherer resourceGatherer;
	
	protected ScoutingStrategy scoutingStrategy;
	
	protected StrategyFactory strategyFactory;
	protected AbstractGameStrategy gameStrategy;
	
	protected WorkerBoard publicBoard;
	
	protected boolean scoutingEnabled = true;
	protected boolean cleanLogging = false;
	protected boolean gameStarted = false;
	
	private Set<Subscriber<FrameUpdate>> subscribers;
	
	public final void run() {
		
		logger.trace("executing run().");
		this.bw = new BW(this.eventListener);
		
		this.publicBoard = getPublicBoard();
		this.interactionHandler = bw.getInteractionHandler();
        this.mapDrawer = bw.getMapDrawer();
		this.bw.setUnitFactory(getUnitFactory());
		
		logger.debug("starting game...");
		bw.startGame();
	}
	
	protected WorkerBoard getPublicBoard() {
	
		return new WorkerBoard();
	}
	
	protected UnitFactory getUnitFactory() {
		
		return new UnitFactory(publicBoard);
	}
	
	public Bot() {
		
		this.eventListener = new BotEventListener(this);
		this.unitInventories = new HashMap<Player, UnitInventory>();
		this.subscribers = new HashSet<>();
	}
	
	public void subscribe(Subscriber<FrameUpdate> subscriber) {
	
		this.subscribers.add(subscriber);
	}
	
	public void unsubscribe(Subscriber<FrameUpdate> subscriber) {
		
		this.subscribers.remove(subscriber);
	}
	
	public abstract void onStart();
	
	final void internalOnStart() {
		
		logger.info("--- game started at {}.", new Date());
		
		this.subscribers.clear();
		
		this.mapAnalyzer = new MapAnalyzer(bw, new BWTA());
        
		this.gameStarted = false;
		
		this.mapAnalyzer.analyze();
		this.mapAnalyzer.sortChokepoints(this.interactionHandler.self().getStartLocation());
		
		logger.info("playing on {} (hash: {})", this.mapAnalyzer.getBWMap().mapFileName(), this.mapAnalyzer.getBWMap().mapHash());
		
		for (Player player : bw.getAllPlayers()) {
			
			UnitInventory unitInventory = new UnitInventory();
			unitInventory.initialize(bw.getBullets(), this.mapAnalyzer);
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
        ConstructionType.Terran_Starport.setConstructionProvider(new FactoryConstruction()); // on purpose
        
        UnitInventory myInventory = this.unitInventories.get(this.player1);
		
		this.buildingPlanner = new BuildingPlanner(myInventory, this.mapAnalyzer, this.interactionHandler);
		this.buildingPlanner.initialize();
		
		this.resourceGatherer = new ResourceGatherer();
		
		this.scoutingStrategy = getScoutingStrategy(this.mapAnalyzer, this.mapDrawer, this.interactionHandler);
		this.strategyFactory = new StrategyFactory(this.bw, this.mapAnalyzer, this.scoutingStrategy, this.buildingPlanner, this.unitInventories.get(player1), this.unitInventories.get(player2));
		
		this.scoutingStrategy.initialize(myInventory.getScouts(), myInventory, this.unitInventories.get(player2));
		
		this.publicBoard.initialize(this.mapAnalyzer, myInventory, this.interactionHandler, this.scoutingStrategy);
		
		this.bw.getAllUnits().stream().filter(u -> u instanceof MineralPatch)
				.forEach(u -> myInventory.register((MineralPatch)u));
		
		this.bw.getAllUnits().stream().filter(u -> u instanceof VespeneGeyser)
		.forEach(u -> myInventory.register((VespeneGeyser)u));

		this.onStart();
	}
	
	protected ScoutingStrategy getScoutingStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {
	
		return new DefaultScoutingStrategy(mapAnalyzer, mapDrawer, interactionHandler);
	}
	
	public void onEnd(boolean isWinner) {
		
	}
	
	final void internalOnEnd(boolean isWinner) {
		
		this.gameStrategy.stop();
		onEnd(isWinner);
	}
	
	public void onFrame() {
		
		int frameCount = interactionHandler.getFrameCount();
//		long milliSeconds = System.currentTimeMillis();
		
		// Once the first frame has been played we truly start the game (units will be initialized)
		if (!gameStarted && frameCount > 0) {
			
			logger.info("frame 1 starting at {}.", new Date());
			
			UnitInventory myInventory = this.unitInventories.get(this.player1);
			this.resourceGatherer.initialize(myInventory.getWorkers(), myInventory.getCommandCenters(), myInventory.getMineralPatches());
			gameStrategy.start(player1.minerals(), player1.gas());
			gameStarted = true;
		} else if (frameCount == 0) {
			return;
		}
		
		FrameUpdate frameUpdate = new FrameUpdate(frameCount, player1.minerals(), player1.gas(), 
				this.interactionHandler.getRemainingLatencyFrames(), this.unitInventories.get(this.player1), this.unitInventories.get(this.player2));
		
		this.subscribers.stream().forEach(s -> s.onReceive(frameUpdate));
		
		if (scoutingEnabled) {
			scoutingStrategy.run(frameCount);
		}

		/*
		 * Do every 5 frames (just for performance reasons)
		 */
		if (frameCount % 5 == 0) {
			
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
	        logger.error("inventory is null. Can't add {}.", unit);
	    } else {
	    	inventory.register(unit);
	    }
	}
	
	/* default */ final void onUnitDiscover(Unit unit) {
		
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
			
			UnitInventory inventory = this.unitInventories.get(((PlayerUnit) unit).getPlayer());
			if (unit instanceof SCV) {
            	
        		SCV scv = (SCV) unit;
        		if (scv.getPlayer().equals(this.player1)) {
        			
	        		unsubscribe((SCV)unit);
        		}
        	}
			if (inventory == null) {
				
				logger.error("Could not find a unit inventory for player {}.", ((PlayerUnit) unit).getPlayer());
			}
			inventory.unregister((PlayerUnit) unit);
		} else if (unit instanceof MineralPatch) {
			
			this.unitInventories.get(this.player1).unregister((MineralPatch)unit);
		}
	}
	
	public void onUnitMorph(Unit unit) {
		
		
	}
	
	/* default */ final void internalOnUnitMorph(Unit unit) {
		
		if (unit instanceof GasMiningFacility) {
			UnitInventory inventory = unitInventories.get(((GasMiningFacility) unit).getPlayer());
			inventory.getVespeneGeysers().stream().filter(r -> r.getId() == unit.getId()).findFirst().ifPresent(c -> inventory.getVespeneGeysers().remove(c));
			onUnitCreate(unit);
//		} else if (unit instanceof VespeneGeyser) {
//			UnitInventory inventory = unitInventories.get(((Refinery) unit).getPlayer());
//			inventory.getRefineries().stream().filter(r -> r.getId() == unit.getId()).findFirst().ifPresent(c -> onUnitDestroy(c));
//			onUnitComplete(unit);
		} else {
			
			if (unit instanceof Building && unit.getInitialType().getRace() == Race.Zerg) {
				
				logger.trace("zerg building {} morphed. Will add to unit inventory...", unit);
				UnitInventory inventory = this.unitInventories.get(((PlayerUnit) unit).getPlayer());
				inventory.getAllUnits().stream().filter(u -> u.getId() == unit.getId()).findAny().ifPresent(u -> inventory.unregister(unit));
				inventory.register(unit);
			}
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
            if (unit instanceof SCV) {
            	
        		SCV scv = (SCV) unit;
        		if (scv.getPlayer().equals(this.player1)) {
        			
	        		scv.initialize(inventory.getMineralPatches(), inventory.getRefineries());
	        		subscribe((SCV)unit);
        		}
        	}
        } else {
        	
            inventory = this.unitInventories.get(this.player1);
        }
        
        addToInventory(unit, inventory, interactionHandler.getFrameCount());
	}
}