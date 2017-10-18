package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

public class FactoryConstruction extends ConstructionProvider {

	public FactoryConstruction() {
		super(UnitType.Terran_Factory);
	}

	@Override
	public TilePosition getBuildTile(UnitInventory unitInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<ConstructionProject> projects, TilePosition aroundHere) {
	
		
		TilePosition nextPosition = aroundHere;
		TilePosition extensionPosition = new TilePosition(nextPosition.getX() + 4, nextPosition.getY() + 1);
		
		for (int i = 0; !mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.getUnitType(), builder, true) || !mapAnalyzer.getBWMap().canBuildHere(extensionPosition, UnitType.Terran_Machine_Shop, builder, true) || collidesWithConstruction(nextPosition, projects); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				extensionPosition = new TilePosition(nextPosition.getX() + 3, nextPosition.getY() + 1);
				
				if (mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.getUnitType(), builder, true) && mapAnalyzer.getBWMap().canBuildHere(extensionPosition, UnitType.Terran_Machine_Shop, builder, true)
						&& !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}
}
