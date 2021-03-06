package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

public abstract class ScoutingStrategy {

	protected MapDrawer mapDrawer;
	protected MapAnalyzer mapAnalyzer;
	protected InteractionHandler interactionHandler;
	
	public ScoutingStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		this.mapAnalyzer = mapAnalyzer;
		this.mapDrawer = mapDrawer;
		this.interactionHandler = interactionHandler;
	}
	
	public abstract void initialize(Group<MobileUnit> squad, UnitInventory myInventory, UnitInventory enemyInventory);

	public abstract void run(int frame);
}
