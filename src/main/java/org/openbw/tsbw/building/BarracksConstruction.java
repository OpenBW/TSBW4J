package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

import bwta.Region;

public class BarracksConstruction extends ConstructionProvider {

	public BarracksConstruction() {
		super(UnitType.Terran_Barracks);
	}

	@Override
	public TilePosition getBuildTile(UnitInventory unitInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<ConstructionProject> projects) {
	
		Region region;
		if (unitInventory.getMain() == null) {
			region = mapAnalyzer.getRegion(builder.getPosition());
		} else {
			region = mapAnalyzer.getRegion(unitInventory.getMain().getPosition());
		}
		TilePosition choke = region.getChokepoints().iterator().next().getCenter().toTilePosition();
		TilePosition center = region.getCenter().toTilePosition();
		
		TilePosition position = new TilePosition((center.getX() + choke.getX()) / 2, (center.getY() + choke.getY()) / 2);
		TilePosition nextPosition = position;
		
		for (int i = 0; !mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.getUnitType(), builder, true) || collidesWithConstruction(nextPosition, projects); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(position.getX() + x, position.getY() + y);
				if (mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.getUnitType(), builder, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}
}
