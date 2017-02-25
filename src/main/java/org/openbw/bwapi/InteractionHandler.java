package org.openbw.bwapi;

import java.util.List;

import bwapi.Game;
import bwapi.Key;
import bwapi.Position;
import bwapi.Unit;

/**
 * Contains all interaction-related bwapi functionality.
 */
public final class InteractionHandler {

	private Game game;
	
	public InteractionHandler() {

	}

	public void initialize(Game game) {
		this.game = game;
	}
	
	/**
	 * {@link bwapi.Game#getLastError()}
	 */
	public bwapi.Error getLastError() {
		return game.getLastError();
	}
	
	/**
	 * {@link bwapi.Game#leaveGame()}
	 */
	public void leaveGame() {
		game.leaveGame();
	}
	
	/**
	 * {@link bwapi.Game#getScreenPosition()}
	 */
	public Position getScreenPosition() {
		return game.getScreenPosition();
	}
	
	/**
	 * {@link bwapi.Game#getMousePosition()}
	 */
	public Position getMousePosition() {
		return game.getMousePosition();
	}
	
	/**
	 * {@link bwapi.Game#getKeyState(Key)}
	 */
	public boolean getKeyState(Key key) {
		return game.getKeyState(key);
	}

	/**
	 * {@link bwapi.Game#getFrameCount()}
	 */
	public int getFrameCount() {
		return game.getFrameCount();
	}

	/**
	 * {@link bwapi.Game#getFPS()}
	 */
	public int getFPS() {
		return game.getFPS();
	}
	
	/**
	 * {@link bwapi.Game#getRemainingLatencyFrames()}
	 */
	public int getRemainingLatencyFrames() {
		return game.getRemainingLatencyFrames();
	}
	
	/**
	 * {@link bwapi.Game#getLatencyFrames()}
	 */
	public int getLatencyFrames() {
		return game.getLatencyFrames();
	}
	
	/**
	 * {@link bwapi.Game#getLatency()}
	 */
	public int getLatency() {
		return game.getLatency();
	}

	/**
	 * {@link bwapi.Game#getSelectedUnits()}
	 */
	public List<Unit> getSelectedUnits() {
		return game.getSelectedUnits();
	}

	/**
	 * {@link bwapi.Game#sendText(String)}
	 */
	public void sendText(String text) {
		game.sendText(text);
	}
	
	/**
	 * {@link bwapi.Game#setLocalSpeed(int)}
	 */
	public void setLocalSpeed(int speed) {
		game.setLocalSpeed(speed);
	}

	/**
	 * {@link bwapi.Game#enableFlag(int)}
	 */
	public void enableFlag(int flag) {
		game.enableFlag(flag);
	}

}
