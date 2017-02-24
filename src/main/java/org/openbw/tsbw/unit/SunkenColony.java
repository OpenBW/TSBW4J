package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

public class SunkenColony extends Building implements Construction {

	private static SunkenColony constructionInstance = null;
	
	SunkenColony(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private SunkenColony(BWMap bwMap) {
		super(bwMap, UnitType.Zerg_Sunken_Colony);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new SunkenColony(bwMap);
		}
		return constructionInstance;
	}
}
