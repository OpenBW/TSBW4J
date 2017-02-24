package org.openbw.tsbw.unit;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.UnitType;

public class PlayerUnit extends Unit {

	protected DamageEvaluator damageEvaluator;
	protected UnitType unitType;
	private Position lastKnownPosition;
	private int lastKnownHitPoints;
	
	/* default */ PlayerUnit(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		
		super(bwUnit, bwMap, timeSpotted);
		this.damageEvaluator = damageEvaluator;
		this.unitType = bwUnit.getType();
		this.lastKnownPosition = bwUnit.getPosition();
		this.lastKnownHitPoints = bwUnit.getHitPoints();
	}

	protected PlayerUnit() {
		
	}
	
	/**
	 * Convenience method to be used e.g. when doing target-file micro (just provide unit weapon range as radius).
	 * @param radius
	 * @param units
	 * @return weakest unit within given radius
	 */
	public <T extends PlayerUnit> T getWeakestUnitInRadius(int radius, Collection<T> units) {
		
		List<T> inRange = this.getUnitsInRadius(radius, units);
		T weakestUnit;
		if (inRange.isEmpty()) {
			weakestUnit = this.getClosest(units);
		} else {
			Comparator<T> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
			weakestUnit = inRange.parallelStream().min(comp).get();
		}
		return weakestUnit;
	}
	
	public void update(Position position, int hitPoints) {
		this.lastKnownPosition = position;
		this.lastKnownHitPoints = hitPoints;
	}
	
	public int maxHitPoints() {
		return this.unitType.maxHitPoints();
	}
	
	public int getHitPoints() {
		return bwUnit.getHitPoints();
	}
	
	public int getMineralPrice() {
		return this.unitType.mineralPrice();
	}

	public int getGasPrice() {
		return this.unitType.gasPrice();
	}
	
	public int getLastKnownHitPoints() {
		
		return lastKnownHitPoints;
	}
	
	public Position getLastKnownPosition() {
		return lastKnownPosition;
	}
	
	public TilePosition getLastKnownTilePosition() {
		return new TilePosition((this.lastKnownPosition.getX() - this.unitType.dimensionLeft()) / 32, (this.lastKnownPosition.getY() - this.unitType.dimensionUp()) / 32);
	}
	
	public boolean isAttacking() {
		return bwUnit.isAttacking();
	}
	
	public boolean isStartingAttack() {
		return bwUnit.isStartingAttack();
	}
	
	public boolean isAttackFrame() {
		return bwUnit.isAttackFrame();
	}
	
	public boolean isFlying() {
		return bwUnit.isFlying();
	}
	
	public boolean isFlyer() {
		return unitType.isFlyer();
	}
	
	public int getDamageTo(PlayerUnit to) {
		
		return damageEvaluator.getDamageTo(to.bwUnit.getType(), this.unitType, to.bwUnit.getPlayer(), this.bwUnit.getPlayer());
	}
	
	public int getDamageFrom(PlayerUnit from) {
		
		return damageEvaluator.getDamageFrom(from.bwUnit.getType(), this.unitType, from.bwUnit.getPlayer(), this.bwUnit.getPlayer());
	}
	
	@Override
	public String toString() {
		if (this.bwUnit == null) {
			return "construction:" + this.unitType;
		} else {
			return this.bwUnit.getID() + ":" + this.unitType;
		}
	}
}
