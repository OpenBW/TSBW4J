package org.openbw.bwapi;

import bwapi.Color;
import bwapi.Game;
import bwapi.Position;

/**
 * Contains all map-drawing-related bwapi functionality.
 */
public class MapDrawer {

	private Game game;
	private boolean drawingEnabled = true;
	
	public MapDrawer() {
		
	}
	
	public MapDrawer(boolean enabled) {
		this.drawingEnabled = enabled;
	}
	
	/**
	 * Globally enable or disable drawing on the map.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.drawingEnabled = enabled;
	}
	
	public boolean isEnabled() {
		return drawingEnabled;
	}
	
	public void initialize(Game game) {
		this.game = game;
	}
	
	/**
	 * {@link bwapi.Game#getScreenPosition()}
	 */
	public Position getScreenPosition() {
		return game.getScreenPosition();
	}
	
	/**
	 * {@link bwapi.Game#drawTextScreen(int, int, String)}
	 */
	public void drawTextScreen(int x, int y, String text) {
		if (!drawingEnabled) return;
		game.drawTextScreen(x, y, text);
	}

	/**
	 * {@link bwapi.Game#drawCircleMap(int, int, int, Color)}
	 */
	public void drawCircleMap(int x, int y, int radius, Color color) {
		if (!drawingEnabled) return;
		game.drawCircleMap(x, y, radius, color);
	}
	
	/**
	 * {@link bwapi.Game#drawCircleMap(int, int, int, Color, boolean)}
	 */
	public void drawCircleMap(int x, int y, int radius, Color color, boolean isSolid) {
		if (!drawingEnabled) return;
		game.drawCircleMap(x, y, radius, color, isSolid);
	}

	/**
	 * {@link bwapi.Game#drawBoxMap(int, int, int, int, Color)}
	 */
	public void drawBoxMap(int left, int top, int right, int bottom, Color color) {
		if (!drawingEnabled) return;
		game.drawBoxMap(left, top, right, bottom, color);
	}
	
	/**
	 * {@link bwapi.Game#drawBoxMap(int, int, int, int, Color, boolean)}
	 */
	public void drawBoxMap(int left, int top, int right, int bottom, Color color, boolean isSolid) {
		if (!drawingEnabled) return;
		game.drawBoxMap(left, top, right, bottom, color, isSolid);
	}
	
	/**
	 * {@link bwapi.Game#drawBoxScreen(int, int, int, int, Color, boolean)}
	 */
	public void drawBoxScreen(int left, int top, int right, int bottom, Color color, boolean isSolid) {
		if (!drawingEnabled) return;
		game.drawBoxScreen(left, top, right, bottom, color, isSolid);
	}

	/**
	 * {@link bwapi.Game#drawLineMap(int, int, int, int, Color)}
	 */
	public void drawLineMap(int x1, int y1, int x2, int y2, Color color) {
		if (!drawingEnabled) return;
		game.drawLineMap(x1, y1, x2, y2, color);
	}

	/**
	 * {@link bwapi.Game#drawTextMap(int, int, String)}
	 */
	public void drawTextMap(int x, int y, String text) {
		if (!drawingEnabled) return;
		game.drawTextMap(x, y, text);
	}

	/**
	 * {@link bwapi.Game#drawTextMap(Position, String)}
	 */
	public void drawTextMap(Position p, String text) {
		if (!drawingEnabled) return;
		game.drawTextMap(p, text);
	}

	/**
	 * {@link bwapi.Game#drawLineMap(Position, Position, Color)}
	 */
	public void drawLineMap(Position a, Position b, Color color) {
		if (!drawingEnabled) return;
		game.drawLineMap(a, b, color);
	}

	/**
	 * {@link bwapi.Game#drawCircleMap(Position, int, Color)}
	 */
	public void drawCircleMap(Position p, int radius, Color color) {
		if (!drawingEnabled) return;
		game.drawCircleMap(p, radius, color);
	}
	
	/**
	 * {@link bwapi.Game#drawCircleMap(Position, int, Color, boolean)}
	 */
	public void drawCircleMap(Position p, int radius, Color color, boolean isSolid) {
		if (!drawingEnabled) return;
		game.drawCircleMap(p, radius, color, isSolid);
	}
}
