package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class Armory extends Building implements Construction, Mechanical {

	private static Armory constructionInstance = null;
	
	Armory(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Armory(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Armory);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Armory(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean upgradeVehiclePlating() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Vehicle_Plating);
	}
	
	public boolean upgradeVehicleWeapons() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Vehicle_Weapons);
	}
	
	public boolean upgradeShipPlating() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Ship_Plating);
	}
	
	public boolean upgradeShipWeapons() {
		return super.bwUnit.upgrade(UpgradeType.Terran_Ship_Weapons);
	}
}
