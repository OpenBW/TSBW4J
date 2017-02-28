package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Factory extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Factory, bwMap);
		}
		return constructionInstance;
	}
	
	Factory(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
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
