package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class EngineeringBay extends Building implements Construction, Mechanical {

	private static EngineeringBay constructionInstance = null;
	
	EngineeringBay(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private EngineeringBay(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Engineering_Bay);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new EngineeringBay(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean upgradeInfantryArmor() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Infantry_Armor);
	}
	
	public boolean upgradeInfantryWeapons() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Infantry_Weapons);
	}
}
