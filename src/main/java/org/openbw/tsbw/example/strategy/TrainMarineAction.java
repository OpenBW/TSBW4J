package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.tsbw.Group;

public class TrainMarineAction implements BoAction {

	private Group<Barracks> allBarracks;
	
	/* default */ TrainMarineAction(Group<Barracks> barracks) {
		
		this.allBarracks = barracks;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		
		int minQueueSize = 1;
		Barracks bestBarracks = null;
		for (Barracks barracks : allBarracks) {
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
