package org.openbw.tsbw.building;

import java.util.List;
import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

public class CommandCenterConstruction extends ConstructionProvider {

    private TilePosition startLocation;
    
	public CommandCenterConstruction(TilePosition startLocation) {
		
		super(UnitType.Terran_Command_Center);
		this.startLocation = startLocation;
	}

	@Override
	public TilePosition getBuildTile(UnitInventory unitInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<Project> projects) {
		
		TilePosition buildTile = null;
		
		TilePosition mainPosition;
		if (unitInventory.getMain() != null) {
			mainPosition = unitInventory.getMain().getTilePosition();
		} else {
			mainPosition = this.startLocation;
		}
		
		List<TilePosition> baseLocations = mapAnalyzer.getBaseLocationsAsPosition();
		
		double distance = Double.MAX_VALUE;
		for (TilePosition currentPosition : baseLocations) {
			
			if (mapAnalyzer.canBuildHere(currentPosition, super.getUnitType(), builder)  && !collidesWithConstruction(currentPosition, projects) 
					&& !this.startLocation.equals(currentPosition)) {
				
				double currentDistance = mapAnalyzer.getGroundDistance(mainPosition, currentPosition);
				if (currentDistance < distance) {
					buildTile = currentPosition;
					distance = currentDistance;
				}
			}
		} 
		return buildTile;
	}
}
