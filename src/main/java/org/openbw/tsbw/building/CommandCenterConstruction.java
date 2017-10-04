package org.openbw.tsbw.building;

import java.util.List;
import java.util.Queue;

import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.MyMap;
import org.openbw.tsbw.UnitInventory;

public class CommandCenterConstruction extends DefaultConstruction {

    private TilePosition startLocation;
    
	public CommandCenterConstruction(BWMap bwMap, TilePosition startLocation) {
		super(UnitType.Terran_Factory, bwMap);
		
		this.startLocation = startLocation;
	}

	@Override
	public TilePosition getBuildTile(SCV builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition buildTile = null;
		
		TilePosition mainPosition;
		if (unitInventory.getMain() != null) {
			mainPosition = unitInventory.getMain().getTilePosition();
		} else {
			mainPosition = this.startLocation;
		}
		
		List<TilePosition> baseLocations = MyMap.getBaseLocationsAsPosition();
		
		double distance = Double.MAX_VALUE;
		for (TilePosition currentPosition : baseLocations) {
			
			if (bwMap.canBuildHere(currentPosition, UnitType.Terran_Command_Center, true)  && !collidesWithConstruction(currentPosition, projects) 
					&& !this.startLocation.equals(currentPosition)) {
				
				double currentDistance = MyMap.getGroundDistance(mainPosition, currentPosition);
				if (currentDistance < distance) {
					buildTile = currentPosition;
					distance = currentDistance;
				}
			}
		} 
		return buildTile;
	}
}
