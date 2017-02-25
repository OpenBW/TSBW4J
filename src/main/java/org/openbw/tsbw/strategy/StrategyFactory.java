package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class StrategyFactory {

	public abstract AbstractGameStrategy getStrategy(MapDrawer mapDrawer, BWMap bwMap, 
			ScoutingStrategy scoutingStrategy, UnitInventory unitInventory1, UnitInventory unitInventory2, 
			BuildingPlanner buildingPlanner, DamageEvaluator damageEvaluator);
	
}
