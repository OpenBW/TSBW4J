package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Unit;
import bwapi.UnitType;

public class PhotonCannon extends Building implements Construction {

	private static PhotonCannon constructionInstance = null;
	
	PhotonCannon(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private PhotonCannon(BWMap bwMap) {
		super(bwMap, UnitType.Protoss_Photon_Cannon);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new PhotonCannon(bwMap);
		}
		return constructionInstance;
	}
}
