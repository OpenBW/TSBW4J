package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Factory extends Building implements Construction, Mechanical {

	private static Factory constructionInstance = null;
	
	Factory(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Factory(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Factory);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Factory(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainVulture() {
		return super.bwUnit.train(UnitType.Terran_Vulture);
	}

	public boolean trainSiegeTank() {
		return super.bwUnit.train(UnitType.Terran_Siege_Tank_Tank_Mode);
	}

	public boolean trainGoliath() {
		return super.bwUnit.train(UnitType.Terran_Goliath);
	}

	public boolean setRallyPoint(Position target) {
		return super.bwUnit.setRallyPoint(target);
	}

	public int getTrainingQueueSize() {
		return super.bwUnit.getTrainingQueue().size();
	}
}
