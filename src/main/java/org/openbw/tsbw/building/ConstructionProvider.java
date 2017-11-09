package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

public class ConstructionProvider {

	private UnitType unitType;
	
	public ConstructionProvider(UnitType unitType) {
		
		this.unitType = unitType;
	}
	
	public UnitType getUnitType() {
		return this.unitType;
	}

	public TilePosition getBuildTile(UnitInventory myInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<ConstructionProject> projects) {
		
		TilePosition position = myInventory.getMain().getInitialTilePosition();
		
		return getBuildTile(myInventory, mapAnalyzer, builder, projects, position);
	}
	
	public TilePosition getBuildTile(UnitInventory myInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<ConstructionProject> projects, TilePosition aroundHere) {
		
		TilePosition nextPosition = aroundHere;
		
		for (int i = 0; !mapAnalyzer.canBuildHere(nextPosition, this.unitType, builder) || collidesWithConstruction(nextPosition, projects); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				if (mapAnalyzer.canBuildHere(nextPosition, this.unitType, builder) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		return nextPosition;
	}
	
	protected boolean collidesWithConstruction(TilePosition position, Queue<ConstructionProject> projects) {
		
		for (ConstructionProject project : projects) {
			
			if (project.collidesWithConstruction(position)) {
				return true;
			}
		}
		return false;
	}
}
