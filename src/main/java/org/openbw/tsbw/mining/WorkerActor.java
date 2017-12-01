package org.openbw.tsbw.mining;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.tsbw.FrameUpdate;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;
import org.openbw.tsbw.unit.VespeneGeyser;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;

public class WorkerActor extends BasicActor<WorkerMessage, Void> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger();
	
	private SCV scv;
	private WorkerBoard publicBoard;
	private Group<MineralPatch> mineralPatches;
	private Group<VespeneGeyser> geysers;
	private boolean alive;
	
	int wakeUp = 0;
	
	WorkerActor(SCV scv, WorkerBoard publicBoard) {
		
		this.scv = scv;
		this.publicBoard = publicBoard;
		this.alive = true;
		this.spawn();
	}
	
	void initialize(Group<MineralPatch> mineralPatches, Group<VespeneGeyser> geysers) {
		
		this.mineralPatches = mineralPatches;
		this.geysers = geysers;
	}
	
	public void mine() {
		
		MineralPatch patch = this.mineralPatches.first();
		this.publicBoard.assign(this.scv, patch);
	}
	
	public void stopMine() {
		
		this.publicBoard.removeFromPatch(scv);
	}
	
	void onFrame(FrameUpdate frameUpdate) {
		
	}

	protected void mining() throws InterruptedException, SuspendExecution {
		
	}
	
	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		while (alive) {
			
			WorkerMessage message = receive();
		}
		
		return null;
	}
}
