package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class AbstractGameStrategy {

	protected MapDrawer mapDrawer;
	protected BWMap bwMap;
	protected ScoutingStrategy scoutingStrategy;
	protected UnitInventory myUnitInventory;
	protected UnitInventory enemyUnitInventory;
	protected BuildingPlanner buildingPlanner;
	protected DamageEvaluator damageEvaluator;
	
	public AbstractGameStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			UnitInventory myUnitInventory, UnitInventory enemyUnitInventory, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator) {
		
		this.mapDrawer = mapDrawer;
		this.bwMap = bwMap;
		this.scoutingStrategy = scoutingStrategy;
		this.myUnitInventory = myUnitInventory;
		this.enemyUnitInventory = enemyUnitInventory;
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
	 */
	public abstract void start(int startMinerals);
	
	/**
	 * Is called at every frame in the game.
	 * @param frame
	 * @param availableMinerals
	 * @param availableSupply
	 */
	public abstract void run(int frame, int availableMinerals, int availableSupply);
}
