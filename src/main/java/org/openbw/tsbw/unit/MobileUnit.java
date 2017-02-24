package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.WeaponType;

public class MobileUnit extends PlayerUnit {

	/* default */ MobileUnit(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
		this.damageEvaluator = damageEvaluator;
	}

	public boolean holdPosition() {
		return super.bwUnit.holdPosition();
	}
	
	public boolean holdPosition(boolean shiftCommand) {
		return super.bwUnit.holdPosition(shiftCommand);
	}
	
	public double topSpeed() {
		return super.unitType.topSpeed();
	}
	
	public boolean isIdle() {
		return super.bwUnit.isIdle();
	}
	
	public boolean isMoving() {
		return super.bwUnit.isMoving();
	}

	public Position getTargetPosition() {
		return super.bwUnit.getTargetPosition();
	}

	public int getSightRange() {
		return super.unitType.sightRange();
	}
	
	public boolean move(Position target, boolean shiftQueueCommand) {
		return super.bwUnit.move(target, shiftQueueCommand);
	}
	
	public boolean move(Position target) {
		return super.bwUnit.move(target);
	}

	public WeaponType getGroundWeapon() {
		return super.unitType.groundWeapon();
	}
	
	public WeaponType getAirWeapon() {
		return super.unitType.airWeapon();
	}
	
	public int getTurnRadius() {
		return super.unitType.turnRadius();
	}
	
	public boolean attack(Unit target) {
		return super.bwUnit.attack(target.bwUnit);
	}
	
	public boolean attack(Position target) {
		return super.bwUnit.attack(target);
	}
	
	public boolean isStuck() {
		return super.bwUnit.isStuck();
	}
	
	public int getSupplyRequired() {
		return super.unitType.supplyRequired();
	}
}
