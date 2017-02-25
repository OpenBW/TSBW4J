package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;

public class Wraith extends MobileUnit implements Cloakable, Mechanical, SpellCaster {

	/* default */ Wraith(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void cloakingField() {
		super.bwUnit.useTech(TechType.Cloaking_Field);
	}
}
