package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class Academy extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Academy, bwMap);
		}
		return constructionInstance;
	}

	Academy(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
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
