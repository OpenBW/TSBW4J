package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

public class Nexus extends Townhall implements Construction {

	private static Nexus constructionInstance = null;
	
	Nexus(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private Nexus(BWMap bwMap) {
		super(bwMap, UnitType.Protoss_Nexus);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new Nexus(bwMap);
		}
		return constructionInstance;
	}
	
	public boolean isTraining() {
		return super.bwUnit.isTraining();
	}

	public boolean trainWorker() {
		return super.bwUnit.train(UnitType.Protoss_Probe);
	}
}
