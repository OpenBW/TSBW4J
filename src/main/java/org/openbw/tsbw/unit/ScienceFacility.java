package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class ScienceFacility extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Science_Facility, bwMap);
		}
		return constructionInstance;
	}	
	
	ScienceFacility(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean researchEmpShockwave() {
		return super.bwUnit.research(TechType.EMP_Shockwave);
	}
	
	public boolean researchIrradiate() {
		return super.bwUnit.research(TechType.Irradiate);
	}
	
	public boolean upgradeTitanReactor() {
		return super.bwUnit.upgrade(UpgradeType.Titan_Reactor);
	}
	
	public boolean upgradeU238Shells() {
		return super.bwUnit.upgrade(UpgradeType.U_238_Shells);
	}
	
	public boolean upgradeCaduceusReactor() {
		return super.bwUnit.upgrade(UpgradeType.Caduceus_Reactor);
	}
}
