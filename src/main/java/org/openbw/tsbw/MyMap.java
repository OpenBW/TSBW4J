package org.openbw.tsbw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.util.Pair;

import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

// TODO un-static this
public class MyMap {

	private static final Logger logger = LogManager.getLogger();

	private static Map<Chokepoint, Integer> chokePoints = new HashMap<Chokepoint, Integer>();
	
	private MyMap() {
	}
	
	public static void analyze() {
		
		logger.info("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();
		logger.info("Map data ready");
	}
	
	public static Position getRegionCenter(TilePosition position) {
		
		Region region = BWTA.getRegion(position);
		if (region == null) {
			return null;
		} else {
			return region.getCenter();
		}
	}
	
	public static List<BaseLocation> getBaseLocations() {
	    
	    return BWTA.getBaseLocations();
	}
	
	public static List<TilePosition> getBaseLocationsAsPosition() {
	
		List<TilePosition> locations = new ArrayList<TilePosition>();
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			locations.add(baseLocation.getTilePosition());
		}
		return locations;
	}
	
	public static int getGroundDistance(TilePosition pos1, TilePosition pos2) {
		
		return (int)BWTA.getGroundDistance(pos1, pos2);
	}
	
	public static boolean isConnected(TilePosition pos1, TilePosition pos2) {
		
		return BWTA.isConnected(pos1, pos2);
	}
	
	public static void sortChokepoints(TilePosition startLocation) {
		
		chokePoints.clear();
		Region startRegion = BWTA.getRegion(startLocation);
		fillMap(startRegion, 0);
	}
	
	private static void fillMap(Region region, int value) {
		
		for (Chokepoint chokepoint : region.getChokepoints()) {
			if (!chokePoints.containsKey(chokepoint) || chokePoints.get(chokepoint) > value) {
				chokePoints.put(chokepoint, value);
				Pair<Region, Region> regionPair = chokepoint.getRegions();
				if (regionPair.first.equals(region)) {
					fillMap(regionPair.second, value + 1);
				} else {
					fillMap(regionPair.first, value + 1);
				}
			}
		}
	}
	
	public static List<TilePosition> getShortestPath(TilePosition start, TilePosition end) {
	    return BWTA.getShortestPath(start, end);
	}
	
	public static Region getRegion(Position position) {
	    
	    return BWTA.getRegion(position);
	}
	
	public static Region getRegion(int x, int y) {
	
	    return BWTA.getRegion(x, y);
	}

	public static List<Region> getRegions() {
	    
	    return BWTA.getRegions();
	}
	
	public static Set<Chokepoint> getChokepoints() {
		
	    return MyMap.chokePoints.keySet();
	}
	
	public static Chokepoint getBestChokepoint(Region region) {
		
		if (region == null || region.getChokepoints().isEmpty()) {
			return null;
		}
		
		Chokepoint bestChokepoint = region.getChokepoints().iterator().next();
		for (Chokepoint chokepoint : region.getChokepoints()) {
			
			if (getValue(chokepoint) < getValue(bestChokepoint)) {
				bestChokepoint = chokepoint;
			}
		}
		return bestChokepoint;
	}

	public static Chokepoint getChokepoint(int value) {
		
		for (Chokepoint point : chokePoints.keySet()) {
			if (chokePoints.get(point) == value) {
				return point;
			}
		}
		return null;
	}
	
	public static int getValue(Chokepoint chokepoint) {
		
		if (chokePoints.containsKey(chokepoint)) {
			return chokePoints.get(chokepoint);
		} else {
			return 99;
		}
	}

	public static List<BaseLocation> getStartLocations() {
		return BWTA.getStartLocations();
	}

}
