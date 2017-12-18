package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;

public class ConstructionProvider {

	protected static final int MAX_SEARCH_RADIUS = 100; // to prevent an endless loop in case no build tile can be found
	
	protected UnitType unitType;
	
	public ConstructionProvider(UnitType unitType) {
		
		this.unitType = unitType;
	}
	
	public UnitType getUnitType() {
		
		return this.unitType;
	}

	public TilePosition getBuildTile(UnitInventory myInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<Project> projects) {
		
		TilePosition position = myInventory.getMain().getInitialTilePosition();
		
		return getBuildTile(myInventory, mapAnalyzer, builder, projects, position);
	}
	
	public TilePosition getBuildTile(UnitInventory myInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<Project> projects, TilePosition aroundHere) {
		
		TilePosition nextPosition = aroundHere;
		
		for (int i = 0; i < MAX_SEARCH_RADIUS; i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				if (mapAnalyzer.canBuildHere(nextPosition, this.unitType, builder) 
						&& !collidesWithConstruction(nextPosition, this.unitType, projects) && !collidesWithMiningArea(myInventory, nextPosition)) {
					
					return nextPosition;
				}
			}
		}
		return null;
	}
	
	protected boolean collidesWithMiningArea(UnitInventory myInventory, TilePosition position) {
	
		for (CommandCenter cc : myInventory.getCommandCenters()) {
			
			MineralPatch nearestPatch = myInventory.getMineralPatches().stream().min((u1, u2) -> Double.compare(
	        		u1.getDistance(cc.getPosition()), 
	        		u2.getDistance(cc.getPosition()))).get();
			
			if (position.toPosition().getDistance(nearestPatch.getMiddle(cc)) < 192) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean collidesWithConstruction(TilePosition position, UnitType unitType, Queue<Project> projects) {
		
		for (Project project : projects) {
			
			if (project.collidesWithConstruction(position, unitType)) {
				return true;
			}
		}
		return false;
	}
}
