package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;

public abstract class ScoutingStrategy {

	protected MapDrawer mapDrawer;
	protected BWMap bwMap;
	protected InteractionHandler interactionHandler;
	
	public ScoutingStrategy(BWMap bwMap, MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		this.bwMap = bwMap;
		this.mapDrawer = mapDrawer;
		this.interactionHandler = interactionHandler;
	}
	
	public abstract void initialize(Squad<MobileUnit> squad, UnitInventory unitInventory);

	public abstract void run(int frame);
}
