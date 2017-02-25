package org.openbw.tsbw.unit;

import org.openbw.tsbw.Group;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Refinery extends Building implements Construction, Mechanical {

	private static Refinery constructionInstance = null;
	
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
	
	public TilePosition getBuildTile(Worker builder, Group<Geyser> geysers) {
		
		if (geysers.isEmpty()) {
			return null;
		} else {
			return geysers.iterator().next().getTilePosition();
		}
	}
	
	// TODO finish
}
