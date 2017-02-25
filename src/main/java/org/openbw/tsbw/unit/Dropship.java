package org.openbw.tsbw.unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

public class Dropship extends MobileUnit implements Mechanical {

	private static final Logger logger = LogManager.getLogger();
	
	/* default */ Dropship(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean load(MobileUnit unit) {
		
		if (unit.isFlyer()) {
			logger.error("Can't load a {} into a dropship. Only non-flying units allowed.", unit);
			return false;
		} else {
			return super.bwUnit.load(unit.bwUnit);
		}
	}
	
	public boolean unload(MobileUnit unit) {
		return super.bwUnit.unload(unit.bwUnit);
	}
	
	public boolean unloadAll() {
		return super.bwUnit.unloadAll();
	}
}
