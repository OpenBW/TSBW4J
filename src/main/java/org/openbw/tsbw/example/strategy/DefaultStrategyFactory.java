package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.bwapi.Player;
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
			Player self, Player enemy, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator, InteractionHandler interactionHandler) {
		
		if (type == Type.BUILD_ORDER) {
			
			return new BuildOrderStrategy(mapDrawer, bwMap, scoutingStrategy, self, enemy, buildingPlanner, damageEvaluator, interactionHandler);
		} else {
			
			return new DummyStrategy(mapDrawer, bwMap, scoutingStrategy, self, enemy, buildingPlanner, damageEvaluator, interactionHandler);
		}
	}


}
