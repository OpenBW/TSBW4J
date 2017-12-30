package org.openbw.tsbw.example.scouting;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

import bwta.Region;

public class DefaultScoutingStrategy extends ScoutingStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	private Group<MobileUnit> squad;
	
	private double[][] initialMap;
	private double[][] map;
	
	private double totalScore;
	private BWMap bwMap;
	
	public DefaultScoutingStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		super(mapAnalyzer, mapDrawer, interactionHandler);
		this.bwMap = mapAnalyzer.getBWMap();
	}
	
	public double calculateRelativeScore() {
		return calculateScore() / totalScore;
	}
	
	public int calculateScore() {
		
		int score = 0;
		
		for (int x = 0; x < bwMap.mapWidth(); x++) {
			for (int y = 0; y < bwMap.mapHeight(); y++) {
				
				score += map[x][y];
			}
		}
		return score;
	}
	
	public void initializeEmpty() {
		map = new double[bwMap.mapWidth()][bwMap.mapHeight()];
		initialMap = new double[bwMap.mapWidth()][bwMap.mapHeight()];
	}
	
	@Override
	public void initialize(Group<MobileUnit> squad, UnitInventory myInventory, UnitInventory enemyInventory) {
		
		this.squad = squad;
		
		List<Region> veryHighValueRegions = new ArrayList<Region>();
		List<Region> highValueRegions = new ArrayList<Region>();
		for (TilePosition baseLocation : this.bwMap.getStartPositions()) {
			veryHighValueRegions.add(mapAnalyzer.getRegion(baseLocation.toPosition()));
		}
		for (TilePosition baseLocation : bwMap.getStartPositions()) {
			highValueRegions.add(mapAnalyzer.getRegion(baseLocation.toPosition()));
		}
		
		initializeEmpty();
		
		for (int x = 0; x < bwMap.mapWidth(); x++) {
			for (int y = 0; y < bwMap.mapHeight(); y++) {
				
				map[x][y] = 2;
				if (highValueRegions.contains(mapAnalyzer.getRegion(x, y))) {
					map[x][y] += 1;
				}
				if (veryHighValueRegions.contains(mapAnalyzer.getRegion(x, y))) {
					map[x][y] += 2;
				}
				if (mapAnalyzer.getRegion(this.interactionHandler.self().getStartLocation().toPosition()).equals(mapAnalyzer.getRegion(x, y))) {
					map[x][y] -= 0.5;
				}
				initialMap[x][y] = map[x][y];
			}
		}
		
		totalScore = calculateScore();
	}
	
	private void updateHeatMap(int frame) {
		
		for (int x = 0; x < bwMap.mapWidth(); x++) {
			
			for (int y = 0; y < bwMap.mapHeight(); y++) {
				
				 if (bwMap.isVisible(x, y)) {
					 map[x][y] = 0;
				 } if (map[x][y] != 2.0 && map[x][y] != 3.0 && bwMap.isWalkable(x*4+2, y*4+2)){
					 map[x][y] += (frame - lastFrame) / 5000.0 * initialMap[x][y];
				 }
			}
		}
	}
	
	private int lastFrame;
	@Override
	public void run(int frame) {
		
		if (squad.size() > 0) {
			updateHeatMap(frame);
		}
		
		for (MobileUnit unit : squad) {

			// go explore some dark space
			if (!unit.isMoving() || unit.getDistance(unit.getTargetPosition()) < 16) { // this is to not lose time when scout full-stops
				Position scoutingTarget = getScoutingTargetPosition(unit);
				if (scoutingTarget!= null) {
					if (unit instanceof SCV) {
						unit.move(scoutingTarget);
					} else {
						unit.attack(scoutingTarget);
					}
					logger.debug("Ordering {} to explore {}", unit, unit.getTargetPosition());
				}
			}
		}
		lastFrame = frame;
	}

	public void showHeatMap() {
		
		TilePosition screenPosition = interactionHandler.getScreenPosition().toTilePosition();
		
		mapDrawer.drawTextScreen(10, 20, "showing scouting heat map");
		for (int x = screenPosition.getX(); x < screenPosition.getX() + Constants.SCREEN_WIDTH; x++) {
			
			for (int y = screenPosition.getY(); y < screenPosition.getY() + Constants.SCREEN_HEIGHT; y++) {
				
				mapDrawer.drawCircleMap(tileToPos(x), tileToPos(y), (int)map[x][y], Color.YELLOW);
			}
		}
	}

	/**
	 * convert tile coordinate to position coordinate at middle of tile
	 * @param coord
	 * @return
	 */
	private int tileToPos(int coordinate) {
		return coordinate*32+16;
	}
	
	/**
	 * Naive implementation looking for the closest unexplored tile. Is not "collaborative" in the
	 * sense that units divide and conquer.
	 * @param scout
	 * @return
	 */
	private Position getScoutingTargetPosition(MobileUnit scout) {
		
		Position scoutTarget = null;
		
		double minDistanceToScout = Double.MAX_VALUE;
		double maxHeat = 0;
		for (int x = 0; x < bwMap.mapWidth(); x++) {
			
			for (int y = 0; y < bwMap.mapHeight(); y++) {
				
				if (scout.isFlyer() || 
						(bwMap.isWalkable(x*4+2, y*4+2) && bwMap.hasPath(scout.getPosition(), new Position(x*32, y*32)))) {
					
					if (map[x][y] > maxHeat) {
						maxHeat = map[x][y];
						minDistanceToScout = Double.MAX_VALUE;
					}
					if (map[x][y] == maxHeat) {
						double distanceToScout = scout.getDistance(tileToPos(x), tileToPos(y));
						
						// [potential improvement] for performance reasons this check could be done only for potential candidates
						if (bwMap.getGroundHeight(x, y) != bwMap.getGroundHeight(scout.getTilePosition())) {
							distanceToScout += 100; // penalty for high / low ground difference
						}
						
						if (distanceToScout > 0 && distanceToScout < minDistanceToScout) {
							
							minDistanceToScout = distanceToScout;
							scoutTarget = new Position(tileToPos(x), tileToPos(y));
						}
					}
				}
			}
		}
		
		return scoutTarget;
	}
}
