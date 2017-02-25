package org.openbw.tsbw.strategy;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.MapDrawer;

public abstract class ScoutingFactory {

	public abstract ScoutingStrategy getStrategy(BWMap bwMap, MapDrawer mapDrawer);
}
