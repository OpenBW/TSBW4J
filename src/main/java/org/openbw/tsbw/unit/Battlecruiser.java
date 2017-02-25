package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;

public class Battlecruiser extends MobileUnit implements Mechanical, SpellCaster {

	/* default */ Battlecruiser(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void yamatoGun(PlayerUnit unit) {
		super.bwUnit.useTech(TechType.Yamato_Gun, unit.bwUnit);
	}
}
