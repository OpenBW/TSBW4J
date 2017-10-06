package org.openbw.tsbw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.util.Pair;

import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

// TODO un-static this
public class MapAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private BWMap bwMap;
	private BWTA bwta;
	private Map<Chokepoint, Integer> chokePoints = new HashMap<Chokepoint, Integer>();
	
	public MapAnalyzer(BWMap bwMap, BWTA bwta) {
		
		this.bwMap = bwMap;
		this.bwta = bwta;
	}
	
	public BWMap getBWMap() {
		
		return this.bwMap;
	}
	
	public void analyze() {
		
		logger.info("Analyzing map...");
		bwta.analyze();
		logger.info("Map data ready");
	}
	
	public Position getRegionCenter(TilePosition position) {
		
		Region region = bwta.getRegion(position);
		if (region == null) {
			return null;
		} else {
			return region.getCenter();
		}
	}
	
	public List<BaseLocation> getBaseLocations() {
	    
	    return bwta.getBaseLocations();
	}
	
	public List<TilePosition> getBaseLocationsAsPosition() {
	
		List<TilePosition> locations = new ArrayList<TilePosition>();
		for (BaseLocation baseLocation : bwta.getBaseLocations()) {
			locations.add(baseLocation.getTilePosition());
		}
		return locations;
	}
	
	public int getGroundDistance(TilePosition pos1, TilePosition pos2) {
		
		return (int)bwta.getGroundDistance(pos1, pos2);
	}
	
	public boolean isConnected(TilePosition pos1, TilePosition pos2) {
		
		return bwta.isConnected(pos1, pos2);
	}
	
	public void sortChokepoints(TilePosition startLocation) {
		
		chokePoints.clear();
		Region startRegion = bwta.getRegion(startLocation);
		fillMap(startRegion, 0);
	}
	
	private void fillMap(Region region, int value) {
		
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
	
	public List<TilePosition> getShortestPath(TilePosition start, TilePosition end) {
		
	    return bwta.getShortestPath(start, end);
	}
	
	public Region getRegion(Position position) {
	    
	    return bwta.getRegion(position);
	}
	
	public Region getRegion(int x, int y) {
	
	    return bwta.getRegion(x, y);
	}

	public List<Region> getRegions() {
	    
	    return bwta.getRegions();
	}
	
	public Set<Chokepoint> getChokepoints() {
		
	    return this.chokePoints.keySet();
	}
	
	public Chokepoint getBestChokepoint(Region region) {
		
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

	public Chokepoint getChokepoint(int value) {
		
		for (Chokepoint point : chokePoints.keySet()) {
			if (chokePoints.get(point) == value) {
				return point;
			}
		}
		return null;
	}
	
	public int getValue(Chokepoint chokepoint) {
		
		if (chokePoints.containsKey(chokepoint)) {
			return chokePoints.get(chokepoint);
		} else {
			return 99;
		}
	}

	public List<BaseLocation> getStartLocations() {
		
		return bwta.getStartLocations();
	}

}
