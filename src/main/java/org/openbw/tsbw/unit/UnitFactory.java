package org.openbw.tsbw.unit;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

/**
 * Responsible for creating type-safe Units from bwapi.Units.
 */
public class UnitFactory {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Creates a type-safe unit class from a bwapi.Unit.
	 * Typically used at the start of the game for MineralPatch and Geyser.
	 * @param type class to create
	 * @param bwUnit bwapi.Unit
	 * @param bwMap map for positioning
	 */
	public static <T> T create(Class<T> type, bwapi.Unit bwUnit, BWMap bwMap) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		logger.trace("creating {}.", type);
		return type.getDeclaredConstructor(bwapi.Unit.class, BWMap.class).newInstance(bwUnit, bwMap);
	}

	/**
	 * Creates a type-safe unit class from a bwapi.Unit during the game.
	 * @param type class to create
	 * @param damageEvaluator used for from/to damage calculations
	 * @param bwMap map for positioning
	 * @param bwUnit
	 * @param timeSpotted frame when this unit got created
	 */
	public static <T> T create(Class<T> type, DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		logger.trace("creating {}.", type);
		return type.getDeclaredConstructor(DamageEvaluator.class, BWMap.class, bwapi.Unit.class, Integer.TYPE)
				.newInstance(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
}
