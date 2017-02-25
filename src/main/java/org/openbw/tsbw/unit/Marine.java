package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;
import bwapi.Unit;

public class Marine extends MobileUnit implements Organic {

	/* default */ Marine(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void stimPacks() {
		super.bwUnit.useTech(TechType.Stim_Packs);
	}
}
