package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.TechType;

public class ScienceVessel extends MobileUnit implements Detector, Mechanical, SpellCaster {

	/* default */ ScienceVessel(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public void defensiveMatrix(PlayerUnit unit) {
		super.bwUnit.useTech(TechType.Defensive_Matrix, unit.bwUnit);
	}
	
	public void irradiate(Organic unit) {
		super.bwUnit.useTech(TechType.Irradiate, ((PlayerUnit)unit).bwUnit);
	}
	
	public void empShockWave(Position position) {
		super.bwUnit.useTech(TechType.EMP_Shockwave, position);
	}
}
