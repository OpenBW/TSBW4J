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

public class Nexus extends Townhall implements Construction {

	private static Nexus constructionInstance = null;
	
	Nexus(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Nexus(BWMap bwMap) {
		super(bwMap, UnitType.Protoss_Nexus);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Nexus(bwMap);
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
			
			if (bwMap.canBuildHere(currentPosition, UnitType.Protoss_Nexus) && BWTA.isConnected(main.getTilePosition(), currentPosition)) {
				
				double currentDistance = BWTA.getGroundDistance(main.getTilePosition(), currentPosition);
				if (currentDistance < distance) {
					buildTile = currentPosition;
					distance = currentDistance;
				}
			}
		} 
		return buildTile;
	}

	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainWorker() {
		return super.bwUnit.train(UnitType.Protoss_Probe);
	}
}
