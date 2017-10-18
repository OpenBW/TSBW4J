package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.BW;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;

public abstract class StrategyFactory {

	public abstract AbstractGameStrategy getStrategy(BW bw, MapAnalyzer mapAnalyzer, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory myInventory, UnitInventory enemyInventory);
	
}
