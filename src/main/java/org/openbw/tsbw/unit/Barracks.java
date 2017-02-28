package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Barracks extends Building implements Mechanical {

	private static Construction constructionInstance = null;
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new ConstructionProvider(UnitType.Terran_Barracks, bwMap);
		}
		return constructionInstance;
	}
	
	Barracks(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainMarine() {
		return super.bwUnit.train(UnitType.Terran_Marine);
	}

	public boolean trainMedic() {
		return super.bwUnit.train(UnitType.Terran_Medic);
	}

	public boolean trainFirebat() {
		return super.bwUnit.train(UnitType.Terran_Firebat);
	}

	public boolean trainGhost() {
		return super.bwUnit.train(UnitType.Terran_Ghost);
	}

	public boolean setRallyPoint(Position target) {
		return super.bwUnit.setRallyPoint(target);
	}

	public int getTrainingQueueSize() {
		return super.bwUnit.getTrainingQueue().size();
	}
}
