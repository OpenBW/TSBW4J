package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.tsbw.MapAnalyzer;

public abstract class MiningFactory {

	public abstract MiningStrategy getStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler);
}
