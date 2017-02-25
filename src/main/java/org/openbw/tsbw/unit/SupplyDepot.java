package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

public class SupplyDepot extends Building implements Construction, Mechanical {

	private static SupplyDepot constructionInstance = null;
	
	SupplyDepot(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private SupplyDepot(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Supply_Depot);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new SupplyDepot(bwMap);
		}
		return constructionInstance;
	}
}
