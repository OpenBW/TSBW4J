package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.tsbw.UnitInventory;

public class TrainMarineAction implements BoAction {

	private UnitInventory unitInventory;
	
	/* default */ TrainMarineAction(UnitInventory unitInventory) {
		
		this.unitInventory = unitInventory;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		
		int minQueueSize = 1;
		Barracks bestBarracks = null;
		for (Barracks barracks : unitInventory.getBarracks()) {
			if (barracks.getTrainingQueueSize() < minQueueSize) {
				minQueueSize = barracks.getTrainingQueueSize();
				bestBarracks = barracks;
			}
		}
		if (bestBarracks != null && availableMinerals >= 50 && availableSupply >= 2) {
			return bestBarracks.trainMarine();
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "action: train marine";
	}
}
