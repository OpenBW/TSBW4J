package org.openbw.tsbw.example.scouting;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

public class DefaultScoutingFactory extends ScoutingFactory {

	@Override
	public ScoutingStrategy getStrategy(BWMap bwMap, MapDrawer mapDrawer) {

		return new DefaultScoutingStrategy(bwMap, mapDrawer);
	}
}
