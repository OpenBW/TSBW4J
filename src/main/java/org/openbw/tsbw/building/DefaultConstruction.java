package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;


public class DefaultConstruction {
	
	protected UnitType unitType;
	protected MapAnalyzer mapAnalyzer;
	
	public DefaultConstruction(UnitType unitType, MapAnalyzer mapAnalyzer) {
		this.unitType = unitType;
		this.mapAnalyzer = mapAnalyzer;
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

	public TilePosition getBuildTile(SCV builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition position = unitInventory.getMain().getInitialTilePosition();
		
		return getBuildTile(builder, position, unitInventory, projects);
	}

	public TilePosition getBuildTile(SCV builder, TilePosition aroundHere, UnitInventory unitInventory,
			Queue<ConstructionProject> projects) {
		
		TilePosition nextPosition = aroundHere;
		
		for (int i = 0; !mapAnalyzer.getBWMap().canBuildHere(nextPosition, this.unitType, true); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				if (mapAnalyzer.getBWMap().canBuildHere(nextPosition, this.unitType, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}

	@Override
	public String toString() {
		return this.unitType + " construction";
	}
}
