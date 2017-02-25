package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.TechType;

public class Vulture extends MobileUnit implements Mechanical {

	/* default */ Vulture(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void spiderMines(Position position) {
		super.bwUnit.useTech(TechType.Spider_Mines, position);
	}
}
