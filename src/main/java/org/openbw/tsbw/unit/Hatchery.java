package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

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
}
