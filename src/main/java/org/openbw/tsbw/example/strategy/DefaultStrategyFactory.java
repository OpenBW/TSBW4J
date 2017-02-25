package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.strategy.StrategyFactory;

public class DefaultStrategyFactory extends StrategyFactory {

	@Override
	public AbstractGameStrategy getStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			UnitInventory myUnitInventory, UnitInventory enemyUnitInventory, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator) {
		
		AbstractGameStrategy gameStrategy = new DefaultStrategy(mapDrawer, bwMap, scoutingStrategy, myUnitInventory,
				enemyUnitInventory, buildingPlanner, damageEvaluator);
		
		return gameStrategy;
	}


}
