package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class EngineeringBay extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Engineering_Bay, bwMap);
		}
		return constructionInstance;
	}
	
	EngineeringBay(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean upgradeInfantryArmor() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Infantry_Armor);
	}
	
	public boolean upgradeInfantryWeapons() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Infantry_Weapons);
	}
}
