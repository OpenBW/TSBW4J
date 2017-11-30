package org.openbw.tsbw.strategy;

import java.util.HashMap;
import java.util.Map;

import org.openbw.bwapi4j.BW;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;

public class StrategyFactory {

private Map<String, AbstractGameStrategy> strategies;
	
	private BW bw;
	private MapAnalyzer mapAnalyzer;
	private ScoutingStrategy scoutingStrategy;
	private BuildingPlanner buildingPlanner;
	private UnitInventory player1Inventory;
	private UnitInventory player2Inventory;
	
	public StrategyFactory(BW bw, MapAnalyzer mapAnalyzer, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory player1Inventory,
			UnitInventory player2Inventory) {
		
		this.strategies = new HashMap<>();
		
		this.bw = bw;
		this.mapAnalyzer = mapAnalyzer;
		this.scoutingStrategy = scoutingStrategy;
		this.buildingPlanner = buildingPlanner;
		this.player1Inventory = player1Inventory;
		this.player2Inventory = player2Inventory;
	}
	
	public void register(String name, AbstractGameStrategy strategy) {
	
		this.strategies.put(name, strategy);
	}
	
	public AbstractGameStrategy getStrategy(String name) {
		
		AbstractGameStrategy strategy = this.strategies.get(name);
		if (strategy != null) {
			
			strategy.initialize(bw, mapAnalyzer, scoutingStrategy, buildingPlanner, player1Inventory, player2Inventory);
		}
		return strategy;
	}
}
