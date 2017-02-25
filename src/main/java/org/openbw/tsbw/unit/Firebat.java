package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;

public class Firebat extends MobileUnit implements Organic {

	/* default */ Firebat(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void stimPacks() {
		super.bwUnit.useTech(TechType.Stim_Packs);
	}
}
