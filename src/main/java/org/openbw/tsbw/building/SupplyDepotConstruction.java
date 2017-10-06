package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

import bwta.Region;

public class SupplyDepotConstruction extends DefaultConstruction {

	public SupplyDepotConstruction(MapAnalyzer mapAnalyzer) {
		super(UnitType.Terran_Factory, mapAnalyzer);
	}

	@Override
	public TilePosition getBuildTile(SCV builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Region region;
		if (unitInventory.getMain() == null) {
			region = mapAnalyzer.getRegion(unitInventory.getAvailableWorkers().first().getPosition());
		} else {
			region = mapAnalyzer.getRegion(unitInventory.getMain().getPosition());
		}
		TilePosition choke = region.getChokepoints().iterator().next().getCenter().toTilePosition();
		TilePosition center = region.getCenter().toTilePosition();
		int dx = center.getX() - choke.getX();
		int dy = center.getY() - choke.getY();
		double d = Math.sqrt(dx * dx + dy * dy);
		
		TilePosition position = new TilePosition((int)(center.getX() + dx * 10 / d), (int)(center.getY() + dy * 10 / d));
		TilePosition nextPosition = position;
		
		for (int i = 0; !mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.unitType, true); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(position.getX() + x, position.getY() + y);
				if (mapAnalyzer.getBWMap().canBuildHere(nextPosition, super.unitType, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}
}
