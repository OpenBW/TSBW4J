package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi4j.unit.CommandCenter;

public class TrainWorkerAction implements BoAction {

	private CommandCenter commandCenter;
	
	/* default */ TrainWorkerAction(CommandCenter commandCenter) {
		
		this.commandCenter = commandCenter;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		
		if (!commandCenter.isTraining() && availableMinerals >= 50 && availableSupply >= 2) {
			return commandCenter.trainWorker();
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "action: train worker from " + commandCenter;
	}
}
