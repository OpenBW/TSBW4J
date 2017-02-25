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

	public enum Type {DUMMY, BUILD_ORDER}
	
	private Type type;
	
	public DefaultStrategyFactory(Type type) {
		
		this.type = type;
	}
	
	@Override
	public AbstractGameStrategy getStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			UnitInventory myUnitInventory, UnitInventory enemyUnitInventory, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator) {
		
		if (type == Type.BUILD_ORDER) {
			
			return new BuildOrderStrategy(mapDrawer, bwMap, scoutingStrategy, myUnitInventory,
					enemyUnitInventory, buildingPlanner, damageEvaluator);
		} else {
			
			return new DummyStrategy(mapDrawer, bwMap, scoutingStrategy, myUnitInventory,
					enemyUnitInventory, buildingPlanner, damageEvaluator);
		}
	}


}
