package org.openbw.tsbw.strategy;

import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;

public abstract class MiningFactory {

	public abstract MiningStrategy getStrategy(MapDrawer mapDrawer, InteractionHandler interactionHandler);
}
