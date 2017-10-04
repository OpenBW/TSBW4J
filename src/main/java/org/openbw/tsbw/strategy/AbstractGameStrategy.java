package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.DamageEvaluator;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Player;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class AbstractGameStrategy {

	protected MapDrawer mapDrawer;
	protected BWMap bwMap;
	protected ScoutingStrategy scoutingStrategy;
	protected Player self;
	protected Player enemy;
	protected BuildingPlanner buildingPlanner;
	protected DamageEvaluator damageEvaluator;
	protected InteractionHandler interactionHandler;
	
	public AbstractGameStrategy(BW bw, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner) {
		
		this.mapDrawer = bw.getMapDrawer();
		this.damageEvaluator = bw.getDamageEvaluator();
		this.bwMap = bw.getBWMap();
		this.interactionHandler = bw.getInteractionHandler();
		this.self = this.interactionHandler.self();
		this.enemy = this.interactionHandler.enemy();
		this.buildingPlanner = buildingPlanner;
		this.scoutingStrategy = scoutingStrategy;
		
	}

	/**
	 * Is called before a new game is about to start.
	 */
	public abstract void initialize();
	
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
		// do nothing
	}
}
