package org.openbw.tsbw.unit;

import org.openbw.tsbw.Group;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TilePosition;
import bwapi.Unit;

public class Refinery extends Building {

	Refinery(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}

	public TilePosition getBuildTile(Worker builder, Group<Geyser> geysers) {
		
		if (geysers.isEmpty()) {
			return null;
		} else {
			return geysers.iterator().next().getTilePosition();
		}
	}
}
