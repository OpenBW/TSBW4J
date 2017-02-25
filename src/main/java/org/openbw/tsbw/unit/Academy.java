package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class Academy extends Building implements Construction, Mechanical {

	private static Academy constructionInstance = null;
	
	Academy(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Academy(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Academy);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Academy(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean researchStimPacks() {
		return super.bwUnit.research(TechType.Stim_Packs);
	}
	
	public boolean researchRestoration() {
		return super.bwUnit.research(TechType.Restoration);
	}
	
	public boolean researchOpticalFlare() {
		return super.bwUnit.research(TechType.Optical_Flare);
	}
	
	public boolean upgradeU238Shells() {
		return super.bwUnit.upgrade(UpgradeType.U_238_Shells);
	}
	
	public boolean upgradeCaduceusReactor() {
		return super.bwUnit.upgrade(UpgradeType.Caduceus_Reactor);
	}
}
