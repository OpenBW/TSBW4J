package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

public class Bunker extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Bunker, bwMap);
		}
		return constructionInstance;
	}
	
	Bunker(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean load(MobileUnit unit) {
		
		return super.bwUnit.load(unit.bwUnit);
	}
	
	public boolean unload(MobileUnit unit) {
		return super.bwUnit.unload(unit.bwUnit);
	}
	
	public boolean unloadAll() {
		return super.bwUnit.unloadAll();
	}
}
