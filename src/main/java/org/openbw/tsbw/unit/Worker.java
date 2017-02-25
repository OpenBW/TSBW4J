package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TilePosition;
import bwapi.UnitType;

// TODO split this into worker and SCV (build, repair, etc.)
public class Worker extends MobileUnit implements Mechanical {

	/* default */ Worker(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}

	public boolean haltConstruction() {
		return super.bwUnit.haltConstruction();
	}
	
	public boolean gather(Refinery refinery) {
		return super.bwUnit.gather(refinery.bwUnit);
	}
	
	public boolean gather(Refinery refinery, boolean shiftQueueCommand) {
		return super.bwUnit.gather(refinery.bwUnit, shiftQueueCommand);
	}
	
	public boolean gather(MineralPatch mineralPatch) {
		return super.bwUnit.gather(mineralPatch.bwUnit);
	}
	
	public boolean gather(MineralPatch mineralPatch, boolean shiftQueueCommand) {
		return super.bwUnit.gather(mineralPatch.bwUnit, shiftQueueCommand);
	}
	
	public boolean isGatheringMinerals() {
		return super.bwUnit.isGatheringMinerals();
	}

	public boolean isCarryingMinerals() {
		return super.bwUnit.isCarryingMinerals();
	}

	public void returnCargo() {
		super.bwUnit.returnCargo();
	}

	public boolean isConstructing() {
		return super.bwUnit.isConstructing();
	}

	public boolean build(UnitType type, TilePosition target) {
		return super.bwUnit.build(type, target);
	}
	
	public boolean resumeBuilding(Building building) {
		return super.bwUnit.rightClick(building.bwUnit);
	}

	public boolean isGatheringGas() {
		return super.bwUnit.isGatheringGas();
	}
	
	public void repair(Mechanical unit) {
		super.bwUnit.repair(((Unit)unit).bwUnit);
	}
}
