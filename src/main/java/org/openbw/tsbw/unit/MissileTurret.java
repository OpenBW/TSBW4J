package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.UnitType;

public class MissileTurret extends Building implements Construction, Detector, Mechanical {

	private static MissileTurret constructionInstance = null;
	
	MissileTurret(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private MissileTurret(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Missile_Turret);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new MissileTurret(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean attack(Unit target) {
		return super.bwUnit.attack(target.bwUnit);
	}
}
