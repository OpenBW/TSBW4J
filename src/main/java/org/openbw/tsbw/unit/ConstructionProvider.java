package org.openbw.tsbw.unit;

import java.util.Queue;

import org.openbw.bwapi.BWMap;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;

import bwapi.Region;
import bwapi.TilePosition;
import bwapi.UnitType;

public class ConstructionProvider implements Construction {
	
	protected UnitType unitType;
	protected BWMap bwMap;
	
	public ConstructionProvider(UnitType unitType, BWMap bwMap) {
		this.unitType = unitType;
		this.bwMap = bwMap;
	}
	
	protected boolean collidesWithConstruction(TilePosition position, Queue<ConstructionProject> projects) {
		
		for (ConstructionProject project : projects) {
			
			TilePosition site = project.getConstructionSite();
			Construction construction = project.getConstruction();
			
			if (site != null && construction != null) {
				if (site.getX() + construction.tileWidth() > position.getX() &&  site.getX() < position.getX() + construction.tileWidth()
						&& site.getY() + construction.tileHeight() > position.getY() && site.getY() < position.getY() + construction.tileHeight()) {
					
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Region region;
		if (unitInventory.getMain() == null) {
			region = bwMap.getRegionAt(unitInventory.getAvailableWorkers().first().getPosition());
		} else {
			region = bwMap.getRegionAt(unitInventory.getMain().getPosition());
		}
		TilePosition position = region.getCenter().toTilePosition();
		
		return getBuildTile(builder, position, unitInventory, projects);
	}

	@Override
	public TilePosition getBuildTile(Worker builder, TilePosition aroundHere, UnitInventory unitInventory,
			Queue<ConstructionProject> projects) {
		
		TilePosition nextPosition = aroundHere;
		
		for (int i = 0; !bwMap.canBuildHere(nextPosition, this.unitType, true); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				if (bwMap.canBuildHere(nextPosition, this.unitType, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}

	@Override
	public int getMineralPrice() {
		return unitType.mineralPrice();
	}

	@Override
	public int getGasPrice() {
		return unitType.gasPrice();
	}

	@Override
	public int tileHeight() {
		return unitType.tileHeight();
	}

	@Override
	public int tileWidth() {
		return unitType.tileWidth();
	}

	@Override
	public boolean build(Worker worker, TilePosition constructionSite) {
		return worker.build(this.unitType, constructionSite);
	}
	
	@Override
	public String toString() {
		return this.unitType + " construction";
	}
}
