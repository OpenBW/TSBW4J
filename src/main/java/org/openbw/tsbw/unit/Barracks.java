package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Barracks extends Building implements Construction, Mechanical {

	private static Barracks constructionInstance = null;
	
	Barracks(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Barracks(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Barracks);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Barracks(bwMap);
		}
		return constructionInstance;
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
