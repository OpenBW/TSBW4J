package org.openbw.tsbw.unit;

public class UnitFactory extends org.openbw.bwapi4j.unit.UnitFactory {

	protected WorkerBoard workerBoard;
	
	public UnitFactory(WorkerBoard workerBoard) {
		
		this.workerBoard = workerBoard;
	}
	
	@Override
	protected SCV getSCV(int unitId, int timeSpotted) {

		return new SCV(unitId, new WorkerActor(this.workerBoard));
	}


	@Override
	protected MineralPatch getMineralPatch(int unitId, int timeSpotted) {

		return new MineralPatch(unitId);
	}


	@Override
	protected Refinery getRefinery(int unitId, int timeSpotted) {

		return new Refinery(unitId, timeSpotted);
	}


	@Override
	protected VespeneGeyser getVespeneGeyser(int unitId, int timeSpotted) {

		return new VespeneGeyser(unitId);
	}
}
