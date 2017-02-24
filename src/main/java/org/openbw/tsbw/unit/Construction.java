package org.openbw.tsbw.unit;

import java.util.Queue;

import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;

import bwapi.TilePosition;

public interface Construction extends Mechanical {

	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects);
	
	public TilePosition getBuildTile(Worker builder, TilePosition aroundHere, UnitInventory unitInventory, Queue<ConstructionProject> projects);
	
	public int getMineralPrice();
	
	public int getGasPrice();
	
	public int tileHeight();
	
	public int tileWidth();

	public boolean build(Worker assignedWorker, TilePosition constructionSite);
}
