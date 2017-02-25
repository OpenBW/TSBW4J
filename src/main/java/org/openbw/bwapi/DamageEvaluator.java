package org.openbw.bwapi;

import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;

/**
 * Contains all damage-related bwapi functionality.
 */
public final class DamageEvaluator {

	private Game game;
	
	public DamageEvaluator() {
		
		
	}
	
	public void initialize(Game game) {
		
		this.game = game;
	}

	public int getDamageFrom(UnitType fromType, UnitType toType, Player fromPlayer, Player toPlayer) {
		
		return game.getDamageFrom(fromType, toType, fromPlayer, toPlayer);
	}
	
	public int getDamageFrom(UnitType fromType, UnitType toType) {
		return game.getDamageFrom(fromType, toType);
	}
	
	public int getDamageFrom(UnitType fromType, UnitType toType, Player fromPlayer) {
		return game.getDamageFrom(fromType, toType, fromPlayer);
	}
	
	public int getDamageTo(UnitType toType, UnitType fromType) {
		return game.getDamageTo(toType, fromType);
	}
	
	public int getDamageTo(UnitType toType, UnitType fromType, Player toPlayer) {
		return game.getDamageTo(toType, fromType, toPlayer);
	}
	
	public int getDamageTo(UnitType toType, UnitType fromType, Player toPlayer, Player fromPlayer) {
		return game.getDamageTo(toType, fromType, toPlayer, fromPlayer);
	}
}
