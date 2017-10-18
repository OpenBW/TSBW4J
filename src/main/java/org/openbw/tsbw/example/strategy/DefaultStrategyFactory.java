package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi4j.BW;
import org.openbw.tsbw.MapAnalyzer;
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
	public AbstractGameStrategy getStrategy(BW bw, MapAnalyzer mapAnalyzer, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory myInventory, UnitInventory enemyInventory) {
		
		if (type == Type.BUILD_ORDER) {
			
			return new BuildOrderStrategy(bw, mapAnalyzer, scoutingStrategy, buildingPlanner, myInventory, enemyInventory);
		} else {
			
			return new DummyStrategy(bw, mapAnalyzer, scoutingStrategy, buildingPlanner, myInventory, enemyInventory);
		}
	}


}
