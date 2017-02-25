package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.TechType;

public class SiegeTank extends MobileUnit implements Mechanical {

	protected boolean sieged;
	
	/* default */ SiegeTank(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
		this.sieged = false;
	}
	
	public void siegeMode(Position position) {
		if (!sieged) {
			super.bwUnit.useTech(TechType.Tank_Siege_Mode);
		}
	}
	
	public void tankMode(Position position) {
		if (sieged) {
			super.bwUnit.useTech(TechType.Tank_Siege_Mode);
		}
	}
}
