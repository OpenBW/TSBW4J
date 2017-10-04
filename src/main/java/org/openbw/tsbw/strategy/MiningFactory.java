package org.openbw.tsbw.strategy;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;

public abstract class MiningFactory {

	public abstract MiningStrategy getStrategy(MapDrawer mapDrawer, InteractionHandler interactionHandler);
}
