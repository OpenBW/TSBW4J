package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Starport extends Building implements Construction, Mechanical {

	private static Starport constructionInstance = null;
	
	Starport(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Starport(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Starport);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Starport(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainWraith() {
		return super.bwUnit.train(UnitType.Terran_Wraith);
	}

	public boolean trainDropship() {
		return super.bwUnit.train(UnitType.Terran_Dropship);
	}

	public boolean trainScienceVessel() {
		return super.bwUnit.train(UnitType.Terran_Science_Vessel);
	}

	public boolean trainValkyrie() {
		return super.bwUnit.train(UnitType.Terran_Valkyrie);
	}

	public boolean trainBattlecruiser() {
		return super.bwUnit.train(UnitType.Terran_Battlecruiser);
	}

	public boolean setRallyPoint(Position target) {
		return super.bwUnit.setRallyPoint(target);
	}

	public int getTrainingQueueSize() {
		return super.bwUnit.getTrainingQueue().size();
	}
}
