package org.openbw.tsbw.example.scouting;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.strategy.ScoutingFactory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

public class DefaultScoutingFactory extends ScoutingFactory {

	@Override
	public ScoutingStrategy getStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {

		return new DefaultScoutingStrategy(mapAnalyzer, mapDrawer, interactionHandler);
	}
}
