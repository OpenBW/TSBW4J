package org.openbw.tsbw.unit;

import java.util.Queue;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Refinery extends Building implements Construction, Mechanical {

	private static Construction constructionInstance = null;
	
	Refinery(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}

	private Refinery(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Refinery);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Refinery(bwMap);
		}
		return constructionInstance;
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, TilePosition aroundHere, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Position around = aroundHere.toPosition();
		
		Group<Geyser> geysers = unitInventory.getGeysers();
		if (geysers.isEmpty()) {
			return null;
		} else {
			return geysers.stream().min((u1, u2) -> Integer.compare(u1.getDistance(around), u2.getDistance(around))).get().getTilePosition();
		}
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition aroundHere = builder == null ? unitInventory.getMain().getTilePosition() : builder.getTilePosition();
		return getBuildTile(builder, aroundHere, unitInventory, projects);
	}
}
