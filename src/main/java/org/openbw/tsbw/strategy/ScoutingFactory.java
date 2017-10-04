package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;

public abstract class ScoutingFactory {

	public abstract ScoutingStrategy getStrategy(BWMap bwMap, MapDrawer mapDrawer, InteractionHandler interactionHandler);
}
