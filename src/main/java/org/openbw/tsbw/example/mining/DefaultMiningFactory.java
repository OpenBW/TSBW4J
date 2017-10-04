package org.openbw.tsbw.example.mining;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.MiningStrategy;

public class DefaultMiningFactory extends MiningFactory {

	@Override
	public MiningStrategy getStrategy(MapDrawer mapDrawer, InteractionHandler interactionHandler) {

		return new SimpleMiningStrategy(mapDrawer, interactionHandler);
	}

	

}
