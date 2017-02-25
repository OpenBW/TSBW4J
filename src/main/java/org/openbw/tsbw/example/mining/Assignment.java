package org.openbw.tsbw.example.mining;

import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.Worker;

/* default */ class Assignment {

	/* default */ enum Status {MOVING, WAITING, MINING, RETURNING}
	
	private MineralPatch patch;
	private Worker worker;
	private Status status;
	private int wakeup = 0;
	
	/* default */ Assignment(Worker worker, MineralPatch patch) {
		this.worker = worker;
		this.patch = patch;
		this.status = Status.RETURNING;
	}
	
	
	/* default */ int getWakeup() {
		return wakeup;
	}


	/* default */ void setWakeup(int wakeup) {
		this.wakeup = wakeup;
	}


	/* default */ void gather() {
		this.worker.gather(this.patch);
	}
	
	/* default */ Worker getWorker() {
		return this.worker;
	}
	
	/* default */ MineralPatch getMineralPatch() {
		return this.patch;
	}
	
	/* default */ Status getStatus() {
		return this.status;
	}
	
	/* default */ void setStatus(Status status) {
		this.status = status;
	}
}
