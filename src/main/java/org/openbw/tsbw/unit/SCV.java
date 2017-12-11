package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.Subscriber;
import org.openbw.tsbw.building.ConstructionType;

public class SCV extends org.openbw.bwapi4j.unit.SCV implements Subscriber<FrameUpdate> {

	private WorkerActor workerActor;
	
	private Group<MineralPatch> mineralPatches;
	private Group<Refinery> refineries;
	
	protected SCV(int id, WorkerActor workerActor) {
		
		super(id);
		this.workerActor = workerActor;
		this.workerActor.setSCV(this);
	}

	public void scout() {
		
		this.workerActor.sendOrInterrupt(new ScoutMessage());
	}
	
	public void gatherMinerals() {
		
		gather(this.mineralPatches.first());
	}
	
	public void gather(MineralPatch mineralPatch) {
		
		mineralPatch.addScv();
		this.workerActor.sendOrInterrupt(new GatherMineralsMessage(mineralPatch));
	}
	
	public void gather(Refinery refinery) {
		
		this.workerActor.setAvailable(false);
		this.workerActor.sendOrInterrupt(new GatherGasMessage(refinery));
	}
	
	public void gatherGas() {
		
		gather(this.refineries.first());
	}
	
	public void construct(TilePosition constructionSite, ConstructionType type) {
		
		this.workerActor.setAvailable(false);
		this.workerActor.sendOrInterrupt(new BuildMessage(constructionSite, type));
	}
	
	public boolean isAvailable() {
		
		return this.workerActor.isAvailable();
	}
	
	public boolean isGathering() {
		
		return this.workerActor.isGathering();
	}

	public void initialize(Group<MineralPatch> mineralPatches, Group<Refinery> refineries) {
		
		this.mineralPatches = mineralPatches;
		this.refineries = refineries;
		this.workerActor.spawn();
	}

	@Override
	public void onReceive(FrameUpdate frameUpdate) {
		
		this.workerActor.onFrame(frameUpdate);
	}
}
