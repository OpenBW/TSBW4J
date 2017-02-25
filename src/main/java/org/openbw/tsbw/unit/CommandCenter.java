package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class CommandCenter extends Townhall implements Construction, Mechanical {

	private static CommandCenter constructionInstance = null;
	
	CommandCenter(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private CommandCenter(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Command_Center);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new CommandCenter(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainWorker() {
		return super.bwUnit.train(UnitType.Terran_SCV);
	}
	
	public boolean setRallyPoint(Position target) {
		return super.bwUnit.setRallyPoint(target);
	}
}
