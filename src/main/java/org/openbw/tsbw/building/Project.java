package org.openbw.tsbw.building;

import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;

public interface Project {

	public void onFrame(Message message);
	
	public int getQueuedGas();
	
	public int getQueuedMinerals();
	
	public void constructionStarted(Building building);
	
	public boolean isConstructing(Building building);
	
	public boolean collidesWithConstruction(TilePosition position, UnitType unitType);
	
	public boolean isDone();
	
	public void completed();
	
	public void drawConstructionSite(MapDrawer mapDrawer);
	
	public boolean isOfType(ConstructionType constructionType);
}
