package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;

public class Drone extends MobileUnit implements Organic, Burrowable {

	/* default */ Drone(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean hasMorphed() {
		return !this.unitType.equals(bwUnit.getType());
	}
	
	// TODO finish (burrow, morph/build, ...)
}
