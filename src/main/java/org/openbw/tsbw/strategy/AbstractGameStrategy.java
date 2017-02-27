package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.MapDrawer;
import org.openbw.bwapi.Player;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class AbstractGameStrategy {

	protected MapDrawer mapDrawer;
	protected BWMap bwMap;
	protected ScoutingStrategy scoutingStrategy;
	protected Player self;
	protected Player enemy;
	protected BuildingPlanner buildingPlanner;
	protected DamageEvaluator damageEvaluator;
	
	public AbstractGameStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			Player player1, Player player2, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator) {
		
		this.mapDrawer = mapDrawer;
		this.bwMap = bwMap;
		this.scoutingStrategy = scoutingStrategy;
		this.self = player1;
		this.enemy = player2;
		this.buildingPlanner = buildingPlanner;
		this.damageEvaluator = damageEvaluator;
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
}
