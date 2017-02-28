package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.UnitType;

public class MissileTurret extends Building implements Detector, Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Missile_Turret, bwMap);
		}
		return constructionInstance;
	}	
	
	MissileTurret(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean attack(Unit target) {
		return super.bwUnit.attack(target.bwUnit);
	}
}
