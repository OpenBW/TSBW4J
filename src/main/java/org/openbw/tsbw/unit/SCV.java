package org.openbw.tsbw.unit;

import org.openbw.tsbw.mining.WorkerActor;

public class SCV extends org.openbw.bwapi4j.unit.SCV {

	private WorkerActor workerActor;
	private boolean available;
	
	protected SCV(int id) {
		
		super(id);
		this.available = true;
	}

	public WorkerActor getActor() {
		
		return this.workerActor;
	}
	
	public void setActor(WorkerActor workerActor) {
		
		this.workerActor = workerActor;
	}
	
	public void mine() {
		
		this.workerActor.mine();
	}

	public boolean isAvailable() {
	
		return available;
	}
	
	public void setAvailable(boolean available) {
		
		this.available = available;
		if (available) {
			this.workerActor.mine();
		} else {
			this.workerActor.stopMine();
		}
	}
}
