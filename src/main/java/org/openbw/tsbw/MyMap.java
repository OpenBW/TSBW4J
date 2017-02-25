package org.openbw.tsbw;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bwapi.Pair;
import bwapi.TilePosition;
import bwta.BWTA;
import bwta.Chokepoint;
import bwta.Region;

// TODO un-static this
public class MyMap {

	private static Map<Chokepoint, Integer> chokePoints = new HashMap<Chokepoint, Integer>();
	
	private MyMap() {
	
	}
	
	public static int getGroundDistance(TilePosition pos1, TilePosition pos2) {
		
		// "no BWTA" hack
		return (int)pos1.getDistance(pos2);
		// return BWTA.getGroundDistance2(pos1, pos2);
	}
	
	public static boolean isConnected(TilePosition pos1, TilePosition pos2) {
		
		// "no BWTA" hack
		return true;
		// return BWTA.isConnected(pos1, pos2);
	}
	
	public static void sortChokePoints(TilePosition startLocation) {
		
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
	
	public static Set<Chokepoint> getChokepoints() {
		return MyMap.chokePoints.keySet();
	}
	
	public static Chokepoint getBestChokepoint(Region region) {
		
		if (region == null || region.getChokepoints().isEmpty()) {
			return null;
		}
		
		Chokepoint bestChokepoint = region.getChokepoints().get(0);
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

}
