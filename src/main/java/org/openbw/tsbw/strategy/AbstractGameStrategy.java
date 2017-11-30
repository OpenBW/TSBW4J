package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.DamageEvaluator;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Player;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class AbstractGameStrategy {

	protected MapDrawer mapDrawer;
	protected MapAnalyzer mapAnalyzer;
	protected ScoutingStrategy scoutingStrategy;
	protected Player self;
	protected Player enemy;
	protected BuildingPlanner buildingPlanner;
	protected DamageEvaluator damageEvaluator;
	protected InteractionHandler interactionHandler;
	protected UnitInventory myInventory;
	protected UnitInventory enemyInventory;
	
	/**
	 * Is called before a new game is about to start.
	 */
	public void initialize(BW bw, MapAnalyzer mapAnalyzer, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory player1Inventory,
			UnitInventory player2Inventory) {
		
		this.mapDrawer = bw.getMapDrawer();
		this.damageEvaluator = bw.getDamageEvaluator();
		this.interactionHandler = bw.getInteractionHandler();
		this.self = this.interactionHandler.self();
		this.enemy = this.interactionHandler.enemy();
		this.mapAnalyzer = mapAnalyzer;
		this.scoutingStrategy = scoutingStrategy;
		this.buildingPlanner = buildingPlanner;
		this.myInventory = player1Inventory;
		this.enemyInventory = player2Inventory;
	}
	
	/**
	 * Is called after a new game started (i.e., all initial units have been discovered and completed.
	 * @param startMinerals minerals at the start of the game
	 * @param startGas gas at the start of the game
	 */
	public abstract void start(int startMinerals, int startGas);
	
	/**
	 * Is called at every frame in the game.
	 * @param frame
	 * @param availableMinerals
	 * @param availableGas
	 * @param availableSupply
	 */
	public abstract void run(int frame, int availableMinerals, int availableGas, int availableSupply);
	
	/**
	 * Is called after a game has ended. Can optionally be used to do cleanup before starting a new game.
	 */
	public void stop() {
		
		// default: do nothing
	}
}
