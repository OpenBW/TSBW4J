package org.openbw.tsbw.unit;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class Hatchery extends Townhall implements Construction {

	private static Hatchery constructionInstance = null;
	
	Hatchery(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Hatchery(BWMap bwMap) {
		super(bwMap, UnitType.Zerg_Hatchery);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Hatchery(bwMap);
		}
		return constructionInstance;
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition buildTile = null;
		Building main = unitInventory.getMain();
		
		List<BaseLocation> baseLocations = BWTA.getBaseLocations();
		Iterator<BaseLocation> iterator = baseLocations.iterator();
		
		double distance = Double.MAX_VALUE;
		while (iterator.hasNext()) {
			TilePosition currentPosition = iterator.next().getTilePosition();
			
			if (bwMap.canBuildHere(currentPosition, UnitType.Zerg_Hatchery) && BWTA.isConnected(main.getTilePosition(), currentPosition)) {
				
				double currentDistance = BWTA.getGroundDistance(main.getTilePosition(), currentPosition);
				if (currentDistance < distance) {
					buildTile = currentPosition;
					distance = currentDistance;
				}
			}
		} 
		return buildTile;
	}
}
