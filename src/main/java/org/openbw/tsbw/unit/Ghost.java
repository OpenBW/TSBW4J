package org.openbw.tsbw.unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.TechType;

public class Ghost extends MobileUnit implements Organic, Cloakable, SpellCaster {

	private static final Logger logger = LogManager.getLogger();
	
	/* default */ Ghost(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void personnelCloacking() {
		super.bwUnit.useTech(TechType.Personnel_Cloaking);
	}
	
	public void lockDown(Mechanical unit) {
		if (unit instanceof Unit) {
			super.bwUnit.useTech(TechType.Lockdown, ((Unit)unit).bwUnit);
		} else {
			logger.error("unit {} is not a valid target for lockDown.", unit);
		}
	}
	
	public void NuclearStrike(Position position) {
		super.bwUnit.useTech(TechType.Nuclear_Strike, position);
	}
}
