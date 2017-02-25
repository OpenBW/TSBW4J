package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.unit.MobileUnit;

public abstract class ScoutingStrategy {

	protected MapDrawer mapDrawer;
	protected BWMap bwMap;
	
	public ScoutingStrategy(BWMap bwMap, MapDrawer mapDrawer) {
		
		this.bwMap = bwMap;
		this.mapDrawer = mapDrawer;
	}
	
	public abstract void initialize(Squad<MobileUnit> squad, UnitInventory unitInventory);

	public abstract void run(int frame);
}
