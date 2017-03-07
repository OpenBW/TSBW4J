package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.bwapi.Player;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class StrategyFactory {

	public abstract AbstractGameStrategy getStrategy(MapDrawer mapDrawer, BWMap bwMap, 
			ScoutingStrategy scoutingStrategy, Player player1, Player player2, 
			BuildingPlanner buildingPlanner, DamageEvaluator damageEvaluator, InteractionHandler interactionHandler);
	
}
