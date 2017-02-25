package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;

public class Medic extends MobileUnit implements Organic, SpellCaster {

	/* default */ Medic(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void healing(PlayerUnit unit) {
		super.bwUnit.useTech(TechType.Healing, unit.bwUnit);
	}
	
	public void opticalFlare(PlayerUnit unit) {
		super.bwUnit.useTech(TechType.Optical_Flare, unit.bwUnit);
	}
	
	public void restoration(PlayerUnit unit) {
		super.bwUnit.useTech(TechType.Restoration, unit.bwUnit);
	}
}
