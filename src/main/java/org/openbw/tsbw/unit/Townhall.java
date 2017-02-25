package org.openbw.tsbw.unit;

import java.util.List;
import java.util.Queue;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.tsbw.MyMap;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * A super class for command center, hatchery, and nexus.
 * Allows for more generic implementations of behavior dealing with e.g. any mains or where to place an expansion. 
 */
public abstract class Townhall extends Building {

	Townhall(BWMap bwMap, UnitType unitType) {
		super(bwMap, unitType);
	}

	public Townhall(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}

	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition buildTile = null;
		Building main = unitInventory.getMain();
		
		List<TilePosition> baseLocations = bwMap.getStartLocations();
		
		double distance = Double.MAX_VALUE;
		for (TilePosition currentPosition : baseLocations) {
			
			// this specific check for CommandCenter holds for any Townhall (CommandCenter, Hatchery, Nexus)
			if (bwMap.canBuildHere(currentPosition, UnitType.Terran_Command_Center) && MyMap.isConnected(main.getTilePosition(), currentPosition)) {
				
				double currentDistance = MyMap.getGroundDistance(main.getTilePosition(), currentPosition);
				if (currentDistance < distance) {
					buildTile = currentPosition;
					distance = currentDistance;
				}
			}
		} 
		return buildTile;
	}
}
