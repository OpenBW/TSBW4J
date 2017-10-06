package org.openbw.tsbw.example.mining;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.strategy.MiningFactory;
import org.openbw.tsbw.strategy.MiningStrategy;

public class DefaultMiningFactory extends MiningFactory {

	@Override
	public MiningStrategy getStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {

		return new SimpleMiningStrategy(mapAnalyzer, mapDrawer, interactionHandler);
	}

	

}
