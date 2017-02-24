package org.openbw.bwapi;

import java.util.List;

import bwapi.Game;
import bwapi.Position;
import bwapi.Region;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * Contains all map-related bwapi functionality.
 */
public class BWMap {

	private Game game;
	
	public BWMap() {
	
	}
	
	public void initialize(Game game) {
		this.game = game;
	}

	/**
	 * {@link bwapi.Game#getAllRegions()}
	 */
	public List<Region> getAllRegions() {
		return game.getAllRegions();
	}	
	
	/**
	 * {@link bwapi.Game#getRegion(int)}
	 */
	public Region getRegion(int pos) {
		return game.getRegion(pos);
	}	
	
	/**
	 * {@link bwapi.Game#getRegionAt(Position)}
	 */
	public Region getRegionAt(Position position) {
		return game.getRegionAt(position);
	}
	
	/**
	 * {@link bwapi.Game#getRegionAt(int, int)}
	 */
	public Region getRegionAt(int x, int y) {
		return game.getRegionAt(x, y);
	}
	
	/**
	 * {@link bwapi.Game#mapHash()}
	 */
	public String mapHash() {
		return game.mapHash();
	}
	
	/**
	 * {@link bwapi.Game#mapFileName()}
	 */
	public String mapFileName() {
		return game.mapFileName();
	}
	
	/**
	 * {@link bwapi.Game#getGroundHeight(TilePosition)}
	 */
	public int getGroundHeight(TilePosition position) {
		return game.getGroundHeight(position);
	}

	/**
	 * {@link bwapi.Game#getGroundHeight(int, int)}
	 */
	public int getGroundHeight(int tileX, int tileY) {
		return game.getGroundHeight(tileX, tileY);
	}
	
	/**
	 * {@link bwapi.Game#getStartLocations()}
	 */
	public List<TilePosition> getStartLocations() {
		return game.getStartLocations();
	}
	
	/**
	 * {@link bwapi.Player#getStartLocation()}
	 */
	public TilePosition getMyStartLocation() {
		return game.self().getStartLocation();
	}

	/**
	 * {@link bwapi.Game#isVisible(int, int)}
	 */
	public boolean isVisible(int tileX, int tileY) {
		return game.isVisible(tileX, tileY);
	}

	/**
	 * {@link bwapi.Game#isWalkable(int, int)}
	 */
	public boolean isWalkable(int walkX, int walkY) {
		return game.isWalkable(walkX, walkY);
	}

	/**
	 * {@link bwapi.Game#hasPath(Position, Position)}
	 */
	public boolean hasPath(Position source, Position destination) {
		return game.hasPath(source, destination);
	}
	
	/**
	 * {@link bwapi.Game#mapWidth()}
	 */
	public int mapWidth() {
		return game.mapWidth();
	}

	/**
	 * {@link bwapi.Game#mapHeight()}
	 */
	public int mapHeight() {
		return game.mapHeight();
	}
	
	/**
	 * {@link bwapi.Game#isVisible(TilePosition)}
	 */
	public boolean isVisible(TilePosition position) {
		return game.isVisible(position);
	}
	
	/**
	 * {@link bwapi.Game#canBuildHere(TilePosition, UnitType)}
	 */
	public boolean canBuildHere(TilePosition position, UnitType type) {
		return game.canBuildHere(position, type);
	}
	
	public boolean canBuildHere(TilePosition position, UnitType type, boolean accountForUnits) {
		
		if (!accountForUnits) {
			return canBuildHere(position, type);
		}
		if (game.canBuildHere(position, type)) {
			for (Unit unit : game.getAllUnits()) {
					
				if (unit.getTilePosition().getX() + unit.getType().tileWidth() > position.getX() &&  unit.getTilePosition().getX() < position.getX() + type.tileWidth()
						&& unit.getTilePosition().getY() + unit.getType().tileHeight() > position.getY() && unit.getTilePosition().getY() < position.getY() + type.tileHeight()) {
					
					
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
